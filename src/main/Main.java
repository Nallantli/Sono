package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import main.base.CommandManager;
import main.phl.PhoneLoader;
import main.phl.PhoneManager;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Scope;
import main.sono.err.SonoException;

public class Main {
	private static final String VERSION = "Beta 1.2.0";
	public static boolean DEBUG;

	private static Map<String, String> globalOptions = new HashMap<>();
	private static Scanner sc = null;

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

	public static String getGlobalOption(final String key) {
		if (globalOptions.containsKey(key))
			return globalOptions.get(key);
		return null;
	}

	public static Scanner getScanner() {
		return sc;
	}

	public static void initScanner() {
		sc = new Scanner(System.in);
	}

	public static void main(final String[] args) {
		try {
			String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			path = URLDecoder.decode(path, "UTF-8");
			path = path.replace("/res/sono.jar", "");
			globalOptions.put("PATH", path);
			final File directory = new File(path, ".config");
			if (!directory.exists())
				directory.mkdir();

			final File config = new File(directory, "config");
			if (!config.createNewFile() && getOption("-d", args) == null) {
				try (FileReader fr = new FileReader(config); BufferedReader br = new BufferedReader(fr);) {
					String line;
					while ((line = br.readLine()) != null) {
						final String[] s = line.split("=");
						globalOptions.put(s[0], s[1]);
					}
				} catch (final IOException e) {
					System.err.println("Error reading from /config file.");
					System.exit(1);
				}
			} else {
				try (FileWriter fw = new FileWriter(config); BufferedWriter bw = new BufferedWriter(fw);) {
					final String tsvpath = getOption("-d", args);
					final File data = new File(tsvpath);
					globalOptions.put("DATA", data.getAbsolutePath());
					bw.write("DATA=" + globalOptions.get("DATA"));
				} catch (final IOException e) {
					System.err.println("Error writing to /config file.");
					System.exit(1);
				}
			}
		} catch (final IOException e1) {
			System.err.println("Error detecting application folder.");
			System.exit(1);
		}

		try {
			final CommandManager command = new CommandManager();
			PhoneLoader pl = null;
			Interpreter sono = null;
			if (getOption("-l", args) != null) {
				globalOptions.put("LING", "FALSE");
				sono = new Interpreter(new Scope(null), new PhoneManager(), command);
			} else {
				globalOptions.put("LING", "TRUE");
				pl = new PhoneLoader(globalOptions.get("DATA"), false);
				sono = new Interpreter(new Scope(null), pl.getManager(), command);
			}

			if (getOption("-g", args) != null) {
				DEBUG = true;
			} else {
				DEBUG = false;
			}

			if (args.length > 0 && args[0].charAt(0) != '-') {
				try {
					sono.runCode(".", "load \"" + args[0] + "\"");
				} catch (final SonoException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			} else {
				try {
					System.out.println("Sono " + VERSION);
					if (globalOptions.get("LING").equals("TRUE")) {
						System.out.println("Phonological Data Loaded From <" + globalOptions.get("DATA") + ">");
					} else {
						System.out.println("Phonological Operations Disabled");
					}
					sc = new Scanner(System.in);
					while (true) {
						System.out.print("> ");
						final String line = sc.nextLine();
						try {
							final Datum result = sono.runCode(".", line);
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
				} catch (final Exception e) {
					e.printStackTrace();
				} finally {
					sc.close();
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}