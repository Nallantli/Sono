package main.base;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

import main.sono.err.SonoCompilationException;

public class ExtensionLoader<C> {
	public C loadClass(File file, String classpath, Class<C> parentClass) throws ClassNotFoundException {
		try {
			ClassLoader loader = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() },
					getClass().getClassLoader());
			Class<?> clazz = Class.forName(classpath, true, loader);
			Class<? extends C> newClass = clazz.asSubclass(parentClass);
			Constructor<? extends C> constructor = newClass.getConstructor();
			return constructor.newInstance();

		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new SonoCompilationException(
				"Class " + classpath + " wasn't found in " + file.getAbsolutePath());
	}
}