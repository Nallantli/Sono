package main.sono.ops;

import main.sono.Datum;
import main.sono.Function;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class Execute extends Binary {
	public Execute(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.EXECUTE, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		final Datum datumB = b.evaluate(scope);
		final int pValuesSize = datumB.getVectorLength(line);
		Datum[] pValues = null;
		Function f = null;
		if (a.getType() == Type.INNER) {
			final Datum datumA = ((Inner) a).getA().evaluate(scope);
			if (datumA.getType() != Datum.Type.STRUCTURE) {
				final Datum[] tempValues = new Datum[pValuesSize + 1];
				tempValues[0] = datumA;
				for (int i = 0; i < pValuesSize; i++)
					tempValues[i + 1] = datumB.indexVector(i);
				pValues = tempValues;
				final Datum functionB = ((Inner) a).getB().evaluate(scope);
				f = functionB.getFunction(datumA.getType(), line);
				if (f == null)
					f = functionB.getFunction(Datum.Type.ANY, line);
			} else {
				pValues = datumB.getVector(line);
				f = a.evaluate(scope).getFunction(Datum.Type.ANY, line);
			}
		} else {
			pValues = datumB.getVector(line);
			final Datum fDatum = a.evaluate(scope);
			if (pValuesSize != 0)
				f = fDatum.getFunction(datumB.indexVector(0).getType(), line);
			if (f == null)
				f = fDatum.getFunction(Datum.Type.ANY, line);
		}
		if (f == null)
			throw new SonoRuntimeException("No function found", line);
		return f.execute(pValues, line);
	}

	@Override
	public String toString() {
		return a.toString() + Interpreter.stringFromList(((Sequence) b).getVector(), "(", ")");
	}
}