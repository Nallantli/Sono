package main.sono.ops;

import java.util.ArrayList;
import java.util.List;

import main.phl.Phone;
import main.phl.Word;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class ToWord extends Unary {
	public ToWord(final Interpreter interpreter, final Token line, final Operator a) {
		super(interpreter, Type.TO_WORD, line, a);
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) throws InterruptedException {
		checkInterrupted();
		final Datum datumA = a.evaluate(scope, overrides);
		if (datumA.getType() == Datum.Type.VECTOR) {
			final List<Phone> phones = new ArrayList<>();
			final List<Word.SyllableDelim> delimits = new ArrayList<>();
			boolean flag = true;
			final int dataSize = datumA.getVectorLength(line, overrides);
			for (int i = 0; i < dataSize; i++) {
				final Datum d = datumA.indexVector(i);
				if (d.getType() == Datum.Type.PHONE) {
					phones.add(d.getPhone(line, overrides));
					if (flag)
						delimits.add(Word.SyllableDelim.NULL);
					else
						flag = true;
				} else if (d.getType() == Datum.Type.STRING) {
					flag = false;
					switch (d.getString(line, overrides)) {
						case ".":
							delimits.add(Word.SyllableDelim.DELIM);
							break;
						case "+":
							delimits.add(Word.SyllableDelim.MORPHEME);
							break;
						default:
							throw new SonoRuntimeException("Value <" + d.getDebugString(line, overrides)
									+ "> is not applicable as a word delimiter", line);
					}
				}
			}
			return new Datum(new Word(phones, delimits));
		} else if (datumA.getType() == Datum.Type.STRING) {
			try {
				return new Datum(interpreter.getManager().interpretSequence(datumA.getString(line, overrides)));
			} catch (final Exception e) {
				throw new SonoRuntimeException(
						"Value <" + datumA.getDebugString(line, overrides) + "> cannot be converted to a Word.", line);
			}
		}
		throw new SonoRuntimeException(
				"Value <" + datumA.getDebugString(line, overrides) + "> cannot be converted to a Word.", line);
	}

	@Override
	public String toString() {
		return "word " + a.toString();
	}
}