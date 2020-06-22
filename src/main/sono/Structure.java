package main.sono;

import java.util.List;
import java.util.ArrayList;

import main.Main;
import main.sono.err.SonoRuntimeException;

public class Structure {
	private final Scope parent;
	private final Scope mainScope;
	private final Operator main;
	private final boolean stat;
	private final int key;
	private final Interpreter interpreter;

	private final boolean instantiated;

	public Structure(final boolean stat, final Scope parent, final Operator main, final int key,
			final Interpreter interpreter) {
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

	public int getKey() {
		return this.key;
	}

	public Scope getScope() {
		return this.mainScope;
	}

	public Datum instantiate(final List<Datum> params, final List<String> trace) {
		if (stat)
			throw new SonoRuntimeException("Cannot instantiate a static class.", trace);
		final Structure structure = new Structure(this, trace);
		structure.main.evaluate(structure.mainScope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
		structure.mainScope.setVariable(Interpreter.THIS, new Datum(structure), trace);
		structure.mainScope.getVariable(Interpreter.INIT, trace).getFunction(Datum.Type.ANY, trace).execute(params,
				(Main.DEBUG ? new ArrayList<>(trace) : trace));
		return structure.mainScope.getVariable(Interpreter.THIS, trace);
	}

	public String toStringTrace(final List<String> trace) {
		if (!instantiated)
			return (stat ? "STATIC-" : "STRUCT-") + Interpreter.deHash(key);
		else {
			if (this.mainScope.variableExists(Interpreter.GETSTR)) {
				return this.mainScope.getVariable(Interpreter.GETSTR, trace).getFunction(Datum.Type.ANY, trace)
						.execute(new ArrayList<>(), (Main.DEBUG ? new ArrayList<>(trace) : trace))
						.toRawStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace));
			} else {
				return "STRUCT-" + Interpreter.deHash(key);
			}
		}
	}
}