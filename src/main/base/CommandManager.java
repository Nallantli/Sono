package main.base;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.Main;
import main.sono.Datum;
import main.sono.err.SonoCompilationException;

public class CommandManager {
	private Map<String, Command> commands;

	public CommandManager() {
		commands = new HashMap<>();
	}

	public void importLibrary(String directory, String filename, String classname) {
		File file = new File(directory, filename);
		if (!file.exists())
			file = new File(Main.getGlobalOption("PATH"), "lib/" + filename);
		try {
			ExtensionLoader<Library> loader = new ExtensionLoader<>();
			Library library = loader.loadClass(file, classname, Library.class);
			commands.putAll(library.getCommands());
		} catch (ClassNotFoundException e) {
			throw new SonoCompilationException("Cannot find library <" + filename + ">");
		}
	}

	public Datum execute(String key, Datum datum, List<String> trace) {
		return commands.get(key).execute(datum, trace);
	}
}