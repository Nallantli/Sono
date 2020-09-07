package main.sono.ops;

import java.util.ArrayList;
import java.util.List;

import main.phl.Rule;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Operator;
import main.sono.Scope;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class DecRule extends Unary {
	private final Rule.Type ruleType;

	public DecRule(final Interpreter interpreter, final Token line, final Rule.Type ruleType, final Operator a) {
		super(interpreter, Type.DEC_RULE, line, a);
		this.ruleType = ruleType;
	}

	@Override
	public Datum evaluate(final Scope scope, final Object[] overrides) {
		while (a.getType() != Type.SLASH)
			a = ((Sequence) a).operators[0];
		final Datum dSearch = ((Binary) ((Binary) a).getA()).getA().evaluate(scope, overrides);
		final Datum rawTrans = ((Binary) ((Binary) a).getA()).getB().evaluate(scope, overrides);
		final Datum rawInit = ((Binary) ((Binary) a).getB()).getA().evaluate(scope, overrides);
		final Datum rawFin = ((Binary) ((Binary) a).getB()).getB().evaluate(scope, overrides);

		Object search = null;
		Datum[] dTrans;
		Datum[] dInit;
		Datum[] dFin;
		final List<Object> trans = new ArrayList<>();
		final List<Object> init = new ArrayList<>();
		final List<Object> fin = new ArrayList<>();

		if (dSearch.getType() == Datum.Type.MATRIX)
			search = dSearch.getMatrix(line, overrides);
		else if (dSearch.getType() == Datum.Type.PHONE)
			search = dSearch.getPhone(line, overrides);

		if (rawTrans.getType() == Datum.Type.VECTOR)
			dTrans = rawTrans.getVector(line, overrides);
		else {
			dTrans = new Datum[1];
			dTrans[0] = rawTrans;
		}

		for (final Datum d : dTrans) {
			switch (d.getType()) {
				case PHONE:
					trans.add(d.getPhone(line, overrides));
					break;
				case MATRIX:
					trans.add(d.getMatrix(line, overrides));
					break;
				case NULL:
					break;
				default:
					throw new SonoRuntimeException(
							"Value <" + d.getDebugString(line, overrides) + "> cannot be used in a Rule declaration.", line);
			}
		}

		if (rawInit.getType() == Datum.Type.VECTOR)
			dInit = rawInit.getVector(line, overrides);
		else {
			dInit = new Datum[1];
			dInit[0] = rawInit;
		}

		for (final Datum d : dInit) {
			switch (d.getType()) {
				case PHONE:
					init.add(d.getPhone(line, overrides));
					break;
				case MATRIX:
					init.add(d.getMatrix(line, overrides));
					break;
				case STRING:
					switch (d.getString(line, overrides)) {
						case "#":
							init.add(Rule.Variants.WORD_INITIAL);
							break;
						case "$":
							init.add(Rule.Variants.SYLLABLE_INIT);
							break;
						case "+":
							init.add(Rule.Variants.MORPHEME);
							break;
						default:
							break;
					}
					break;
				case NULL:
					break;
				default:
					throw new SonoRuntimeException(
							"Value <" + d.getDebugString(line, overrides) + "> cannot be used in a Rule declaration.", line);
			}
		}

		if (rawFin.getType() == Datum.Type.VECTOR)
			dFin = rawFin.getVector(line, overrides);
		else {
			dFin = new Datum[1];
			dFin[0] = rawFin;
		}

		for (final Datum d : dFin) {
			switch (d.getType()) {
				case PHONE:
					fin.add(d.getPhone(line, overrides));
					break;
				case MATRIX:
					fin.add(d.getMatrix(line, overrides));
					break;
				case STRING:
					switch (d.getString(line, overrides)) {
						case "#":
							fin.add(Rule.Variants.WORD_FINAL);
							break;
						case "$":
							fin.add(Rule.Variants.SYLLABLE_END);
							break;
						case "+":
							fin.add(Rule.Variants.MORPHEME);
							break;
						default:
							break;
					}
					break;
				case NULL:
					break;
				default:
					throw new SonoRuntimeException(
							"Value <" + d.getDebugString(line, overrides) + "> cannot be used in a Rule declaration.", line);
			}
		}

		switch (ruleType) {
			case A_BACKWARD:
				return new Datum(new Rule.DeleteBackward(search, trans, init, fin));
			case A_FORWARD:
				return new Datum(new Rule.DeleteForward(search, trans, init, fin));
			case SIMPLE:
				return new Datum(new Rule.Simple(search, trans, init, fin));
			default:
				return null;
		}
	}

	@Override
	public String toString() {
		return ruleType + " |> " + a.toString();
	}
}