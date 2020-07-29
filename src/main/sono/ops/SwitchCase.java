package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class SwitchCase extends Operator {
	private final Datum key;
	private final Operator seq;

	public SwitchCase(final Interpreter interpreter, final Token line, final Datum key, final Operator seq) {
		super(interpreter, Type.SWITCH_CASE, line);
		this.key = key;
		this.seq = seq;
	}

	public Datum getKey() {
		return this.key;
	}

	public Operator getOperator() {
		return this.seq;
	}

	@Override
	public Datum evaluate(final Scope scope) {
		throw new SonoRuntimeException("Cannot evaluate uncontrolled goto statement", line);
	}

	@Override
	public String toString() {
		return key.toString() + " goto " + seq.toString();
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