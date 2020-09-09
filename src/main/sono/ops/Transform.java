package main.sono.ops;

import main.phl.Phone;
import main.phl.Word;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class Transform extends Binary {
	public Transform(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.TRANSFORM, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Datum datumA = a.evaluate(scope, overrides);
		final Datum datumB = b.evaluate(scope, overrides);
		switch (datumB.getType()) {
			case MATRIX:
				final Phone ret = datumA.getPhone(line, overrides).transform(datumB.getMatrix(line, overrides), true);
				if (ret == null)
					return new Datum();
				return new Datum(ret);
			case RULE:
				final Word result = datumB.getRule(line, overrides).transform(interpreter.getManager(),
						datumA.getWord(line, overrides));
				return new Datum(result);
			default:
				throw new SonoRuntimeException("Cannot transform value <" + datumA.getDebugString(line, overrides)
						+ "> with value <" + datumB.getDebugString(line, overrides) + ">", line);
		}
	}

	@Override
	public String toString() {
		return a.toString() + " >> " + b.toString();
	}
}