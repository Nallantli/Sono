package main.sono;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import main.phl.PhoneManager;
import main.sono.err.SonoRuntimeException;

public class Scope {
	private Map<Integer, Datum> data;
	private final Scope parent;

	private Scope(final Scope parent, final Map<Integer, Datum> data) {
		this.parent = parent;
		this.data = data;
	}

	public Scope(final Scope parent) {
		this(parent, new HashMap<>());
	}

	private void setMap(final Map<Integer, Datum> data) {
		this.data = data;
	}

	public Scope instantiate(final PhoneManager pm, final List<String> trace) {
		final Scope scope = new Scope(this.parent);
		final Map<Integer, Datum> newMap = new HashMap<>();
		for (final Map.Entry<Integer, Datum> e : data.entrySet())
			newMap.put(e.getKey(), new Datum(pm, e.getValue(), scope, trace));
		scope.setMap(newMap);
		return scope;
	}

	public Datum getVariable(final int key, final Interpreter interpreter, final List<String> trace) {
		Scope curr = this;
		while (curr != null) {
			if (curr.data.containsKey(key))
				return curr.data.get(key);
			curr = curr.parent;
		}

		throw new SonoRuntimeException(
				"Variable <" + interpreter.deHash(key) + "> is not within scope or does not exist.", trace);
	}

	public Datum setVariable(final Interpreter interpreter, final int key, final Datum value,
			final List<String> trace) {
		if (data.containsKey(key)) {
			if (value != null)
				this.data.get(key).set(interpreter.getManager(), value, trace);
		} else {
			if (value != null)
				this.data.put(key, value);
			else
				this.data.put(key, new Datum());
		}
		return getVariable(key, interpreter, trace);
	}

	public boolean variableExists(final int key) {
		return data.containsKey(key);
	}
}