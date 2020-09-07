package main.base;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import main.SonoWrapper;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Token;
import main.sono.err.SonoCompilationException;

public class CommandManager {
	private final Map<String, Library> classes;

	public CommandManager() {
		classes = new HashMap<>();
	}

	public void importLibrary(final String directory, final String filename, final String rawDir, final String path,
			final String classpath, final Interpreter interpreter) {
		File file = new File(directory, filename);
		if (!file.exists())
			file = new File(SonoWrapper.getGlobalOption("PATH"),
					"lib" + File.separator + directory + File.separator + filename);
		if (!file.exists())
			file = new File(SonoWrapper.getGlobalOption("PATH"), "lib" + File.separator + path);
		try {
			final ExtensionLoader<Library> loader = new ExtensionLoader<>();
			classes.put(filename, loader.loadClass(file, classpath, Library.class, interpreter));
		} catch (final ClassNotFoundException e) {
			throw new SonoCompilationException("Cannot find library <" + filename + ">");
		}
	}

	public Datum execute(final String clazz, final String key, final Datum[] data, final Token line,
			final Object[] overrides) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Library l = classes.get(clazz + ".jar");
		if (data.length > 0) {
			final Method m = l.getClass().getDeclaredMethod(key, Datum[].class, Token.class, Object[].class);
			return (Datum) m.invoke(l, data, line, overrides);
		} else {
			final Method m = l.getClass().getDeclaredMethod(key, Token.class, Object[].class);
			return (Datum) m.invoke(l, line, overrides);
		}
	}
}