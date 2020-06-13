package src.sono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import src.phl.*;
import src.sono.err.SonoRuntimeException;

public abstract class Operator {
	public enum Type {
		VARIABLE, DATUM, SET, TRANSFORM, SOFT_LIST, HARD_LIST, RULE_DEC, ARROW, SLASH, UNDERSCORE, MATRIX_DEC, SEQ_DEC,
		COMMON, ADD, SUB, MUL, DIV, MOD, INDEX, EQUAL, NEQUAL, LESS, MORE, ELESS, EMORE, MATRIX_CONV, NUMBER_CONV,
		CONTRAST, VAR_DEC, LIST_DEC, ITERATOR, LOOP, RANGE_UNTIL, BREAK, IF_ELSE, LAMBDA, RETURN, JOIN_DEC, STR_DEC,
		FIND_DEC, AND, OR, LEN, INNER, REF_DEC, JOIN,

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

		public List<Operator> getList() {
			return operators;
		}
	}

	public static class Variable extends Operator {
		private String varName;

		public Variable(String varName) {
			super(Type.VARIABLE);
			this.varName = varName;
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			return scope.getVariable(varName);
		}

		public String getKey() {
			return varName;
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			datumA.set(datumB);
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			if (datumB.getType() == Datum.Type.MATRIX)
				return new Datum(datumA.getPhone().transform(datumB.getMatrix(), true));
			if (datumB.getType() == Datum.Type.RULE) {
				Word result = datumB.getRule().transform(datumA.getWord());
				return new Datum(result);
			} else if (datumB.getType() == Datum.Type.LIST) {
				List<Datum> transformation = datumB.getList();
				Datum curr = datumA;
				for (Datum t : transformation)
					curr = (new Operator.Transform(new Operator.Container(curr), new Operator.Container(t)))
							.evaluate(scope, interpreter);
				return curr;
			} else {
				throw new SonoRuntimeException(
						"Cannot transform value <" + datumA + "> with non-Matrix value <" + datumB + ">");
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			List<Datum> data = new ArrayList<>();
			for (Operator o : operators) {
				if (o.type == Type.RANGE_UNTIL)
					data.addAll(((RangeUntil) o).getRange(scope, interpreter));
				else {
					Datum d = o.evaluate(scope, interpreter);
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			List<Datum> data = new ArrayList<>();
			Scope newScope = new Scope(scope);
			for (Operator o : operators) {
				if (o.type == Type.RANGE_UNTIL)
					data.addAll(((RangeUntil) o).getRange(newScope, interpreter));
				else {
					Datum d = o.evaluate(newScope, interpreter);
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Matrix matrix = new Matrix();
			for (Operator o : operators)
				matrix.put(o.evaluate(scope, interpreter).getPair());
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

		public List<Datum> getRange(Scope scope, Interpreter interpreter) {
			List<Datum> data = new ArrayList<>();
			BigDecimal datumA = a.evaluate(scope, interpreter).getNumber();
			BigDecimal datumB = b.evaluate(scope, interpreter).getNumber();
			for (BigDecimal i = datumA; i.compareTo(datumB) < 0; i = i.add(new BigDecimal(1)))
				data.add(new Datum(i));
			return data;
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter) {
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " -> " + b.toString();
		}
	}

	public static class Ref extends Operator {
		String varName;

		public Ref(String varName) {
			super(Type.REF_DEC);
			this.varName = varName;
		}

		public String getKey() {
			return varName;
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter) {
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " in " + b.toString();
		}
	}

	public static class Loop extends Binary {
		public Loop(Operator a, Operator b) {
			super(Type.LOOP, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			List<Datum> values = ((Iterator) a).getB().evaluate(scope, interpreter).getList();
			String variable = ((Variable) ((Iterator) a).getA()).getKey();
			List<Datum> results = new ArrayList<>();
			for (Datum d : values) {
				Scope loopScope = new Scope(scope);
				loopScope.setVariable(variable, d);
				Datum result = b.evaluate(loopScope, interpreter);
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			while (a.type != Type.SLASH)
				a = ((Sequence) a).operators.get(0);
			Datum dsearch = ((Binary) ((Binary) a).getA()).getA().evaluate(scope, interpreter);
			Datum dtrans = ((Binary) ((Binary) a).getA()).getB().evaluate(scope, interpreter);
			Datum dinit = ((Binary) ((Binary) a).getB()).getA().evaluate(scope, interpreter);
			Datum dfin = ((Binary) ((Binary) a).getB()).getB().evaluate(scope, interpreter);

			Object search = null;
			Object trans = null;
			Object init = null;
			Object fin = null;

			if (dsearch.getType() == Datum.Type.MATRIX)
				search = dsearch.getMatrix();
			else if (dsearch.getType() == Datum.Type.PHONE)
				search = dsearch.getPhone();
			if (dtrans.getType() == Datum.Type.MATRIX)
				trans = dtrans.getMatrix();
			else if (dtrans.getType() == Datum.Type.PHONE)
				trans = dtrans.getPhone();
			if (dinit.getType() == Datum.Type.MATRIX)
				init = dinit.getMatrix();
			else if (dinit.getType() == Datum.Type.PHONE)
				init = dinit.getPhone();
			if (dfin.getType() == Datum.Type.MATRIX)
				fin = dfin.getMatrix();
			else if (dfin.getType() == Datum.Type.PHONE)
				fin = dfin.getPhone();

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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			if (datumA.getType() == Datum.Type.LIST) {
				List<Phone> phones = new ArrayList<>();
				for (Datum d : datumA.getList())
					phones.add(d.getPhone());
				return new Datum(new Word(phones));
			} else if (datumA.getType() == Datum.Type.STRING) {
				return new Datum(interpreter.getManager().interpretSequence(datumA.getString()));
			}
			throw new SonoRuntimeException("Value <" + datumA.toString() + "> cannot be converted to a Word.");
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			List<Datum> list = new ArrayList<>();
			Datum datumA = a.evaluate(scope, interpreter);
			switch (datumA.getType()) {
				case MATRIX:
					for (Pair p : datumA.getMatrix())
						list.add(new Datum(p));
					break;
				case STRING:
					for (char c : datumA.getString().toCharArray())
						list.add(new Datum(String.valueOf(c)));
					break;
				case WORD:
					for (int i = 0; i < datumA.getWord().size(); i++)
						list.add(new Datum(datumA.getWord().get(i)));
					break;
				case LIST:
					return datumA;
				default:
					throw new SonoRuntimeException("Cannot convert value <" + datumA + "> into a List");
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			String s = a.evaluate(scope, interpreter).toRawString();
			return new Datum(s);
		}

		@Override
		public String toString() {
			return "str " + a.toString();
		}
	}

	public static class Join extends Unary {
		public Join(Operator a) {
			super(Type.JOIN, a);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			List<Datum> list = a.evaluate(scope, interpreter).getList();
			StringBuilder s = new StringBuilder();
			for (Datum d : list)
				s.append(d.getString());
			return new Datum(s.toString());
		}

		@Override
		public String toString() {
			return "join " + a.toString();
		}
	}

	public static class MatConv extends Unary {
		public MatConv(Operator a) {
			super(Type.MATRIX_CONV, a);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			if (datumA.getType() == Datum.Type.LIST) {
				List<Datum> list = datumA.getList();
				Matrix m = new Matrix();
				for (Datum d : list)
					m.put(d.getPair());
				return new Datum(m);
			} else if (datumA.getType() == Datum.Type.PHONE) {
				return new Datum(datumA.getPhone().getMatrix());
			}
			throw new SonoRuntimeException("Cannot convert value <" + datumA.toString() + "> to a Matrix.");
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Matrix matrix = a.evaluate(scope, interpreter).getMatrix();
			List<Datum> data = b.evaluate(scope, interpreter).getList();
			List<Phone> phones = new ArrayList<>();
			for (Datum d : data)
				phones.add(d.getPhone());
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			return new Datum(new BigDecimal(a.evaluate(scope, interpreter).getString()));
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			switch (datumA.getType()) {
				case STRING:
					return new Datum(BigDecimal.valueOf(datumA.getString().length()));
				case WORD:
					return new Datum(BigDecimal.valueOf(datumA.getWord().size()));
				case LIST:
					return new Datum(BigDecimal.valueOf(datumA.getList().size()));
				case MATRIX:
					return new Datum(BigDecimal.valueOf(datumA.getMatrix().size()));
				default:
					throw new SonoRuntimeException("Cannot get length of value <" + datumA.toString() + ">");
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			if (datumA.getType() != datumB.getType())
				throw new SonoRuntimeException("Cannot add values <" + datumA.toString() + "> and <" + datumB.toString()
						+ ">, of types: " + datumA.getType() + ", " + datumB.getType());
			switch (datumA.getType()) {
				case NUMBER:
					return new Datum(datumA.getNumber().add(datumB.getNumber()));
				case LIST:
					List<Datum> newList = new ArrayList<>();
					newList.addAll(datumA.getList());
					newList.addAll(datumB.getList());
					return new Datum(newList);
				case MATRIX:
					Matrix newMatrix = new Matrix();
					newMatrix.putAll(datumA.getMatrix());
					newMatrix.putAll(datumB.getMatrix());
					return new Datum(newMatrix);
				case WORD:
					Word newWord = new Word();
					newWord.addAll(datumA.getWord());
					newWord.addAll(datumB.getWord());
					return new Datum(newWord);
				case STRING:
					return new Datum(datumA.getString() + datumB.getString());
				default:
					throw new SonoRuntimeException("Values of type <" + datumA.getType() + "> cannot be added.");
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			return new Datum(datumA.getNumber().subtract(datumB.getNumber()));
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			return new Datum(datumA.getNumber().multiply(datumB.getNumber()));
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			return new Datum(datumA.getNumber().divide(datumB.getNumber()));
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			return new Datum(new BigDecimal(datumA.getNumber().longValue() % datumB.getNumber().longValue()));
		}

		@Override
		public String toString() {
			return a.toString() + " % " + b.toString();
		}
	}

	public static class Index extends Binary {
		public Index(Operator a, Operator b) {
			super(Type.INDEX, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			return datumA.getList().get(datumB.getNumber().intValue());
		}

		@Override
		public String toString() {
			return a.toString() + " .index " + b.toString();
		}
	}

	public static class Equal extends Binary {
		public Equal(Operator a, Operator b) {
			super(Type.EQUAL, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			return new Datum(interpreter.getManager().getContrast(a.evaluate(scope, interpreter).getPhone(),
					b.evaluate(scope, interpreter).getPhone()));
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			List<Datum> data = a.evaluate(scope, interpreter).getList();
			List<Phone> phones = new ArrayList<>();
			for (Datum d : data)
				phones.add(d.getPhone());
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			return scope.setVariable(varName, new Datum());
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum condition = a.evaluate(scope, interpreter);
			if (condition.getNumber().compareTo(new BigDecimal(1)) == 0) {
				return b.evaluate(scope, interpreter);
			} else if (c != null) {
				return c.evaluate(scope, interpreter);
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			List<String> pNames = new ArrayList<>();
			List<Boolean> pRefs = new ArrayList<>();
			for (Operator d : ((Sequence) a).getList()) {
				if (d.type == Type.REF_DEC) {
					pRefs.add(true);
					pNames.add(((Ref) d).getKey());
				} else {
					pNames.add(((Variable) d).getKey());
					pRefs.add(false);
				}
			}
			return new Datum(new Function(scope, pNames, pRefs, b));
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			List<Datum> pValues = b.evaluate(scope, interpreter).getList();
			Function f = a.evaluate(scope, interpreter).getFunction();
			return f.execute(pValues, interpreter);
		}

		@Override
		public String toString() {
			return a.toString() + " (" + b.toString() + ")";
		}
	}

	public static class Less extends Binary {
		public Less(Operator a, Operator b) {
			super(Type.LESS, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			return new Datum(
					datumA.getNumber().compareTo(datumB.getNumber()) < 0 ? new BigDecimal(1) : new BigDecimal(0));
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			return new Datum(
					datumA.getNumber().compareTo(datumB.getNumber()) > 0 ? new BigDecimal(1) : new BigDecimal(0));
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			return new Datum(
					datumA.getNumber().compareTo(datumB.getNumber()) <= 0 ? new BigDecimal(1) : new BigDecimal(0));
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			return new Datum(
					datumA.getNumber().compareTo(datumB.getNumber()) >= 0 ? new BigDecimal(1) : new BigDecimal(0));
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			return new Datum(datumA.getNumber().compareTo(BigDecimal.valueOf(1)) == 0
					&& datumB.getNumber().compareTo(BigDecimal.valueOf(1)) == 0 ? new BigDecimal(1)
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Datum datumA = a.evaluate(scope, interpreter);
			Datum datumB = b.evaluate(scope, interpreter);
			return new Datum(datumA.getNumber().compareTo(BigDecimal.valueOf(1)) == 0
					|| datumB.getNumber().compareTo(BigDecimal.valueOf(1)) == 0 ? new BigDecimal(1)
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
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			Function f = ((Execute) b).getA().evaluate(scope, interpreter).getFunction();
			List<Datum> params = ((Execute) b).getB().evaluate(scope, interpreter).getList();
			params.add(0, a.evaluate(scope, interpreter));
			return f.execute(params, interpreter);
		}

		@Override
		public String toString() {
			return a.toString() + " . " + b.toString();
		}
	}

	public static class OuterCall extends Binary {
		public OuterCall(Operator a, Operator b) {
			super(Type.OUTER_CALL, a, b);
		}

		@Override
		public Datum evaluate(Scope scope, Interpreter interpreter) {
			return interpreter.getCommandManager().execute(a.evaluate(scope, interpreter).getString(),
					b.evaluate(scope, interpreter));
		}

		@Override
		public String toString() {
			return a.toString() + " _OUTER_CALL_ " + b.toString();
		}
	}

	public Operator(Type type) {
		this.type = type;
	}

	public abstract Datum evaluate(Scope scope, Interpreter interpreter);

	public abstract String toString();
}