package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Scanner;

import client.io.ErrorOutput;
import client.io.StandardOutput;
import main.SonoWrapper;
import main.phl.PhoneLoader;
import main.sono.Datum;
import main.sono.err.SonoCompilationException;
import main.sono.err.SonoException;

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
		String path = SonoClient.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (final UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		path = path.replace("/res/sono.jar", "");

		SonoWrapper.setGlobalOption("PATH", path);

		if (getOption("-d", args) != null) {
			final File directory = new File(path, ".config");
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
			final File directory = new File(path, ".config");
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

		if (getOption("-l", args) != null) {
			SonoWrapper.setGlobalOption("LING", "FALSE");
		} else {
			try {
				pl = new PhoneLoader(SonoWrapper.getGlobalOption("DATA"), false);
			} catch (final IOException e) {
				e.printStackTrace();
			}
			SonoWrapper.setGlobalOption("LING", "TRUE");
		}

		if (getOption("-g", args) != null) {
			SonoWrapper.DEBUG = true;
		} else {
			SonoWrapper.DEBUG = false;
		}

		String filename = null;
		if (args.length > 0 && args[0].charAt(0) != '-') {
			filename = args[0];
		}

		final SonoWrapper center = new SonoWrapper(pl, path, filename, new StandardOutput(), new ErrorOutput());

		System.out.println("Sono " + SonoWrapper.VERSION);
		if (SonoWrapper.getGlobalOption("LING").equals("TRUE")) {
			System.out.println("Phonological Data Loaded From <" + SonoWrapper.getGlobalOption("DATA") + ">");
		} else {
			System.out.println("Phonological Operations Disabled");
		}
		try {
			center.run("load \"system\"");
			System.out.println("Loaded System Library");
		} catch (final SonoCompilationException e) {
			System.out.println("Could not load System Library");
		}

		try (Scanner sc = new Scanner(System.in);) {
			while (true) {
				System.out.print("> ");
				final String line = sc.nextLine();
				try {
					final Datum result = center.run(line);
					if (result.getType() == Datum.Type.VECTOR) {
						int i = 0;
						for (final Datum d : result.getVector(new ArrayList<>()))
							System.out.println("\t" + (i++) + ":\t" + d.toStringTrace(new ArrayList<>()));
					} else {
						System.out.println("\t" + result.toStringTrace(new ArrayList<>()));
					}
				} catch (final SonoException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
}