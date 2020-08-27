package client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SonoDownloader {
	private static final int BUFFER_SIZE = 4096;

	public static void copyURLToFile(final URL url, final File file) throws IOException {
		try (InputStream input = url.openStream()) {
			if (file.exists()) {
				if (!file.canWrite())
					throw new IOException("File '" + file + "' cannot be written");
			} else {
				final File parent = file.getParentFile();
				if ((parent != null) && (!parent.exists()) && (!parent.mkdirs())) {
					throw new IOException("File '" + file + "' could not be created");
				}
			}

			try (FileOutputStream output = new FileOutputStream(file);) {
				final byte[] buffer = new byte[4096];
				int n = 0;
				while (-1 != (n = input.read(buffer))) {
					output.write(buffer, 0, n);
				}
			}
		}
	}

	public static void unzipFile(final File zipFilePath, final File destDir) throws IOException {
		try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));) {
			ZipEntry entry = zipIn.getNextEntry();
			while (entry != null) {
				final String filePath = destDir.getAbsolutePath() + File.separator + entry.getName();
				if (!entry.isDirectory()) {
					extractFile(zipIn, filePath);
				} else {
					final File dir = new File(filePath);
					dir.mkdirs();
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
		}
	}

	private static void extractFile(final ZipInputStream zipIn, final String filePath) throws IOException {
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));) {
			final byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
		}
	}

	public static void deleteDirectory(final File directoryToBeDeleted) throws IOException {
		final File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (final File file : allContents) {
				deleteDirectory(file);
			}
		}
		Files.delete(directoryToBeDeleted.toPath());
	}

}