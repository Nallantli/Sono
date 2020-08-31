package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class DecEntry extends Binary {
	public DecEntry(final Interpreter i, final Token line, final Operator a, final Operator b) {
		super(i, Operator.Type.ENTRY, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		return null;
	}

	@Override
	public String toString() {
		return a.toString() + " : " + b.toString();
	}

}