package gq.netin.drill.hasher;

import gq.netin.drill.util.Util;

/**
 *
 * @author netindev
 *
 */
public class Hasher {

	public static native void slowHash(byte[] input, byte[] output, int variant);

	static {
		try {
			switch (Util.getOperatingSystem()) {
			case WIN:
				Util.loadLibrary("/lib/win/cryptonight.dll");
				break;
			case UNIX:
				Util.loadLibrary("/lib/unix/libcryptonight.so");
				break;
			default:
				System.err.println("Only (UNIX and WIN) x64 are supported.");
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
