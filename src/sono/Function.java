package src.sono;

import java.util.List;

public class Function {
	private Scope parent;
	private List<String> paramKeys;
	private List<Boolean> paramRefs;
	private Operator body;

	public Function(Scope parent, List<String> paramKeys, List<Boolean> paramRefs, Operator body) {
		this.paramKeys = paramKeys;
		this.paramRefs = paramRefs;
		this.parent = parent;
		this.body = body;
	}

	public Datum execute(List<Datum> paramValues, Interpreter interpreter) {
		Scope scope = new Scope(parent);
		for (int i = 0; i < paramKeys.size(); i++) {
			if (i < paramValues.size()) {
				if (Boolean.FALSE.equals(paramRefs.get(i))) {
					Datum d = new Datum();
					d.set(paramValues.get(i));
					scope.setVariable(paramKeys.get(i), d);
				} else {
					scope.setVariable(paramKeys.get(i), paramValues.get(i));
				}
			} else {
				scope.setVariable(paramKeys.get(i), new Datum());
			}
		}

		Datum r = body.evaluate(scope, interpreter);
		if (r.getRet()) {
			r.setRet(false);
			return r;
		}
		return new Datum();
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