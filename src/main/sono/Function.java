package main.sono;

import java.util.List;

import main.SonoWrapper;

import java.util.ArrayList;

public class Function {
	private Scope parent;
	private final List<Integer> paramKeys;
	private final List<Boolean> paramFins;
	private final List<Boolean> paramRefs;
	private final Operator body;
	private final Interpreter interpreter;

	public Function(final Scope parent, final List<Integer> paramKeys, final List<Boolean> paramRefs,
			final List<Boolean> paramFins, final Operator body, final Interpreter interpreter) {
		this.paramKeys = paramKeys;
		this.paramRefs = paramRefs;
		this.paramFins = paramFins;
		this.parent = parent;
		this.body = body;
		this.interpreter = interpreter;
	}

	public Datum execute(final List<Datum> paramValues, final List<String> trace) {
		final Scope scope = new Scope(parent);
		for (int i = 0; i < paramKeys.size(); i++) {
			if (i < paramValues.size()) {
				if (Boolean.TRUE.equals(paramRefs.get(i))) {
					scope.setVariable(interpreter, paramKeys.get(i), paramValues.get(i), trace);
				} else if (Boolean.TRUE.equals(paramFins.get(i))) {
					final Datum d = new Datum();
					d.set(interpreter.getManager(), paramValues.get(i), trace);
					d.setMutable(false);
					scope.setVariable(interpreter, paramKeys.get(i), d, trace);
				} else {
					final Datum d = new Datum();
					d.set(interpreter.getManager(), paramValues.get(i), trace);
					scope.setVariable(interpreter, paramKeys.get(i), d, trace);
				}
			} else {
				scope.setVariable(interpreter, paramKeys.get(i), new Datum(), trace);
			}
		}

		final Datum r = body.evaluate(scope, (SonoWrapper.DEBUG ? new ArrayList<>(trace) : trace));
		if (r.getRet()) {
			r.setRet(false);
			return r;
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