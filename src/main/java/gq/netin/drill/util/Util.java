package gq.netin.drill.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 *
 * @author netindev
 *
 */
public class Util {

	public enum OS {
		WIN, MAC, UNIX, UNKNOWN
	}

	public static OS getOperatingSystem() {
		final String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		if ((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
			return OS.MAC;
		} else if (os.indexOf("win") >= 0) {
			return OS.WIN;
		} else if (os.indexOf("nux") >= 0) {
			return OS.UNIX;
		} else {
			return OS.UNKNOWN;
		}
	}

	public static void loadLibrary(String name) throws IOException {
		final InputStream inputStream = Util.class.getResourceAsStream(name);
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
