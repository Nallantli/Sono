package main.sono;

import java.util.ArrayList;
import java.util.List;

import java.util.Arrays;

import main.SonoWrapper;
import main.phl.*;
import main.sono.err.SonoRuntimeException;

public abstract class Operator {

	public enum Type {
		VARIABLE, DATUM, SET, TRANSFORM, SOFT_LIST, HARD_LIST, RULE_DEC, ARROW, SLASH, UNDERSCORE, MATRIX_DEC, SEQ_DEC,
		COMMON, ADD, SUB, MUL, DIV, MOD, INDEX, EQUAL, NEQUAL, LESS, MORE, ELESS, EMORE, MATRIX_CONV, NUMBER_CONV,
		CONTRAST, VAR_DEC, LIST_DEC, ITERATOR, LOOP, RANGE_UNTIL, BREAK, IF_ELSE, LAMBDA, RETURN, JOIN_DEC, STR_DEC,
		FIND_DEC, AND, OR, LEN, INNER, REF_DEC, TYPE_CONV, TYPE_DEC, STRUCT_DEC, STATIC_DEC, CLASS_DEC, NEW_DEC, POW,
		FEAT_DEC, THROW, TRY_CATCH, CHAR, ALLOC, FINAL, REGISTER, CODE, REFER,

		// INTERPRETER USE
		UNARY, BINARY, SEQUENCE, EXECUTE, OUTER_CALL
	}

	protected Type type;
	protected Interpreter interpreter;

	private abstract static class Unary extends Operator {
		protected Operator a;

		public Unary(final Interpreter i, final Type type, final Operator a) {
			super(i, type);
			this.a = a;
		}

		public Operator getA() {
			return a;
		}

		@Override
		public List<Operator> getChildren() {
			return Arrays.asList(a);
		}
	}

	private abstract static class Binary extends Unary {
		protected Operator b;

		public Binary(final Interpreter i, final Type type, final Operator a, final Operator b) {
			super(i, type, a);
			this.b = b;
		}

		public Operator getB() {
			return b;
		}

		@Override
		public List<Operator> getChildren() {
			return Arrays.asList(a, b);
		}
	}

	public abstract static class Sequence extends Operator {
		protected List<Operator> operators;

		public Sequence(final Interpreter i, final Type type, final List<Operator> operators) {
			super(i, type);
			this.operators = operators;
		}

		public List<Operator> getVector() {
			return operators;
		}

		@Override
		public List<Operator> getChildren() {
			return getVector();
		}
	}

	public abstract static class Casting extends Operator {
		protected int varName;

		public Casting(final Interpreter i, final Type type, final int varName) {
			super(i, type);
			this.varName = varName;
		}

		public int getKey() {
			return varName;
		}

		@Override
		public List<Operator> getChildren() {
			return new ArrayList<>();
		}
	}

	public static class Variable extends Casting {
		public Variable(final Interpreter interpreter, final int varName) {
			super(interpreter, Type.VARIABLE, varName);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return scope.getVariable(varName, interpreter, trace);
		}

		@Override
		public String toString() {
			return interpreter.deHash(getKey());
		}
	}

	public static class Container extends Operator {
		private final Datum datum;

		public Container(final Interpreter interpreter, final Datum datum) {
			super(interpreter, Type.DATUM);
			this.datum = datum;
		}

		public Datum getDatum() {
			return datum;
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return datum;
		}

		@Override
		public String toString() {
			return datum.toString();
		}

		@Override
		public List<Operator> getChildren() {
			return new ArrayList<>();
		}
	}

	public static class Set extends Binary {
		public Set(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.SET, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			datumA.set(interpreter.getManager(), datumB, trace);
			return datumA;
		}

		@Override
		public String toString() {
			return a.toString() + " = " + b.toString();
		}
	}

	public static class Transform extends Binary {
		public Transform(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.TRANSFORM, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			if (datumB.getType() == Datum.Type.MATRIX) {
				final Phone ret = datumA.getPhone(trace).transform(datumB.getMatrix(trace), true);
				if (ret == null)
					return new Datum();
				return new Datum(ret);
			} else if (datumB.getType() == Datum.Type.RULE) {
				final Word result = datumB.getRule(trace).transform(interpreter.getManager(), datumA.getWord(trace));
				return new Datum(result);
			} else {
				throw new SonoRuntimeException("Cannot transform value <" + datumA.toStringTrace(trace)
						+ "> with value <" + datumB.toStringTrace(trace) + ">", trace);
			}
		}

		@Override
		public String toString() {
			return a.toString() + " >> " + b.toString();
		}
	}

	public static class SoftList extends Sequence {
		public SoftList(final Interpreter interpreter, final List<Operator> operators) {
			super(interpreter, Type.SOFT_LIST, operators);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			Datum[] data = null;
			if (Interpreter.containsInstance(operators, RangeUntil.class)) {
				final List<Datum> list = new ArrayList<>();
				for (final Operator o : operators) {
					if (o.type == Type.RANGE_UNTIL)
						list.addAll(((RangeUntil) o).getRange(scope, trace));
					else {
						final Datum d = o.evaluate(scope, trace);
						if (d.getType() == Datum.Type.I_BREAK)
							return d;
						if (d.getRet() || d.getRefer())
							return d;
						list.add(d);
					}
				}
				data = list.toArray(new Datum[0]);
			} else {
				data = new Datum[operators.size()];
				int i = 0;
				for (final Operator o : operators) {
					final Datum d = o.evaluate(scope, trace);
					if (d.getType() == Datum.Type.I_BREAK)
						return d;
					if (d.getRet() || d.getRefer())
						return d;
					data[i++] = d;
				}
			}
			if (data.length == 1)
				return data[0];
			return new Datum(data);
		}

		@Override
		public String toString() {
			return Interpreter.stringFromList(operators.toArray(), "(", ")");
		}
	}

	public static class HardList extends Sequence {
		public HardList(final Interpreter interpreter, final List<Operator> operators) {
			super(interpreter, Type.HARD_LIST, operators);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			Datum[] data = null;
			if (Interpreter.containsInstance(operators, RangeUntil.class)) {
				final List<Datum> list = new ArrayList<>();
				for (final Operator o : operators) {
					if (o.type == Type.RANGE_UNTIL)
						list.addAll(((RangeUntil) o).getRange(scope, trace));
					else {
						final Datum d = o.evaluate(scope, trace);
						if (d.getType() == Datum.Type.I_BREAK)
							return d;
						if (d.getRet() || d.getRefer())
							return d;
						list.add(d);
					}
				}
				data = list.toArray(new Datum[0]);
			} else {
				data = new Datum[operators.size()];
				int i = 0;
				for (final Operator o : operators) {
					final Datum d = o.evaluate(scope, trace);
					if (d.getType() == Datum.Type.I_BREAK)
						return d;
					if (d.getRet() || d.getRefer())
						return d;
					data[i++] = d;
				}
			}
			return new Datum(data);
		}

		@Override
		public String toString() {
			return Interpreter.stringFromList(operators.toArray(), "{", "}");
		}
	}

	public static class MatrixDec extends Sequence {
		public MatrixDec(final Interpreter interpreter, final List<Operator> operators) {
			super(interpreter, Type.MATRIX_DEC, operators);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Matrix matrix = new Matrix();
			for (final Operator o : operators)
				matrix.put(interpreter.getManager(), o.evaluate(scope, trace).getPair(trace));
			return new Datum(matrix);
		}

		@Override
		public String toString() {
			return Interpreter.stringFromList(operators.toArray(), "[", "]");
		}
	}

	public static class RangeUntil extends Binary {
		public RangeUntil(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.RANGE_UNTIL, a, b);
		}

		public List<Datum> getRange(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final int datumA = (int) a.evaluate(scope, trace).getNumber(trace);
			final int datumB = (int) b.evaluate(scope, trace).getNumber(trace);
			final List<Datum> data = new ArrayList<>();
			for (int i = datumA; i < datumB; i++)
				data.add(new Datum(i));
			return data;
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " until " + b.toString();
		}
	}

	public static class Arrow extends Binary {
		public Arrow(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.ARROW, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " -> " + b.toString();
		}
	}

	public static class Ref extends Casting {
		public Ref(final Interpreter interpreter, final int varName) {
			super(interpreter, Type.REF_DEC, varName);
			this.varName = varName;
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return null;
		}

		@Override
		public String toString() {
			return "ref " + interpreter.deHash(varName);
		}
	}

	public static class Final extends Casting {
		public Final(final Interpreter interpreter, final int varName) {
			super(interpreter, Type.FINAL, varName);
			this.varName = varName;
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return null;
		}

		@Override
		public String toString() {
			return "final " + interpreter.deHash(varName);
		}
	}

	public static class FeatDec extends Unary {
		public FeatDec(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.FEAT_DEC, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Datum datumA = a.evaluate(scope, trace);
			return new Datum(interpreter.getManager().interpretFeature(datumA.getString(trace)));
		}

		@Override
		public String toString() {
			return "feat " + a.toString();
		}
	}

	public static class Alloc extends Unary {
		public Alloc(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.ALLOC, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final double datumA = a.evaluate(scope, trace).getNumber(trace);
			final Datum[] data = new Datum[(int) datumA];
			for (int i = 0; i < datumA; i++) {
				data[i] = new Datum();
			}
			return new Datum(data);
		}

		@Override
		public String toString() {
			return "alloc " + a.toString();
		}
	}

	public static class Throw extends Unary {
		public Throw(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.THROW, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			throw new SonoRuntimeException(datumA.getString(trace), trace);
		}

		@Override
		public String toString() {
			return "throw " + a.toString();
		}
	}

	public static class TryCatch extends Unary {
		private Operator b = null;

		public TryCatch(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.TRY_CATCH, a);
		}

		public void setCatch(final Operator b) {
			this.b = b;
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			try {
				return a.evaluate(scope, trace);
			} catch (final SonoRuntimeException e) {
				if (b != null) {
					final Scope catchScope = new Scope(scope);
					catchScope.setVariable(interpreter, interpreter.ERROR, new Datum(e.getMessage()), trace);
					final Datum[] list = new Datum[trace.size()];
					for (int i = 0; i < trace.size(); i++)
						list[i] = new Datum(trace.get(trace.size() - i - 1));
					catchScope.setVariable(interpreter, interpreter.TRACE, new Datum(list), trace);
					return b.evaluate(catchScope, trace);
				} else {
					return new Datum();
				}
			}
		}

		@Override
		public String toString() {
			return "try " + a.toString() + (b != null ? " catch " + b.toString() : "");
		}

		@Override
		public List<Operator> getChildren() {
			if (b == null) {
				return Arrays.asList(a);
			} else {
				return Arrays.asList(a, b);
			}
		}
	}

	public static class Slash extends Binary {

		public Slash(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.SLASH, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " // " + b.toString();
		}
	}

	public static class Underscore extends Binary {
		public Underscore(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.UNDERSCORE, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " _ " + b.toString();
		}
	}

	public static class Iterator extends Binary {
		public Iterator(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.ITERATOR, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " in " + b.toString();
		}
	}

	public static class StructDec extends Casting {
		public StructDec(final Interpreter interpreter, final int varName) {
			super(interpreter, Type.STRUCT_DEC, varName);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return null;
		}

		@Override
		public String toString() {
			return "struct " + interpreter.deHash(varName);
		}
	}

	public static class StaticDec extends Casting {
		public StaticDec(final Interpreter interpreter, final int varName) {
			super(interpreter, Type.STATIC_DEC, varName);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return null;
		}

		@Override
		public String toString() {
			return "static " + interpreter.deHash(varName);
		}
	}

	public static class TypeDec extends Binary {
		public TypeDec(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.TYPE_DEC, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " :: " + b.toString();
		}
	}

	public static class Loop extends Binary {
		public Loop(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.LOOP, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			if (a.type == Type.ITERATOR) {
				final Datum[] values = ((Iterator) a).getB().evaluate(scope, trace).getVector(trace);
				final int variable = ((Variable) ((Iterator) a).getA()).getKey();
				for (final Datum d : values) {
					final Scope loopScope = new Scope(scope);
					loopScope.setVariable(interpreter, variable, d, trace);
					final Datum result = b.evaluate(loopScope, trace);
					if (result.getType() == Datum.Type.I_BREAK)
						break;
					if (result.getRet() || result.getRefer())
						return result;
				}
			} else {
				while (a.evaluate(scope, trace).getNumber(trace) != 0) {
					final Scope loopScope = new Scope(scope);
					final Datum result = b.evaluate(loopScope, trace);
					if (result.getType() == Datum.Type.I_BREAK)
						break;
					if (result.getRet() || result.getRefer())
						return result;
				}
			}
			return new Datum();
		}

		@Override
		public String toString() {
			return a.toString() + " do " + b.toString();
		}
	}

	public static class RuleDec extends Unary {
		private final Rule.Type ruleType;

		public RuleDec(final Interpreter interpreter, final Rule.Type ruleType, final Operator a) {
			super(interpreter, Type.RULE_DEC, a);
			this.ruleType = ruleType;
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			while (a.type != Type.SLASH)
				a = ((Sequence) a).operators.get(0);
			final Datum dsearch = ((Binary) ((Binary) a).getA()).getA().evaluate(scope, trace);
			final Datum rawTrans = ((Binary) ((Binary) a).getA()).getB().evaluate(scope, trace);
			final Datum rawInit = ((Binary) ((Binary) a).getB()).getA().evaluate(scope, trace);
			final Datum rawFin = ((Binary) ((Binary) a).getB()).getB().evaluate(scope, trace);

			Object search = null;
			Datum[] dtrans;
			Datum[] dinit;
			Datum[] dfin;
			final List<Object> trans = new ArrayList<>();
			final List<Object> init = new ArrayList<>();
			final List<Object> fin = new ArrayList<>();

			if (dsearch.getType() == Datum.Type.MATRIX)
				search = dsearch.getMatrix(trace);
			else if (dsearch.getType() == Datum.Type.PHONE)
				search = dsearch.getPhone(trace);

			if (rawTrans.getType() == Datum.Type.VECTOR)
				dtrans = rawTrans.getVector(trace);
			else {
				dtrans = new Datum[1];
				dtrans[0] = rawTrans;
			}

			for (final Datum d : dtrans) {
				if (d.type == Datum.Type.PHONE)
					trans.add(d.getPhone(trace));
				else if (d.type == Datum.Type.MATRIX)
					trans.add(d.getMatrix(trace));
				else if (d.getType() != Datum.Type.NULL)
					throw new SonoRuntimeException(
							"Value <" + d.toStringTrace(trace) + "> cannot be used in a Rule declaration.", trace);
			}

			if (rawInit.getType() == Datum.Type.VECTOR)
				dinit = rawInit.getVector(trace);
			else {
				dinit = new Datum[1];
				dinit[0] = rawInit;
			}

			for (final Datum d : dinit) {
				if (d.type == Datum.Type.PHONE)
					init.add(d.getPhone(trace));
				else if (d.type == Datum.Type.MATRIX)
					init.add(d.getMatrix(trace));
				else if (d.type == Datum.Type.STRING) {
					switch (d.getString(trace)) {
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
				} else if (d.getType() != Datum.Type.NULL) {
					throw new SonoRuntimeException(
							"Value <" + d.toStringTrace(trace) + "> cannot be used in a Rule declaration.", trace);
				}
			}

			if (rawFin.getType() == Datum.Type.VECTOR)
				dfin = rawFin.getVector(trace);
			else {
				dfin = new Datum[1];
				dfin[0] = rawFin;
			}

			for (final Datum d : dfin) {
				if (d.type == Datum.Type.PHONE)
					fin.add(d.getPhone(trace));
				else if (d.type == Datum.Type.MATRIX)
					fin.add(d.getMatrix(trace));
				else if (d.type == Datum.Type.STRING) {
					switch (d.getString(trace)) {
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
				} else if (d.getType() != Datum.Type.NULL) {
					throw new SonoRuntimeException(
							"Value <" + d.toStringTrace(trace) + "> cannot be used in a Rule declaration.", trace);
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

	public static class Register extends Binary {
		public Register(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.REGISTER, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final String segment = a.evaluate(scope, trace).getString(trace);
			final Matrix features = b.evaluate(scope, trace).getMatrix(trace);
			final Phone ret = interpreter.getManager().registerNewPhone(segment, features);
			if (ret == null)
				throw new SonoRuntimeException("Cannot register Phone with features <" + features + ">", trace);
			final Datum[] oldV = interpreter.getScope().getVariable(interpreter.ALL, interpreter, trace)
					.getVector(trace);
			final Datum[] newV = new Datum[oldV.length + 1];
			for (int i = 0; i < oldV.length; i++)
				newV[i] = oldV[i];
			newV[oldV.length] = new Datum(ret);
			interpreter.getScope().setVariable(interpreter, interpreter.ALL, new Datum(newV), trace);
			return new Datum(ret);
		}

		@Override
		public String toString() {
			return a.toString() + " register " + b.toString();
		}
	}

	public static class SeqDec extends Unary {
		public SeqDec(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.SEQ_DEC, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Datum datumA = a.evaluate(scope, trace);
			if (datumA.getType() == Datum.Type.VECTOR) {
				final List<Phone> phones = new ArrayList<>();
				final List<Word.SyllableDelim> delims = new ArrayList<>();
				boolean flag = true;
				for (final Datum d : datumA.getVector(trace)) {
					if (d.getType() == Datum.Type.PHONE) {
						phones.add(d.getPhone(trace));
						if (flag)
							delims.add(Word.SyllableDelim.NULL);
						else
							flag = true;
					} else if (d.getType() == Datum.Type.STRING) {
						flag = false;
						switch (d.getString(trace)) {
							case ".":
								delims.add(Word.SyllableDelim.DELIM);
								break;
							case "+":
								delims.add(Word.SyllableDelim.MORPHEME);
								break;
							default:
								throw new SonoRuntimeException(
										"Value <" + d.getString(trace) + "> is not applicable as a word delimeter",
										trace);
						}
					}
				}
				return new Datum(new Word(phones, delims));
			} else if (datumA.getType() == Datum.Type.STRING) {
				return new Datum(interpreter.getManager().interpretSequence(datumA.getString(trace)));
			}
			throw new SonoRuntimeException("Value <" + datumA.toStringTrace(trace) + "> cannot be converted to a Word.",
					trace);
		}

		@Override
		public String toString() {
			return "word " + a.toString();
		}
	}

	public static class ListDec extends Unary {
		public ListDec(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.LIST_DEC, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			Datum[] list = null;
			final Datum datumA = a.evaluate(scope, trace);
			int i;
			switch (datumA.getType()) {
				case MATRIX:
					list = new Datum[datumA.getMatrix(trace).size()];
					if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					i = 0;
					for (final Pair p : datumA.getMatrix(trace))
						list[i++] = new Datum(p);
					break;
				case STRING:
					final String s = datumA.getString(trace);
					list = new Datum[s.length()];
					for (i = 0; i < s.length(); i++)
						list[i] = new Datum(String.valueOf(s.charAt(i)));
					break;
				case WORD:
					final List<Datum> tempList = new ArrayList<>();
					if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					for (i = 0; i < datumA.getWord(trace).size(); i++) {
						if (datumA.getWord(trace).getDelim(i) != Word.SyllableDelim.NULL)
							tempList.add(new Datum(datumA.getWord(trace).getDelim(i).toString()));
						tempList.add(new Datum(datumA.getWord(trace).get(i)));
					}
					list = tempList.toArray(new Datum[0]);
					break;
				case VECTOR:
					return datumA;
				case STRUCTURE:
					return datumA.getStructure(trace).getScope().getVariable(interpreter.GETLIST, interpreter, trace)
							.getFunction(Datum.Type.ANY, trace).execute(null, trace);
				default:
					throw new SonoRuntimeException(
							"Cannot convert value <" + datumA.toStringTrace(trace) + "> into a List", trace);
			}
			return new Datum(list);
		}

		@Override
		public String toString() {
			return "vec " + a.toString();
		}
	}

	public static class StringDec extends Unary {
		public StringDec(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.STR_DEC, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final String s = a.evaluate(scope, trace).toRawStringTrace(trace);
			return new Datum(s);
		}

		@Override
		public String toString() {
			return "str " + a.toString();
		}
	}

	public static class TypeConv extends Unary {
		public TypeConv(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.TYPE_CONV, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			return new Datum(datumA.getTypeString(interpreter));
		}

		@Override
		public String toString() {
			return "type " + a.toString();
		}
	}

	public static class MatConv extends Unary {
		public MatConv(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.MATRIX_CONV, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Datum datumA = a.evaluate(scope, trace);
			if (datumA.getType() == Datum.Type.VECTOR) {
				final Datum[] list = datumA.getVector(trace);
				final Matrix m = new Matrix();
				for (final Datum d : list)
					m.put(interpreter.getManager(), d.getPair(trace));
				return new Datum(m);
			} else if (datumA.getType() == Datum.Type.PHONE) {
				return new Datum(datumA.getPhone(trace).getMatrix());
			}
			throw new SonoRuntimeException("Cannot convert value <" + datumA.toStringTrace(trace) + "> to a Matrix.",
					trace);
		}

		@Override
		public String toString() {
			return "mat " + a.toString();
		}
	}

	public static class Find extends Binary {
		public Find(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.FIND_DEC, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Matrix matrix = a.evaluate(scope, trace).getMatrix(trace);
			final Datum[] data = b.evaluate(scope, trace).getVector(trace);
			final List<Phone> phones = new ArrayList<>();
			for (final Datum d : data)
				phones.add(d.getPhone(trace));
			final List<Phone> list = interpreter.getManager().getPhones(phones, matrix);
			final Datum[] newData = new Datum[list.size()];
			for (int i = 0; i < list.size(); i++) {
				newData[i] = new Datum(list.get(i));
			}
			return new Datum(newData);
		}

		@Override
		public String toString() {
			return "find " + a.toString();
		}
	}

	public static class NumConv extends Unary {
		public NumConv(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.NUMBER_CONV, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			if (datumA.getType() == Datum.Type.NUMBER) {
				return datumA;
			} else if (datumA.getType() == Datum.Type.STRING) {
				try {
					return new Datum((double) Double.valueOf(datumA.getString(trace)));
				} catch (final Exception e) {
					return new Datum();
				}
			} else {
				throw new SonoRuntimeException(
						"Cannot convert value <" + datumA.toStringTrace(trace) + "> to a Number.", trace);
			}
		}

		@Override
		public String toString() {
			return "num " + a.toString();
		}
	}

	public static class Code extends Unary {
		public Code(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.CODE, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final String s = a.evaluate(scope, trace).getString(trace);
			if (s.length() != 1)
				throw new SonoRuntimeException("Value <" + s + "> is not a single char.", trace);
			return new Datum((int) s.charAt(0));
		}

		@Override
		public String toString() {
			return "code " + a.toString();
		}
	}

	public static class Char extends Unary {
		public Char(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.CHAR, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			try {
				final int i = (int) datumA.getNumber(trace);
				return new Datum(String.valueOf((char) i));
			} catch (final Exception e) {
				throw new SonoRuntimeException("Value <" + datumA.toStringTrace(trace) + "> is not of type `Number`",
						trace);
			}
		}

		@Override
		public String toString() {
			return "code " + a.toString();
		}
	}

	public static class Length extends Unary {
		public Length(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.LEN, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			switch (datumA.getType()) {
				case STRING:
					return new Datum(datumA.getString(trace).length());
				case WORD:
					if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					return new Datum(datumA.getWord(trace).size());
				case VECTOR:
					return new Datum(datumA.getVector(trace).length);
				case MATRIX:
					if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					return new Datum(datumA.getMatrix(trace).size());
				case STRUCTURE:
					return datumA.getStructure(trace).getScope().getVariable(interpreter.GETLEN, interpreter, trace)
							.getFunction(Datum.Type.ANY, trace).execute(null, trace);
				default:
					throw new SonoRuntimeException("Cannot get length of value <" + datumA.toStringTrace(trace) + ">",
							trace);
			}
		}

		@Override
		public String toString() {
			return "len " + a.toString();
		}
	}

	public static class Return extends Unary {
		public Return(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.RETURN, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			datumA.setRet(true);
			return datumA;
		}

		@Override
		public String toString() {
			return "return " + a.toString();
		}
	}

	public static class Refer extends Unary {
		public Refer(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.REFER, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			datumA.setRefer(true);
			return datumA;
		}

		@Override
		public String toString() {
			return "refer " + a.toString();
		}
	}

	public static class Add extends Binary {
		public Add(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.ADD, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			if (datumA.getType() != datumB.getType())
				throw new SonoRuntimeException("Cannot add values <" + datumA.toStringTrace(trace) + "> and <"
						+ datumB.toStringTrace(trace) + ">, of types: " + datumA.getType() + ", " + datumB.getType(),
						trace);
			switch (datumA.getType()) {
				case NUMBER:
					return new Datum(datumA.getNumber(trace) + datumB.getNumber(trace));
				case VECTOR:
					final Datum[] vectorA = datumA.getVector(trace);
					final Datum[] vectorB = datumB.getVector(trace);
					final Datum[] newList = new Datum[vectorA.length + vectorB.length];
					for (int i = 0; i < vectorA.length; i++) {
						newList[i] = vectorA[i];
					}
					for (int i = 0; i < vectorB.length; i++) {
						newList[i + vectorA.length] = vectorB[i];
					}
					return new Datum(newList);
				case MATRIX:
					if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					final Matrix newMatrix = new Matrix();
					newMatrix.putAll(interpreter.getManager(), datumA.getMatrix(trace));
					newMatrix.putAll(interpreter.getManager(), datumB.getMatrix(trace));
					return new Datum(newMatrix);
				case WORD:
					if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					final Word newWord = new Word();
					newWord.addAll(datumA.getWord(trace));
					newWord.addAll(datumB.getWord(trace));
					return new Datum(newWord);
				case STRING:
					return new Datum(datumA.getString(trace) + datumB.getString(trace));
				default:
					throw new SonoRuntimeException("Values of type <" + datumA.getType() + "> cannot be added.", trace);
			}
		}

		@Override
		public String toString() {
			return a.toString() + " + " + b.toString();
		}
	}

	public static class Sub extends Binary {
		public Sub(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.SUB, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			return new Datum(datumA.getNumber(trace) - datumB.getNumber(trace));
		}

		@Override
		public String toString() {
			return a.toString() + " - " + b.toString();
		}
	}

	public static class Mul extends Binary {
		public Mul(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.MUL, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			return new Datum(datumA.getNumber(trace) * datumB.getNumber(trace));
		}

		@Override
		public String toString() {
			return a.toString() + " * " + b.toString();
		}
	}

	public static class Div extends Binary {
		public Div(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.DIV, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			try {
				return new Datum(datumA.getNumber(trace) / datumB.getNumber(trace));
			} catch (final Exception e) {
				return new Datum();
			}
		}

		@Override
		public String toString() {
			return a.toString() + " / " + b.toString();
		}
	}

	public static class Mod extends Binary {
		public Mod(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.MOD, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			try {
				return new Datum(datumA.getNumber(trace) % datumB.getNumber(trace));
			} catch (final Exception e) {
				return new Datum();
			}
		}

		@Override
		public String toString() {
			return a.toString() + " % " + b.toString();
		}
	}

	public static class Pow extends Binary {
		public Pow(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.POW, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			try {
				return new Datum(Math.pow(datumA.getNumber(trace), datumB.getNumber(trace)));
			} catch (final Exception e) {
				return new Datum();
			}
		}

		@Override
		public String toString() {
			return a.toString() + " ** " + b.toString();
		}
	}

	public static class Index extends Binary {
		public Index(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.INDEX, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			if (datumA.getType() == Datum.Type.VECTOR) {
				try {
					return datumA.getVector(trace)[(int) datumB.getNumber(trace)];
				} catch (final Exception e) {
					throw new SonoRuntimeException(
							"Cannot index List <" + datumA.toStringTrace(trace) + "> with value <"
									+ datumB.toStringTrace(trace) + ">; Length: " + datumA.getVector(trace).length,
							trace);
				}
			} else if (datumA.getType() == Datum.Type.STRUCTURE) {
				return datumA.getStructure(trace).getScope().getVariable(interpreter.GETINDEX, interpreter, trace)
						.getFunction(Datum.Type.ANY, trace).execute(new Datum[] { datumB }, trace);
			}
			throw new SonoRuntimeException("Cannot index value <" + datumA.toStringTrace(trace) + ">", trace);
		}

		@Override
		public String toString() {
			return a.toString() + Interpreter.stringFromList(((Sequence) b).getVector().toArray(), "[", "]");
		}
	}

	public static class Equal extends Binary {
		public Equal(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.EQUAL, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			return new Datum(datumA.equals(datumB) ? 1 : 0);
		}

		@Override
		public String toString() {
			return a.toString() + " == " + b.toString();
		}
	}

	public static class NEqual extends Binary {
		public NEqual(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.NEQUAL, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			return new Datum(datumA.equals(datumB) ? 0 : 1);
		}

		@Override
		public String toString() {
			return a.toString() + " != " + b.toString();
		}
	}

	public static class Contrast extends Binary {
		public Contrast(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.CONTRAST, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			return new Datum(interpreter.getManager().getContrast(a.evaluate(scope, trace).getPhone(trace),
					b.evaluate(scope, trace).getPhone(trace)));
		}

		@Override
		public String toString() {
			return a.toString() + " ?> " + b.toString();
		}
	}

	public static class Common extends Unary {
		public Common(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.COMMON, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Datum[] data = a.evaluate(scope, trace).getVector(trace);
			final List<Phone> phones = new ArrayList<>();
			for (final Datum d : data)
				phones.add(d.getPhone(trace));
			return new Datum(interpreter.getManager().getCommon(phones));
		}

		@Override
		public String toString() {
			return "com " + a.toString();
		}

	}

	public static class VarDec extends Operator {
		int varName;

		public VarDec(final Interpreter interpreter, final int varName) {
			super(interpreter, Type.VAR_DEC);
			this.varName = varName;
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return scope.setVariable(interpreter, varName, null, trace);
		}

		@Override
		public String toString() {
			return "var " + interpreter.deHash(varName);
		}

		@Override
		public List<Operator> getChildren() {
			return new ArrayList<>();
		}
	}

	public static class Break extends Operator {
		public Break(final Interpreter interpreter) {
			super(interpreter, Type.BREAK);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return new Datum.Break();
		}

		@Override
		public String toString() {
			return "break";
		}

		@Override
		public List<Operator> getChildren() {
			return new ArrayList<>();
		}
	}

	public static class IfElse extends Binary {
		Operator c;

		public IfElse(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.IF_ELSE, a, b);
			this.c = null;
		}

		public void setElse(final Operator c) {
			this.c = c;
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum condition = a.evaluate(scope, trace);
			if (condition.getNumber(trace) != 0) {
				return b.evaluate(scope, trace);
			} else if (c != null) {
				return c.evaluate(scope, trace);
			}
			return new Datum();
		}

		@Override
		public String toString() {
			return a.toString() + " then " + b.toString() + (c != null ? " else " + c.toString() : "");
		}

		@Override
		public List<Operator> getChildren() {
			if (c == null) {
				return Arrays.asList(a, b);
			} else {
				return Arrays.asList(a, b, c);
			}
		}
	}

	public static class Lambda extends Binary {
		public Lambda(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.LAMBDA, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			List<Operator> paramsRaw;
			int[] pNames = null;
			boolean[] pRefs = null;
			boolean[] pFins = null;
			Datum.Type fType = Datum.Type.ANY;
			int i = 0;
			if (a.type == Type.HARD_LIST) {
				paramsRaw = ((Sequence) a).getVector();
				pNames = new int[paramsRaw.size()];
				pRefs = new boolean[paramsRaw.size()];
				pFins = new boolean[paramsRaw.size()];
				for (final Operator d : paramsRaw) {
					if (d.type == Type.REF_DEC) {
						pRefs[i] = true;
						pFins[i] = false;
						pNames[i] = ((Ref) d).getKey();
					} else if (d.type == Type.FINAL) {
						pFins[i] = true;
						pRefs[i] = false;
						pNames[i] = ((Final) d).getKey();
					} else {
						pRefs[i] = false;
						pFins[i] = false;
						pNames[i] = ((Variable) d).getKey();
					}
					i++;
				}
			} else if (a.type == Type.TYPE_DEC) {
				final Datum t = ((TypeDec) a).getA().evaluate(scope, trace);
				if (!t.isTemplative())
					throw new SonoRuntimeException(
							"Value <" + t.toStringTrace(trace) + "> cannot be used to designate an objective function.",
							trace);
				fType = t.getType();
				paramsRaw = ((Sequence) ((TypeDec) a).getB()).getVector();
				pNames = new int[paramsRaw.size()];
				pRefs = new boolean[paramsRaw.size()];
				pFins = new boolean[paramsRaw.size()];
				for (final Operator d : paramsRaw) {
					if (d.type == Type.REF_DEC) {
						pRefs[i] = true;
						pFins[i] = false;
						pNames[i] = ((Ref) d).getKey();
					} else if (d.type == Type.FINAL) {
						pFins[i] = true;
						pRefs[i] = false;
						pNames[i] = ((Final) d).getKey();
					} else {
						pRefs[i] = false;
						pFins[i] = false;
						pNames[i] = ((Variable) d).getKey();
					}
					i++;
				}
			}
			return new Datum(fType, new Function(scope, pNames, pRefs, pFins, b, interpreter));
		}

		@Override
		public String toString() {
			return a.toString() + " => " + b.toString();
		}
	}

	public static class Execute extends Binary {
		public Execute(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.EXECUTE, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumB = b.evaluate(scope, trace);
			Datum[] pValues = datumB.getVector(trace);
			Function f = null;
			if (a.type == Type.INNER) {
				final Datum datumA = ((Inner) a).getA().evaluate(scope, trace);
				if (datumA.getType() != Datum.Type.STRUCTURE) {
					final Datum[] tempValues = new Datum[pValues.length + 1];
					tempValues[0] = datumA;
					for (int i = 0; i < pValues.length; i++) {
						tempValues[i + 1] = pValues[i];
					}
					pValues = tempValues;
					final Datum functionB = ((Inner) a).getB().evaluate(scope, trace);
					f = functionB.getFunction(datumA.getType(), trace);
					if (f == null)
						f = functionB.getFunction(Datum.Type.ANY, trace);
				} else {
					f = a.evaluate(scope, trace).getFunction(Datum.Type.ANY, trace);
				}
			} else {
				final Datum fDatum = a.evaluate(scope, trace);
				if (pValues.length != 0) {
					f = fDatum.getFunction(pValues[0].getType(), trace);
				}
				if (f == null) {
					f = fDatum.getFunction(Datum.Type.ANY, trace);
				}
			}
			return f.execute(pValues, trace);
		}

		@Override
		public String toString() {
			return a.toString() + Interpreter.stringFromList(((Sequence) b).getVector().toArray(), "(", ")");
		}
	}

	public static class Less extends Binary {
		public Less(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.LESS, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			return new Datum(datumA.getNumber(trace) < datumB.getNumber(trace) ? 1 : 0);
		}

		@Override
		public String toString() {
			return a.toString() + " < " + b.toString();
		}
	}

	public static class More extends Binary {
		public More(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.LESS, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			return new Datum(datumA.getNumber(trace) > datumB.getNumber(trace) ? 1 : 0);
		}

		@Override
		public String toString() {
			return a.toString() + " > " + b.toString();
		}
	}

	public static class ELess extends Binary {
		public ELess(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.ELESS, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			return new Datum(datumA.getNumber(trace) <= datumB.getNumber(trace) ? 1 : 0);
		}

		@Override
		public String toString() {
			return a.toString() + " <= " + b.toString();
		}
	}

	public static class EMore extends Binary {
		public EMore(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.EMORE, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			return new Datum(datumA.getNumber(trace) >= datumB.getNumber(trace) ? 1 : 0);
		}

		@Override
		public String toString() {
			return a.toString() + " >= " + b.toString();
		}
	}

	public static class And extends Binary {
		public And(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.AND, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			if (datumA.getNumber(trace) != 0) {
				final Datum datumB = b.evaluate(scope, trace);
				if (datumB.getNumber(trace) != 0) {
					return new Datum(1);
				}
			}
			return new Datum(0);
		}

		@Override
		public String toString() {
			return a.toString() + " && " + b.toString();
		}
	}

	public static class Or extends Binary {
		public Or(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.OR, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			if (datumA.getNumber(trace) != 0) {
				return new Datum(1);
			} else {
				final Datum datumB = b.evaluate(scope, trace);
				if (datumB.getNumber(trace) != 0) {
					return new Datum(1);
				}
			}
			return new Datum(0);
		}

		@Override
		public String toString() {
			return a.toString() + " || " + b.toString();
		}
	}

	public static class Inner extends Binary {
		public Inner(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.INNER, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum object = a.evaluate(scope, trace);
			if (object.type != Datum.Type.STRUCTURE) {
				if (!object.isTemplative())
					throw new SonoRuntimeException("Value <" + object.toStringTrace(trace)
							+ "> is not templative and therefore cannot extract objective methods.", trace);
				final Datum fHolder = b.evaluate(scope, trace);
				return new Datum(object.getType(), fHolder.getFunction(object.getType(), trace));
			} else {
				return b.evaluate(object.getStructure(trace).getScope(), trace);
			}
		}

		@Override
		public String toString() {
			return a.toString() + "." + b.toString();
		}
	}

	public static class OuterCall extends Binary {
		public OuterCall(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.OUTER_CALL, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			return interpreter.getCommandManager().execute(a.evaluate(scope, trace).getString(trace),
					b.evaluate(scope, trace), trace, interpreter);
		}

		@Override
		public String toString() {
			return a.toString() + " _OUTER_CALL_ " + b.toString();
		}
	}

	public static class ClassDec extends Binary {
		public ClassDec(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.CLASS_DEC, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			boolean stat = false;
			final int varName = ((Casting) a).getKey();
			if (a.type == Type.STATIC_DEC)
				stat = true;
			final Structure structure = new Structure(stat, scope, b, varName, interpreter);
			if (stat)
				b.evaluate(structure.getScope(), trace);
			return scope.setVariable(interpreter, varName, new Datum(structure), trace);
		}

		@Override
		public String toString() {
			return a.toString() + " class " + b.toString();
		}
	}

	public static class NewDec extends Unary {
		public NewDec(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.NEW_DEC, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Structure struct = ((Execute) a).getA().evaluate(scope, trace).getStructure(trace);
			final Datum[] params = ((Execute) a).getB().evaluate(scope, trace).getVector(trace);
			return struct.instantiate(params, trace);
		}

		@Override
		public String toString() {
			return "new " + a.toString();
		}
	}

	public Operator(final Interpreter interpreter, final Type type) {
		this.interpreter = interpreter;
		this.type = type;
	}

	public abstract List<Operator> getChildren();

	public abstract Datum evaluate(Scope scope, List<String> trace);

	public abstract String toString();
}