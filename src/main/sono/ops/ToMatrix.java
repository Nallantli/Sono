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
		super(interpreter, Type.MATRIX_CONVERT, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		final Datum datumA = a.evaluate(scope);
		if (datumA.getType() == Datum.Type.VECTOR) {
			final int listSize = datumA.getVectorLength(line);
			final Matrix m = new Matrix(interpreter.getManager());
			for (int i = 0; i < listSize; i++) {
				final Feature p = datumA.indexVector(i).getFeature(line);
				m.put(p.getKey(), p.getQuality());
			}
			return new Datum(m);
		} else if (datumA.getType() == Datum.Type.PHONE) {
			return new Datum(datumA.getPhone(line).getMatrix());
		}
		throw new SonoRuntimeException("Cannot convert value <" + datumA.getDebugString(line) + "> to a Matrix.", line);
	}

	@Override
	public String toString() {
		return "mat " + a.toString();
	}
}