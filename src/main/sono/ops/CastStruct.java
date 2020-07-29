package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Scope;
import main.sono.Token;

public class CastStruct extends Casting {
	public CastStruct(final Interpreter interpreter, final Token line, final int varName) {
		super(interpreter, Type.STRUCT_DEC, line, varName);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		return null;
	}

	@Override
	public String toString() {
		return "struct " + interpreter.deHash(varName);
	}
}