package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class CastStruct extends Casting {
	public CastStruct(final Interpreter interpreter, final Token line, final int varName) {
		super(interpreter, Type.CAST_STRUCT, line, varName);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		throw new SonoRuntimeException("Operator cannot exist as a terminal node (Must be used in Class declaration)",
				line);
	}

	@Override
	public String toString() {
		return "struct " + interpreter.deHash(varName);
	}
}