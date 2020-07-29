package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Structure;
import main.sono.Token;

public class DecClass extends Binary {
	public DecClass(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.CLASS_DEC, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		Structure.Type stype = Structure.Type.STRUCT;
		Operator objectOperator = null;
		Structure extending = null;
		if (a.getType() == Type.EXTENDS) {
			objectOperator = ((CastExtends) a).getA();
			extending = ((CastExtends) a).getB().evaluate(scope).getStructure(line);
		} else {
			objectOperator = a;
		}
		final int varName = ((Casting) objectOperator).getKey();
		if (objectOperator.getType() == Type.STATIC_DEC)
			stype = Structure.Type.STATIC;
		else if (objectOperator.getType() == Type.ABSTRACT_DEC)
			stype = Structure.Type.ABSTRACT;
		Operator main = b;
		if (extending != null)
			main = new SoftList(interpreter, line, new Operator[] { extending.getMain(), main });
		final Structure structure = new Structure(scope.getStructure(), stype, scope, main, varName, interpreter);
		if (stype == Structure.Type.STATIC)
			b.evaluate(structure.getScope());
		return scope.setVariable(interpreter, varName, new Datum(structure), line);
	}

	@Override
	public String toString() {
		return a.toString() + " class " + b.toString();
	}
}