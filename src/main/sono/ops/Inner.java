package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Structure;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class Inner extends Binary {
	public Inner(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.INNER, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Datum object = a.evaluate(scope, overrides);
		if (object.getType() != Datum.Type.STRUCTURE) {
			if (!object.isPrototypic())
				throw new SonoRuntimeException("Value <" + object.getDebugString(line, overrides)
						+ "> is not prototypic and therefore cannot extract objective methods.", line);
			final Datum fHolder = b.evaluate(scope, overrides);
			return new Datum(object.getType(), fHolder.getFunction(object.getType(), line, overrides));
		} else {
			final Structure s = object.getStructure(line, overrides);
			if (s.perusable())
				return b.evaluate(object.getStructure(line, overrides).getScope(), overrides);
			throw new SonoRuntimeException("Class <" + s.getName() + "> is not perusable.", line);
		}
	}

	@Override
	public String toString() {
		return a.toString() + "." + b.toString();
	}
}