package intr;

import java.util.HashMap;
import java.util.Map;

public class Scope {
	Map<String, Datum> data;
	Scope parent;

	public Scope(Scope parent) {
		this.parent = parent;
		this.data = new HashMap<>();
	}

	public Datum getVariable(String key) {
		Scope curr = this;
		while (curr != null) {
			if (curr.data.containsKey(key))
				return curr.data.get(key);
			curr = curr.parent;
		}

		throw new SonoRuntimeException("Variable <" + key + "> is not within scope or does not exist.");
	}

	public Datum setVariable(String key, Datum value) {
		this.data.put(key, value);
		return getVariable(key);
	}
}