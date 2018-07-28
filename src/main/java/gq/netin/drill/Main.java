package gq.netin.drill;

import java.util.Arrays;

import gq.netin.drill.miner.Miner;
import gq.netin.drill.util.Info;

/**
 *
 * @author netindev
 *
 */
public class Main {

	public static void main(String[] args) {
		Info.printInfo();
		if (!(System.getProperty("os.arch").indexOf("64") != -1)) {
			System.err.println(Info.PACKAGE_NAME + " does not work with x86 os.");
			return;
		}
		String host = null, user = null, pass = "x";
		int port = -1, thread = 1;
		final String[] requiredArgs = { "help", "server", "port", "user" };
		final String arguments = String.join(",", args);
		boolean keepAlive = false;
		if (Arrays.stream(requiredArgs).parallel().anyMatch(arguments::contains)) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("--host")) {
					host = args[i + 1];
				} else if (args[i].contains("--port")) {
					try {
						Integer.parseInt(args[i + 1]);
					} catch (final Exception e) {
						System.err.println("- Invalid port");
						return;
					}
					port = Integer.parseInt(args[i + 1]);
				} else if (args[i].equals("--user")) {
					user = args[i + 1];
				} else if (args[i].equals("--pass")) {
					pass = args[i + 1];
				} else if (args[i].equals("--keepalive")) {
					keepAlive = true;
				} else if (args[i].equals("--threads")) {
					try {
						Integer.parseInt(args[i + 1]);
					} catch (final Exception e) {
						System.err.println("- Invalid thread");
						return;
					}
					thread = Integer.parseInt(args[i + 1]);
				} else if (args[i].equals("--help")) {
					System.out.println("--host*              server host");
					System.out.println("--port*              server port");
					System.out.println("--user*              server username");
					System.out.println("--pass               server password");
					System.out.println("--threads            thread count");
					System.out.println("--help               show help");
					System.out.println();
					System.out.println("Usage: java -jar pickaxe.jar --host netin.gq --port 3333 --user netindev.7700k --pass 123 --thread 3");
					System.out.println("Remove --help argument to proceed");
					return;
				} else {
					if (i % 2 == 0) {
						System.out.println("The argument: " + args[i] + " was not recognized");
					}
				}
			}
		} else {
			System.out.println("Missing required arguments, check * arguments in --help");
			return;
		}
		if (thread > Runtime.getRuntime().availableProcessors()) {
			thread = Runtime.getRuntime().availableProcessors();
		}
		final Miner miner = new Miner(host, port, user, pass, thread, keepAlive);
		try {
			miner.connect();
			miner.mine();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
