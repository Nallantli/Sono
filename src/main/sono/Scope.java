package main.sono;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import main.sono.err.SonoRuntimeException;

public class Scope {
	private Map<String, Datum> data;
	private final Scope parent;

	private Scope(final Scope parent, final Map<String, Datum> data) {
		this.parent = parent;
		this.data = data;
	}

	public Scope(final Scope parent) {
		this(parent, new HashMap<>());
	}

	private void setMap(final Map<String, Datum> data) {
		this.data = data;
	}

	public Scope instantiate(final List<String> trace) {
		final Scope scope = new Scope(this.parent);
		final Map<String, Datum> newMap = new HashMap<>();
		for (final Map.Entry<String, Datum> e : data.entrySet())
			newMap.put(e.getKey(), new Datum(e.getValue(), scope, trace));
		scope.setMap(newMap);
		return scope;
	}

	public Datum getVariable(final String key, final List<String> trace) {
		Scope curr = this;
		while (curr != null) {
			if (curr.data.containsKey(key))
				return curr.data.get(key);
			curr = curr.parent;
		}

		throw new SonoRuntimeException("Variable <" + key + "> is not within scope or does not exist.", trace);
	}

	public Datum setVariable(final String key, final Datum value, final List<String> trace) {
		if (data.containsKey(key)) {
			if (value != null)
				this.data.get(key).set(value, trace);
		} else {
			if (value != null)
				this.data.put(key, value);
			else
				this.data.put(key, new Datum());
		}
		return getVariable(key, trace);
	}

	public boolean variableExists(final String key) {
		return data.containsKey(key);
	}
}