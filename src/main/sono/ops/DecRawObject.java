package main.sono.ops;

import java.util.HashMap;
import java.util.Map;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class DecRawObject extends Sequence {

	public DecRawObject(final Interpreter i, final Token line, final Operator[] operators) {
		super(i, Operator.Type.DEC_RAW_OBJECT, line, operators);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Map<String, Datum> entries = new HashMap<>();
		for (final Operator o : operators) {
			if (o.getType() != Operator.Type.DEC_ENTRY)
				throw new SonoRuntimeException("Dictionary values require entries as children in declaration", line);
			final DecEntry entry = (DecEntry) o;
			final String entryKey = entry.getA().evaluate(scope, overrides).getString(line, overrides);
			final Datum entryValue = entry.getB().evaluate(scope, overrides);
			entries.put(entryKey, entryValue);
		}
		return new Datum(entries);
	}

	@Override
	public String toString() {
		return Interpreter.stringFromList(operators, "@{", "}", ",");
	}

}