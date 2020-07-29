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
	public Datum evaluate(final Scope scope) {
		final Datum datumA = a.evaluate(scope);
		if (datumA.getBool(line)) {
			final Datum datumB = b.evaluate(scope);
			if (datumB.getBool(line))
				return new Datum(true);
		}
		return new Datum(false);
	}

	@Override
	public String toString() {
		return a.toString() + " && " + b.toString();
	}
}