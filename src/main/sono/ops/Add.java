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
	public Datum evaluate(final Scope scope) {
		final Datum datumA = a.evaluate(scope);
		final Datum datumB = b.evaluate(scope);
		if (datumA.getType() != datumB.getType())
			throw new SonoRuntimeException(
					"Cannot add values <" + datumA.getDebugString(line) + "> and <" + datumB.getDebugString(line) + ">",
					line);
		switch (datumA.getType()) {
			case NUMBER:
				return new Datum(datumA.getNumber(line) + datumB.getNumber(line));
			case VECTOR:
				return Datum.arrayConcat(datumA, datumB);
			case MATRIX:
				final Matrix newMatrix = new Matrix(interpreter.getManager());
				newMatrix.putAll(datumA.getMatrix(line));
				newMatrix.putAll(datumB.getMatrix(line));
				return new Datum(newMatrix);
			case WORD:
				final Word newWord = new Word();
				newWord.addAll(datumA.getWord(line));
				newWord.addAll(datumB.getWord(line));
				return new Datum(newWord);
			case STRING:
				return new Datum(datumA.getString(line) + datumB.getString(line));
			default:
				throw new SonoRuntimeException("Values of type <" + datumA.getType() + "> cannot be added.", line);
		}
	}

	@Override
	public String toString() {
		return a.toString() + " + " + b.toString();
	}
}