package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class CastType extends Binary {
	public CastType(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.TYPE_DEC, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		return null;
	}

	@Override
	public String toString() {
		return a.toString() + " :: " + b.toString();
	}
}