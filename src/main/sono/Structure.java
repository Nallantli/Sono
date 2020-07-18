package main.sono;

import java.util.Collections;
import java.util.List;

import main.sono.err.SonoRuntimeException;

public class Structure {
	private static int HASHCODE = 0;

	private final Scope parent;
	private final Scope mainScope;
	private final Operator main;
	private final boolean stat;
	private final int key;
	private final Interpreter interpreter;

	private final Structure parentStructure;

	private final boolean instantiated;
	private final int hash;

	public Structure(final Structure parentStructure, final boolean stat, final Scope parent, final Operator main,
			final int key, final Interpreter interpreter) {
		this.parentStructure = parentStructure;
		this.stat = stat;
		this.parent = parent;
		this.main = main;
		this.mainScope = new Scope(this, this.parent);
		this.key = key;
		this.interpreter = interpreter;
		this.instantiated = false;
		this.hash = HASHCODE++;
	}

	public Structure(final Structure structure, final List<String> trace) {
		this.parentStructure = structure.parentStructure;
		this.interpreter = structure.interpreter;
		this.stat = structure.stat;
		this.parent = structure.parent;
		this.main = structure.main;
		this.mainScope = structure.mainScope.instantiate(this, interpreter.getManager(), trace);
		this.instantiated = true;
		this.key = structure.key;
		this.hash = HASHCODE++;
	}

	public int getKey() {
		return this.key;
	}

	public Scope getScope() {
		return this.mainScope;
	}

	public Datum instantiate(final Datum[] params, final List<String> trace) {
		if (stat)
			throw new SonoRuntimeException("Cannot instantiate a static class.", trace);
		final Structure structure = new Structure(this, trace);
		structure.main.evaluate(structure.mainScope, trace);
		structure.mainScope.setVariable(interpreter, interpreter.THIS, new Datum(structure), trace);
		structure.mainScope.getVariable(interpreter.INIT, interpreter, trace).getFunction(Datum.Type.ANY, trace)
				.execute(params, trace);
		return structure.mainScope.getVariable(interpreter.THIS, interpreter, trace);
	}

	public String getName() {
		return (parentStructure != null ? parentStructure.getName() + "." : "") + interpreter.deHash(key);
	}

	public String toStringTrace(final List<String> trace) {
		if (!instantiated)
			return (stat ? "STATIC-" : "STRUCT-") + getName();
		else {
			if (this.mainScope.variableExists(interpreter.GET_STR))
				return this.mainScope.getVariable(interpreter.GET_STR, interpreter, trace)
						.getFunction(Datum.Type.ANY, trace).execute(null, trace).toRawStringTrace(trace);
			else
				return "STRUCT-" + getName();
		}
	}

	public int getHash() {
		if (instantiated && this.mainScope.variableExists(interpreter.GET_HASH))
			return (int) this.mainScope.getVariable(interpreter.GET_HASH, interpreter, Collections.emptyList())
					.getFunction(Datum.Type.ANY, Collections.emptyList()).execute(null, Collections.emptyList())
					.getNumber(Collections.emptyList());
		return this.hash;
	}

	public boolean getEquals(final Structure o) {
		if (instantiated && this.mainScope.variableExists(interpreter.ISEQUALS))
			return this.mainScope.getVariable(interpreter.ISEQUALS, interpreter, Collections.emptyList())
					.getFunction(Datum.Type.ANY, Collections.emptyList())
					.execute(new Datum[] { new Datum(o) }, Collections.emptyList())
					.getNumber(Collections.emptyList()) != 0;
		return this == o;
	}

	/**
	 * @deprecated
	 */
	@Override
	@Deprecated(since = "1.5.13")
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * @deprecated
	 */
	@Override
	@Deprecated(since = "1.5.13")
	public boolean equals(final Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;

			return this.getEquals((Structure) o);
	}
}