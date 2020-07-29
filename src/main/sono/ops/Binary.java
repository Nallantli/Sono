package main.sono.ops;

import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Token;

public abstract class Binary extends Unary {
	protected Operator b;

	public Binary(final Interpreter i, final Type type, final Token line, final Operator a, final Operator b) {
		super(i, type, line, a);
		this.b = b;
	}

	public Operator getB() {
		return b;
	}

	@Override
	public Operator[] getChildren() {
		return new Operator[] { a, b };
	}

	@Override
	public void condense() {
		super.condense();
		b.condense();
		if (b.getType() == Type.SOFT_LIST && b.getChildren().length == 1)
			b = b.getChildren()[0];
	}
}