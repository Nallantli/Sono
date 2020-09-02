package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class CastStatic extends Casting {
	public CastStatic(final Interpreter interpreter, final Token line, final int varName) {
		super(interpreter, Type.STATIC_DEC, line, varName);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		throw new SonoRuntimeException("Operator cannot exist as a terminal node (Must be used in Class declaration)",
				line);
	}

	@Override
	public String toString() {
		return "static " + interpreter.deHash(varName);
	}
}