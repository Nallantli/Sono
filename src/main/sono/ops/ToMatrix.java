package main.sono.ops;

import main.phl.Feature;
import main.phl.Matrix;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class ToMatrix extends Unary {
	public ToMatrix(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.TO_MATRIX, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Datum datumA = a.evaluate(scope, overrides);
		if (datumA.getType() == Datum.Type.VECTOR) {
			final int listSize = datumA.getVectorLength(line, overrides);
			final Matrix m = new Matrix(interpreter.getManager());
			for (int i = 0; i < listSize; i++) {
				final Feature p = datumA.indexVector(i).getFeature(line, overrides);
				m.put(p.getKey(), p.getQuality());
			}
			return new Datum(m);
		} else if (datumA.getType() == Datum.Type.PHONE) {
			return new Datum(datumA.getPhone(line, overrides).getMatrix());
		}
		throw new SonoRuntimeException("Cannot convert value <" + datumA.getDebugString(line, overrides) + "> to a Matrix.", line);
	}

	@Override
	public String toString() {
		return "mat " + a.toString();
	}
}