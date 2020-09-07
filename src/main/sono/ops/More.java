package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class More extends Binary {
	public More(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.MORE, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Datum datumA = a.evaluate(scope, overrides);
		final Datum datumB = b.evaluate(scope, overrides);
		return new Datum(datumA.getNumber(line, overrides) > datumB.getNumber(line, overrides));
	}

	@Override
	public String toString() {
		return a.toString() + " > " + b.toString();
	}
}