package main.sono.ops;

import java.util.ArrayList;
import java.util.List;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class RangeUntil extends Binary {
	public RangeUntil(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.RANGE_UNTIL, line, a, b);
	}

	public List<Datum> getRange(final Scope scope, final Object[] overrides) {
		final int datumA = (int) a.evaluate(scope, overrides).getNumber(line, overrides);
		final int datumB = (int) b.evaluate(scope, overrides).getNumber(line, overrides);
		final List<Datum> data = new ArrayList<>();
		for (int i = datumA; i < datumB; i++)
			data.add(new Datum(i));
		return data;
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		return null;
	}

	@Override
	public String toString() {
		return a.toString() + " until " + b.toString();
	}
}