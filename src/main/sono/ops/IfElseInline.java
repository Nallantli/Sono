package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class IfElseInline extends Binary {
	private Operator c;

	public IfElseInline(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.IF_ELSE_INLINE, line, a, b);
		this.c = null;
	}

	public void setElse(final Operator c) {
		this.c = c;
	}

	@Override
	public Datum evaluate(final Scope scope) {
		final Datum condition = a.evaluate(scope);
		if (condition.getBool(line)) {
			return b.evaluate(scope);
		} else {
			if (c == null)
				throw new SonoRuntimeException("Inline if-then-statements must have an ELSE clause", line);
			return c.evaluate(scope);
		}
	}

	@Override
	public String toString() {
		return a.toString() + " ? " + b.toString() + " : " + c.toString();
	}

	@Override
	public void condense() {
		super.condense();
		c.condense();
		if (c.getType() == Type.SOFT_LIST && c.getChildren().length == 1)
			c = c.getChildren()[0];
	}

	@Override
	public Operator[] getChildren() {
		if (c == null)
			return new Operator[] { a, b };
		else
			return new Operator[] { a, b, c };
	}
}