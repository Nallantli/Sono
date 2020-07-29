package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Scanner;

import client.io.ErrorOutput;
import client.io.StandardInput;
import client.io.StandardOutput;
import main.SonoWrapper;
import main.phl.PhoneLoader;
import main.sono.Datum;

public class SonoClient {
	private static String getOption(final String option, final String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(option)) {
				if (i + 1 < args.length)
					return args[i + 1];
				else
					return "NA";
			}
		}
		return null;
	}

	public static void main(final String[] args) {
		final String configPath = System.getProperty("user.home");
		String path = SonoClient.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (final UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		path = path.replace("/res/sono.jar", "");

		SonoWrapper.setGlobalOption("PATH", path);

		SonoWrapper.setGlobalOption("WRITE", "TRUE");
		SonoWrapper.setGlobalOption("SOCKET", "TRUE");
		SonoWrapper.setGlobalOption("GRAPHICS", "TRUE");

		boolean force = false;

		if (getOption("-d", args) != null) {
			force = true;
			final File directory = new File(configPath, ".sono");
			if (!directory.exists())
				directory.mkdir();
			final File config = new File(directory, "config");
			try (FileWriter fw = new FileWriter(config); BufferedWriter bw = new BufferedWriter(fw);) {
				final String tsvpath = getOption("-d", args);
				final File data = new File(tsvpath);
				SonoWrapper.setGlobalOption("DATA", data.getAbsolutePath());
				bw.write("DATA=" + SonoWrapper.getGlobalOption("DATA"));
			} catch (final IOException e) {
				System.err.println("Error writing to /config file.");
				System.exit(1);
			}
		} else {
			final File directory = new File(configPath, ".sono");
			if (!directory.exists())
				directory.mkdir();

			final File config = new File(directory, "config");
			if (config.exists()) {
				try (FileReader fr = new FileReader(config); BufferedReader br = new BufferedReader(fr);) {
					String line;
					while ((line = br.readLine()) != null) {
						final String[] s = line.split("=");
						SonoWrapper.setGlobalOption(s[0], s[1]);
					}
				} catch (final IOException e) {
					System.err.println("Error reading from /config file.");
					System.exit(1);
				}
			} else {
				System.err.println("Please initialize /config file with -d");
				System.exit(1);
			}
		}

		PhoneLoader pl = null;

		try {
			pl = new PhoneLoader(SonoWrapper.getGlobalOption("DATA"), force);
		} catch (final IOException e) {
			e.printStackTrace();
		}

		File filename = null;
		if (args.length > 0 && args[0].charAt(0) != '-') {
			filename = new File(args[0]);
		}
		final Scanner sc = new Scanner(System.in);
		final SonoWrapper center = new SonoWrapper(pl, filename, new StandardOutput(), new ErrorOutput(),
				new StandardInput(sc));

		if (filename == null) {
			System.out.println("Sono " + SonoWrapper.VERSION);
			System.out.println("Phonological Data Loaded From <" + SonoWrapper.getGlobalOption("DATA") + ">");

			while (true) {
				System.out.print("> ");
				final String line = sc.nextLine();
				try {
					final Datum result = center.run(".", line);
					if (result.getType() == Datum.Type.VECTOR) {
						int i = 0;
						for (final Datum d : result.getVector(null))
							System.out.println("\t" + (i++) + ":\t" + d.toStringTrace(null));
					} else {
						System.out.println("\t" + result.toStringTrace(null));
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}