package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Structure;
import main.sono.Token;

public class DecNew extends Unary {
	public DecNew(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.DEC_NEW, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Structure struct = ((Execute) a).getA().evaluate(scope, overrides).getStructure(line, overrides);
		final Datum[] params = ((Execute) a).getB().evaluate(scope, overrides).getVector(line, overrides);
		return struct.instantiate(params, line, overrides);
	}

	@Override
	public String toString() {
		return "new " + a.toString();
	}
}