package main.sono;

import java.util.List;
import java.util.ArrayList;

public class Function {
	private Scope parent;
	private final List<String> paramKeys;
	private final List<Boolean> paramRefs;
	private final Operator body;
	private final Interpreter interpreter;

	public Function(final Scope parent, final List<String> paramKeys, final List<Boolean> paramRefs,
			final Operator body, final Interpreter interpreter) {
		this.paramKeys = paramKeys;
		this.paramRefs = paramRefs;
		this.parent = parent;
		this.body = body;
		this.interpreter = interpreter;
	}

	public Datum execute(final List<Datum> paramValues, final List<String> trace) {
		final Scope scope = new Scope(parent);
		for (int i = 0; i < paramKeys.size(); i++) {
			if (i < paramValues.size()) {
				if (Boolean.FALSE.equals(paramRefs.get(i))) {
					final Datum d = new Datum();
					d.set(paramValues.get(i), trace);
					scope.setVariable(paramKeys.get(i), d, trace);
				} else {
					scope.setVariable(paramKeys.get(i), paramValues.get(i), trace);
				}
			} else {
				scope.setVariable(paramKeys.get(i), new Datum(), trace);
			}
		}

		final Datum r = body.evaluate(scope, interpreter, new ArrayList<>(trace));
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