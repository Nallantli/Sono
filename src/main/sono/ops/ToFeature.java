package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class ToFeature extends Unary {
	public ToFeature(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.FEAT_DEC, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		final Datum datumA = a.evaluate(scope);
		return new Datum(interpreter.getManager().interpretFeature(datumA.getString(line)));
	}

	@Override
	public String toString() {
		return "feat " + a.toString();
	}
}