package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class Length extends Unary {
	public Length(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.LENGTH, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Datum datumA = a.evaluate(scope, overrides);
		switch (datumA.getType()) {
			case STRING:
				return new Datum(datumA.getString(line, overrides).length());
			case WORD:
				return new Datum(datumA.getWord(line, overrides).size());
			case VECTOR:
				return new Datum(datumA.getVectorLength(line, overrides));
			case DICTIONARY:
				return new Datum(datumA.getDictionaryLength(line, overrides));
			case MATRIX:
				return new Datum(datumA.getMatrix(line, overrides).size());
			case STRUCTURE:
				return datumA.getStructure(line, overrides).getScope().getVariable(interpreter.GET_LEN, interpreter, line, overrides)
						.getFunction(Datum.Type.ANY, line, overrides).execute(null, line, overrides);
			default:
				throw new SonoRuntimeException("Cannot get length of value <" + datumA.getDebugString(line, overrides) + ">",
						line);
		}
	}

	@Override
	public String toString() {
		return "len " + a.toString();
	}
}