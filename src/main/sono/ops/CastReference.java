package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Scope;
import main.sono.Token;

public class CastReference extends Casting {
	public CastReference(final Interpreter interpreter, final Token line, final int varName) {
		super(interpreter, Type.REF_DEC, line, varName);
		this.varName = varName;
	}

	@Override
	public Datum evaluate(final Scope scope) {
		return null;
	}

	@Override
	public String toString() {
		return "ref " + interpreter.deHash(varName);
	}
}