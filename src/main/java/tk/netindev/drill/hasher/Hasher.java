package tk.netindev.drill.hasher;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tk.netindev.drill.util.Misc;

/**
 *
 * @author netindev
 *
 */
public class Hasher {

   private static final Logger logger = LoggerFactory
         .getLogger(Hasher.class.getName());

   public static native void slowHash(byte[] input, byte[] output, int variant);

   static {
      String library = null;
      final String system = System.getProperty("os.name").toLowerCase();
      if (system.indexOf("win") >= 0) {
         library = "/win/x64/cryptonight.dll";
      } else if (system.indexOf("nix") >= 0 || system.indexOf("nux") >= 0
            || system.indexOf("aix") >= 0) {
         library = "/unix/x64/libcryptonight.so";
      } else {
         logger.error("Couldn't find a dynamic-link library for your system.");
      }
      try {
         Misc.loadLibrary(library);
      } catch (final IOException e) {
         logger.error(e.getMessage());
      }
   }

}
