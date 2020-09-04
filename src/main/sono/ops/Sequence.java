package main.sono.ops;

import java.util.ArrayList;
import java.util.List;

import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Token;

public abstract class Sequence extends Operator {
	protected Operator[] operators;
	protected boolean containsRange;

	public Sequence(final Interpreter i, final Type type, final Token line, final Operator[] operators) {
		super(i, type, line);
		this.operators = operators;
		this.containsRange = true;
	}

	public Operator[] getVector() {
		return operators;
	}

	@Override
	public Operator[] getChildren() {
		return getVector();
	}

	@Override
	public void condense() {
		final List<Operator> newO = new ArrayList<>();
		for (final Operator o : operators) {
			o.condense();
			if (o.getType() == Type.SOFT_LIST && o.getChildren().length == 1)
				newO.add(o.getChildren()[0]);
			else
				newO.add(o);
		}
		this.operators = newO.toArray(new Operator[0]);
		this.containsRange = Interpreter.containsInstance(this.operators, RangeUntil.class);
	}

	@Override
	protected String getInfo() {
		return "(" + containsRange + ")";
	}
}