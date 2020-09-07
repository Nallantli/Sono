package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class NotEqualsPure extends Binary {
	public NotEqualsPure(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.NOT_EQUALS_PURE, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Datum datumA = a.evaluate(scope, overrides);
		final Datum datumB = b.evaluate(scope, overrides);
		return new Datum(!datumA.isEqualPure(datumB, line, overrides));
	}

	@Override
	public String toString() {
		return a.toString() + " !== " + b.toString();
	}
}