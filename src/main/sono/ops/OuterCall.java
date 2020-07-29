package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class OuterCall extends Binary {
	public OuterCall(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.OUTER_CALL, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		return interpreter.getCommandManager().execute(a.evaluate(scope).getString(line), b.evaluate(scope), line,
				interpreter);
	}

	@Override
	public String toString() {
		return a.toString() + " _OUTER_CALL_ " + b.toString();
	}
}