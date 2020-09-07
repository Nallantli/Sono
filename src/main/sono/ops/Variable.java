package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Scope;
import main.sono.Token;

public class Variable extends Casting {
	public Variable(final Interpreter interpreter, final Token line, final int varName) {
		super(interpreter, Type.VARIABLE, line, varName);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		return scope.getVariable(varName, interpreter, line, overrides);
	}

	@Override
	public String toString() {
		return interpreter.deHash(getKey());
	}
}