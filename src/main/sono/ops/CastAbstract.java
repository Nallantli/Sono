package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Scope;
import main.sono.Token;

public class CastAbstract extends Casting {
	public CastAbstract(final Interpreter interpreter, final Token line, final int varName) {
		super(interpreter, Type.ABSTRACT_DEC, line, varName);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		return null;
	}

	@Override
	public String toString() {
		return "abstract " + interpreter.deHash(varName);
	}
}