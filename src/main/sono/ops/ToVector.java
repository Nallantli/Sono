package main.sono.ops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.phl.Feature;
import main.phl.Word;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class ToVector extends Unary {
	public ToVector(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.TO_VECTOR, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		Datum[] list = null;
		final Datum datumA = a.evaluate(scope, overrides);
		int i;
		switch (datumA.getType()) {
			case MATRIX:
				list = new Datum[datumA.getMatrix(line, overrides).size()];
				i = 0;
				for (final Feature p : datumA.getMatrix(line, overrides))
					list[i++] = new Datum(p);
				break;
			case STRING:
				final String s = datumA.getString(line, overrides);
				list = new Datum[s.length()];
				for (i = 0; i < s.length(); i++)
					list[i] = new Datum(String.valueOf(s.charAt(i)));
				break;
			case WORD:
				final List<Datum> tempList = new ArrayList<>();
				for (i = 0; i < datumA.getWord(line, overrides).size(); i++) {
					if (datumA.getWord(line, overrides).getDelim(i) != Word.SyllableDelim.NULL)
						tempList.add(new Datum(datumA.getWord(line, overrides).getDelim(i).toString()));
					tempList.add(new Datum(datumA.getWord(line, overrides).get(i)));
				}
				list = tempList.toArray(new Datum[0]);
				break;
			case VECTOR:
				return datumA;
			case DICTIONARY:
				Map<String, Datum> dict = datumA.getMap(line, overrides);
				list = new Datum[dict.size()];
				i = 0;
				for (Map.Entry<String, Datum> e : dict.entrySet())
					list[i++] = new Datum(Map.of("key", new Datum(e.getKey()), "value", e.getValue()));
				break;
			case STRUCTURE:
				return datumA.getStructure(line, overrides).getScope().getVariable(interpreter.GET_LIST, interpreter, line, overrides)
						.getFunction(Datum.Type.ANY, line, overrides).execute(null, line, overrides);
			default:
				throw new SonoRuntimeException("Cannot convert value <" + datumA.getDebugString(line, overrides) + "> into a List",
						line);
		}
		return new Datum(list);
	}

	@Override
	public String toString() {
		return "vec " + a.toString();
	}
}