package tk.netindev.drill.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

/**
 *
 * @author netindev
 *
 */
public class Misc {

   public static Integer getProcessCpuLoad() {
      double value = ((OperatingSystemMXBean) ManagementFactory
            .getOperatingSystemMXBean()).getProcessCpuLoad();
      String string = String.format("%.2f", value);
      return Integer.parseInt(string.substring(string.length() - 2));
   }

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
