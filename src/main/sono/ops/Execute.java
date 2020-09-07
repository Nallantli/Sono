package main.sono.ops;

import main.sono.Datum;
import main.sono.Function;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Structure;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class Execute extends Binary {
	public Execute(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.EXECUTE, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Datum datumB = b.evaluate(scope, overrides);
		final int pValuesSize = datumB.getVectorLength(line, overrides);
		Datum[] pValues = null;
		Function f = null;
		if (a.getType() == Type.INNER) {
			final Datum datumA = ((Inner) a).getA().evaluate(scope, overrides);

			if (datumA.getType() == Datum.Type.STRUCTURE) {
				f = a.evaluate(scope, overrides).getFunction(Datum.Type.ANY, line, overrides);

				final Structure structure = datumA.getStructure(line, overrides);
				if (f != null && f.getParent() == structure.getScope()) {
					pValues = datumB.getVector(line, overrides);
				} else {
					final Datum[] tempValues = new Datum[pValuesSize + 1];
					tempValues[0] = datumA;
					for (int i = 0; i < pValuesSize; i++)
						tempValues[i + 1] = datumB.indexVector(i);
					pValues = tempValues;
				}
			} else {
				final Datum functionB = ((Inner) a).getB().evaluate(scope, overrides);
				f = functionB.getFunction(datumA.getType(), line, overrides);
				if (f == null)
					f = functionB.getFunction(Datum.Type.ANY, line, overrides);

				final Datum[] tempValues = new Datum[pValuesSize + 1];
				tempValues[0] = datumA;
				for (int i = 0; i < pValuesSize; i++)
					tempValues[i + 1] = datumB.indexVector(i);
				pValues = tempValues;
			}
		} else {
			pValues = datumB.getVector(line, overrides);
			final Datum fDatum = a.evaluate(scope, overrides);
			if (pValuesSize > 0)
				f = fDatum.getFunction(datumB.indexVector(0).getType(), line, overrides);
			if (f == null)
				f = fDatum.getFunction(Datum.Type.ANY, line, overrides);
		}
		if (f == null)
			throw new SonoRuntimeException("No function found", line);

		return f.execute(pValues, line, overrides);
	}

	@Override
	public String toString() {
		return a.toString() + Interpreter.stringFromList(((Sequence) b).getVector(), "(", ")", ",");
	}
}