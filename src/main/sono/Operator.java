package main.sono;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import main.Main;
import main.phl.*;
import main.sono.err.SonoRuntimeException;

public abstract class Operator {

	public enum Type {
		VARIABLE, DATUM, SET, TRANSFORM, SOFT_LIST, HARD_LIST, RULE_DEC, ARROW, SLASH, UNDERSCORE, MATRIX_DEC, SEQ_DEC,
		COMMON, ADD, SUB, MUL, DIV, MOD, INDEX, EQUAL, NEQUAL, LESS, MORE, ELESS, EMORE, MATRIX_CONV, NUMBER_CONV,
		CONTRAST, VAR_DEC, LIST_DEC, ITERATOR, LOOP, RANGE_UNTIL, BREAK, IF_ELSE, LAMBDA, RETURN, JOIN_DEC, STR_DEC,
		FIND_DEC, AND, OR, LEN, INNER, REF_DEC, TYPE_CONV, TYPE_DEC, STRUCT_DEC, STATIC_DEC, CLASS_DEC, NEW_DEC, POW,
		FEAT_DEC, THROW, TRY_CATCH, CHAR, ALLOC, FINAL,

		// INTERPRETER USE
		UNARY, BINARY, SEQUENCE, EXECUTE, OUTER_CALL
	}

	protected Type type;

	private abstract static class Unary extends Operator {
		protected Operator a;

		public Unary(final Type type, final Operator a) {
			super(type);
			this.a = a;
		}

		public Operator getA() {
			return a;
		}
	}

	private abstract static class Binary extends Unary {
		protected Operator b;

		public Binary(final Type type, final Operator a, final Operator b) {
			super(type, a);
			this.b = b;
		}

		public Operator getB() {
			return b;
		}
	}

	public abstract static class Sequence extends Operator {
		protected List<Operator> operators;

		public Sequence(final Type type, final List<Operator> operators) {
			super(type);
			this.operators = operators;
		}

		public List<Operator> getVector() {
			return operators;
		}
	}

	public abstract static class Casting extends Operator {
		protected int varName;

		public Casting(final Type type, final int varName) {
			super(type);
			this.varName = varName;
		}

		public int getKey() {
			return varName;
		}
	}

	public static class Variable extends Casting {
		public Variable(final int varName) {
			super(Type.VARIABLE, varName);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return scope.getVariable(varName, trace);
		}

		@Override
		public String toString() {
			return Interpreter.deHash(getKey());
		}
	}

	public static class Container extends Operator {
		private final Datum datum;

		public Container(final Datum datum) {
			super(Type.DATUM);
			this.datum = datum;
		}

		public Datum getDatum() {
			return datum;
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return datum;
		}

		@Override
		public String toString() {
			return datum.toString();
		}
	}

	public static class Set extends Binary {
		public Set(final Operator a, final Operator b) {
			super(Type.SET, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			datumA.set(datumB, trace);
			return datumA;
		}

		@Override
		public String toString() {
			return a.toString() + " = " + b.toString();
		}
	}

	public static class Transform extends Binary {
		public Transform(final Operator a, final Operator b) {
			super(Type.TRANSFORM, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			if (Main.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			if (datumB.getType() == Datum.Type.MATRIX)
				return new Datum(datumA.getPhone(trace).transform(datumB.getMatrix(trace), true));
			if (datumB.getType() == Datum.Type.RULE) {
				final Word result = datumB.getRule(trace).transform(datumA.getWord(trace));
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
		public SoftList(final List<Operator> operators) {
			super(Type.SOFT_LIST, operators);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (operators.size() != 1 && Main.DEBUG)
				trace.add(this.toString());
			final List<Datum> data = new ArrayList<>();
			for (final Operator o : operators) {
				if (o.type == Type.RANGE_UNTIL)
					data.addAll(((RangeUntil) o).getRange(scope, interpreter,
							(Main.DEBUG ? new ArrayList<>(trace) : trace)));
				else {
					final Datum d = o.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
					if (d.getType() == Datum.Type.I_BREAK)
						return d;
					if (d.getRet())
						return d;
					data.add(d);
				}
			}
			if (data.size() == 1)
				return data.get(0);
			return new Datum(data);
		}

		@Override
		public String toString() {
			return Interpreter.stringFromList(operators, "(", ")");
		}
	}

	public static class HardList extends Sequence {
		public HardList(final List<Operator> operators) {
			super(Type.HARD_LIST, operators);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (operators.size() != 1 && Main.DEBUG)
				trace.add(this.toString());
			final List<Datum> data = new ArrayList<>();
			final Scope newScope = new Scope(scope);
			for (final Operator o : operators) {
				if (o.type == Type.RANGE_UNTIL)
					data.addAll(((RangeUntil) o).getRange(newScope, interpreter,
							(Main.DEBUG ? new ArrayList<>(trace) : trace)));
				else {
					final Datum d = o.evaluate(newScope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
					if (d.getType() == Datum.Type.I_BREAK)
						return d;
					if (d.getRet())
						return d;
					data.add(d);
				}
			}
			return new Datum(data);
		}

		@Override
		public String toString() {
			return Interpreter.stringFromList(operators, "{", "}");
		}
	}

	public static class MatrixDec extends Sequence {
		public MatrixDec(final List<Operator> operators) {
			super(Type.MATRIX_DEC, operators);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			if (Main.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Matrix matrix = new Matrix();
			for (final Operator o : operators)
				matrix.put(o.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace))
						.getPair((Main.DEBUG ? new ArrayList<>(trace) : trace)));
			return new Datum(matrix);
		}

		@Override
		public String toString() {
			return Interpreter.stringFromList(operators, "[", "]");
		}
	}

	public static class RangeUntil extends Binary {
		public RangeUntil(final Operator a, final Operator b) {
			super(Type.RANGE_UNTIL, a, b);
		}

		public List<Datum> getRange(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final List<Datum> data = new ArrayList<>();
			final BigDecimal datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace))
					.getNumber(trace);
			final BigDecimal datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace))
					.getNumber(trace);
			for (BigDecimal i = datumA; i.compareTo(datumB) < 0; i = i.add(new BigDecimal(1)))
				data.add(new Datum(i));
			return data;
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " until " + b.toString();
		}
	}

	public static class Arrow extends Binary {
		public Arrow(final Operator a, final Operator b) {
			super(Type.ARROW, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " -> " + b.toString();
		}
	}

	public static class Ref extends Casting {
		public Ref(final int varName) {
			super(Type.REF_DEC, varName);
			this.varName = varName;
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return "ref " + Interpreter.deHash(varName);
		}
	}

	public static class Final extends Casting {
		public Final(final int varName) {
			super(Type.FINAL, varName);
			this.varName = varName;
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return "final " + Interpreter.deHash(varName);
		}
	}

	public static class FeatDec extends Unary {
		public FeatDec(final Operator a) {
			super(Type.FEAT_DEC, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			if (Main.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(interpreter.getManager().interpretFeature(datumA.getString(trace)));
		}

		@Override
		public String toString() {
			return "feat " + a.toString();
		}
	}

	public static class Alloc extends Unary {
		public Alloc(final Operator a) {
			super(Type.ALLOC, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			List<Datum> data = new ArrayList<>();
			for (int i = 0; datumA.getNumber(trace).compareTo(BigDecimal.valueOf(i)) > 0; i++) {
				data.add(new Datum());
			}
			return new Datum(data);
		}

		@Override
		public String toString() {
			return "alloc " + a.toString();
		}
	}

	public static class Throw extends Unary {
		public Throw(final Operator a) {
			super(Type.THROW, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			throw new SonoRuntimeException(datumA.getString(trace), trace);
		}

		@Override
		public String toString() {
			return "throw " + a.toString();
		}
	}

	public static class TryCatch extends Unary {
		private Operator b = null;

		public TryCatch(final Operator a) {
			super(Type.TRY_CATCH, a);
		}

		public void setCatch(final Operator b) {
			this.b = b;
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			try {
				return a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			} catch (final SonoRuntimeException e) {
				if (b != null) {
					final Scope catchScope = new Scope(scope);
					catchScope.setVariable(Interpreter.ERROR, new Datum(e.getMessage()), trace);
					final List<Datum> list = new ArrayList<>();
					for (final String s : trace)
						list.add(0, new Datum(s));
					catchScope.setVariable(Interpreter.TRACE, new Datum(list), trace);
					return b.evaluate(catchScope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
				} else {
					return new Datum();
				}
			}
		}

		@Override
		public String toString() {
			return "try " + a.toString() + (b != null ? " catch " + b.toString() : "");
		}
	}

	public static class Slash extends Binary {

		public Slash(final Operator a, final Operator b) {
			super(Type.SLASH, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " // " + b.toString();
		}
	}

	public static class Underscore extends Binary {
		public Underscore(final Operator a, final Operator b) {
			super(Type.UNDERSCORE, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " _ " + b.toString();
		}
	}

	public static class Iterator extends Binary {
		public Iterator(final Operator a, final Operator b) {
			super(Type.ITERATOR, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " in " + b.toString();
		}
	}

	public static class StructDec extends Casting {
		public StructDec(final int varName) {
			super(Type.STRUCT_DEC, varName);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return "struct " + Interpreter.deHash(varName);
		}
	}

	public static class StaticDec extends Casting {
		public StaticDec(final int varName) {
			super(Type.STATIC_DEC, varName);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return "static " + Interpreter.deHash(varName);
		}
	}

	public static class TypeDec extends Binary {
		public TypeDec(final Operator a, final Operator b) {
			super(Type.TYPE_DEC, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " :: " + b.toString();
		}
	}

	public static class Loop extends Binary {
		public Loop(final Operator a, final Operator b) {
			super(Type.LOOP, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			if (a.type == Type.ITERATOR) {
				final List<Datum> values = ((Iterator) a).getB()
						.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace)).getVector(trace);
				final int variable = ((Variable) ((Iterator) a).getA()).getKey();
				for (final Datum d : values) {
					final Scope loopScope = new Scope(scope);
					loopScope.setVariable(variable, d, trace);
					final Datum result = b.evaluate(loopScope, interpreter,
							(Main.DEBUG ? new ArrayList<>(trace) : trace));
					if (result.getType() == Datum.Type.I_BREAK)
						break;
					if (result.getRet())
						return result;
				}
			} else {
				while (a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace)).getNumber(trace)
						.compareTo(BigDecimal.ZERO) != 0) {
					final Scope loopScope = new Scope(scope);
					final Datum result = b.evaluate(loopScope, interpreter,
							(Main.DEBUG ? new ArrayList<>(trace) : trace));
					if (result.getType() == Datum.Type.I_BREAK)
						break;
					if (result.getRet())
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

		public RuleDec(final Rule.Type ruleType, final Operator a) {
			super(Type.RULE_DEC, a);
			this.ruleType = ruleType;
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			if (Main.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			while (a.type != Type.SLASH)
				a = ((Sequence) a).operators.get(0);
			final Datum dsearch = ((Binary) ((Binary) a).getA()).getA().evaluate(scope, interpreter,
					(Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum rawTrans = ((Binary) ((Binary) a).getA()).getB().evaluate(scope, interpreter,
					(Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum rawInit = ((Binary) ((Binary) a).getB()).getA().evaluate(scope, interpreter,
					(Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum rawFin = ((Binary) ((Binary) a).getB()).getB().evaluate(scope, interpreter,
					(Main.DEBUG ? new ArrayList<>(trace) : trace));

			Object search = null;
			List<Datum> dtrans;
			List<Datum> dinit;
			List<Datum> dfin;
			final List<Object> trans = new ArrayList<>();
			final List<Object> init = new ArrayList<>();
			final List<Object> fin = new ArrayList<>();

			if (dsearch.getType() == Datum.Type.MATRIX)
				search = dsearch.getMatrix(trace);
			else if (dsearch.getType() == Datum.Type.PHONE)
				search = dsearch.getPhone(trace);

			if (rawTrans.getType() == Datum.Type.VECTOR)
				dtrans = rawTrans.getVector(trace);
			else
				dtrans = Arrays.asList(rawTrans);

			for (final Datum d : dtrans) {
				if (d.type == Datum.Type.PHONE)
					trans.add(d.getPhone(trace));
				else if (d.type == Datum.Type.MATRIX)
					trans.add(d.getMatrix(trace));
				else if (d.getType() != Datum.Type.NULL)
					throw new SonoRuntimeException(
							"Value <" + d.toStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace))
									+ "> cannot be used in a Rule declaration.",
							trace);
			}

			if (rawInit.getType() == Datum.Type.VECTOR)
				dinit = rawInit.getVector(trace);
			else
				dinit = Arrays.asList(rawInit);

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
							init.add(Rule.Variants.SYLLABLE);
							break;
						case "+":
							init.add(Rule.Variants.MORPHEME);
							break;
						default:
							break;
					}
				} else if (d.getType() != Datum.Type.NULL) {
					throw new SonoRuntimeException(
							"Value <" + d.toStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace))
									+ "> cannot be used in a Rule declaration.",
							trace);
				}
			}

			if (rawFin.getType() == Datum.Type.VECTOR)
				dfin = rawFin.getVector(trace);
			else
				dfin = Arrays.asList(rawFin);

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
							fin.add(Rule.Variants.SYLLABLE);
							break;
						case "+":
							fin.add(Rule.Variants.MORPHEME);
							break;
						default:
							break;
					}
				} else if (d.getType() != Datum.Type.NULL) {
					throw new SonoRuntimeException(
							"Value <" + d.toStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace))
									+ "> cannot be used in a Rule declaration.",
							trace);
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
			return ruleType + " : " + a.toString();
		}
	}

	public static class SeqDec extends Unary {
		public SeqDec(final Operator a) {
			super(Type.SEQ_DEC, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			if (Main.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			if (datumA.getType() == Datum.Type.VECTOR) {
				final List<Phone> phones = new ArrayList<>();
				final List<Word.SyllableDelim> delims = new ArrayList<>();
				for (final Datum d : datumA.getVector(trace)) {
					if (d.getType() == Datum.Type.PHONE) {
						phones.add(d.getPhone(trace));
						delims.add(Word.SyllableDelim.NULL);
					} else if (d.getType() == Datum.Type.STRING) {
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
			throw new SonoRuntimeException(
					"Value <" + datumA.toStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace))
							+ "> cannot be converted to a Word.",
					trace);
		}

		@Override
		public String toString() {
			return "word " + a.toString();
		}
	}

	public static class ListDec extends Unary {
		public ListDec(final Operator a) {
			super(Type.LIST_DEC, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final List<Datum> list = new ArrayList<>();
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			switch (datumA.getType()) {
				case MATRIX:
					if (Main.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					for (final Pair p : datumA.getMatrix(trace))
						list.add(new Datum(p));
					break;
				case STRING:
					for (final char c : datumA.getString(trace).toCharArray())
						list.add(new Datum(String.valueOf(c)));
					break;
				case WORD:
					if (Main.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					for (int i = 0; i < datumA.getWord(trace).size(); i++) {
						if (datumA.getWord(trace).getDelim(i) != Word.SyllableDelim.NULL)
							list.add(new Datum(datumA.getWord(trace).getDelim(i).toString()));
						list.add(new Datum(datumA.getWord(trace).get(i)));
					}
					break;
				case VECTOR:
					return datumA;
				case STRUCTURE:
					return datumA.getStructure(trace).getScope().getVariable(Interpreter.GETLIST, trace)
							.getFunction(Datum.Type.ANY, trace)
							.execute(new ArrayList<>(), (Main.DEBUG ? new ArrayList<>(trace) : trace));
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
		public StringDec(final Operator a) {
			super(Type.STR_DEC, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final String s = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace))
					.toRawStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(s);
		}

		@Override
		public String toString() {
			return "str " + a.toString();
		}
	}

	public static class TypeConv extends Unary {
		public TypeConv(final Operator a) {
			super(Type.TYPE_CONV, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(datumA.getTypeString());
		}

		@Override
		public String toString() {
			return "type " + a.toString();
		}
	}

	public static class MatConv extends Unary {
		public MatConv(final Operator a) {
			super(Type.MATRIX_CONV, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			if (Main.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			if (datumA.getType() == Datum.Type.VECTOR) {
				final List<Datum> list = datumA.getVector(trace);
				final Matrix m = new Matrix();
				for (final Datum d : list)
					m.put(d.getPair((Main.DEBUG ? new ArrayList<>(trace) : trace)));
				return new Datum(m);
			} else if (datumA.getType() == Datum.Type.PHONE) {
				return new Datum(datumA.getPhone(trace).getMatrix());
			}
			throw new SonoRuntimeException("Cannot convert value <"
					+ datumA.toStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace)) + "> to a Matrix.", trace);
		}

		@Override
		public String toString() {
			return "mat " + a.toString();
		}
	}

	public static class Find extends Binary {
		public Find(final Operator a, final Operator b) {
			super(Type.FIND_DEC, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			if (Main.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final Matrix matrix = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace))
					.getMatrix(trace);
			final List<Datum> data = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace))
					.getVector(trace);
			final List<Phone> phones = new ArrayList<>();
			for (final Datum d : data)
				phones.add(d.getPhone(trace));
			final List<Phone> list = interpreter.getManager().getPhones(phones, matrix);
			final List<Datum> newData = new ArrayList<>();
			for (final Phone p : list) {
				newData.add(new Datum(p));
			}
			return new Datum(newData);
		}

		@Override
		public String toString() {
			return "find " + a.toString();
		}
	}

	public static class NumConv extends Unary {
		public NumConv(final Operator a) {
			super(Type.NUMBER_CONV, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			if (datumA.getType() == Datum.Type.NUMBER) {
				return datumA;
			} else if (datumA.getType() == Datum.Type.STRING) {
				try {
					return new Datum(new BigDecimal(datumA.getString(trace)));
				} catch (final Exception e) {
					return new Datum();
				}
			} else {
				throw new SonoRuntimeException("Cannot convert value <"
						+ datumA.toStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace)) + "> to a Number.",
						trace);
			}
		}

		@Override
		public String toString() {
			return "num " + a.toString();
		}
	}

	public static class Char extends Unary {
		public Char(final Operator a) {
			super(Type.CHAR, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final String s = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace))
					.getString(trace);
			if (s.length() != 1)
				throw new SonoRuntimeException("Value <" + s + "> is not a single char.", trace);
			return new Datum(BigDecimal.valueOf((int) s.charAt(0)));
		}

		@Override
		public String toString() {
			return "char " + a.toString();
		}
	}

	public static class Length extends Unary {
		public Length(final Operator a) {
			super(Type.LEN, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			switch (datumA.getType()) {
				case STRING:
					return new Datum(BigDecimal.valueOf(datumA.getString(trace).length()));
				case WORD:
					if (Main.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					return new Datum(BigDecimal.valueOf(datumA.getWord(trace).size()));
				case VECTOR:
					return new Datum(BigDecimal.valueOf(datumA.getVector(trace).size()));
				case MATRIX:
					if (Main.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					return new Datum(BigDecimal.valueOf(datumA.getMatrix(trace).size()));
				case STRUCTURE:
					return datumA.getStructure(trace).getScope().getVariable(Interpreter.GETLEN, trace)
							.getFunction(Datum.Type.ANY, trace)
							.execute(new ArrayList<>(), (Main.DEBUG ? new ArrayList<>(trace) : trace));
				default:
					throw new SonoRuntimeException("Cannot get length of value <"
							+ datumA.toStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace)) + ">", trace);
			}
		}

		@Override
		public String toString() {
			return "len " + a.toString();
		}
	}

	public static class Return extends Unary {
		public Return(final Operator a) {
			super(Type.RETURN, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			datumA.setRet(true);
			return datumA;
		}

		@Override
		public String toString() {
			return "return " + a.toString();
		}
	}

	public static class Add extends Binary {
		public Add(final Operator a, final Operator b) {
			super(Type.ADD, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			if (datumA.getType() != datumB.getType())
				throw new SonoRuntimeException(
						"Cannot add values <" + datumA.toStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace))
								+ "> and <" + datumB.toStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace))
								+ ">, of types: " + datumA.getType() + ", " + datumB.getType(),
						trace);
			switch (datumA.getType()) {
				case NUMBER:
					return new Datum(datumA.getNumber(trace).add(datumB.getNumber(trace)));
				case VECTOR:
					final List<Datum> newList = new ArrayList<>();
					newList.addAll(datumA.getVector(trace));
					newList.addAll(datumB.getVector(trace));
					return new Datum(newList);
				case MATRIX:
					if (Main.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					final Matrix newMatrix = new Matrix();
					newMatrix.putAll(datumA.getMatrix(trace));
					newMatrix.putAll(datumB.getMatrix(trace));
					return new Datum(newMatrix);
				case WORD:
					if (Main.getGlobalOption("LING").equals("FALSE"))
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
		public Sub(final Operator a, final Operator b) {
			super(Type.SUB, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(datumA.getNumber(trace).subtract(datumB.getNumber(trace)));
		}

		@Override
		public String toString() {
			return a.toString() + " - " + b.toString();
		}
	}

	public static class Mul extends Binary {
		public Mul(final Operator a, final Operator b) {
			super(Type.MUL, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(datumA.getNumber(trace).multiply(datumB.getNumber(trace), MathContext.DECIMAL128));
		}

		@Override
		public String toString() {
			return a.toString() + " * " + b.toString();
		}
	}

	public static class Div extends Binary {
		public Div(final Operator a, final Operator b) {
			super(Type.DIV, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			try {
				return new Datum(datumA.getNumber(trace).divide(datumB.getNumber(trace), MathContext.DECIMAL128));
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
		public Mod(final Operator a, final Operator b) {
			super(Type.MOD, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			try {
				return new Datum(datumA.getNumber(trace).remainder(datumB.getNumber(trace)));
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
		public Pow(final Operator a, final Operator b) {
			super(Type.POW, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			try {
				return new Datum(BigDecimal.valueOf(
						Math.pow(datumA.getNumber(trace).doubleValue(), datumB.getNumber(trace).doubleValue())));
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
		public Index(final Operator a, final Operator b) {
			super(Type.INDEX, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			if (datumA.getType() == Datum.Type.VECTOR) {
				try {
					return datumA.getVector(trace).get(datumB.getNumber(trace).intValue());
				} catch (Exception e) {
					throw new SonoRuntimeException(
							"Cannot index List <" + datumA.toStringTrace(trace) + "> with value <"
									+ datumB.toStringTrace(trace) + ">; Length: " + datumA.getVector(trace).size(),
							trace);
				}
			} else if (datumA.getType() == Datum.Type.STRUCTURE) {
				return datumA.getStructure(trace).getScope().getVariable(Interpreter.GETINDEX, trace)
						.getFunction(Datum.Type.ANY, trace)
						.execute(new ArrayList<>(Arrays.asList(datumB)), (Main.DEBUG ? new ArrayList<>(trace) : trace));
			}
			throw new SonoRuntimeException(
					"Cannot index value <" + datumA.toStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace)) + ">",
					trace);
		}

		@Override
		public String toString() {
			return a.toString() + Interpreter.stringFromList(((Sequence) b).getVector(), "[", "]");
		}
	}

	public static class Equal extends Binary {
		public Equal(final Operator a, final Operator b) {
			super(Type.EQUAL, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(datumA.equals(datumB) ? new BigDecimal(1) : new BigDecimal(0));
		}

		@Override
		public String toString() {
			return a.toString() + " == " + b.toString();
		}
	}

	public static class NEqual extends Binary {
		public NEqual(final Operator a, final Operator b) {
			super(Type.NEQUAL, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(datumA.equals(datumB) ? new BigDecimal(0) : new BigDecimal(1));
		}

		@Override
		public String toString() {
			return a.toString() + " != " + b.toString();
		}
	}

	public static class Contrast extends Binary {
		public Contrast(final Operator a, final Operator b) {
			super(Type.CONTRAST, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			if (Main.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			return new Datum(interpreter.getManager().getContrast(
					a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace)).getPhone(trace),
					b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace)).getPhone(trace)));
		}

		@Override
		public String toString() {
			return a.toString() + " ?> " + b.toString();
		}
	}

	public static class Common extends Unary {
		public Common(final Operator a) {
			super(Type.COMMON, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			if (Main.getGlobalOption("LING").equals("FALSE"))
				throw new SonoRuntimeException(
						"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.", trace);
			final List<Datum> data = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace))
					.getVector(trace);
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

		public VarDec(final int varName) {
			super(Type.VAR_DEC);
			this.varName = varName;
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return scope.setVariable(varName, null, trace);
		}

		@Override
		public String toString() {
			return "var " + Interpreter.deHash(varName);
		}
	}

	public static class Break extends Operator {
		public Break() {
			super(Type.BREAK);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return new Datum.Break();
		}

		@Override
		public String toString() {
			return "break";
		}

	}

	public static class IfElse extends Binary {
		Operator c;

		public IfElse(final Operator a, final Operator b) {
			super(Type.IF_ELSE, a, b);
			this.c = null;
		}

		public void setElse(final Operator c) {
			this.c = c;
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum condition = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			if (condition.getNumber(trace).compareTo(new BigDecimal(1)) == 0) {
				return b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			} else if (c != null) {
				return c.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			}
			return new Datum();
		}

		@Override
		public String toString() {
			return a.toString() + " then " + b.toString() + (c != null ? " else " + c.toString() : "");
		}
	}

	public static class Lambda extends Binary {
		public Lambda(final Operator a, final Operator b) {
			super(Type.LAMBDA, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final List<Integer> pNames = new ArrayList<>();
			final List<Boolean> pRefs = new ArrayList<>();
			final List<Boolean> pFins = new ArrayList<>();
			Datum.Type fType = Datum.Type.ANY;
			if (a.type == Type.HARD_LIST) {
				for (final Operator d : ((Sequence) a).getVector()) {
					if (d.type == Type.REF_DEC) {
						pRefs.add(true);
						pFins.add(false);
						pNames.add(((Ref) d).getKey());
					} else if (d.type == Type.FINAL) {
						pFins.add(true);
						pRefs.add(false);
						pNames.add(((Final) d).getKey());
					} else {
						pNames.add(((Variable) d).getKey());
						pRefs.add(false);
						pFins.add(false);
					}
				}
			} else if (a.type == Type.TYPE_DEC) {
				final Datum t = ((TypeDec) a).getA().evaluate(scope, interpreter,
						(Main.DEBUG ? new ArrayList<>(trace) : trace));
				if (!t.isTemplative())
					throw new SonoRuntimeException(
							"Value <" + t.toStringTrace(trace) + "> cannot be used to designate an objective function.",
							trace);
				fType = t.getType();
				for (final Operator d : ((Sequence) ((TypeDec) a).getB()).getVector()) {
					if (d.type == Type.REF_DEC) {
						pRefs.add(true);
						pFins.add(false);
						pNames.add(((Ref) d).getKey());
					} else if (d.type == Type.FINAL) {
						pFins.add(true);
						pRefs.add(false);
						pNames.add(((Final) d).getKey());
					} else {
						pNames.add(((Variable) d).getKey());
						pRefs.add(false);
						pFins.add(false);
					}
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
		public Execute(final Operator a, final Operator b) {
			super(Type.EXECUTE, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final List<Datum> pValues = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace))
					.getVector(trace);
			Function f = null;
			if (a.type == Type.INNER) {
				final Datum datumA = ((Inner) a).getA().evaluate(scope, interpreter,
						(Main.DEBUG ? new ArrayList<>(trace) : trace));
				if (datumA.getType() != Datum.Type.STRUCTURE) {
					pValues.add(0, datumA);
					f = ((Inner) a).getB().evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace))
							.getFunction(datumA.getType(), trace);
				} else {
					f = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace))
							.getFunction(Datum.Type.ANY, trace);
				}
			} else {
				Datum fDatum = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
				if (!pValues.isEmpty()) {
					f = fDatum.getFunction(pValues.get(0).getType(), trace);
				}
				if (f == null) {
					f = fDatum.getFunction(Datum.Type.ANY, trace);
				}
			}
			return f.execute(pValues, (Main.DEBUG ? new ArrayList<>(trace) : trace));
		}

		@Override
		public String toString() {
			return a.toString() + Interpreter.stringFromList(((Sequence) b).getVector(), "(", ")");
		}
	}

	public static class Less extends Binary {
		public Less(final Operator a, final Operator b) {
			super(Type.LESS, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(datumA.getNumber(trace).compareTo(datumB.getNumber(trace)) < 0 ? new BigDecimal(1)
					: new BigDecimal(0));
		}

		@Override
		public String toString() {
			return a.toString() + " < " + b.toString();
		}
	}

	public static class More extends Binary {
		public More(final Operator a, final Operator b) {
			super(Type.LESS, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(datumA.getNumber(trace).compareTo(datumB.getNumber(trace)) > 0 ? new BigDecimal(1)
					: new BigDecimal(0));
		}

		@Override
		public String toString() {
			return a.toString() + " > " + b.toString();
		}
	}

	public static class ELess extends Binary {
		public ELess(final Operator a, final Operator b) {
			super(Type.ELESS, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(datumA.getNumber(trace).compareTo(datumB.getNumber(trace)) <= 0 ? new BigDecimal(1)
					: new BigDecimal(0));
		}

		@Override
		public String toString() {
			return a.toString() + " <= " + b.toString();
		}
	}

	public static class EMore extends Binary {
		public EMore(final Operator a, final Operator b) {
			super(Type.EMORE, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(datumA.getNumber(trace).compareTo(datumB.getNumber(trace)) >= 0 ? new BigDecimal(1)
					: new BigDecimal(0));
		}

		@Override
		public String toString() {
			return a.toString() + " >= " + b.toString();
		}
	}

	public static class And extends Binary {
		public And(final Operator a, final Operator b) {
			super(Type.AND, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(datumA.getNumber(trace).compareTo(BigDecimal.valueOf(1)) == 0
					&& datumB.getNumber(trace).compareTo(BigDecimal.valueOf(1)) == 0 ? new BigDecimal(1)
							: new BigDecimal(0));
		}

		@Override
		public String toString() {
			return a.toString() + " && " + b.toString();
		}
	}

	public static class Or extends Binary {
		public Or(final Operator a, final Operator b) {
			super(Type.OR, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum datumA = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			final Datum datumB = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return new Datum(datumA.getNumber(trace).compareTo(BigDecimal.valueOf(1)) == 0
					|| datumB.getNumber(trace).compareTo(BigDecimal.valueOf(1)) == 0 ? new BigDecimal(1)
							: new BigDecimal(0));
		}

		@Override
		public String toString() {
			return a.toString() + " || " + b.toString();
		}
	}

	public static class Inner extends Binary {
		public Inner(final Operator a, final Operator b) {
			super(Type.INNER, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Datum object = a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			if (object.type != Datum.Type.STRUCTURE) {
				if (!object.isTemplative())
					throw new SonoRuntimeException(
							"Value <" + object.toStringTrace((Main.DEBUG ? new ArrayList<>(trace) : trace))
									+ "> is not templative and therefore cannot extract objective methods.",
							trace);
				final Datum fHolder = b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
				return new Datum(object.getType(), fHolder.getFunction(object.getType(), trace));
			} else {
				return b.evaluate(object.getStructure(trace).getScope(), interpreter,
						(Main.DEBUG ? new ArrayList<>(trace) : trace));
			}
		}

		@Override
		public String toString() {
			return a.toString() + "." + b.toString();
		}
	}

	public static class OuterCall extends Binary {
		public OuterCall(final Operator a, final Operator b) {
			super(Type.OUTER_CALL, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			return interpreter.getCommandManager().execute(
					a.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace)).getString(trace),
					b.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace)), trace);
		}

		@Override
		public String toString() {
			return a.toString() + " _OUTER_CALL_ " + b.toString();
		}
	}

	public static class ClassDec extends Binary {
		public ClassDec(final Operator a, final Operator b) {
			super(Type.CLASS_DEC, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			boolean stat = false;
			final int varName = ((Casting) a).getKey();
			if (a.type == Type.STATIC_DEC)
				stat = true;
			final Structure structure = new Structure(stat, scope, b, varName, interpreter);
			if (stat)
				b.evaluate(structure.getScope(), interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace));
			return scope.setVariable(varName, new Datum(structure), trace);
		}

		@Override
		public String toString() {
			return a.toString() + " class " + b.toString();
		}
	}

	public static class NewDec extends Unary {
		public NewDec(final Operator a) {
			super(Type.NEW_DEC, a);
		}

		@Override
		public Datum evaluate(final Scope scope, final Interpreter interpreter, final List<String> trace) {
			if (Main.DEBUG)
				trace.add(this.toString());
			final Structure struct = ((Execute) a).getA()
					.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace)).getStructure(trace);
			final List<Datum> params = ((Execute) a).getB()
					.evaluate(scope, interpreter, (Main.DEBUG ? new ArrayList<>(trace) : trace)).getVector(trace);
			return struct.instantiate(params, (Main.DEBUG ? new ArrayList<>(trace) : trace));
		}

		@Override
		public String toString() {
			return "new " + a.toString();
		}
	}

	public Operator(final Type type) {
		this.type = type;
	}

	public abstract Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace);

	public abstract String toString();
}