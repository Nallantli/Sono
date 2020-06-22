package main.base;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

import main.sono.err.SonoCompilationException;

public class ExtensionLoader<C> {
	public C loadClass(final File file, final String classpath, final Class<C> parentClass)
			throws ClassNotFoundException {
		try {
			final ClassLoader loader = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() },
					getClass().getClassLoader());
			final Class<?> clazz = Class.forName(classpath, true, loader);
			final Class<? extends C> newClass = clazz.asSubclass(parentClass);
			final Constructor<? extends C> constructor = newClass.getConstructor();
			return constructor.newInstance();

		} catch (final Exception e) {
			e.printStackTrace();
		}
		throw new SonoCompilationException("Class " + classpath + " wasn't found in " + file.getAbsolutePath());
	}
}