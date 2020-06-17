package src.sono;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import src.sono.err.SonoRuntimeException;

public class Scope {
	private Map<String, Datum> data;
	private Scope parent;

	private Scope(Scope parent, Map<String, Datum> data) {
		this.parent = parent;
		this.data = data;
	}

	public Scope(Scope parent) {
		this(parent, new HashMap<>());
	}

	private void setMap(Map<String, Datum> data) {
		this.data = data;
	}

	public Scope instantiate(List<String> trace) {
		Scope scope = new Scope(this.parent);
		Map<String, Datum> newMap = new HashMap<>();
		for (Map.Entry<String, Datum> e : data.entrySet())
			newMap.put(e.getKey(), new Datum(e.getValue(), scope, trace));
		scope.setMap(newMap);
		return scope;
	}

	public Datum getVariable(String key, List<String> trace) {
		Scope curr = this;
		while (curr != null) {
			if (curr.data.containsKey(key))
				return curr.data.get(key);
			curr = curr.parent;
		}

		throw new SonoRuntimeException("Variable <" + key + "> is not within scope or does not exist.", trace);
	}

	public Datum setVariable(String key, Datum value, List<String> trace) {
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

	public boolean variableExists(String key) {
		return data.containsKey(key);
	}
}