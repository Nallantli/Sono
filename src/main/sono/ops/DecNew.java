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
	public Datum evaluate(final Scope scope) {
		final Structure struct = ((Execute) a).getA().evaluate(scope).getStructure(line);
		final Datum[] params = ((Execute) a).getB().evaluate(scope).getVector(line);
		return struct.instantiate(params, line);
	}

	@Override
	public String toString() {
		return "new " + a.toString();
	}
}