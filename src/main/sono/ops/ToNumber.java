package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class ToNumber extends Unary {
	public ToNumber(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.TO_NUMBER, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Datum datumA = a.evaluate(scope, overrides);
		if (datumA.getType() == Datum.Type.NUMBER) {
			return datumA;
		} else if (datumA.getType() == Datum.Type.STRING) {
			try {
				return new Datum((double) Double.valueOf(datumA.getString(line, overrides)));
			} catch (final Exception e) {
				return new Datum();
			}
		} else {
			throw new SonoRuntimeException("Cannot convert value <" + datumA.getDebugString(line, overrides) + "> to a Number.",
					line);
		}
	}

	@Override
	public String toString() {
		return "num " + a.toString();
	}
}