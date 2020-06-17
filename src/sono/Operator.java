package src.sono;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import src.phl.*;
import src.sono.err.SonoRuntimeException;

public abstract class Operator {

	public enum Type {
		VARIABLE, DATUM, SET, TRANSFORM, SOFT_LIST, HARD_LIST, RULE_DEC, ARROW, SLASH, UNDERSCORE, MATRIX_DEC, SEQ_DEC,
		COMMON, ADD, SUB, MUL, DIV, MOD, INDEX, EQUAL, NEQUAL, LESS, MORE, ELESS, EMORE, MATRIX_CONV, NUMBER_CONV,
		CONTRAST, VAR_DEC, LIST_DEC, ITERATOR, LOOP, RANGE_UNTIL, BREAK, IF_ELSE, LAMBDA, RETURN, JOIN_DEC, STR_DEC,
		FIND_DEC, AND, OR, LEN, INNER, REF_DEC, TYPE_CONV, TYPE_DEC, STRUCT_DEC, STATIC_DEC, CLASS_DEC, NEW_DEC, POW,

		// INTERPRETER USE
		UNARY, BINARY, SEQUENCE, EXECUTE, OUTER_CALL
	}

	protected Type type;

	private abstract static class Unary extends Operator {
		protected Operator a;

		public Unary(Type type, Operator a) {
			super(type);
			this.a = a;
		}

		public Operator getA() {
			return a;
		}
	}

	private abstract static class Binary extends Unary {
		protected Operator b;

		public Binary(Type type, Operator a, Operator b) {
			super(type, a);
			this.b = b;
		}

		public Operator getB() {
			return b;
		}
	}

	public abstract static class Sequence extends Operator {
		protected List<Operator> operators;

		public Sequence(Type type, List<Operator> operators) {
			super(type);
			this.operators = operators;
		}

		public List<Operator> getVector() {
			return operators;
		}
	}

	public abstract static class Casting extends Operator {
		protected String varName;

		public Casting(Type type, String varName) {
			super(type);
			this.varName = varName;
		}

		public String getKey() {
			return varName;
		}
	}

	public static class Variable extends Casting {
		public Variable(String varName) {
			super(Type.VARIABLE, varName);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return scope.getVariable(varName, trace);
		}

		@Override
		public String toString() {
			return varName;
		}
	}

	public static class Container extends Operator {
		private Datum datum;

		public Container(Datum datum) {
			super(Type.DATUM);
			this.datum = datum;
		}

		public Datum getDatum() {
			return datum;
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return datum;
		}

		@Override
		public String toString() {
			return datum.toString();
		}
	}

	public static class Set extends Binary {
		public Set(Operator a, Operator b) {
			super(Type.SET, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			datumA.set(datumB, trace);
			return datumA;
		}

		@Override
		public String toString() {
			return a.toString() + " = " + b.toString();
		}
	}

	public static class Transform extends Binary {
		public Transform(Operator a, Operator b) {
			super(Type.TRANSFORM, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			if (datumB.getType() == Datum.Type.MATRIX)
				return new Datum(datumA.getPhone(trace).transform(datumB.getMatrix(trace), true));
			if (datumB.getType() == Datum.Type.RULE) {
				Word result = datumB.getRule(trace).transform(datumA.getWord(trace));
				return new Datum(result);
			} else if (datumB.getType() == Datum.Type.VECTOR) {
				List<Datum> transformation = datumB.getVector(trace);
				Datum curr = datumA;
				for (Datum t : transformation)
					curr = (new Operator.Transform(new Operator.Container(curr), new Operator.Container(t)))
							.evaluate(scope, interpreter, new ArrayList<>(trace));
				return curr;
			} else {
				throw new SonoRuntimeException("Cannot transform value <" + datumA.toStringTrace(trace)
						+ "> with non-Matrix value <" + datumB.toStringTrace(trace) + ">", trace);
			}
		}

		@Override
		public String toString() {
			return a.toString() + " >> " + b.toString();
		}
	}

	public static class SoftList extends Sequence {
		public SoftList(List<Operator> operators) {
			super(Type.SOFT_LIST, operators);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			List<Datum> data = new ArrayList<>();
			for (Operator o : operators) {
				if (o.type == Type.RANGE_UNTIL)
					data.addAll(((RangeUntil) o).getRange(scope, interpreter, new ArrayList<>(trace)));
				else {
					Datum d = o.evaluate(scope, interpreter, new ArrayList<>(trace));
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
		public HardList(List<Operator> operators) {
			super(Type.HARD_LIST, operators);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			List<Datum> data = new ArrayList<>();
			Scope newScope = new Scope(scope);
			for (Operator o : operators) {
				if (o.type == Type.RANGE_UNTIL)
					data.addAll(((RangeUntil) o).getRange(newScope, interpreter, new ArrayList<>(trace)));
				else {
					Datum d = o.evaluate(newScope, interpreter, new ArrayList<>(trace));
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
		public MatrixDec(List<Operator> operators) {
			super(Type.MATRIX_DEC, operators);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Matrix matrix = new Matrix();
			for (Operator o : operators)
				matrix.put(o.evaluate(scope, interpreter, new ArrayList<>(trace)).getPair(new ArrayList<>(trace)));
			return new Datum(matrix);
		}

		@Override
		public String toString() {
			return Interpreter.stringFromList(operators, "[", "]");
		}
	}

	public static class RangeUntil extends Binary {
		public RangeUntil(Operator a, Operator b) {
			super(Type.RANGE_UNTIL, a, b);
		}

		public List<Datum> getRange(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			List<Datum> data = new ArrayList<>();
			BigDecimal datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace)).getNumber(trace);
			BigDecimal datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace)).getNumber(trace);
			for (BigDecimal i = datumA; i.compareTo(datumB) < 0; i = i.add(new BigDecimal(1)))
				data.add(new Datum(i));
			return data;
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " until " + b.toString();
		}
	}

	public static class Arrow extends Binary {
		public Arrow(Operator a, Operator b) {
			super(Type.ARROW, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " -> " + b.toString();
		}
	}

	public static class Ref extends Casting {
		public Ref(String varName) {
			super(Type.REF_DEC, varName);
			this.varName = varName;
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return "&" + varName;
		}
	}

	public static class Slash extends Binary {

		public Slash(Operator a, Operator b) {
			super(Type.SLASH, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " // " + b.toString();
		}
	}

	public static class Underscore extends Binary {
		public Underscore(Operator a, Operator b) {
			super(Type.UNDERSCORE, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " _ " + b.toString();
		}
	}

	public static class Iterator extends Binary {
		public Iterator(Operator a, Operator b) {
			super(Type.ITERATOR, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " in " + b.toString();
		}
	}

	public static class StructDec extends Casting {
		public StructDec(String varName) {
			super(Type.STRUCT_DEC, varName);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return "struct " + varName;
		}
	}

	public static class StaticDec extends Casting {
		public StaticDec(String varName) {
			super(Type.STATIC_DEC, varName);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return "static " + varName;
		}
	}

	public static class TypeDec extends Binary {
		public TypeDec(Operator a, Operator b) {
			super(Type.TYPE_DEC, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " :: " + b.toString();
		}
	}

	public static class Loop extends Binary {
		public Loop(Operator a, Operator b) {
			super(Type.LOOP, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			List<Datum> values = ((Iterator) a).getB().evaluate(scope, interpreter, new ArrayList<>(trace))
					.getVector(trace);
			String variable = ((Variable) ((Iterator) a).getA()).getKey();
			List<Datum> results = new ArrayList<>();
			for (Datum d : values) {
				Scope loopScope = new Scope(scope);
				loopScope.setVariable(variable, d, trace);
				Datum result = b.evaluate(loopScope, interpreter, new ArrayList<>(trace));
				if (result.getType() == Datum.Type.I_BREAK)
					break;
				if (result.getRet())
					return result;
				results.add(result);
			}
			return new Datum(results);
		}

		@Override
		public String toString() {
			return a.toString() + " do " + b.toString();
		}
	}

	public static class RuleDec extends Unary {
		private Rule.Type ruleType;

		public RuleDec(Rule.Type ruleType, Operator a) {
			super(Type.RULE_DEC, a);
			this.ruleType = ruleType;
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			while (a.type != Type.SLASH)
				a = ((Sequence) a).operators.get(0);
			Datum dsearch = ((Binary) ((Binary) a).getA()).getA().evaluate(scope, interpreter, new ArrayList<>(trace));
			List<Datum> dtrans = ((Binary) ((Binary) a).getA()).getB()
					.evaluate(scope, interpreter, new ArrayList<>(trace)).getVector(trace);
			List<Datum> dinit = ((Binary) ((Binary) a).getB()).getA()
					.evaluate(scope, interpreter, new ArrayList<>(trace)).getVector(trace);
			List<Datum> dfin = ((Binary) ((Binary) a).getB()).getB()
					.evaluate(scope, interpreter, new ArrayList<>(trace)).getVector(trace);

			Object search = null;
			List<Object> trans = new ArrayList<>();
			List<Object> init = new ArrayList<>();
			List<Object> fin = new ArrayList<>();

			if (dsearch.getType() == Datum.Type.MATRIX)
				search = dsearch.getMatrix(trace);
			else if (dsearch.getType() == Datum.Type.PHONE)
				search = dsearch.getPhone(trace);
			for (Datum d : dtrans) {
				if (d.type == Datum.Type.PHONE)
					trans.add(d.getPhone(trace));
				else if (d.type == Datum.Type.MATRIX)
					trans.add(d.getMatrix(trace));
				else
					throw new SonoRuntimeException("Value <" + d.toStringTrace(new ArrayList<>(trace))
							+ "> cannot be used in a Rule declaration.", trace);
			}
			for (Datum d : dinit) {
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
						default:
							break;
					}
				} else {
					throw new SonoRuntimeException("Value <" + d.toStringTrace(new ArrayList<>(trace))
							+ "> cannot be used in a Rule declaration.", trace);
				}
			}
			for (Datum d : dfin) {
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
						default:
							break;
					}
				} else {
					throw new SonoRuntimeException("Value <" + d.toStringTrace(new ArrayList<>(trace))
							+ "> cannot be used in a Rule declaration.", trace);
				}
			}

			switch (ruleType) {
				case A_BACKWARD:
					// TODO
					return null;
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
		public SeqDec(Operator a) {
			super(Type.SEQ_DEC, a);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			if (datumA.getType() == Datum.Type.VECTOR) {
				List<Phone> phones = new ArrayList<>();
				for (Datum d : datumA.getVector(trace))
					phones.add(d.getPhone(trace));
				return new Datum(new Word(phones));
			} else if (datumA.getType() == Datum.Type.STRING) {
				return new Datum(interpreter.getManager().interpretSequence(datumA.getString(trace)));
			}
			throw new SonoRuntimeException(
					"Value <" + datumA.toStringTrace(new ArrayList<>(trace)) + "> cannot be converted to a Word.",
					trace);
		}

		@Override
		public String toString() {
			return "word " + a.toString();
		}
	}

	public static class ListDec extends Unary {
		public ListDec(Operator a) {
			super(Type.LIST_DEC, a);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			List<Datum> list = new ArrayList<>();
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			switch (datumA.getType()) {
				case MATRIX:
					for (Pair p : datumA.getMatrix(trace))
						list.add(new Datum(p));
					break;
				case STRING:
					for (char c : datumA.getString(trace).toCharArray())
						list.add(new Datum(String.valueOf(c)));
					break;
				case WORD:
					for (int i = 0; i < datumA.getWord(trace).size(); i++)
						list.add(new Datum(datumA.getWord(trace).get(i)));
					break;
				case VECTOR:
					return datumA;
				case STRUCTURE:
					return datumA.getStructure(trace).getScope().getVariable("getList", trace)
							.getFunction(Datum.Type.ANY, trace).execute(new ArrayList<>(), new ArrayList<>(trace));
				default:
					throw new SonoRuntimeException(
							"Cannot convert value <" + datumA.toStringTrace(trace) + "> into a List", trace);
			}
			return new Datum(list);
		}

		@Override
		public String toString() {
			return "list " + a.toString();
		}
	}

	public static class StringDec extends Unary {
		public StringDec(Operator a) {
			super(Type.STR_DEC, a);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			String s = a.evaluate(scope, interpreter, new ArrayList<>(trace)).toRawStringTrace(new ArrayList<>(trace));
			return new Datum(s);
		}

		@Override
		public String toString() {
			return "str " + a.toString();
		}
	}

	public static class TypeConv extends Unary {
		public TypeConv(Operator a) {
			super(Type.TYPE_CONV, a);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			return new Datum(datumA.getTypeString());
		}

		@Override
		public String toString() {
			return "type " + a.toString();
		}
	}

	public static class MatConv extends Unary {
		public MatConv(Operator a) {
			super(Type.MATRIX_CONV, a);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			if (datumA.getType() == Datum.Type.VECTOR) {
				List<Datum> list = datumA.getVector(trace);
				Matrix m = new Matrix();
				for (Datum d : list)
					m.put(d.getPair(new ArrayList<>(trace)));
				return new Datum(m);
			} else if (datumA.getType() == Datum.Type.PHONE) {
				return new Datum(datumA.getPhone(trace).getMatrix());
			}
			throw new SonoRuntimeException(
					"Cannot convert value <" + datumA.toStringTrace(new ArrayList<>(trace)) + "> to a Matrix.", trace);
		}

		@Override
		public String toString() {
			return "mat " + a.toString();
		}
	}

	public static class Find extends Binary {
		public Find(Operator a, Operator b) {
			super(Type.FIND_DEC, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Matrix matrix = a.evaluate(scope, interpreter, new ArrayList<>(trace)).getMatrix(trace);
			List<Datum> data = b.evaluate(scope, interpreter, new ArrayList<>(trace)).getVector(trace);
			List<Phone> phones = new ArrayList<>();
			for (Datum d : data)
				phones.add(d.getPhone(trace));
			List<Phone> list = interpreter.getManager().getPhones(phones, matrix);
			List<Datum> newData = new ArrayList<>();
			for (Phone p : list) {
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
		public NumConv(Operator a) {
			super(Type.NUMBER_CONV, a);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			if (datumA.getType() == Datum.Type.NUMBER) {
				return datumA;
			} else if (datumA.getType() == Datum.Type.STRING) {
				try {
					return new Datum(new BigDecimal(datumA.getString(trace)));
				} catch (Exception e) {
					return new Datum();
				}
			} else {
				throw new SonoRuntimeException(
						"Cannot convert value <" + datumA.toStringTrace(new ArrayList<>(trace)) + "> to a Number.",
						trace);
			}
		}

		@Override
		public String toString() {
			return "num " + a.toString();
		}
	}

	public static class Length extends Unary {
		public Length(Operator a) {
			super(Type.LEN, a);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			switch (datumA.getType()) {
				case STRING:
					return new Datum(BigDecimal.valueOf(datumA.getString(trace).length()));
				case WORD:
					return new Datum(BigDecimal.valueOf(datumA.getWord(trace).size()));
				case VECTOR:
					return new Datum(BigDecimal.valueOf(datumA.getVector(trace).size()));
				case MATRIX:
					return new Datum(BigDecimal.valueOf(datumA.getMatrix(trace).size()));
				case STRUCTURE:
					return datumA.getStructure(trace).getScope().getVariable("getLen", trace)
							.getFunction(Datum.Type.ANY, trace).execute(new ArrayList<>(), new ArrayList<>(trace));
				default:
					throw new SonoRuntimeException(
							"Cannot get length of value <" + datumA.toStringTrace(new ArrayList<>(trace)) + ">", trace);
			}
		}

		@Override
		public String toString() {
			return "len " + a.toString();
		}
	}

	public static class Return extends Unary {
		public Return(Operator a) {
			super(Type.RETURN, a);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			datumA.setRet(true);
			return datumA;
		}

		@Override
		public String toString() {
			return "return " + a.toString();
		}
	}

	public static class Add extends Binary {
		public Add(Operator a, Operator b) {
			super(Type.ADD, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			if (datumA.getType() != datumB.getType())
				throw new SonoRuntimeException("Cannot add values <" + datumA.toStringTrace(new ArrayList<>(trace))
						+ "> and <" + datumB.toStringTrace(new ArrayList<>(trace)) + ">, of types: " + datumA.getType()
						+ ", " + datumB.getType(), trace);
			switch (datumA.getType()) {
				case NUMBER:
					return new Datum(datumA.getNumber(trace).add(datumB.getNumber(trace)));
				case VECTOR:
					List<Datum> newList = new ArrayList<>();
					newList.addAll(datumA.getVector(trace));
					newList.addAll(datumB.getVector(trace));
					return new Datum(newList);
				case MATRIX:
					Matrix newMatrix = new Matrix();
					newMatrix.putAll(datumA.getMatrix(trace));
					newMatrix.putAll(datumB.getMatrix(trace));
					return new Datum(newMatrix);
				case WORD:
					Word newWord = new Word();
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
		public Sub(Operator a, Operator b) {
			super(Type.SUB, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			return new Datum(datumA.getNumber(trace).subtract(datumB.getNumber(trace)));
		}

		@Override
		public String toString() {
			return a.toString() + " - " + b.toString();
		}
	}

	public static class Mul extends Binary {
		public Mul(Operator a, Operator b) {
			super(Type.MUL, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			return new Datum(datumA.getNumber(trace).multiply(datumB.getNumber(trace), MathContext.DECIMAL128));
		}

		@Override
		public String toString() {
			return a.toString() + " * " + b.toString();
		}
	}

	public static class Div extends Binary {
		public Div(Operator a, Operator b) {
			super(Type.DIV, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			try {
				return new Datum(datumA.getNumber(trace).divide(datumB.getNumber(trace), MathContext.DECIMAL128));
			} catch (Exception e) {
				return new Datum();
			}
		}

		@Override
		public String toString() {
			return a.toString() + " / " + b.toString();
		}
	}

	public static class Mod extends Binary {
		public Mod(Operator a, Operator b) {
			super(Type.MOD, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			try {
				return new Datum(datumA.getNumber(trace).remainder(datumB.getNumber(trace)));
			} catch (Exception e) {
				return new Datum();
			}
		}

		@Override
		public String toString() {
			return a.toString() + " % " + b.toString();
		}
	}

	public static class Pow extends Binary {
		public Pow(Operator a, Operator b) {
			super(Type.POW, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			try {
				return new Datum(BigDecimal.valueOf(
						Math.pow(datumA.getNumber(trace).doubleValue(), datumB.getNumber(trace).doubleValue())));
			} catch (Exception e) {
				return new Datum();
			}
		}

		@Override
		public String toString() {
			return a.toString() + " ** " + b.toString();
		}
	}

	public static class Index extends Binary {
		public Index(Operator a, Operator b) {
			super(Type.INDEX, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			if (datumA.getType() == Datum.Type.VECTOR) {
				return datumA.getVector(trace).get(datumB.getNumber(trace).intValue());
			} else if (datumA.getType() == Datum.Type.STRUCTURE) {
				return datumA.getStructure(trace).getScope().getVariable("getIndex", trace)
						.getFunction(Datum.Type.ANY, trace)
						.execute(new ArrayList<>(Arrays.asList(datumB)), new ArrayList<>(trace));
			}
			throw new SonoRuntimeException("Cannot index value <" + datumA.toStringTrace(new ArrayList<>(trace)) + ">",
					trace);
		}

		@Override
		public String toString() {
			return a.toString() + Interpreter.stringFromList(((Sequence) b).getVector(), "[", "]");
		}
	}

	public static class Equal extends Binary {
		public Equal(Operator a, Operator b) {
			super(Type.EQUAL, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			return new Datum(datumA.equals(datumB) ? new BigDecimal(1) : new BigDecimal(0));
		}

		@Override
		public String toString() {
			return a.toString() + " == " + b.toString();
		}
	}

	public static class NEqual extends Binary {
		public NEqual(Operator a, Operator b) {
			super(Type.NEQUAL, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			return new Datum(datumA.equals(datumB) ? new BigDecimal(0) : new BigDecimal(1));
		}

		@Override
		public String toString() {
			return a.toString() + " != " + b.toString();
		}
	}

	public static class Contrast extends Binary {
		public Contrast(Operator a, Operator b) {
			super(Type.CONTRAST, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return new Datum(interpreter.getManager().getContrast(
					a.evaluate(scope, interpreter, new ArrayList<>(trace)).getPhone(trace),
					b.evaluate(scope, interpreter, new ArrayList<>(trace)).getPhone(trace)));
		}

		@Override
		public String toString() {
			return a.toString() + " ?> " + b.toString();
		}
	}

	public static class Common extends Unary {
		public Common(Operator a) {
			super(Type.COMMON, a);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			List<Datum> data = a.evaluate(scope, interpreter, new ArrayList<>(trace)).getVector(trace);
			List<Phone> phones = new ArrayList<>();
			for (Datum d : data)
				phones.add(d.getPhone(trace));
			return new Datum(interpreter.getManager().getCommon(phones));
		}

		@Override
		public String toString() {
			return "com " + a.toString();
		}

	}

	public static class VarDec extends Operator {
		String varName;

		public VarDec(String varName) {
			super(Type.VAR_DEC);
			this.varName = varName;
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return scope.setVariable(varName, null, trace);
		}

		@Override
		public String toString() {
			return "let " + varName;
		}
	}

	public static class Break extends Operator {
		public Break() {
			super(Type.BREAK);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
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

		public IfElse(Operator a, Operator b) {
			super(Type.IF_ELSE, a, b);
			this.c = null;
		}

		public void setElse(Operator c) {
			this.c = c;
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum condition = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			if (condition.getNumber(trace).compareTo(new BigDecimal(1)) == 0) {
				return b.evaluate(scope, interpreter, new ArrayList<>(trace));
			} else if (c != null) {
				return c.evaluate(scope, interpreter, new ArrayList<>(trace));
			}
			return new Datum();
		}

		@Override
		public String toString() {
			return a.toString() + " then " + b.toString() + (c != null ? " else " + c.toString() : "");
		}
	}

	public static class Lambda extends Binary {
		public Lambda(Operator a, Operator b) {
			super(Type.LAMBDA, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			List<String> pNames = new ArrayList<>();
			List<Boolean> pRefs = new ArrayList<>();
			Datum.Type fType = Datum.Type.ANY;
			if (a.type == Type.HARD_LIST) {
				for (Operator d : ((Sequence) a).getVector()) {
					if (d.type == Type.REF_DEC) {
						pRefs.add(true);
						pNames.add(((Ref) d).getKey());
					} else {
						pNames.add(((Variable) d).getKey());
						pRefs.add(false);
					}
				}
			} else if (a.type == Type.TYPE_DEC) {
				Datum t = ((TypeDec) a).getA().evaluate(scope, interpreter, new ArrayList<>(trace));
				if (!t.isTemplative())
					throw new SonoRuntimeException(
							"Value <" + t.toStringTrace(trace) + "> cannot be used to designate an objective function.",
							trace);
				fType = t.getType();
				for (Operator d : ((Sequence) ((TypeDec) a).getB()).getVector()) {
					if (d.type == Type.REF_DEC) {
						pRefs.add(true);
						pNames.add(((Ref) d).getKey());
					} else {
						pNames.add(((Variable) d).getKey());
						pRefs.add(false);
					}
				}
			}
			return new Datum(fType, new Function(scope, pNames, pRefs, b, interpreter));
		}

		@Override
		public String toString() {
			return a.toString() + " => " + b.toString();
		}
	}

	public static class Execute extends Binary {
		public Execute(Operator a, Operator b) {
			super(Type.EXECUTE, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			List<Datum> pValues = b.evaluate(scope, interpreter, new ArrayList<>(trace)).getVector(trace);
			Function f = a.evaluate(scope, interpreter, new ArrayList<>(trace)).getFunction(Datum.Type.ANY, trace);
			if (f == null)
				throw new SonoRuntimeException("This function is required to be used post-objectively.", trace);
			return f.execute(pValues, new ArrayList<>(trace));
		}

		@Override
		public String toString() {
			return a.toString() + Interpreter.stringFromList(((Sequence) b).getVector(), "(", ")");
		}
	}

	public static class Less extends Binary {
		public Less(Operator a, Operator b) {
			super(Type.LESS, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			return new Datum(datumA.getNumber(trace).compareTo(datumB.getNumber(trace)) < 0 ? new BigDecimal(1)
					: new BigDecimal(0));
		}

		@Override
		public String toString() {
			return a.toString() + " < " + b.toString();
		}
	}

	public static class More extends Binary {
		public More(Operator a, Operator b) {
			super(Type.LESS, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			return new Datum(datumA.getNumber(trace).compareTo(datumB.getNumber(trace)) > 0 ? new BigDecimal(1)
					: new BigDecimal(0));
		}

		@Override
		public String toString() {
			return a.toString() + " > " + b.toString();
		}
	}

	public static class ELess extends Binary {
		public ELess(Operator a, Operator b) {
			super(Type.ELESS, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			return new Datum(datumA.getNumber(trace).compareTo(datumB.getNumber(trace)) <= 0 ? new BigDecimal(1)
					: new BigDecimal(0));
		}

		@Override
		public String toString() {
			return a.toString() + " <= " + b.toString();
		}
	}

	public static class EMore extends Binary {
		public EMore(Operator a, Operator b) {
			super(Type.EMORE, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
			return new Datum(datumA.getNumber(trace).compareTo(datumB.getNumber(trace)) >= 0 ? new BigDecimal(1)
					: new BigDecimal(0));
		}

		@Override
		public String toString() {
			return a.toString() + " >= " + b.toString();
		}
	}

	public static class And extends Binary {
		public And(Operator a, Operator b) {
			super(Type.AND, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
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
		public Or(Operator a, Operator b) {
			super(Type.OR, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum datumA = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			Datum datumB = b.evaluate(scope, interpreter, new ArrayList<>(trace));
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
		public Inner(Operator a, Operator b) {
			super(Type.INNER, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Datum object = a.evaluate(scope, interpreter, new ArrayList<>(trace));
			if (object.type != Datum.Type.STRUCTURE) {
				if (b.type == Type.EXECUTE) {
					List<Datum> params = ((Execute) b).getB().evaluate(scope, interpreter, new ArrayList<>(trace))
							.getVector(trace);
					Datum fHolder = ((Execute) b).getA().evaluate(scope, interpreter, new ArrayList<>(trace));
					params.add(0, object);
					Function f = fHolder.getFunction(object.getType(), trace);
					if (f == null)
						f = fHolder.getFunction(Datum.Type.ANY, trace);
					if (f == null)
						throw new SonoRuntimeException("Function does not accept type <" + object.getType() + ">",
								trace);
					return f.execute(params, new ArrayList<>(trace));
				} else {
					if (!object.isTemplative())
						throw new SonoRuntimeException("Value <" + object.toStringTrace(new ArrayList<>(trace))
								+ "> is not templative and therefore cannot extract objective methods.", trace);
					Datum fHolder = b.evaluate(scope, interpreter, new ArrayList<>(trace));
					return new Datum(object.getType(), fHolder.getFunction(object.getType(), trace));
				}
			} else {
				return b.evaluate(object.getStructure(trace).getScope(), interpreter, new ArrayList<>(trace));
			}
		}

		@Override
		public String toString() {
			return a.toString() + "." + b.toString();
		}
	}

	public static class OuterCall extends Binary {
		public OuterCall(Operator a, Operator b) {
			super(Type.OUTER_CALL, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			return interpreter.getCommandManager().execute(
					a.evaluate(scope, interpreter, new ArrayList<>(trace)).getString(trace),
					b.evaluate(scope, interpreter, new ArrayList<>(trace)), trace);
		}

		@Override
		public String toString() {
			return a.toString() + " _OUTER_CALL_ " + b.toString();
		}
	}

	public static class ClassDec extends Binary {
		public ClassDec(Operator a, Operator b) {
			super(Type.CLASS_DEC, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			boolean stat = false;
			String varName = ((Casting) a).getKey();
			if (a.type == Type.STATIC_DEC)
				stat = true;
			Structure structure = new Structure(stat, scope, b, varName, interpreter);
			if (stat)
				b.evaluate(structure.getScope(), interpreter, new ArrayList<>(trace));
			return scope.setVariable(varName, new Datum(structure), trace);
		}

		@Override
		public String toString() {
			return a.toString() + " class " + b.toString();
		}
	}

	public static class NewDec extends Unary {
		public NewDec(Operator a) {
			super(Type.NEW_DEC, a);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace) {
			trace.add(this.toString());
			Structure struct = ((Execute) a).getA().evaluate(scope, interpreter, new ArrayList<>(trace))
					.getStructure(trace);
			List<Datum> params = ((Execute) a).getB().evaluate(scope, interpreter, new ArrayList<>(trace))
					.getVector(trace);
			return struct.instantiate(params, new ArrayList<>(trace));
		}

		@Override
		public String toString() {
			return "new " + a.toString();
		}
	}

	public Operator(Type type) {
		this.type = type;
	}

	public abstract Datum evaluate(Scope scope, Interpreter interpreter, List<String> trace);

	public abstract String toString();
}