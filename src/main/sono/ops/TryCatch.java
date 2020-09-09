package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class TryCatch extends Unary {
	private Operator b;

	public TryCatch(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.TRY_CATCH, line, a);
		this.b = null;
	}

	public void setCatch(final Operator b) {
		this.b = b;
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		try {
			return a.evaluate(new Scope(scope.getStructure(), scope, false), overrides);
		} catch (final SonoRuntimeException e) {
			if (b != null) {
				final Scope catchScope = new Scope(scope.getStructure(), scope, false);
				catchScope.setVariable(interpreter, interpreter.ERROR, new Datum(e.getMessage()), line, overrides);
				return b.evaluate(catchScope, overrides);
			}
		}
		return new Datum();
	}

	@Override
	public String toString() {
		return "try " + a.toString() + (b != null ? " catch " + b.toString() : "");
	}

	@Override
	public Operator[] getChildren() {
		if (b == null)
			return new Operator[] { a };
		else
			return new Operator[] { a, b };
	}
}