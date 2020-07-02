package main;

import java.util.HashMap;
import java.util.Map;

import main.base.CommandManager;
import main.phl.PhoneLoader;
import main.phl.PhoneManager;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Scope;
import main.sono.err.SonoException;
import main.sono.io.Output;

public class SonoWrapper {
	public static final String VERSION = "Beta 1.4.5";
	public static boolean DEBUG;

	private Interpreter sono = null;
	private final Output stderr;

	private static Map<String, String> globalOptions = new HashMap<>();

	public static String getGlobalOption(final String key) {
		if (globalOptions.containsKey(key))
			return globalOptions.get(key);
		return null;
	}

	public static void setGlobalOption(final String key, final String value) {
		globalOptions.put(key, value);
	}

	public Datum run(final String code) {
		try {
			if (sono != null)
				return sono.runCode(".", code);
		} catch (final Exception e) {
			stderr.println(e.getMessage());
		}
		return new Datum();
	}

	public SonoWrapper(final PhoneLoader pl, final String path, final String filename, final Output stdout,
			final Output stderr) {
		this.stderr = stderr;
		final CommandManager command = new CommandManager();
		if (pl == null) {
			sono = new Interpreter(new Scope(null), new PhoneManager(), command, stdout, stderr);
		} else {
			sono = new Interpreter(new Scope(null), pl.getManager(), command, stdout, stderr);
		}

		if (filename != null) {
			try {
				sono.runCode(".", "load \"" + filename + "\"");
				System.exit(0);
			} catch (final SonoException e) {
				stderr.println(e.getMessage());
			}
		}
	}
}