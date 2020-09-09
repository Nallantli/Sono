package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Container extends Operator {
	private final Datum datum;

	public Container(final Interpreter interpreter, final Token line, final Datum datum) {
		super(interpreter, Type.CONTAINER, line);
		this.datum = datum;
	}

	public Datum getDatum() {
		return datum;
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		return datum;
	}

	@Override
	public String toString() {
		return datum.toString();
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
	protected String getInfo() throws InterruptedException {
		return "(" + datum.getDebugString(null, null) + ")";
	}
}