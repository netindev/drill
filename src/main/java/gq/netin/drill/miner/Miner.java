package gq.netin.drill.miner;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import gq.netin.drill.util.Hex;
import gq.netin.drill.util.Info;

/**
 *
 * @author netindev
 *
 */
public class Miner {

	private final String host, user, password;
	private final int port, threads;
	private final boolean keepAlive;

	public Miner(String host, int port, String user, String password, int threads, boolean keepAlive) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.threads = threads;
		this.keepAlive = keepAlive;
	}

	private List<Thread> threadPool;

	private Socket socket;
	private PrintWriter printWriter;
	private Scanner scanner;

	private long responseTime;

	public boolean connect() {
		try {
			this.socket = new Socket(this.host, this.port);
			this.printWriter = new PrintWriter(this.socket.getOutputStream());
			this.scanner = new Scanner(this.socket.getInputStream());
			this.socket.setTcpNoDelay(true);
			final JSONObject loginParam = new JSONObject();
			loginParam.put("login", this.user);
			if (this.password != null) {
				loginParam.put("pass", this.password);
			}
			loginParam.put("agent", Info.PACKAGE_NAME + "/" + Info.PACKAGE_VERSION);
			final JSONObject loginRequest = new JSONObject();
			loginRequest.put("id", 1);
			loginRequest.put("jsonrpc", "2.0");
			loginRequest.put("method", "login");
			loginRequest.put("params", loginParam);
			this.printWriter.print(loginRequest.toString() + "\n");
			this.printWriter.flush();
			Info.print("Connected successfully");
			return true;
		} catch (final Exception e) {
			Info.print("Could not connect");
			return false;
		}
	}

	public void mine() {
		this.threadPool = new ArrayList<>(this.threads);
		Timer keepAlive = null;
		if (this.keepAlive) {
			final TimerTask task = new TimerTask() {
				@Override
				public void run() {
					Miner.this.keepAlive();
				}
			};
			keepAlive = new Timer();
			keepAlive.schedule(task, 0L, 60L * 1000L);
		}
		while (true) {
			String string = null;
			try {
				string = this.scanner.nextLine();
			} catch (final NoSuchElementException e) {
				this.clearPool();
				if (keepAlive != null) {
					keepAlive.cancel();
				}
				break;
			}
			if (string != null) {
				if (Info.ENABLE_DEBUG) {
					Info.debug(string);
				}
				final JSONObject jsonObject = new JSONObject(string);
				if (jsonObject.has("error")) {
					if (!jsonObject.isNull("error")) {
						Info.error(jsonObject.getJSONObject("error").getString("message"));
						continue;
					}
				}
				if (jsonObject.has("result")) {
					if (jsonObject.getJSONObject("result").getString("status").equals("OK") && !jsonObject.getJSONObject("result").has("job")) {
						Info.print("Share accepted by the pool! (" + (System.currentTimeMillis() - this.responseTime) + " ms)");
						continue;
					}
					final JSONObject resultResponse = jsonObject.getJSONObject("result"), jobResponse = resultResponse.getJSONObject("job");
					final byte[] blob = Hex.fromHexString(jobResponse.getString("blob"));
					final String jobId = jobResponse.getString("job_id");
					final byte[] targetData = Hex.fromHexString(jobResponse.getString("target"));
					final int target = (((targetData[3] << 24) | ((targetData[2] & 255) << 16)) | ((targetData[1] & 255) << 8)) | (targetData[0] & 255);
					final Job job = new Job();
					if (jobResponse.has("id")) {
						job.setId(resultResponse.getString("id"));
					}
					job.setBlob(blob);
					job.setJobId(jobId);
					job.setTarget(target);
					Info.print("New job received, initial difficulty: " + (Integer.MAX_VALUE / job.getTarget()) * 2);
					this.work(job);
				}
				if (jsonObject.has("method")) {
					if (jsonObject.has("params") && jsonObject.getString("method").equals("job")) {
						final JSONObject resultResponse = jsonObject.getJSONObject("params");
						final byte[] blob = Hex.fromHexString(resultResponse.getString("blob"));
						final String jobId = resultResponse.getString("job_id");
						final byte[] targetData = Hex.fromHexString(resultResponse.getString("target"));
						final int target = (((targetData[3] << 24) | ((targetData[2] & 255) << 16)) | ((targetData[1] & 255) << 8)) | (targetData[0] & 255);
						final Job job = new Job();
						if (resultResponse.has("id")) {
							job.setId(resultResponse.getString("id"));
						}
						job.setBlob(blob);
						job.setJobId(jobId);
						job.setTarget(target);
						Info.print("New job received");
						this.work(job);
					}
				}
			}
		}
	}

	protected void send(Job job, byte[] nonce, byte[] result) {
		final JSONObject sendParams = new JSONObject();
		if (job.getId() != null) {
			sendParams.put("id", job.getId());
		}
		sendParams.put("job_id", job.getJobId());
		sendParams.put("nonce", Hex.toHexString(nonce).toLowerCase());
		sendParams.put("result", Hex.toHexString(result).toLowerCase());
		final JSONObject sendResult = new JSONObject();
		sendResult.put("id", 1);
		sendResult.put("jsonrpc", "2.0");
		sendResult.put("method", "submit");
		sendResult.put("params", sendParams);
		if (Info.ENABLE_DEBUG) {
			Info.debug(sendResult.toString());
		}
		this.responseTime = System.currentTimeMillis();
		this.printWriter.print(sendResult.toString() + "\n");
		this.printWriter.flush();
	}

	private void work(Job job) {
		this.clearPool();
		for (int i = 0; i < this.threads; i++) {
			final Thread thread = new Thread(new Worker(this, job, 1000000 * i));
			thread.start();
			this.threadPool.add(thread);
		}
	}

	private void keepAlive() {
		final JSONObject keepAliveParams = new JSONObject();
		keepAliveParams.put("id", "I64d");
		final JSONObject sendKeepAlive = new JSONObject();
		sendKeepAlive.put("id", "1");
		sendKeepAlive.put("jsonrpc", "2.0");
		sendKeepAlive.put("keepalived", keepAliveParams);
		this.printWriter.print(sendKeepAlive.toString() + "\n");
		this.printWriter.flush();
	}

	private void clearPool() {
		this.threadPool.forEach(Thread::interrupt);
		this.threadPool.clear();
	}

}
