package main.sono;

import java.util.List;

public class Function {
	private Scope parent;
	private final int[] paramKeys;
	private final boolean[] paramFins;
	private final boolean[] paramRefs;
	private final Operator body;
	private final Interpreter interpreter;

	public Function(final Scope parent, final int[] paramKeys, final boolean[] paramRefs, final boolean[] paramFins,
			final Operator body, final Interpreter interpreter) {
		this.paramKeys = paramKeys;
		this.paramRefs = paramRefs;
		this.paramFins = paramFins;
		this.parent = parent;
		this.body = body;
		this.interpreter = interpreter;
	}

	public Datum execute(final Datum[] pValues, final List<String> trace) {
		final Scope scope = new Scope(parent);
		for (int i = 0; i < paramKeys.length; i++) {
			if (pValues != null && i < pValues.length) {
				if (Boolean.TRUE.equals(paramRefs[i])) {
					scope.setVariable(interpreter, paramKeys[i], pValues[i], trace);
				} else if (Boolean.TRUE.equals(paramFins[i])) {
					final Datum d = new Datum();
					d.set(interpreter.getManager(), pValues[i], trace);
					d.setMutable(false);
					scope.setVariable(interpreter, paramKeys[i], d, trace);
				} else {
					final Datum d = new Datum();
					d.set(interpreter.getManager(), pValues[i], trace);
					scope.setVariable(interpreter, paramKeys[i], d, trace);
				}
			} else {
				scope.setVariable(interpreter, paramKeys[i], new Datum(), trace);
			}
		}

		final Datum r = body.evaluate(scope, trace);
		if (r.getRefer()) {
			r.setRefer(false);
			return r;
		} else if (r.getRet()) {
			r.setRet(false);
			final Datum nr = new Datum();
			nr.set(interpreter.getManager(), r, trace);
			return nr;
		}
		return new Datum();
	}

	public void setParent(final Scope parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		s.append(Interpreter.stringFromList(paramKeys, "(", ")"));
		s.append(" => ");
		s.append(body.toString());
		return s.toString();
	}
}