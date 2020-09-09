package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class EqualsPure extends Binary {
	public EqualsPure(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.EQUALS_PURE, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Datum datumA = a.evaluate(scope, overrides);
		final Datum datumB = b.evaluate(scope, overrides);
		return new Datum(datumA.isEqualPure(datumB, line, overrides));
	}

	@Override
	public String toString() {
		return a.toString() + " === " + b.toString();
	}
}