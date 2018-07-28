package gq.netin.drill.util;

import java.util.Calendar;

/**
 *
 * @author netindev
 *
 */
public class Info {

	public static final String PACKAGE_NAME = "Drill";
	public static final double PACKAGE_VERSION = 0.01;

	public static final boolean ENABLE_DEBUG = false;

	public static void print(String message) {
		final Calendar calendar = Calendar.getInstance();
		final int second = calendar.get(Calendar.SECOND);
		final int minute = calendar.get(Calendar.MINUTE);
		final int hour = calendar.get(Calendar.HOUR_OF_DAY);
		final int day = calendar.get(Calendar.DAY_OF_MONTH);
		final int month = calendar.get(Calendar.MONTH) + 1;
		System.out.println("[" + day + "/" + month + " " + hour + ":" + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second) + "] " + message);
	}

	public static void error(String message) {
		final Calendar calendar = Calendar.getInstance();
		final int second = calendar.get(Calendar.SECOND);
		final int minute = calendar.get(Calendar.MINUTE);
		final int hour = calendar.get(Calendar.HOUR_OF_DAY);
		final int day = calendar.get(Calendar.DAY_OF_MONTH);
		final int month = calendar.get(Calendar.MONTH) + 1;
		System.err.println("[" + day + "/" + month + " " + hour + ":" + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second) + "] " + message);
	}

	public static void debug(String message) {
		System.out.println("DEBUG: " + message);
	}

	public static void printInfo() {
		System.out.println(Info.PACKAGE_NAME + " Cryptonight CPU miner, version: " + Info.PACKAGE_VERSION + ", written by (netindev)");
		System.out.println("Processor architecture: " + System.getProperty("os.arch").toUpperCase());
		System.out.println("Operating system: " + System.getProperty("os.name"));
		System.out.println();
	}

}
