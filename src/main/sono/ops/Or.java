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
	public Datum evaluate(final Scope scope) {
		final Datum datumA = a.evaluate(scope);
		if (datumA.getBool(line)) {
			return new Datum(true);
		} else {
			final Datum datumB = b.evaluate(scope);
			if (datumB.getBool(line))
				return new Datum(true);
		}
		return new Datum(false);
	}

	@Override
	public String toString() {
		return a.toString() + " || " + b.toString();
	}
}