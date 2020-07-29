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
	public Datum evaluate(final Scope scope) {
		final Datum datumA = a.evaluate(scope);
		final Datum datumB = b.evaluate(scope);
		if (datumA.getType() == Datum.Type.VECTOR) {
			try {
				return datumA.indexVector((int) datumB.getNumber(line));
			} catch (final Exception e) {
				throw new SonoRuntimeException("Cannot index List <" + datumA.getDebugString(line) + "> with value <"
						+ datumB.getDebugString(line) + ">; Length: " + datumA.getVectorLength(line), line);
			}
		} else if (datumA.getType() == Datum.Type.STRUCTURE) {
			return datumA.getStructure(line).getScope().getVariable(interpreter.GET_INDEX, interpreter, line)
					.getFunction(Datum.Type.ANY, line).execute(new Datum[] { datumB }, line);
		}
		throw new SonoRuntimeException("Cannot index value <" + datumA.getDebugString(line) + ">", line);
	}

	@Override
	public String toString() {
		return a.toString() + "[" + b.toString() + "]";
	}
}