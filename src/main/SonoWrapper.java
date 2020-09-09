package main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import main.base.CommandManager;
import main.phl.PhoneLoader;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Scope;
import main.sono.err.SonoException;
import main.sono.io.Input;
import main.sono.io.Output;

public class SonoWrapper {
	public static final String VERSION = "Beta 1.10.4";

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

	public Datum run(final String directory, final String filename, final String code, final boolean drawTree,
			final Scope override, final Object[] overrides) {
		try {
			if (sono != null)
				return sono.runCode(directory, filename, code, drawTree, override, overrides);
		} catch (final Exception e) {
			if (overrides != null)
				((Output) overrides[1]).println(e.getMessage());
			if (stderr != null)
				stderr.println(e.getMessage());
		}
		return new Datum();
	}

	public static String escape(final String s) {
		return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\b", "\\b").replace("\n", "\\n")
				.replace("\r", "\\r").replace("\f", "\\f").replace("\'", "\\'").replace("\"", "\\\"");
	}

	public SonoWrapper(final PhoneLoader pl, final File filename, final Output stdout, final Output stderr,
			final Input stdin, final boolean drawTree, final Scope override) throws InterruptedException {
		this.stderr = stderr;
		final CommandManager command = new CommandManager();
		sono = new Interpreter(override != null ? override : new Scope(null, null, false),
				pl == null ? null : pl.getManager(), command, stdout, stderr, stdin);

		try {
			sono.runCode("", null, "load \"std.so\"", false, override, null);
		} catch (final SonoException e) {
			stderr.println(
					"Failure to load system library, cannot initiate interpreter. Please check /bin directory for 'std.so'.");
			System.exit(1);
		}

		if (filename != null) {
			sono.runCode("", filename.getName(), "load \"" + escape(filename.getAbsolutePath()) + "\"", drawTree, null,
					null);
		}
	}
}