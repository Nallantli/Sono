package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Refer extends Unary {
	public Refer(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.REFER, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Datum datumA = a.evaluate(scope, overrides);
		datumA.setRefer(true);
		return datumA;
	}

	@Override
	public String toString() {
		return "refer " + a.toString();
	}
}