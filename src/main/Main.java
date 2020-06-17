package src.main;

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

import src.phl.PhoneLoader;
import src.sono.Datum;
import src.sono.Interpreter;
import src.sono.Scope;
import src.sono.err.SonoException;

public class Main {
	private static Map<String, String> globalOptions = new HashMap<>();

	private static String getOption(String option, String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(option))
				return args[i + 1];
		}
		return null;
	}

	public static String getGlobalOption(String key) {
		if (globalOptions.containsKey(key))
			return globalOptions.get(key);
		return null;
	}

	public static void main(String[] args) {
		try {
			String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			path = URLDecoder.decode(path, "UTF-8");
			path = path.replace("/SonoLang.jar", "");
			globalOptions.put("PATH", path);
			File directory = new File(path, ".config");
			if (!directory.exists())
				directory.mkdir();

			File config = new File(directory, "config");
			if (!config.createNewFile() && getOption("-d", args) == null) {
				try (FileReader fr = new FileReader(config); BufferedReader br = new BufferedReader(fr);) {
					String line;
					while ((line = br.readLine()) != null) {
						String[] s = line.split("=");
						globalOptions.put(s[0], s[1]);
					}
				} catch (IOException e) {
					System.err.println("Error reading from /config file.");
					System.exit(1);
				}
			} else {
				try (FileWriter fw = new FileWriter(config); BufferedWriter bw = new BufferedWriter(fw);) {
					String tsvpath = getOption("-d", args);
					File data = new File(tsvpath);
					globalOptions.put("DATA", data.getAbsolutePath());
					bw.write("DATA=" + globalOptions.get("DATA"));
				} catch (IOException e) {
					System.err.println("Error writing to /config file.");
					System.exit(1);
				}
			}
		} catch (IOException e1) {
			System.err.println("Error detecting application folder.");
			System.exit(1);
		}

		try {
			CommandManager command = new CommandManager();
			PhoneLoader pl = new PhoneLoader(globalOptions.get("DATA"), false);
			Interpreter sono = new Interpreter(new Scope(null), pl.getManager(), command);

			try (Scanner sc = new Scanner(System.in)) {
				while (true) {
					System.out.print("> ");
					String line = sc.nextLine();
					try {
						Datum result = sono.runCode(".", line);
						if (result.getType() == Datum.Type.VECTOR) {
							int i = 0;
							for (Datum d : result.getVector(new ArrayList<>()))
								System.out.println("\t" + (i++) + ":\t" + d.toStringTrace(new ArrayList<>()));
						} else {
							System.out.println("\t" + result.toStringTrace(new ArrayList<>()));
						}
					} catch (SonoException e) {
						System.err.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}