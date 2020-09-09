package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Set extends Binary {
	public Set(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.SET, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Datum datumA = a.evaluate(scope, overrides);
		final Datum datumB = b.evaluate(scope, overrides);
		datumA.set(interpreter.getManager(), datumB, line, overrides);
		return datumA;
	}

	@Override
	public String toString() {
		return a.toString() + " = " + b.toString();
	}
}