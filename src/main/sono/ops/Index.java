package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class Index extends Binary {
	public Index(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.INDEX, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Datum datumA = a.evaluate(scope, overrides);
		final Datum datumB = b.evaluate(scope, overrides);
		if (datumA.getType() == Datum.Type.VECTOR) {
			try {
				return datumA.indexVector((int) datumB.getNumber(line, overrides));
			} catch (final Exception e) {
				throw new SonoRuntimeException("Cannot index List <" + datumA.getDebugString(line, overrides)
						+ "> with value <" + datumB.getDebugString(line, overrides) + ">; Length: "
						+ datumA.getVectorLength(line, overrides), line);
			}
		} else if (datumA.getType() == Datum.Type.STRUCTURE) {
			return datumA.getStructure(line, overrides).getScope().getVariable(interpreter.GET_INDEX, interpreter, line, overrides)
					.getFunction(Datum.Type.ANY, line, overrides).execute(new Datum[] { datumB }, line, overrides);
		} else if (datumA.getType() == Datum.Type.DICTIONARY) {
			return datumA.indexDictionary(datumB.getString(line, overrides));
		}
		throw new SonoRuntimeException("Cannot index value <" + datumA.getDebugString(line, overrides) + ">", line);
	}

	@Override
	public String toString() {
		return a.toString() + "[" + b.toString() + "]";
	}
}