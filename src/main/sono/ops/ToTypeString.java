package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class ToTypeString extends Unary {
	public ToTypeString(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.TO_TYPE_STRING, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		final Datum datumA = a.evaluate(scope);
		return new Datum(datumA.getTypeString());
	}

	@Override
	public String toString() {
		return "type " + a.toString();
	}
}