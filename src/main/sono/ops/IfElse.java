package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class IfElse extends Binary {
	private Operator c;

	public IfElse(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.IF_ELSE, line, a, b);
		this.c = null;
	}

	public void setElse(final Operator c) {
		this.c = c;
	}

	@Override
	public Datum evaluate(final Scope scope) {
		final Datum condition = a.evaluate(scope);
		if (condition.getBool(line))
			return b.evaluate(new Scope(scope.getStructure(), scope));
		else if (c != null)
			return c.evaluate(new Scope(scope.getStructure(), scope));
		return new Datum();
	}

	@Override
	public String toString() {
		return a.toString() + " then " + b.toString() + (c != null ? " else " + c.toString() : "");
	}

	@Override
	public void condense() {
		super.condense();
		if (c != null) {
			c.condense();
			if (c.getType() == Type.SOFT_LIST && c.getChildren().length == 1)
				c = c.getChildren()[0];
		}
	}

	@Override
	public Operator[] getChildren() {
		if (c == null)
			return new Operator[] { a, b };
		else
			return new Operator[] { a, b, c };
	}
}