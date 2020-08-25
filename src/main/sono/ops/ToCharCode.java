package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class ToCharCode extends Unary {
	public ToCharCode(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.CODE, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		final String s = a.evaluate(scope).getString(line);
		if (s.length() != 1)
			throw new SonoRuntimeException("Value <" + s + "> is not a single char.", line);
		return new Datum((int) s.charAt(0));
	}

	@Override
	public String toString() {
		return "code " + a.toString();
	}
}