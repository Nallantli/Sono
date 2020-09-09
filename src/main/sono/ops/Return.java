package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Return extends Unary {
	public Return(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.RETURN, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Datum datumA = a.evaluate(scope, overrides);
		datumA.setRet(true);
		return datumA;
	}

	@Override
	public String toString() {
		return "return " + a.toString();
	}
}