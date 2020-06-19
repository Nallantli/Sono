package main.sono;

import java.util.List;
import java.util.ArrayList;

import main.sono.err.SonoRuntimeException;

public class Structure {
	private final Scope parent;
	private final Scope mainScope;
	private final Operator main;
	private final boolean stat;
	private final String key;
	private final Interpreter interpreter;

	private final boolean instantiated;

	public Structure(final boolean stat, final Scope parent, final Operator main, final String key, final Interpreter interpreter) {
		this.stat = stat;
		this.parent = parent;
		this.main = main;
		this.mainScope = new Scope(this.parent);
		this.key = key;
		this.interpreter = interpreter;
		this.instantiated = false;
	}

	public Structure(final Structure structure, final List<String> trace) {
		this.interpreter = structure.interpreter;
		this.stat = structure.stat;
		this.parent = structure.parent;
		this.main = structure.main;
		this.mainScope = structure.mainScope.instantiate(trace);
		this.instantiated = true;
		this.key = structure.key;
	}

	public String getKey() {
		return this.key;
	}

	public Scope getScope() {
		return this.mainScope;
	}

	public Datum instantiate(final List<Datum> params, final List<String> trace) {
		if (stat)
			throw new SonoRuntimeException("Cannot instantiate a static class.", trace);
		final Structure structure = new Structure(this, trace);
		structure.main.evaluate(structure.mainScope, interpreter, new ArrayList<>(trace));
		structure.mainScope.setVariable("this", new Datum(structure), trace);
		structure.mainScope.getVariable("init", trace).getFunction(Datum.Type.ANY, trace).execute(params,
				new ArrayList<>(trace));
		return structure.mainScope.getVariable("this", trace);
	}

	public String toStringTrace(final List<String> trace) {
		if (!instantiated)
			return (stat ? "STATIC-" : "STRUCT-") + key;
		else {
			if (this.mainScope.variableExists("getStr")) {
				return this.mainScope.getVariable("getStr", trace).getFunction(Datum.Type.ANY, trace).execute(new ArrayList<>(), new ArrayList<>(trace)).toStringTrace(new ArrayList<>(trace));
			} else {
				return "STRUCT-" + key;
			}
		}
	}
}