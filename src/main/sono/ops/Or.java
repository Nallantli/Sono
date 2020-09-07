package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Or extends Binary {
	public Or(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.OR, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Datum datumA = a.evaluate(scope, overrides);
		if (datumA.getBool(line, overrides)) {
			return new Datum(true);
		} else {
			final Datum datumB = b.evaluate(scope, overrides);
			if (datumB.getBool(line, overrides))
				return new Datum(true);
		}
		return new Datum(false);
	}

	@Override
	public String toString() {
		return a.toString() + " || " + b.toString();
	}
}