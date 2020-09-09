package main.sono.ops;

import java.util.ArrayList;
import java.util.List;

import main.phl.Matrix;
import main.phl.Phone;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Find extends Binary {
	public Find(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
		super(interpreter, Type.FIND, line, a, b);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Matrix matrix = a.evaluate(scope, overrides).getMatrix(line, overrides);
		final Datum datumB = b.evaluate(scope, overrides);
		final int dataSize = datumB.getVectorLength(line, overrides);
		final List<Phone> phones = new ArrayList<>();
		for (int i = 0; i < dataSize; i++)
			phones.add(datumB.indexVector(i).getPhone(line, overrides));
		final List<Phone> list = interpreter.getManager().getPhones(phones, matrix);
		final Datum[] newData = new Datum[list.size()];
		for (int i = 0; i < list.size(); i++)
			newData[i] = new Datum(list.get(i));
		return new Datum(newData);
	}

	@Override
	public String toString() {
		return "find " + a.toString();
	}
}