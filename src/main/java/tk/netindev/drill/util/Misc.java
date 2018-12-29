package tk.netindev.drill.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author netindev
 *
 */
public class Misc {

   public static boolean isInteger(String string) {
      try {
         Integer.parseInt(string);
         return true;
      } catch (final Exception e) {
         return false;
      }
   }

   public static void loadLibrary(String name) throws IOException {
      final InputStream inputStream = Misc.class.getResourceAsStream(name);
      final byte[] buffer = new byte[1024];
      int read = -1;
      final File temp = File.createTempFile(name, "");
      final FileOutputStream outputStream = new FileOutputStream(temp);
      while ((read = inputStream.read(buffer)) != -1) {
         outputStream.write(buffer, 0, read);
      }
      outputStream.close();
      inputStream.close();
      System.load(temp.getAbsolutePath());
   }

}
