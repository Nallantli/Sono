package main.base;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.Main;
import main.sono.Datum;
import main.sono.err.SonoCompilationException;

public class CommandManager {
	private final Map<String, Command> commands;

	public CommandManager() {
		commands = new HashMap<>();
	}

	public void importLibrary(final String directory, final String filename, final String classname) {
		File file = new File(directory, filename);
		if (!file.exists())
			file = new File(Main.getGlobalOption("PATH"), "lib/" + filename);
		try {
			final ExtensionLoader<Library> loader = new ExtensionLoader<>();
			final Library library = loader.loadClass(file, classname, Library.class);
			commands.putAll(library.getCommands());
		} catch (final ClassNotFoundException e) {
			throw new SonoCompilationException("Cannot find library <" + filename + ">");
		}
	}

	public Datum execute(final String key, final Datum datum, final List<String> trace) {
		return commands.get(key).execute(datum, trace);
	}
}