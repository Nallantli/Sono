package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Eval extends Unary {
	public Eval(Interpreter i, Token line, Operator a) {
		super(i, Type.EVAL, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final String code = a.evaluate(scope, overrides).getString(line, overrides);
		return interpreter.runCode("", null, code, false, null, overrides);
	}

	@Override
	public String toString() {
		return "eval " + a.toString();
	}

}