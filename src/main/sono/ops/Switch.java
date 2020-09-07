package main.sono.ops;

import java.util.HashMap;
import java.util.Map;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Switch extends Unary {
	private final Map<Datum, Operator> map;
	private Operator c;

	public Switch(final Interpreter interpreter, final Token line, final Operator a, final Map<Datum, Operator> map) {
		super(interpreter, Type.SWITCH, line, a);
		this.map = map;
		this.c = null;
	}

	public void setElse(final Operator c) {
		this.c = c;
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		final Datum key = a.evaluate(scope, overrides);
		if (map.containsKey(key)) {
			return map.get(key).evaluate(new Scope(scope.getStructure(), scope, false), overrides);
		} else if (c != null) {
			return c.evaluate(new Scope(scope.getStructure(), scope, false), overrides);
		}
		return new Datum();
	}

	@Override
	public String toString() {
		return a.toString() + " switch " + map + (c != null ? " else " + c.toString() : "");
	}

	@Override
	public Operator[] getChildren() {
		final Operator[] mapValues = map.values().toArray(new Operator[0]);
		final Operator[] ops = new Operator[mapValues.length + 1];
		ops[0] = a;
		for (int i = 0; i < mapValues.length; i++) {
			ops[i + 1] = mapValues[i];
		}
		return ops;
	}

	@Override
	public void condense() {
		super.condense();
		final Map<Datum, Operator> newMap = new HashMap<>();
		for (final Map.Entry<Datum, Operator> entry : map.entrySet()) {
			final Operator e = entry.getValue();
			e.condense();
			if (e.getType() == Type.SOFT_LIST && e.getChildren().length == 1)
				newMap.put(entry.getKey(), e.getChildren()[0]);
			else
				newMap.put(entry.getKey(), e);
		}
		this.map.clear();
		this.map.putAll(newMap);
		if (c != null) {
			c.condense();
			if (c.getType() == Type.SOFT_LIST && c.getChildren().length == 1)
				c = c.getChildren()[0];
		}
	}
}