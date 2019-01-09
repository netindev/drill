package tk.netindev.drill.miner;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;

import tk.netindev.drill.Drill;
import tk.netindev.drill.util.Hashrate;
import tk.netindev.drill.util.Hex;

/**
 *
 * @author netindev
 *
 */
public class Miner {

   private static final Logger logger = LoggerFactory
         .getLogger(Miner.class.getName());

   private final String host, user, pass;
   private final int port, thread;

   private Socket socket;
   private PrintWriter printWriter;
   private Scanner scanner;

   protected final Hashrate hashrate = new Hashrate();

   private final Set<Worker> set = new HashSet<>();

   public Miner(String host, String user, String pass, int port, int thread) {
      this.host = host;
      this.user = user;
      this.pass = pass;
      this.port = port;
      this.thread = thread;
   }

   private boolean connect() {
      try {
         this.socket = new Socket(this.host, this.port);
         this.printWriter = new PrintWriter(this.socket.getOutputStream());
         this.scanner = new Scanner(this.socket.getInputStream());
         this.socket.setKeepAlive(true);
         final JsonObject params = new JsonObject(), doc = new JsonObject();
         params.add("login", this.user);
         params.add("pass", this.pass);
         params.add("agent", "drill/" + Drill.PACKAGE_VERSION);

         doc.add("jsonrpc", "2.0");
         doc.add("id", 1);
         doc.add("method", "login");
         doc.add("params", params);
         this.printWriter.print(doc.toString() + "\n");
         this.printWriter.flush();
         return true;
      } catch (final UnknownHostException e) {
         logger.error("Can't connect to: " + e.getMessage());
         return false;
      } catch (final IOException e) {
         logger.error(e.getMessage());
         return false;
      }
   }

   public void start() throws Exception {
      if (this.connect()) {
         logger.info("Connected to: " + this.host + ":" + this.port);
         while (this.scanner.hasNextLine()) {
            try {
               final String string = this.scanner.nextLine();
               final Job job = this.parseJob(string);
               if (job != null) {
                  logger.info("New job received, diff: "
                        + (Integer.MAX_VALUE / job.getTarget()) * 2);
                  this.work(job);
                  logger.info("Hashrate: "
                        + String.format("%.2f", this.getHashrate()) + " h/s");
               }
            } catch (final Exception e) {
               logger.error(e.getMessage());
               break;
            }
         }
         logger.error("Disconnected from the pool");
         this.reconnect();
      } else {
         this.reconnect();
      }
   }

   private Job parseJob(String string) {
      final Job job = new Job();
      final AtomicBoolean status = new AtomicBoolean(false),
            info = new AtomicBoolean(false);
      for (Member member : JsonObject.readFrom(string)) {
         if (member.getName().equals("result")) {
            for (Member resultTable : member.getValue().asObject()) {
               if (resultTable.getName().equals("id")) {
                  job.setId(resultTable.getValue().asString());
               } else if (resultTable.getName().equals("job")) {
                  for (Member jobTable : resultTable.getValue().asObject()) {
                     if (jobTable.getName().equals("blob")) {
                        job.setBlob(Hex
                              .unhexlify(jobTable.getValue().asString()));
                     } else if (jobTable.getName().equals("job_id")) {
                        job.setJobId(jobTable.getValue().asString());
                     } else if (jobTable.getName().equals("target")) {
                        final byte[] target = Hex
                              .unhexlify(jobTable.getValue().asString());
                        job.setTarget(
                              (((target[3] << 24) | ((target[2] & 255) << 16))
                                    | ((target[1] & 255) << 8))
                                    | (target[0] & 255));
                     }
                  }
                  status.set(true);
               } else if (resultTable.getName().equals("status")) {
                  if (resultTable.getValue().asString().equals("OK")
                        && !status.get()) {
                     logger.info("Result accepted by the pool!");
                     info.set(true);
                  }
               }
            }
         } else if (member.getName().equals("error")) {
            if (!member.getValue().isNull()) {
               for (Member errorTable : member.getValue().asObject()) {
                  if (errorTable.getName().equals("message")) {
                     if (errorTable.getValue().asString()
                           .equals("Unauthenticated")) {
                        throw new RuntimeException("Unauthenticated");
                     }
                     logger.error(errorTable.getValue().asString());
                     info.set(true);
                  }
               }
            }
         } else if (member.getName().equals("params")) {
            for (Member paramTable : member.getValue().asObject()) {
               if (paramTable.getName().equals("id")) {
                  job.setId(paramTable.getValue().asString());
               } else if (paramTable.getName().equals("blob")) {
                  job.setBlob(
                        Hex.unhexlify(paramTable.getValue().asString()));
               } else if (paramTable.getName().equals("job_id")) {
                  job.setJobId(paramTable.getValue().asString());
               } else if (paramTable.getName().equals("target")) {
                  final byte[] target = Hex
                        .unhexlify(paramTable.getValue().asString());
                  job.setTarget((((target[3] << 24) | ((target[2] & 255) << 16))
                        | ((target[1] & 255) << 8)) | (target[0] & 255));
               }
            }
         }
      }
      return info.get() ? null : job;
   }

   protected void send(Job job, byte[] nonce, byte[] result) {
      final JsonObject params = new JsonObject(), doc = new JsonObject();
      params.add("id", job.getId());
      params.add("job_id", job.getJobId());
      params.add("nonce", Hex.hexlify(nonce).toLowerCase());
      params.add("result", Hex.hexlify(result).toLowerCase());

      doc.add("id", 1);
      doc.add("jsonrpc", "2.0");
      doc.add("method", "submit");
      doc.add("params", params);
      this.printWriter.print(doc.toString() + "\n");
      this.printWriter.flush();
   }

   private void work(Job job) {
      // I didn't find a better way than this.
      for (Thread thread : this.set) {
         thread.interrupt();
      }
      this.set.clear();

      for (int i = 0; i < this.thread; i++) {
         final Worker worker = new Worker(this, job, 100000 * i);
         worker.start();
         this.set.add(worker);
      }
   }

   private float getHashrate() {
      if (this.hashrate.size() < 10) {
         return 0.0F;
      } else {
         final long runningTime = System.currentTimeMillis()
               - this.hashrate.element();
         if (runningTime < 10) {
            return 0.0F;
         } else {
            return (float) (this.hashrate.size() / (runningTime * 0.001D));
         }
      }
   }
   
   private void reconnect() {
      try {
         logger.info(
               "Reconnecting in 30 seconds");
         Thread.sleep(1000L * 30L);
         while (!this.connect()) {
            logger.error(
                  "Couldn't connect, trying to reconnect in 30 seconds");
            for (Thread thread : this.set) {
               thread.interrupt();
            }
            Thread.sleep(1000L * 30L);
         }
         this.start();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
