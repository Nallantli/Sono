package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class DecVariable extends Operator {
	private final int varName;

	public DecVariable(final Interpreter interpreter, final Token line, final int varName) {
		super(interpreter, Type.DEC_VARIABLE, line);
		this.varName = varName;
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		return scope.setVariable(interpreter, varName, null, line, overrides);
	}

	@Override
	public String toString() {
		return "var " + interpreter.deHash(varName);
	}

	@Override
	public Operator[] getChildren() {
		return new Operator[0];
	}

	@Override
	public void condense() {
		// Unnecessary
	}

	@Override
	protected String getInfo() {
		return "(" + interpreter.deHash(varName) + ")";
	}
}