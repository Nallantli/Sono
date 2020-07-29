package main.sono.ops;

import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class Allocate extends Unary {
	public Allocate(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.ALLOC, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope) {
		final Datum datumA = a.evaluate(scope);
		if (datumA.getType() == Datum.Type.NUMBER) {
			final int dn = (int) datumA.getNumber(line);
			if (dn < 0)
				throw new SonoRuntimeException("Cannot allocate Vector with size <" + dn + ">", line);
			final Datum[] data = new Datum[dn];
			for (int i = 0; i < dn; i++)
				data[i] = new Datum();
			return new Datum(data);
		} else if (datumA.getType() == Datum.Type.VECTOR) {
			final Datum[] dv = datumA.getVector(line);
			if (dv[0].getNumber(line) < 0)
				throw new SonoRuntimeException("Cannot allocate Vector with size <" + dv[0].getNumber(line) + ">",
						line);
			final Datum[] data = new Datum[(int) dv[0].getNumber(line)];
			for (int i = 0; i < data.length; i++)
				data[i] = new Datum();
			Datum[] curr = data;
			for (int j = 1; j < dv.length; j++) {
				final int size = (int) dv[j].getNumber(line);
				if (size < 0)
					throw new SonoRuntimeException("Cannot allocate Vector with size <" + size + ">", line);
				final Datum[] next = new Datum[curr.length * size];
				int k = 0;
				for (int i = 0; i < curr.length; i++) {
					final Datum[] temp = new Datum[size];
					for (int x = 0; x < temp.length; x++) {
						temp[x] = new Datum();
						next[k++] = temp[x];
					}
					curr[i].setVector(temp);
				}
				curr = next;
			}
			return new Datum(data);
		} else {
			throw new SonoRuntimeException(
					"Value <" + datumA.getDebugString(line) + "> cannot be used in Vector allocation.", line);
		}
	}

	@Override
	public String toString() {
		return "alloc " + a.toString();
	}
}