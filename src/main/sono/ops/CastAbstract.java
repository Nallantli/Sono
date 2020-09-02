package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class CastAbstract extends Casting {
	public CastAbstract(final Interpreter interpreter, final Token line, final int varName) {
		super(interpreter, Type.ABSTRACT_DEC, line, varName);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		throw new SonoRuntimeException("Operator cannot exist as a terminal node (Must be used in Class declaration)",
				line);
	}

	@Override
	public String toString() {
		return "abstract " + interpreter.deHash(varName);
	}
}