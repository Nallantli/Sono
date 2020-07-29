package main.sono.ops;

import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Token;

public abstract class Unary extends Operator {
	protected Operator a;

	public Unary(final Interpreter i, final Type type, final Token line, final Operator a) {
		super(i, type, line);
		this.a = a;
	}

	public Operator getA() {
		return a;
	}

	@Override
	public Operator[] getChildren() {
		return new Operator[] { a };
	}

	@Override
	public void condense() {
		a.condense();
		if (a.getType() == Type.SOFT_LIST && a.getChildren().length == 1)
			a = a.getChildren()[0];
	}
}