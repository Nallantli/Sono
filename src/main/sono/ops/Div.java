package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Div extends Binary {
	public Div(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.DIV, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Datum datumA = a.evaluate(scope, overrides);
		final Datum datumB = b.evaluate(scope, overrides);
		try {
			return new Datum(datumA.getNumber(line, overrides) / datumB.getNumber(line, overrides));
		} catch (final Exception e) {
			return new Datum();
		}
	}

	@Override
	public String toString() {
		return a.toString() + " / " + b.toString();
	}
}