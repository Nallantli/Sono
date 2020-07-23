package main.sono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.SonoWrapper;
import main.phl.Matrix;
import main.phl.Pair;
import main.phl.Phone;
import main.phl.Rule;
import main.phl.Word;
import main.sono.err.SonoRuntimeException;

public abstract class Operator {

	public enum Type {
		VARIABLE, DATUM, SET, TRANSFORM, SOFT_LIST, HARD_LIST, RULE_DEC, ARROW, SLASH, UNDERSCORE, MATRIX_DEC, SEQ_DEC,
		COMMON, ADD, SUB, MUL, DIV, MOD, INDEX, EQUAL, NOT_EQUAL, LESS, MORE, E_LESS, E_MORE, MATRIX_CONVERT,
		NUMBER_CONVERT, CONTRAST, VAR_DEC, LIST_DEC, ITERATOR, LOOP, RANGE_UNTIL, BREAK, IF_ELSE, LAMBDA, RETURN,
		JOIN_DEC, STR_DEC, FIND_DEC, AND, OR, LEN, INNER, REF_DEC, TYPE_CONVERT, TYPE_DEC, STRUCT_DEC, STATIC_DEC,
		CLASS_DEC, NEW_DEC, POW, FEAT_DEC, THROW, TRY_CATCH, CHAR, ALLOC, FINAL, REGISTER, CODE, REFER, SWITCH, HASH,
		P_EQUALS, P_NOT_EQUAL, ABSTRACT_DEC, EXTENDS,

		// INTERPRETER USE
		UNARY, BINARY, SEQUENCE, EXECUTE, OUTER_CALL, SWITCH_CASE
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
		public Operator[] getChildren() {
			return new Operator[] { a };
		}

		@Override
		public void condense() {
			a.condense();
			if (a.type == Type.SOFT_LIST && a.getChildren().length == 1)
				a = a.getChildren()[0];
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
		public Operator[] getChildren() {
			return new Operator[] { a, b };
		}

		@Override
		public void condense() {
			super.condense();
			b.condense();
			if (b.type == Type.SOFT_LIST && b.getChildren().length == 1)
				b = b.getChildren()[0];
		}
	}

	public abstract static class Sequence extends Operator {
		protected Operator[] operators;

		public Sequence(final Interpreter i, final Type type, final Operator[] operators) {
			super(i, type);
			this.operators = operators;
		}

		public Operator[] getVector() {
			return operators;
		}

		@Override
		public Operator[] getChildren() {
			return getVector();
		}

		@Override
		public void condense() {
			final List<Operator> newO = new ArrayList<>();
			for (final Operator o : operators) {
				o.condense();
				if (o.type == Type.SOFT_LIST && o.getChildren().length == 1)
					newO.add(o.getChildren()[0]);
				else
					newO.add(o);
			}
			this.operators = newO.toArray(new Operator[0]);
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
		public Operator[] getChildren() {
			return new Operator[0];
		}

		@Override
		public void condense() {
			// Unnecessary
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
		public Operator[] getChildren() {
			return new Operator[0];
		}

		@Override
		public void condense() {
			// Unnecessary
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
			switch (datumB.getType()) {
				case MATRIX:
					final Phone ret = datumA.getPhone(trace).transform(datumB.getMatrix(trace), true);
					if (ret == null)
						return new Datum();
					return new Datum(ret);
				case RULE:
					final Word result = datumB.getRule(trace).transform(interpreter.getManager(),
							datumA.getWord(trace));
					return new Datum(result);
				default:
					throw new SonoRuntimeException("Cannot transform value <" + datumA.getDebugString(trace)
							+ "> with value <" + datumB.getDebugString(trace) + ">", trace);
			}
		}

		@Override
		public String toString() {
			return a.toString() + " >> " + b.toString();
		}
	}

	public static class SoftList extends Sequence {
		public SoftList(final Interpreter interpreter, final Operator[] operators) {
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
				data = new Datum[operators.length];
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
			return Interpreter.stringFromList(operators, "(", ")");
		}
	}

	public static class HardList extends Sequence {
		public HardList(final Interpreter interpreter, final Operator[] operators) {
			super(interpreter, Type.HARD_LIST, operators);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			Datum[] data = null;
			final Scope newScope = new Scope(scope.getStructure(), scope);
			if (Interpreter.containsInstance(operators, RangeUntil.class)) {
				final List<Datum> list = new ArrayList<>();
				for (final Operator o : operators) {
					if (o.type == Type.RANGE_UNTIL)
						list.addAll(((RangeUntil) o).getRange(newScope, trace));
					else {
						final Datum d = o.evaluate(newScope, trace);
						if (d.getType() == Datum.Type.I_BREAK || d.getRet() || d.getRefer())
							return d;
						list.add(d);
					}
				}
				data = list.toArray(new Datum[0]);
			} else {
				data = new Datum[operators.length];
				int i = 0;
				for (final Operator o : operators) {
					final Datum d = o.evaluate(newScope, trace);
					if (d.getType() == Datum.Type.I_BREAK || d.getRet() || d.getRefer())
						return d;
					data[i++] = d;
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
		public MatrixDec(final Interpreter interpreter, final Operator[] operators) {
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
			final Matrix matrix = new Matrix(interpreter.getManager());
			for (final Operator o : operators) {
				final Pair p = o.evaluate(scope, trace).getPair(trace);
				matrix.put(p.getFeature(), p.getQuality());
			}
			return new Datum(matrix);
		}

		@Override
		public String toString() {
			return Interpreter.stringFromList(operators, "[", "]");
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

	public static class Allocate extends Unary {
		public Allocate(final Interpreter interpreter, final Operator a) {
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
			for (int i = 0; i < datumA; i++)
				data[i] = new Datum();
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
		private Operator b;

		public TryCatch(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.TRY_CATCH, a);
			this.b = null;
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
					final Scope catchScope = new Scope(scope.getStructure(), scope);
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
		public Operator[] getChildren() {
			if (b == null)
				return new Operator[] { a };
			else
				return new Operator[] { a, b };
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
			return a.toString() + " .. " + b.toString();
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

	public static class AbstractDec extends Casting {
		public AbstractDec(final Interpreter interpreter, final int varName) {
			super(interpreter, Type.ABSTRACT_DEC, varName);
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
			return "abstract " + interpreter.deHash(varName);
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

	public static class Extends extends Binary {
		public Extends(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.EXTENDS, a, b);
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
			return a.toString() + " extends " + b.toString();
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
				final Datum datumAB = ((Iterator) a).getB().evaluate(scope, trace);
				final int valuesSize = datumAB.getVectorLength(trace);
				final int variable = ((Variable) ((Iterator) a).getA()).getKey();
				for (int i = 0; i < valuesSize; i++) {
					final Scope loopScope = new Scope(scope.getStructure(), scope);
					loopScope.setVariable(interpreter, variable, datumAB.indexVector(i), trace);
					final Datum result = b.evaluate(loopScope, trace);
					if (result.getType() == Datum.Type.I_BREAK)
						break;
					if (result.getRet() || result.getRefer())
						return result;
				}
			} else {
				while (a.evaluate(scope, trace).getNumber(trace) != 0) {
					final Scope loopScope = new Scope(scope.getStructure(), scope);
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
				a = ((Sequence) a).operators[0];
			final Datum dSearch = ((Binary) ((Binary) a).getA()).getA().evaluate(scope, trace);
			final Datum rawTrans = ((Binary) ((Binary) a).getA()).getB().evaluate(scope, trace);
			final Datum rawInit = ((Binary) ((Binary) a).getB()).getA().evaluate(scope, trace);
			final Datum rawFin = ((Binary) ((Binary) a).getB()).getB().evaluate(scope, trace);

			Object search = null;
			Datum[] dTrans;
			Datum[] dInit;
			Datum[] dFin;
			final List<Object> trans = new ArrayList<>();
			final List<Object> init = new ArrayList<>();
			final List<Object> fin = new ArrayList<>();

			if (dSearch.getType() == Datum.Type.MATRIX)
				search = dSearch.getMatrix(trace);
			else if (dSearch.getType() == Datum.Type.PHONE)
				search = dSearch.getPhone(trace);

			if (rawTrans.getType() == Datum.Type.VECTOR)
				dTrans = rawTrans.getVector(trace);
			else {
				dTrans = new Datum[1];
				dTrans[0] = rawTrans;
			}

			for (final Datum d : dTrans) {
				switch (d.type) {
					case PHONE:
						trans.add(d.getPhone(trace));
						break;
					case MATRIX:
						trans.add(d.getMatrix(trace));
						break;
					case NULL:
						break;
					default:
						throw new SonoRuntimeException(
								"Value <" + d.getDebugString(trace) + "> cannot be used in a Rule declaration.", trace);
				}
			}

			if (rawInit.getType() == Datum.Type.VECTOR)
				dInit = rawInit.getVector(trace);
			else {
				dInit = new Datum[1];
				dInit[0] = rawInit;
			}

			for (final Datum d : dInit) {
				switch (d.type) {
					case PHONE:
						init.add(d.getPhone(trace));
						break;
					case MATRIX:
						init.add(d.getMatrix(trace));
						break;
					case STRING:
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
						break;
					case NULL:
						break;
					default:
						throw new SonoRuntimeException(
								"Value <" + d.getDebugString(trace) + "> cannot be used in a Rule declaration.", trace);
				}
			}

			if (rawFin.getType() == Datum.Type.VECTOR)
				dFin = rawFin.getVector(trace);
			else {
				dFin = new Datum[1];
				dFin[0] = rawFin;
			}

			for (final Datum d : dFin) {
				switch (d.type) {
					case PHONE:
						fin.add(d.getPhone(trace));
						break;
					case MATRIX:
						fin.add(d.getMatrix(trace));
						break;
					case STRING:
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
						break;
					case NULL:
						break;
					default:
						throw new SonoRuntimeException(
								"Value <" + d.getDebugString(trace) + "> cannot be used in a Rule declaration.", trace);
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
			final Datum allV = interpreter.getScope().getVariable(interpreter.ALL, interpreter, trace);
			final int oldVSize = interpreter.getScope().getVariable(interpreter.ALL, interpreter, trace)
					.getVectorLength(trace);
			final Datum[] newV = new Datum[oldVSize + 1];
			for (int i = 0; i < oldVSize; i++)
				newV[i] = allV.indexVector(i);
			newV[oldVSize] = new Datum(ret);
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
				final List<Word.SyllableDelim> delimits = new ArrayList<>();
				boolean flag = true;
				final int dataSize = datumA.getVectorLength(trace);
				for (int i = 0; i < dataSize; i++) {
					final Datum d = datumA.indexVector(i);
					if (d.getType() == Datum.Type.PHONE) {
						phones.add(d.getPhone(trace));
						if (flag)
							delimits.add(Word.SyllableDelim.NULL);
						else
							flag = true;
					} else if (d.getType() == Datum.Type.STRING) {
						flag = false;
						switch (d.getString(trace)) {
							case ".":
								delimits.add(Word.SyllableDelim.DELIM);
								break;
							case "+":
								delimits.add(Word.SyllableDelim.MORPHEME);
								break;
							default:
								throw new SonoRuntimeException(
										"Value <" + d.getDebugString(trace) + "> is not applicable as a word delimiter",
										trace);
						}
					}
				}
				return new Datum(new Word(phones, delimits));
			} else if (datumA.getType() == Datum.Type.STRING) {
				return new Datum(interpreter.getManager().interpretSequence(datumA.getString(trace)));
			}
			throw new SonoRuntimeException(
					"Value <" + datumA.getDebugString(trace) + "> cannot be converted to a Word.", trace);
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
					return datumA.getStructure(trace).getScope().getVariable(interpreter.GET_LIST, interpreter, trace)
							.getFunction(Datum.Type.ANY, trace).execute(null, trace);
				default:
					throw new SonoRuntimeException(
							"Cannot convert value <" + datumA.getDebugString(trace) + "> into a List", trace);
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

	public static class TypeConvert extends Unary {
		public TypeConvert(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.TYPE_CONVERT, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			return new Datum(datumA.getTypeString());
		}

		@Override
		public String toString() {
			return "type " + a.toString();
		}
	}

	public static class Hash extends Unary {
		public Hash(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.HASH, a);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			return new Datum(datumA.hashCode());
		}

		@Override
		public String toString() {
			return "type " + a.toString();
		}
	}

	public static class MatConvert extends Unary {
		public MatConvert(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.MATRIX_CONVERT, a);
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
				final int listSize = datumA.getVectorLength(trace);
				final Matrix m = new Matrix(interpreter.getManager());
				for (int i = 0; i < listSize; i++) {
					final Pair p = datumA.indexVector(i).getPair(trace);
					m.put(p.getFeature(), p.getQuality());
				}
				return new Datum(m);
			} else if (datumA.getType() == Datum.Type.PHONE) {
				return new Datum(datumA.getPhone(trace).getMatrix());
			}
			throw new SonoRuntimeException("Cannot convert value <" + datumA.getDebugString(trace) + "> to a Matrix.",
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
			final Datum datumB = b.evaluate(scope, trace);
			final int dataSize = datumB.getVectorLength(trace);
			final List<Phone> phones = new ArrayList<>();
			for (int i = 0; i < dataSize; i++)
				phones.add(datumB.indexVector(i).getPhone(trace));
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

	public static class NumConvert extends Unary {
		public NumConvert(final Interpreter interpreter, final Operator a) {
			super(interpreter, Type.NUMBER_CONVERT, a);
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
						"Cannot convert value <" + datumA.getDebugString(trace) + "> to a Number.", trace);
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
				throw new SonoRuntimeException("Value <" + datumA.getDebugString(trace) + "> is not of type `Number`",
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
					return new Datum(datumA.getVectorLength(trace));
				case MATRIX:
					if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					return new Datum(datumA.getMatrix(trace).size());
				case STRUCTURE:
					return datumA.getStructure(trace).getScope().getVariable(interpreter.GET_LEN, interpreter, trace)
							.getFunction(Datum.Type.ANY, trace).execute(null, trace);
				default:
					throw new SonoRuntimeException("Cannot get length of value <" + datumA.getDebugString(trace) + ">",
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
				throw new SonoRuntimeException("Cannot add values <" + datumA.getDebugString(trace) + "> and <"
						+ datumB.getDebugString(trace) + ">", trace);
			switch (datumA.getType()) {
				case NUMBER:
					return new Datum(datumA.getNumber(trace) + datumB.getNumber(trace));
				case VECTOR:
					return Datum.arrayConcat(datumA, datumB);
				case MATRIX:
					if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
						throw new SonoRuntimeException(
								"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.",
								trace);
					final Matrix newMatrix = new Matrix(interpreter.getManager());
					newMatrix.putAll(datumA.getMatrix(trace));
					newMatrix.putAll(datumB.getMatrix(trace));
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
					return datumA.indexVector((int) datumB.getNumber(trace));
				} catch (final Exception e) {
					throw new SonoRuntimeException(
							"Cannot index List <" + datumA.getDebugString(trace) + "> with value <"
									+ datumB.getDebugString(trace) + ">; Length: " + datumA.getVectorLength(trace),
							trace);
				}
			} else if (datumA.getType() == Datum.Type.STRUCTURE) {
				return datumA.getStructure(trace).getScope().getVariable(interpreter.GET_INDEX, interpreter, trace)
						.getFunction(Datum.Type.ANY, trace).execute(new Datum[] { datumB }, trace);
			}
			throw new SonoRuntimeException("Cannot index value <" + datumA.getDebugString(trace) + ">", trace);
		}

		@Override
		public String toString() {
			return a.toString() + "[" + b.toString() + "]";
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
			return new Datum(datumA.isEqual(datumB, trace) ? 1 : 0);
		}

		@Override
		public String toString() {
			return a.toString() + " == " + b.toString();
		}
	}

	public static class PureEqual extends Binary {
		public PureEqual(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.P_EQUALS, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			return new Datum(datumA.isEqualPure(datumB, trace) ? 1 : 0);
		}

		@Override
		public String toString() {
			return a.toString() + " === " + b.toString();
		}
	}

	public static class NEqual extends Binary {
		public NEqual(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.NOT_EQUAL, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			return new Datum(datumA.isEqual(datumB, trace) ? 0 : 1);
		}

		@Override
		public String toString() {
			return a.toString() + " != " + b.toString();
		}
	}

	public static class PureNEqual extends Binary {
		public PureNEqual(final Interpreter interpreter, final Operator a, final Operator b) {
			super(interpreter, Type.P_NOT_EQUAL, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			final Datum datumA = a.evaluate(scope, trace);
			final Datum datumB = b.evaluate(scope, trace);
			return new Datum(datumA.isEqualPure(datumB, trace) ? 0 : 1);
		}

		@Override
		public String toString() {
			return a.toString() + " !== " + b.toString();
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
			final Datum datumA = a.evaluate(scope, trace);
			final int dataSize = datumA.getVectorLength(trace);
			final List<Phone> phones = new ArrayList<>();
			for (int i = 0; i < dataSize; i++)
				phones.add(datumA.indexVector(i).getPhone(trace));
			return new Datum(interpreter.getManager().getCommon(phones));
		}

		@Override
		public String toString() {
			return "com " + a.toString();
		}

	}

	public static class VarDec extends Operator {
		private final int varName;

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
		public Operator[] getChildren() {
			return new Operator[0];
		}

		@Override
		public void condense() {
			// Unnecessary
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
		public Operator[] getChildren() {
			return new Operator[0];
		}

		@Override
		public void condense() {
			// Unnecessary
		}
	}

	public static class IfElse extends Binary {
		private Operator c;

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
			if (condition.getNumber(trace) != 0)
				return b.evaluate(scope, trace);
			else if (c != null)
				return c.evaluate(scope, trace);
			return new Datum();
		}

		@Override
		public String toString() {
			return a.toString() + " then " + b.toString() + (c != null ? " else " + c.toString() : "");
		}

		@Override
		public void condense() {
			super.condense();
			if (c != null) {
				c.condense();
				if (c.type == Type.SOFT_LIST && c.getChildren().length == 1)
					c = c.getChildren()[0];
			}
		}

		@Override
		public Operator[] getChildren() {
			if (c == null)
				return new Operator[] { a, b };
			else
				return new Operator[] { a, b, c };
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
			Operator[] paramsRaw;
			int[] pNames = null;
			boolean[] pRefs = null;
			boolean[] pFins = null;
			Datum.Type fType = Datum.Type.ANY;
			int i = 0;
			if (a.type == Type.HARD_LIST) {
				paramsRaw = ((Sequence) a).getVector();
				pNames = new int[paramsRaw.length];
				pRefs = new boolean[paramsRaw.length];
				pFins = new boolean[paramsRaw.length];
				for (final Operator d : paramsRaw) {
					switch (d.type) {
						case REF_DEC:
							pRefs[i] = true;
							pFins[i] = false;
							pNames[i] = ((Ref) d).getKey();
							break;
						case FINAL:
							pFins[i] = true;
							pRefs[i] = false;
							pNames[i] = ((Final) d).getKey();
							break;
						default:
							pRefs[i] = false;
							pFins[i] = false;
							pNames[i] = ((Variable) d).getKey();
							break;
					}
					i++;
				}
			} else if (a.type == Type.TYPE_DEC) {
				final Datum t = ((TypeDec) a).getA().evaluate(scope, trace);
				if (!t.isPrototypic())
					throw new SonoRuntimeException("Value <" + t.getDebugString(trace)
							+ "> cannot be used to designate an objective function.", trace);
				fType = t.getType();
				paramsRaw = ((Sequence) ((TypeDec) a).getB()).getVector();
				pNames = new int[paramsRaw.length];
				pRefs = new boolean[paramsRaw.length];
				pFins = new boolean[paramsRaw.length];
				for (final Operator d : paramsRaw) {
					switch (d.type) {
						case REF_DEC:
							pRefs[i] = true;
							pFins[i] = false;
							pNames[i] = ((Ref) d).getKey();
							break;
						case FINAL:
							pFins[i] = true;
							pRefs[i] = false;
							pNames[i] = ((Final) d).getKey();
							break;
						default:
							pRefs[i] = false;
							pFins[i] = false;
							pNames[i] = ((Variable) d).getKey();
							break;
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
			final int pValuesSize = datumB.getVectorLength(trace);
			Datum[] pValues = null;
			Function f = null;
			if (a.type == Type.INNER) {
				final Datum datumA = ((Inner) a).getA().evaluate(scope, trace);
				if (datumA.getType() != Datum.Type.STRUCTURE) {
					final Datum[] tempValues = new Datum[pValuesSize + 1];
					tempValues[0] = datumA;
					for (int i = 0; i < pValuesSize; i++)
						tempValues[i + 1] = datumB.indexVector(i);
					pValues = tempValues;
					final Datum functionB = ((Inner) a).getB().evaluate(scope, trace);
					f = functionB.getFunction(datumA.getType(), trace);
					if (f == null)
						f = functionB.getFunction(Datum.Type.ANY, trace);
				} else {
					pValues = datumB.getVector(trace);
					f = a.evaluate(scope, trace).getFunction(Datum.Type.ANY, trace);
				}
			} else {
				pValues = datumB.getVector(trace);
				final Datum fDatum = a.evaluate(scope, trace);
				if (pValuesSize != 0)
					f = fDatum.getFunction(datumB.indexVector(0).getType(), trace);
				if (f == null)
					f = fDatum.getFunction(Datum.Type.ANY, trace);
			}
			if (f == null)
				throw new SonoRuntimeException("No function found", trace);
			return f.execute(pValues, trace);
		}

		@Override
		public String toString() {
			return a.toString() + Interpreter.stringFromList(((Sequence) b).getVector(), "(", ")");
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
			super(interpreter, Type.E_LESS, a, b);
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
			super(interpreter, Type.E_MORE, a, b);
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
				if (datumB.getNumber(trace) != 0)
					return new Datum(1);
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
				if (datumB.getNumber(trace) != 0)
					return new Datum(1);
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
				if (!object.isPrototypic())
					throw new SonoRuntimeException("Value <" + object.getDebugString(trace)
							+ "> is not prototypic and therefore cannot extract objective methods.", trace);
				final Datum fHolder = b.evaluate(scope, trace);
				return new Datum(object.getType(), fHolder.getFunction(object.getType(), trace));
			} else {
				final Structure s = object.getStructure(trace);
				if (s.perusable())
					return b.evaluate(object.getStructure(trace).getScope(), trace);
				throw new SonoRuntimeException("Class <" + s.getName() + "> is not perusable.", trace);
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
			Structure.Type stype = Structure.Type.STRUCT;
			Operator objectOperator = null;
			Structure extending = null;
			if (a.type == Type.EXTENDS) {
				objectOperator = ((Extends) a).getA();
				extending = ((Extends) a).getB().evaluate(scope, trace).getStructure(trace);
			} else {
				objectOperator = a;
			}
			final int varName = ((Casting) objectOperator).getKey();
			if (objectOperator.type == Type.STATIC_DEC)
				stype = Structure.Type.STATIC;
			else if (objectOperator.type == Type.ABSTRACT_DEC)
				stype = Structure.Type.ABSTRACT;
			Operator main = b;
			if (extending != null)
				main = new SoftList(interpreter, new Operator[] { extending.getMain(), main });
			final Structure structure = new Structure(scope.getStructure(), stype, scope, main, varName, interpreter);
			if (stype == Structure.Type.STATIC)
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

	public static class Switch extends Unary {
		private final Map<Datum, Operator> map;
		private Operator c;

		public Switch(final Interpreter interpreter, final Operator a, final Map<Datum, Operator> map) {
			super(interpreter, Type.SWITCH, a);
			this.map = map;
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

			final Datum key = a.evaluate(scope, trace);
			final Operator b = map.get(key);
			if (b == null) {
				if (c == null)
					return new Datum();
				else
					return c.evaluate(scope, trace);
			}
			return map.get(key).evaluate(scope, trace);
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
				if (e.type == Type.SOFT_LIST && e.getChildren().length == 1)
					newMap.put(entry.getKey(), e.getChildren()[0]);
				else
					newMap.put(entry.getKey(), e);
			}
			this.map.clear();
			this.map.putAll(newMap);
			if (c != null) {
				c.condense();
				if (c.type == Type.SOFT_LIST && c.getChildren().length == 1)
					c = c.getChildren()[0];
			}
		}
	}

	public static class SwitchCase extends Operator {
		private final Datum key;
		private final Operator seq;

		public SwitchCase(final Interpreter interpreter, final Datum key, final Operator seq) {
			super(interpreter, Type.SWITCH_CASE);
			this.key = key;
			this.seq = seq;
		}

		public Datum getKey() {
			return this.key;
		}

		public Operator getOperator() {
			return this.seq;
		}

		@Override
		public Datum evaluate(final Scope scope, List<String> trace) {
			if (SonoWrapper.DEBUG) {
				trace = new ArrayList<>(trace);
				trace.add(this.toString());
			}
			throw new SonoRuntimeException("Cannot evaluate uncontrolled goto statement", trace);
		}

		@Override
		public String toString() {
			return key.toString() + " goto " + seq.toString();
		}

		@Override
		public Operator[] getChildren() {
			return new Operator[0];
		}

		@Override
		public void condense() {
			// Unnecessary
		}
	}

	public Operator(final Interpreter interpreter, final Type type) {
		this.interpreter = interpreter;
		this.type = type;
	}

	public abstract Operator[] getChildren();

	public abstract void condense();

	public abstract Datum evaluate(Scope scope, List<String> trace);

	public abstract String toString();
}