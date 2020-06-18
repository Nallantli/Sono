package main.sono;

import java.util.List;
import java.util.ArrayList;

public class Function {
	private Scope parent;
	private List<String> paramKeys;
	private List<Boolean> paramRefs;
	private Operator body;
	private Interpreter interpreter;

	public Function(Scope parent, List<String> paramKeys, List<Boolean> paramRefs, Operator body, Interpreter interpreter) {
		this.paramKeys = paramKeys;
		this.paramRefs = paramRefs;
		this.parent = parent;
		this.body = body;
		this.interpreter = interpreter;
	}

	public Datum execute(List<Datum> paramValues, List<String> trace) {
		Scope scope = new Scope(parent);
		for (int i = 0; i < paramKeys.size(); i++) {
			if (i < paramValues.size()) {
				if (Boolean.FALSE.equals(paramRefs.get(i))) {
					Datum d = new Datum();
					d.set(paramValues.get(i), trace);
					scope.setVariable(paramKeys.get(i), d, trace);
				} else {
					scope.setVariable(paramKeys.get(i), paramValues.get(i), trace);
				}
			} else {
				scope.setVariable(paramKeys.get(i), new Datum(), trace);
			}
		}

		Datum r = body.evaluate(scope, interpreter, new ArrayList<>(trace));
		if (r.getRet()) {
			r.setRet(false);
			return r;
		}
		return new Datum();
	}

	public void setParent(Scope parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(Interpreter.stringFromList(paramKeys, "(", ")"));
		s.append(" => ");
		s.append(body.toString());
		return s.toString();
	}
}