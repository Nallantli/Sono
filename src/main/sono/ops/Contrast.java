package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Contrast extends Binary {
	public Contrast(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.CONTRAST, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		return new Datum(interpreter.getManager().getContrast(a.evaluate(scope, overrides).getPhone(line, overrides),
				b.evaluate(scope, overrides).getPhone(line, overrides)));
	}

	@Override
	public String toString() {
		return a.toString() + " ?> " + b.toString();
	}
}