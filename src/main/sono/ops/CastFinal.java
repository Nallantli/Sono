package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class CastFinal extends Casting {
	public CastFinal(final Interpreter interpreter, final Token line, final int varName) {
		super(interpreter, Type.FINAL, line, varName);
		this.varName = varName;
	}

	@Override
	public Datum evaluate(final Scope scope) {
		throw new SonoRuntimeException(
				"Operator cannot exist as a terminal node (Must be used in Function declaration)", line);
	}

	@Override
	public String toString() {
		return "final " + interpreter.deHash(varName);
	}
}