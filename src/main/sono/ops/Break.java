package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Break extends Operator {
	public Break(final Interpreter interpreter, final Token line) {
		super(interpreter, Type.BREAK, line);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		return new Datum.Break();
	}

	@Override
	public String toString() {
		return "break";
	}

	@Override
	public Operator[] getChildren() {
		return new Operator[0];
	}

	@Override
	public void condense() {
		// Unnecessary
	}
}