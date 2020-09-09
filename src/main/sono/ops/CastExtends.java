package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class CastExtends extends Binary {
	public CastExtends(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.EXTENDS, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		throw new SonoRuntimeException("Operator cannot exist as a terminal node (Must be used in Class declaration)",
				line);
	}

	@Override
	public String toString() {
		return a.toString() + " extends " + b.toString();
	}
}