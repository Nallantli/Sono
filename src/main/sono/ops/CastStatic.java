package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Scope;
import main.sono.Token;

public class CastStatic extends Casting {
	public CastStatic(final Interpreter interpreter, final Token line, final int varName) {
		super(interpreter, Type.STATIC_DEC, line, varName);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		return null;
	}

	@Override
	public String toString() {
		return "static " + interpreter.deHash(varName);
	}
}