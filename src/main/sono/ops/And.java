package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class And extends Binary {
	public And(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.AND, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Datum datumA = a.evaluate(scope, overrides);
		if (datumA.getBool(line, overrides)) {
			final Datum datumB = b.evaluate(scope, overrides);
			if (datumB.getBool(line, overrides))
				return new Datum(true);
		}
		return new Datum(false);
	}

	@Override
	public String toString() {
		return a.toString() + " && " + b.toString();
	}
}