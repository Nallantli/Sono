package main.sono.ops;

import main.phl.Feature;
import main.phl.Matrix;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class DecMatrix extends Sequence {
	public DecMatrix(final Interpreter interpreter, final Token line, final Operator[] operators) {
		super(interpreter, Type.DEC_MATRIX, line, operators);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Matrix matrix = new Matrix(interpreter.getManager());
		for (final Operator o : operators) {
			final Feature p = o.evaluate(scope, overrides).getFeature(line, overrides);
			matrix.put(p.getKey(), p.getQuality());
		}
		return new Datum(matrix);
	}

	@Override
	public String toString() {
		return Interpreter.stringFromList(operators, "[", "]", ",");
	}
}