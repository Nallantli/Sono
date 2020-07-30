package main.sono;

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

	public Datum execute(final Datum[] pValues, final Token line) {
		final Scope scope = new Scope(parent.getStructure(), parent);
		for (int i = 0; i < paramKeys.length; i++) {
			if (pValues != null && i < pValues.length) {
				if (paramRefs[i]) {
					scope.setVariable(interpreter, paramKeys[i], pValues[i], line);
				} else if (paramFins[i]) {
					final Datum d = new Datum();
					d.set(interpreter.getManager(), pValues[i], line);
					d.setMutable(false);
					scope.setVariable(interpreter, paramKeys[i], d, line);
				} else {
					final Datum d = new Datum();
					d.set(interpreter.getManager(), pValues[i], line);
					scope.setVariable(interpreter, paramKeys[i], d, line);
				}
			} else {
				scope.setVariable(interpreter, paramKeys[i], new Datum(), line);
			}
		}

		final Datum r = body.evaluate(scope);
		if (r.getRefer()) {
			r.setRefer(false);
			return r;
		} else if (r.getRet()) {
			r.setRet(false);
			final Datum nr = new Datum();
			nr.set(interpreter.getManager(), r, line);
			return nr;
		}
		return new Datum();
	}

	public void setParent(final Scope parent) {
		this.parent = parent;
	}

	public Scope getParent() {
		return this.parent;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		final Function f = (Function) o;
		return f.toString().equals(this.toString());
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