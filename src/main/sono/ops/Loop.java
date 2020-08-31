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
	public Datum evaluate(final Scope scope) {
		if (a.getType() == Type.ITERATOR) {
			final Datum datumAB = ((Iterator) a).getB().evaluate(scope);
			switch (datumAB.getType()) {
				case VECTOR:
					final int valuesSize = datumAB.getVectorLength(line);
					final int variableV = ((Variable) ((Iterator) a).getA()).getKey();
					for (int i = 0; i < valuesSize; i++) {
						final Scope loopScope = new Scope(scope.getStructure(), scope);
						loopScope.setVariable(interpreter, variableV, datumAB.indexVector(i), line);
						final Datum result = b.evaluate(loopScope);
						if (result.getType() == Datum.Type.I_BREAK)
							break;
						if (result.getRet() || result.getRefer())
							return result;
					}
					break;
				case DICTIONARY:
					final Map<String, Datum> values = datumAB.getMap(line);
					final int variableD = ((Variable) ((Iterator) a).getA()).getKey();
					for (final Map.Entry<String, Datum> e : values.entrySet()) {
						final Scope loopScope = new Scope(scope.getStructure(), scope);
						loopScope.setVariable(interpreter, variableD,
								new Datum(Map.of("key", new Datum(e.getKey()), "value", e.getValue())), line);
						final Datum result = b.evaluate(loopScope);
						if (result.getType() == Datum.Type.I_BREAK)
							break;
						if (result.getRet() || result.getRefer())
							return result;
					}
					break;
				default:
			}
		} else {
			while (a.evaluate(scope).getBool(line)) {
				final Scope loopScope = new Scope(scope.getStructure(), scope);
				final Datum result = b.evaluate(loopScope);
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