package tk.netindev.drill.util;

/**
 *
 * @author netindev
 *
 */
public class Hex {

   private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

   public static int fromHexChar(char c) {
      if ((c >= '0') && (c <= '9')) {
         c -= 48;
      }
      if ((c >= 'A') && (c <= 'F')) {
         c = (char) (c - 'A' + 10);
      } else {
         c = (char) (c - 'a' + 10);
      }
      return c;
   }

   public static String hexlify(byte[] bytes) {
      final char[] hexChars = new char[bytes.length * 2];
      for (int j = 0; j < bytes.length; j++) {
         final int v = bytes[j] & 0xFF;
         hexChars[j * 2] = hexArray[v >>> 4];
         hexChars[j * 2 + 1] = hexArray[v & 0x0F];
      }
      final String ret = new String(hexChars);
      return ret;
   }

   public static byte[] unhexlify(String string) {
      final int length = string.length();
      if (length % 2 != 0) {
         throw new RuntimeException("Odd-length string");
      }
      final byte[] bytes = new byte[length / 2];
      for (int i = 0; i < length; i += 2) {
         final int top = Character.digit(string.charAt(i), 16);
         final int bot = Character.digit(string.charAt(i + 1), 16);
         if (top == -1 || bot == -1) {
            throw new RuntimeException("Non-hexadecimal digit found");
         }
         bytes[i / 2] = (byte) ((top << 4) + bot);
      }
      return bytes;
   }

}
