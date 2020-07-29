package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class CastExtends extends Binary {
	public CastExtends(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.EXTENDS, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		return null;
	}

	@Override
	public String toString() {
		return a.toString() + " extends " + b.toString();
	}
}