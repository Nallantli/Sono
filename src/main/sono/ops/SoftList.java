package main.sono.ops;

import java.util.ArrayList;
import java.util.List;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class SoftList extends Sequence {
	public SoftList(final Interpreter interpreter, final Token line, final Operator[] operators) {
		super(interpreter, Type.SOFT_LIST, line, operators);
	}

	public SoftList(final HardList o) {
		super(o.getInterpreter(), Type.SOFT_LIST, o.getLine(), o.operators);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		Datum[] data = null;
		if (Interpreter.containsInstance(operators, RangeUntil.class)) {
			final List<Datum> list = new ArrayList<>();
			for (final Operator o : operators) {
				if (o.getType() == Type.RANGE_UNTIL)
					list.addAll(((RangeUntil) o).getRange(scope));
				else {
					final Datum d = o.evaluate(scope);
					if (d.getType() == Datum.Type.I_BREAK)
						return d;
					if (d.getRet() || d.getRefer())
						return d;
					list.add(d);
				}
			}
			data = list.toArray(new Datum[0]);
		} else {
			data = new Datum[operators.length];
			int i = 0;
			for (final Operator o : operators) {
				final Datum d = o.evaluate(scope);
				if (d.getType() == Datum.Type.I_BREAK)
					return d;
				if (d.getRet() || d.getRefer())
					return d;
				data[i++] = d;
			}
		}
		if (data.length == 1)
			return data[0];
		return new Datum(data);
	}

	@Override
	public String toString() {
		return Interpreter.stringFromList(operators, "(", ")");
	}
}