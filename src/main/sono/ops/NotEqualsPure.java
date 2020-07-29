package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class NotEqualsPure extends Binary {
	public NotEqualsPure(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.P_NOT_EQUAL, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		final Datum datumA = a.evaluate(scope);
		final Datum datumB = b.evaluate(scope);
		return new Datum(!datumA.isEqualPure(datumB, line));
	}

	@Override
	public String toString() {
		return a.toString() + " !== " + b.toString();
	}
}