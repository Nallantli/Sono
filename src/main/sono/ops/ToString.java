package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class ToString extends Unary {
	public ToString(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.TO_STRING, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final String s = a.evaluate(scope, overrides).toRawStringTrace(line, overrides);
		return new Datum(s);
	}

	@Override
	public String toString() {
		return "str " + a.toString();
	}
}