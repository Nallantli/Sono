package main.base;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import main.SonoWrapper;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Token;
import main.sono.err.SonoCompilationException;

public class CommandManager {
	public interface Command {
		public Datum execute(Datum datum, Token line, Interpreter interpreter);
	}

	private final Map<String, Command> commands;

	public CommandManager() {
		commands = new HashMap<>();
	}

	public void importLibrary(final String directory, final String filename, final String classpath) {
		File file = new File(directory, filename);
		if (!file.exists())
			file = new File(SonoWrapper.getGlobalOption("PATH"), "lib/" + filename);
		try {
			final ExtensionLoader<Library> loader = new ExtensionLoader<>();
			final Library library = loader.loadClass(file, classpath, Library.class);
			commands.putAll(library.getCommands());
		} catch (final ClassNotFoundException e) {
			throw new SonoCompilationException("Cannot find library <" + filename + ">");
		}
	}

	public Datum execute(final String key, final Datum datum, final Token line, final Interpreter interpreter) {
		return commands.get(key).execute(datum, line, interpreter);
	}
}