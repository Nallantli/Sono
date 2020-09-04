package main.sono.ops;

import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Token;

public abstract class Casting extends Operator {
	protected int varName;

	public Casting(final Interpreter i, final Type type, final Token line, final int varName) {
		super(i, type, line);
		this.varName = varName;
	}

	public int getKey() {
		return varName;
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