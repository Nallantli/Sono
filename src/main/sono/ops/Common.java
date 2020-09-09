package main.sono.ops;

import java.util.ArrayList;
import java.util.List;

import main.phl.Phone;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;

public class Common extends Unary {
	public Common(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.COMMON, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Datum datumA = a.evaluate(scope, overrides);
		final int dataSize = datumA.getVectorLength(line, overrides);
		final List<Phone> phones = new ArrayList<>();
		for (int i = 0; i < dataSize; i++)
			phones.add(datumA.indexVector(i).getPhone(line, overrides));
		return new Datum(interpreter.getManager().getCommon(phones));
	}

	@Override
	public String toString() {
		return "com " + a.toString();
	}
}