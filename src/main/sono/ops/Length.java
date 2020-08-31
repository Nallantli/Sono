package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class Length extends Unary {
	public Length(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.LEN, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		final Datum datumA = a.evaluate(scope);
		switch (datumA.getType()) {
			case STRING:
				return new Datum(datumA.getString(line).length());
			case WORD:
				return new Datum(datumA.getWord(line).size());
			case VECTOR:
				return new Datum(datumA.getVectorLength(line));
			case DICTIONARY:
				return new Datum(datumA.getDictionaryLength(line));
			case MATRIX:
				return new Datum(datumA.getMatrix(line).size());
			case STRUCTURE:
				return datumA.getStructure(line).getScope().getVariable(interpreter.GET_LEN, interpreter, line)
						.getFunction(Datum.Type.ANY, line).execute(null, line);
			default:
				throw new SonoRuntimeException("Cannot get length of value <" + datumA.getDebugString(line) + ">",
						line);
		}
	}

	@Override
	public String toString() {
		return "len " + a.toString();
	}
}