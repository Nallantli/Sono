package main.sono;

import java.util.HashMap;
import java.util.Map;

import main.phl.PhoneManager;
import main.sono.err.SonoRuntimeException;

public class Scope {
	private Map<Integer, Datum> data;
	private final Scope parent;
	private final Structure correspondent;

	private Scope(final Structure correspondent, final Scope parent, final Map<Integer, Datum> data) {
		this.correspondent = correspondent;
		this.parent = parent;
		this.data = data;
	}

	public Scope(final Structure correspondent, final Scope parent) {
		this(correspondent, parent, new HashMap<>());
	}

	private void setMap(final Map<Integer, Datum> data) {
		this.data = data;
	}

	public Scope instantiate(final Structure correspondent, final PhoneManager pm, final Token line) {
		final Scope scope = new Scope(correspondent, this.parent);
		final Map<Integer, Datum> newMap = new HashMap<>();
		for (final Map.Entry<Integer, Datum> e : data.entrySet())
			newMap.put(e.getKey(), new Datum(pm, e.getValue(), scope, line));
		scope.setMap(newMap);
		return scope;
	}

	public Datum getVariable(final int key, final Interpreter interpreter, final Token line) {
		if (data.containsKey(key))
			return data.get(key);
		else if (parent != null)
			return parent.getVariable(key, interpreter, line);

		throw new SonoRuntimeException(
				"Variable <" + interpreter.deHash(key) + "> is not within scope or does not exist.", line);
	}

	public Datum setVariable(final Interpreter interpreter, final int key, final Datum value, final Token line) {
		if (data.containsKey(key)) {
			if (value != null)
				this.data.get(key).set(interpreter.getManager(), value, line);
			return data.get(key);
		}

		if (value != null) {
			this.data.put(key, value);
			return value;
		} else {
			final Datum ret = new Datum();
			this.data.put(key, ret);
			return ret;
		}
	}

	public boolean variableExists(final int key) {
		return data.containsKey(key);
	}

	public Structure getStructure() {
		return correspondent;
	}
}