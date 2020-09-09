package main.sono.ops;

import main.phl.Matrix;
import main.phl.Word;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class Add extends Binary {
	public Add(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.ADD, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Datum datumA = a.evaluate(scope, overrides);
		final Datum datumB = b.evaluate(scope, overrides);
		if (datumA.getType() != datumB.getType())
			throw new SonoRuntimeException("Cannot add values <" + datumA.getDebugString(line, overrides) + "> and <"
					+ datumB.getDebugString(line, overrides) + ">", line);
		switch (datumA.getType()) {
			case NUMBER:
				return new Datum(datumA.getNumber(line, overrides) + datumB.getNumber(line, overrides));
			case VECTOR:
				return Datum.arrayConcat(datumA, datumB);
			case MATRIX:
				final Matrix newMatrix = new Matrix(interpreter.getManager());
				newMatrix.putAll(datumA.getMatrix(line, overrides));
				newMatrix.putAll(datumB.getMatrix(line, overrides));
				return new Datum(newMatrix);
			case WORD:
				final Word newWord = new Word();
				newWord.addAll(datumA.getWord(line, overrides));
				newWord.addAll(datumB.getWord(line, overrides));
				return new Datum(newWord);
			case STRING:
				return new Datum(datumA.getString(line, overrides) + datumB.getString(line, overrides));
			default:
				throw new SonoRuntimeException("Values of type <" + datumA.getType() + "> cannot be added.", line);
		}
	}

	@Override
	public String toString() {
		return a.toString() + " + " + b.toString();
	}
}