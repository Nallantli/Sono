package main.sono.ops;

import java.util.Map;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Loop extends Binary {
	public Loop(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.LOOP, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		if (a.getType() == Type.ITERATOR) {
			final Datum datumAB = ((Iterator) a).getB().evaluate(scope, overrides);
			switch (datumAB.getType()) {
				case VECTOR:
					final int valuesSize = datumAB.getVectorLength(line, overrides);
					final int variableV = ((Variable) ((Iterator) a).getA()).getKey();
					for (int i = 0; i < valuesSize; i++) {
						final Scope loopScope = new Scope(scope.getStructure(), scope, false);
						loopScope.setVariable(interpreter, variableV, datumAB.indexVector(i), line, overrides);
						final Datum result = b.evaluate(loopScope, overrides);
						if (result.getType() == Datum.Type.I_BREAK)
							break;
						if (result.getRet() || result.getRefer())
							return result;
					}
					break;
				case DICTIONARY:
					final Map<String, Datum> values = datumAB.getMap(line, overrides);
					final int variableD = ((Variable) ((Iterator) a).getA()).getKey();
					for (final Map.Entry<String, Datum> e : values.entrySet()) {
						final Scope loopScope = new Scope(scope.getStructure(), scope, false);
						loopScope.setVariable(interpreter, variableD,
								new Datum(Map.of("key", new Datum(e.getKey()), "value", e.getValue())), line, overrides);
						final Datum result = b.evaluate(loopScope, overrides);
						if (result.getType() == Datum.Type.I_BREAK)
							break;
						if (result.getRet() || result.getRefer())
							return result;
					}
					break;
				default:
			}
		} else {
			while (a.evaluate(scope, overrides).getBool(line, overrides)) {
				final Scope loopScope = new Scope(scope.getStructure(), scope, false);
				final Datum result = b.evaluate(loopScope, overrides);
				if (result.getType() == Datum.Type.I_BREAK)
					break;
				if (result.getRet() || result.getRefer())
					return result;
			}
		}
		return new Datum();
	}

	@Override
	public String toString() {
		return a.toString() + " do " + b.toString();
	}
}