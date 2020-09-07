package main.sono.ops;

import main.phl.Matrix;
import main.phl.Phone;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class Register extends Binary {
	public Register(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.REGISTER, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final String segment = a.evaluate(scope, overrides).getString(line, overrides);
		final Matrix features = b.evaluate(scope, overrides).getMatrix(line, overrides);
		final Phone ret = interpreter.getManager().registerNewPhone(segment, features);
		if (ret == null)
			throw new SonoRuntimeException("Cannot register Phone with features <" + features + ">", line);
		final Datum allV = interpreter.getScope().getVariable(interpreter.ALL, interpreter, line, overrides);
		final int oldVSize = interpreter.getScope().getVariable(interpreter.ALL, interpreter, line, overrides)
				.getVectorLength(line, overrides);
		final Datum[] newV = new Datum[oldVSize + 1];
		for (int i = 0; i < oldVSize; i++)
			newV[i] = allV.indexVector(i);
		newV[oldVSize] = new Datum(ret);
		interpreter.getScope().setVariable(interpreter, interpreter.ALL, new Datum(newV), line, overrides);
		return new Datum(ret);
	}

	@Override
	public String toString() {
		return a.toString() + " register " + b.toString();
	}
}