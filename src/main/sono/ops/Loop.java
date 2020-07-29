package main.sono.ops;

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
			final int valuesSize = datumAB.getVectorLength(line);
			final int variable = ((Variable) ((Iterator) a).getA()).getKey();
			for (int i = 0; i < valuesSize; i++) {
				final Scope loopScope = new Scope(scope.getStructure(), scope);
				loopScope.setVariable(interpreter, variable, datumAB.indexVector(i), line);
				final Datum result = b.evaluate(loopScope);
				if (result.getType() == Datum.Type.I_BREAK)
					break;
				if (result.getRet() || result.getRefer())
					return result;
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