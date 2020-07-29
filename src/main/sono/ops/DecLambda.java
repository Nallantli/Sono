package main.sono.ops;

import main.sono.Datum;
import main.sono.Function;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class DecLambda extends Binary {
	public DecLambda(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.LAMBDA, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		Operator[] paramsRaw;
		int[] pNames = null;
		boolean[] pRefs = null;
		boolean[] pFins = null;
		Datum.Type fType = Datum.Type.ANY;
		int i = 0;
		if (a.getType() == Type.HARD_LIST) {
			paramsRaw = ((Sequence) a).getVector();
			pNames = new int[paramsRaw.length];
			pRefs = new boolean[paramsRaw.length];
			pFins = new boolean[paramsRaw.length];
			for (final Operator d : paramsRaw) {
				switch (d.getType()) {
					case REF_DEC:
						pRefs[i] = true;
						pFins[i] = false;
						pNames[i] = ((CastReference) d).getKey();
						break;
					case FINAL:
						pFins[i] = true;
						pRefs[i] = false;
						pNames[i] = ((CastFinal) d).getKey();
						break;
					default:
						pRefs[i] = false;
						pFins[i] = false;
						pNames[i] = ((Variable) d).getKey();
						break;
				}
				i++;
			}
		} else if (a.getType() == Type.TYPE_DEC) {
			final Datum t = ((CastType) a).getA().evaluate(scope);
			if (!t.isPrototypic())
				throw new SonoRuntimeException(
						"Value <" + t.getDebugString(line) + "> cannot be used to designate an objective function.",
						line);
			fType = t.getType();
			paramsRaw = ((Sequence) ((CastType) a).getB()).getVector();
			pNames = new int[paramsRaw.length];
			pRefs = new boolean[paramsRaw.length];
			pFins = new boolean[paramsRaw.length];
			for (final Operator d : paramsRaw) {
				switch (d.getType()) {
					case REF_DEC:
						pRefs[i] = true;
						pFins[i] = false;
						pNames[i] = ((CastReference) d).getKey();
						break;
					case FINAL:
						pFins[i] = true;
						pRefs[i] = false;
						pNames[i] = ((CastFinal) d).getKey();
						break;
					default:
						pRefs[i] = false;
						pFins[i] = false;
						pNames[i] = ((Variable) d).getKey();
						break;
				}
				i++;
			}
		}
		return new Datum(fType, new Function(scope, pNames, pRefs, pFins, b, interpreter));
	}

	@Override
	public String toString() {
		return a.toString() + " => " + b.toString();
	}
}