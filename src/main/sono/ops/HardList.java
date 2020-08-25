package main.sono.ops;

import java.util.ArrayList;
import java.util.List;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class HardList extends Sequence {
	private final boolean scoped;

	public HardList(final Interpreter interpreter, final Token line, final Operator[] operators, final boolean scoped) {
		super(interpreter, Type.HARD_LIST, line, operators);
		this.scoped = scoped;
	}

	@Override
	public Datum evaluate(final Scope scope) {
		Datum[] data = null;
		final Scope newScope;
		if (scoped)
			newScope = new Scope(scope.getStructure(), scope);
		else
			newScope = scope;
		if (Interpreter.containsInstance(operators, RangeUntil.class)) {
			final List<Datum> list = new ArrayList<>();
			for (final Operator o : operators) {
				if (o.getType() == Type.RANGE_UNTIL)
					list.addAll(((RangeUntil) o).getRange(newScope));
				else {
					final Datum d = o.evaluate(newScope);
					if (d.getType() == Datum.Type.I_BREAK || d.getRet() || d.getRefer())
						return d;
					list.add(d);
				}
			}
			data = list.toArray(new Datum[0]);
		} else {
			data = new Datum[operators.length];
			int i = 0;
			for (final Operator o : operators) {
				final Datum d = o.evaluate(newScope);
				if (d.getType() == Datum.Type.I_BREAK || d.getRet() || d.getRefer())
					return d;
				data[i++] = d;
			}
		}
		return new Datum(data);
	}

	@Override
	public String toString() {
		return Interpreter.stringFromList(operators, "{", "}");
	}
}