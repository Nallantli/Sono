package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class Throw extends Unary {
	public Throw(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.THROW, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		final Datum datumA = a.evaluate(scope);
		throw new SonoRuntimeException(datumA.getString(line), line);
	}

	@Override
	public String toString() {
		return "throw " + a.toString();
	}
}