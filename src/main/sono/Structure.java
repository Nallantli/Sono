package main.sono;

import main.sono.err.SonoRuntimeException;

public class Structure {
	public enum Type {
		STATIC, STRUCT, ABSTRACT
	}

	private static int HASHCODE = 0;

	private final Scope parent;
	private final Scope mainScope;
	private final Operator main;
	private final Type type;
	private final int key;
	private final Interpreter interpreter;

	private final Structure parentStructure;

	private final boolean instantiated;
	private final int hash;

	public Structure(final Structure parentStructure, final Type type, final Scope parent, final Operator main,
			final int key, final Interpreter interpreter) {
		this.parentStructure = parentStructure;
		this.type = type;
		this.parent = parent;
		this.main = main;
		this.mainScope = new Scope(this, this.parent, false);
		this.key = key;
		this.interpreter = interpreter;
		this.instantiated = false;
		this.hash = HASHCODE++;
	}

	public Structure(final Structure structure, final Token line, final Object[] overrides) {
		this.parentStructure = structure.parentStructure;
		this.interpreter = structure.interpreter;
		this.type = structure.type;
		this.parent = structure.parent;
		this.main = structure.main;
		this.mainScope = structure.mainScope.instantiate(this, interpreter.getManager(), line, overrides);
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

	public Datum instantiate(final Datum[] params, final Token line, final Object[] overrides) {
		if (type != Type.STRUCT)
			throw new SonoRuntimeException("Cannot instantiate a static class.", line);
		final Structure structure = new Structure(this, line, overrides);
		structure.main.evaluate(structure.mainScope, overrides);
		structure.mainScope.setVariable(interpreter, interpreter.THIS, new Datum(structure), line, overrides);
		structure.mainScope.getVariable(interpreter.INIT, interpreter, line, overrides)
				.getFunction(Datum.Type.ANY, line, overrides).execute(params, line, overrides);
		return structure.mainScope.getVariable(interpreter.THIS, interpreter, line, overrides);
	}

	public String getName() {
		return (parentStructure != null ? parentStructure.getName() + "." : "") + interpreter.deHash(key);
	}

	public String toStringTrace(final Token line, final Object[] overrides) {
		if (!instantiated)
			return type.toString() + "-" + getName();
		else {
			if (this.mainScope.variableExists(interpreter.GET_STR))
				return this.mainScope.getVariable(interpreter.GET_STR, interpreter, line, overrides)
						.getFunction(Datum.Type.ANY, line, overrides).execute(null, line, overrides)
						.toRawStringTrace(line, overrides);
			else
				return "STRUCT-" + getName();
		}
	}

	public boolean perusable() {
		if (type == Type.ABSTRACT)
			return false;
		return type == Type.STATIC || (type == Type.STRUCT && instantiated);
	}

	public int getHash(final Object[] overrides) {
		if (instantiated && this.mainScope.variableExists(interpreter.GET_HASH))
			return (int) this.mainScope.getVariable(interpreter.GET_HASH, interpreter, null, overrides)
					.getFunction(Datum.Type.ANY, null, overrides).execute(null, null, overrides)
					.getNumber(null, overrides);
		return this.hash;
	}

	public boolean isEqual(final Structure o, final Token line, final Object[] overrides) {
		if (instantiated && this.mainScope.variableExists(interpreter.ISEQUALS))
			return this.mainScope.getVariable(interpreter.ISEQUALS, interpreter, line, overrides)
					.getFunction(Datum.Type.ANY, line, overrides).execute(new Datum[] { new Datum(o) }, line, overrides)
					.getBool(line, overrides);
		return this == o;
	}

	public Operator getMain() {
		return main;
	}

	/**
	 * @deprecated
	 */
	@Override
	@Deprecated(since = "1.5.13")
	public int hashCode() {
		return getHash(null);
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

		return this.isEqual((Structure) o, null, null);
	}
}