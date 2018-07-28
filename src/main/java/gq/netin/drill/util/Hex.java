package gq.netin.drill.util;

/**
 *
 * @author netindev
 *
 */
public class Hex {

	private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String toHexString(byte[] bytes) {
		final char[] hexChars = new char[(bytes.length * 2)];
		for (int j = 0; j < bytes.length; j++) {
			final int v = bytes[j] & 255;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[(j * 2) + 1] = hexArray[v & 15];
		}
		return new String(hexChars);
	}

	public static byte[] fromHexString(String string) {
		if ((string.length() & 1) != 0) {
			throw new RuntimeException("Invalid hex string");
		}
		final byte[] data = new byte[(string.length() / 2)];
		for (int i = 0; i < string.length() / 2; i++) {
			data[i] = (byte) ((fromHexChar(string.charAt(i * 2)) << 4) | fromHexChar(string.charAt((i * 2) + 1)));
		}
		return data;
	}

	public static int fromHexChar(char c) {
		if (c >= '0' && c <= '9') {
			return c - 48;
		}
		if (c >= 'A' && c <= 'F') {
			return (c - 65) + 10;
		}
		if (c >= 'a' && c <= 'f') {
			return (c - 97) + 10;
		}
		throw new RuntimeException("Invalid hex character");
	}

}
