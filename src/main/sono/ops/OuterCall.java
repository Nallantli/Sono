package main.sono.ops;

import java.lang.reflect.InvocationTargetException;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class OuterCall extends Unary {
	private final String clazz;
	private final String key;

	public OuterCall(final Interpreter interpreter, final Token line, final String clazz, final String key,
			final Operator a) {
		super(interpreter, Type.OUTER_CALL, line, a);
		this.clazz = clazz;
		this.key = key;
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Datum datumA = a.evaluate(scope, overrides);
		try {
			return interpreter.getCommandManager().execute(clazz, key, datumA.getVector(line, overrides), line,
					overrides);
		} catch (final IllegalAccessException | InvocationTargetException e) {
			throw new SonoRuntimeException(
					"The external library <" + clazz + "> does not exist, or the method <" + key + "> does not exist.",
					line);
		} catch (final NoSuchMethodException e) {
			throw new SonoRuntimeException("The external library <" + clazz + "> does not exist.", line);
		}
	}

	@Override
	public String toString() {
		return " _OUTER_CALL_ " + a.toString();
	}

	@Override
	protected String getInfo() {
		return "(" + clazz + ", " + key + ")";
	}
}