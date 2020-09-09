package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class ToChar extends Unary {
	public ToChar(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.TO_CHAR, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Datum datumA = a.evaluate(scope, overrides);
		try {
			final int i = (int) datumA.getNumber(line, overrides);
			return new Datum(String.valueOf((char) i));
		} catch (final Exception e) {
			throw new SonoRuntimeException(
					"Value <" + datumA.getDebugString(line, overrides) + "> is not of type `Number`", line);
		}
	}

	@Override
	public String toString() {
		return "code " + a.toString();
	}
}