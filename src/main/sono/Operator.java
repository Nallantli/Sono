package main.sono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.phl.Matrix;
import main.phl.Feature;
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
	protected Token line;

	private abstract static class Unary extends Operator {
		protected Operator a;

		public Unary(final Interpreter i, final Type type, final Token line, final Operator a) {
			super(i, type, line);
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

		public Binary(final Interpreter i, final Type type, final Token line, final Operator a, final Operator b) {
			super(i, type, line, a);
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

		public Sequence(final Interpreter i, final Type type, final Token line, final Operator[] operators) {
			super(i, type, line);
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

		public Casting(final Interpreter i, final Type type, final Token line, final int varName) {
			super(i, type, line);
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
		public Variable(final Interpreter interpreter, final Token line, final int varName) {
			super(interpreter, Type.VARIABLE, line, varName);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return scope.getVariable(varName, interpreter, line);
		}

		@Override
		public String toString() {
			return interpreter.deHash(getKey());
		}
	}

	public static class Container extends Operator {
		private final Datum datum;

		public Container(final Interpreter interpreter, final Token line, final Datum datum) {
			super(interpreter, Type.DATUM, line);
			this.datum = datum;
		}

		public Datum getDatum() {
			return datum;
		}

		@Override
		public Datum evaluate(final Scope scope) {
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
		public Set(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.SET, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			datumA.set(interpreter.getManager(), datumB, line);
			return datumA;
		}

		@Override
		public String toString() {
			return a.toString() + " = " + b.toString();
		}
	}

	public static class Transform extends Binary {
		public Transform(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.TRANSFORM, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {

			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			switch (datumB.getType()) {
				case MATRIX:
					final Phone ret = datumA.getPhone(line).transform(datumB.getMatrix(line), true);
					if (ret == null)
						return new Datum();
					return new Datum(ret);
				case RULE:
					final Word result = datumB.getRule(line).transform(interpreter.getManager(), datumA.getWord(line));
					return new Datum(result);
				default:
					throw new SonoRuntimeException("Cannot transform value <" + datumA.getDebugString(line)
							+ "> with value <" + datumB.getDebugString(line) + ">", line);
			}
		}

		@Override
		public String toString() {
			return a.toString() + " >> " + b.toString();
		}
	}

	public static class SoftList extends Sequence {
		public SoftList(final Interpreter interpreter, final Token line, final Operator[] operators) {
			super(interpreter, Type.SOFT_LIST, line, operators);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			Datum[] data = null;
			if (Interpreter.containsInstance(operators, RangeUntil.class)) {
				final List<Datum> list = new ArrayList<>();
				for (final Operator o : operators) {
					if (o.type == Type.RANGE_UNTIL)
						list.addAll(((RangeUntil) o).getRange(scope));
					else {
						final Datum d = o.evaluate(scope);
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
					final Datum d = o.evaluate(scope);
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
		public HardList(final Interpreter interpreter, final Token line, final Operator[] operators) {
			super(interpreter, Type.HARD_LIST, line, operators);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			Datum[] data = null;
			final Scope newScope = new Scope(scope.getStructure(), scope);
			if (Interpreter.containsInstance(operators, RangeUntil.class)) {
				final List<Datum> list = new ArrayList<>();
				for (final Operator o : operators) {
					if (o.type == Type.RANGE_UNTIL)
						list.addAll(((RangeUntil) o).getRange(newScope));
					else {
						final Datum d = o.evaluate(newScope);
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
					final Datum d = o.evaluate(newScope);
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
		public MatrixDec(final Interpreter interpreter, final Token line, final Operator[] operators) {
			super(interpreter, Type.MATRIX_DEC, line, operators);
		}

		@Override
		public Datum evaluate(final Scope scope) {

			final Matrix matrix = new Matrix(interpreter.getManager());
			for (final Operator o : operators) {
				final Feature p = o.evaluate(scope).getFeature(line);
				matrix.put(p.getKey(), p.getQuality());
			}
			return new Datum(matrix);
		}

		@Override
		public String toString() {
			return Interpreter.stringFromList(operators, "[", "]");
		}
	}

	public static class RangeUntil extends Binary {
		public RangeUntil(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.RANGE_UNTIL, line, a, b);
		}

		public List<Datum> getRange(final Scope scope) {
			final int datumA = (int) a.evaluate(scope).getNumber(line);
			final int datumB = (int) b.evaluate(scope).getNumber(line);
			final List<Datum> data = new ArrayList<>();
			for (int i = datumA; i < datumB; i++)
				data.add(new Datum(i));
			return data;
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " until " + b.toString();
		}
	}

	public static class Arrow extends Binary {
		public Arrow(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.ARROW, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " -> " + b.toString();
		}
	}

	public static class Ref extends Casting {
		public Ref(final Interpreter interpreter, final Token line, final int varName) {
			super(interpreter, Type.REF_DEC, line, varName);
			this.varName = varName;
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return null;
		}

		@Override
		public String toString() {
			return "ref " + interpreter.deHash(varName);
		}
	}

	public static class Final extends Casting {
		public Final(final Interpreter interpreter, final Token line, final int varName) {
			super(interpreter, Type.FINAL, line, varName);
			this.varName = varName;
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return null;
		}

		@Override
		public String toString() {
			return "final " + interpreter.deHash(varName);
		}
	}

	public static class FeatDec extends Unary {
		public FeatDec(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.FEAT_DEC, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {

			final Datum datumA = a.evaluate(scope);
			return new Datum(interpreter.getManager().interpretFeature(datumA.getString(line)));
		}

		@Override
		public String toString() {
			return "feat " + a.toString();
		}
	}

	public static class Allocate extends Unary {
		public Allocate(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.ALLOC, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			if (datumA.type == Datum.Type.NUMBER) {
				final double dn = datumA.getNumber(line);
				final Datum[] data = new Datum[(int) dn];
				for (int i = 0; i < dn; i++)
					data[i] = new Datum();
				return new Datum(data);
			} else if (datumA.type == Datum.Type.VECTOR) {
				final Datum[] dv = datumA.getVector(line);
				final Datum[] data = new Datum[(int) dv[0].getNumber(line)];
				for (int i = 0; i < data.length; i++)
					data[i] = new Datum();
				Datum[] curr = data;
				for (int j = 1; j < dv.length; j++) {
					final int size = (int) dv[j].getNumber(line);
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

	public static class Throw extends Unary {
		public Throw(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.THROW, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			throw new SonoRuntimeException(datumA.getString(line), line);
		}

		@Override
		public String toString() {
			return "throw " + a.toString();
		}
	}

	public static class TryCatch extends Unary {
		private Operator b;

		public TryCatch(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.TRY_CATCH, line, a);
			this.b = null;
		}

		public void setCatch(final Operator b) {
			this.b = b;
		}

		@Override
		public Datum evaluate(final Scope scope) {
			try {
				return a.evaluate(scope);
			} catch (final SonoRuntimeException e) {
				if (b != null) {
					final Scope catchScope = new Scope(scope.getStructure(), scope);
					catchScope.setVariable(interpreter, interpreter.ERROR, new Datum(e.getMessage()), line);
					/*
					 * final Datum[] list = new Datum[line.size()]; for (int i = 0; i < line.size();
					 * i++) list[i] = new Datum(line.get(line.size() - i - 1));
					 * catchScope.setVariable(interpreter, interpreter.TRACE, new Datum(list));
					 */
					return b.evaluate(catchScope);
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
		public Slash(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.SLASH, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " // " + b.toString();
		}
	}

	public static class Underscore extends Binary {
		public Underscore(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.UNDERSCORE, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " .. " + b.toString();
		}
	}

	public static class Iterator extends Binary {
		public Iterator(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.ITERATOR, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " in " + b.toString();
		}
	}

	public static class StructDec extends Casting {
		public StructDec(final Interpreter interpreter, final Token line, final int varName) {
			super(interpreter, Type.STRUCT_DEC, line, varName);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return null;
		}

		@Override
		public String toString() {
			return "struct " + interpreter.deHash(varName);
		}
	}

	public static class AbstractDec extends Casting {
		public AbstractDec(final Interpreter interpreter, final Token line, final int varName) {
			super(interpreter, Type.ABSTRACT_DEC, line, varName);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return null;
		}

		@Override
		public String toString() {
			return "abstract " + interpreter.deHash(varName);
		}
	}

	public static class StaticDec extends Casting {
		public StaticDec(final Interpreter interpreter, final Token line, final int varName) {
			super(interpreter, Type.STATIC_DEC, line, varName);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return null;
		}

		@Override
		public String toString() {
			return "static " + interpreter.deHash(varName);
		}
	}

	public static class Extends extends Binary {
		public Extends(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.EXTENDS, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " extends " + b.toString();
		}
	}

	public static class TypeDec extends Binary {
		public TypeDec(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.TYPE_DEC, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return null;
		}

		@Override
		public String toString() {
			return a.toString() + " :: " + b.toString();
		}
	}

	public static class Loop extends Binary {
		public Loop(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.LOOP, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			if (a.type == Type.ITERATOR) {
				final Datum datumAB = ((Iterator) a).getB().evaluate(scope);
				final int valuesSize = datumAB.getVectorLength(line);
				final int variable = ((Variable) ((Iterator) a).getA()).getKey();
				for (int i = 0; i < valuesSize; i++) {
					final Scope loopScope = new Scope(scope.getStructure(), scope);
					loopScope.setVariable(interpreter, variable, datumAB.indexVector(i), line);
					final Datum result = b.evaluate(loopScope);
					if (result.getType() == Datum.Type.I_BREAK)
						break;
					if (result.getRet() || result.getRefer())
						return result;
				}
			} else {
				while (a.evaluate(scope).getBool(line)) {
					final Scope loopScope = new Scope(scope.getStructure(), scope);
					final Datum result = b.evaluate(loopScope);
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

		public RuleDec(final Interpreter interpreter, final Token line, final Rule.Type ruleType, final Operator a) {
			super(interpreter, Type.RULE_DEC, line, a);
			this.ruleType = ruleType;
		}

		@Override
		public Datum evaluate(final Scope scope) {

			while (a.type != Type.SLASH)
				a = ((Sequence) a).operators[0];
			final Datum dSearch = ((Binary) ((Binary) a).getA()).getA().evaluate(scope);
			final Datum rawTrans = ((Binary) ((Binary) a).getA()).getB().evaluate(scope);
			final Datum rawInit = ((Binary) ((Binary) a).getB()).getA().evaluate(scope);
			final Datum rawFin = ((Binary) ((Binary) a).getB()).getB().evaluate(scope);

			Object search = null;
			Datum[] dTrans;
			Datum[] dInit;
			Datum[] dFin;
			final List<Object> trans = new ArrayList<>();
			final List<Object> init = new ArrayList<>();
			final List<Object> fin = new ArrayList<>();

			if (dSearch.getType() == Datum.Type.MATRIX)
				search = dSearch.getMatrix(line);
			else if (dSearch.getType() == Datum.Type.PHONE)
				search = dSearch.getPhone(line);

			if (rawTrans.getType() == Datum.Type.VECTOR)
				dTrans = rawTrans.getVector(line);
			else {
				dTrans = new Datum[1];
				dTrans[0] = rawTrans;
			}

			for (final Datum d : dTrans) {
				switch (d.type) {
					case PHONE:
						trans.add(d.getPhone(line));
						break;
					case MATRIX:
						trans.add(d.getMatrix(line));
						break;
					case NULL:
						break;
					default:
						throw new SonoRuntimeException(
								"Value <" + d.getDebugString(line) + "> cannot be used in a Rule declaration.", line);
				}
			}

			if (rawInit.getType() == Datum.Type.VECTOR)
				dInit = rawInit.getVector(line);
			else {
				dInit = new Datum[1];
				dInit[0] = rawInit;
			}

			for (final Datum d : dInit) {
				switch (d.type) {
					case PHONE:
						init.add(d.getPhone(line));
						break;
					case MATRIX:
						init.add(d.getMatrix(line));
						break;
					case STRING:
						switch (d.getString(line)) {
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
								"Value <" + d.getDebugString(line) + "> cannot be used in a Rule declaration.", line);
				}
			}

			if (rawFin.getType() == Datum.Type.VECTOR)
				dFin = rawFin.getVector(line);
			else {
				dFin = new Datum[1];
				dFin[0] = rawFin;
			}

			for (final Datum d : dFin) {
				switch (d.type) {
					case PHONE:
						fin.add(d.getPhone(line));
						break;
					case MATRIX:
						fin.add(d.getMatrix(line));
						break;
					case STRING:
						switch (d.getString(line)) {
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
								"Value <" + d.getDebugString(line) + "> cannot be used in a Rule declaration.", line);
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
		public Register(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.REGISTER, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {

			final String segment = a.evaluate(scope).getString(line);
			final Matrix features = b.evaluate(scope).getMatrix(line);
			final Phone ret = interpreter.getManager().registerNewPhone(segment, features);
			if (ret == null)
				throw new SonoRuntimeException("Cannot register Phone with features <" + features + ">", line);
			final Datum allV = interpreter.getScope().getVariable(interpreter.ALL, interpreter, line);
			final int oldVSize = interpreter.getScope().getVariable(interpreter.ALL, interpreter, line)
					.getVectorLength(line);
			final Datum[] newV = new Datum[oldVSize + 1];
			for (int i = 0; i < oldVSize; i++)
				newV[i] = allV.indexVector(i);
			newV[oldVSize] = new Datum(ret);
			interpreter.getScope().setVariable(interpreter, interpreter.ALL, new Datum(newV), line);
			return new Datum(ret);
		}

		@Override
		public String toString() {
			return a.toString() + " register " + b.toString();
		}
	}

	public static class SeqDec extends Unary {
		public SeqDec(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.SEQ_DEC, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {

			final Datum datumA = a.evaluate(scope);
			if (datumA.getType() == Datum.Type.VECTOR) {
				final List<Phone> phones = new ArrayList<>();
				final List<Word.SyllableDelim> delimits = new ArrayList<>();
				boolean flag = true;
				final int dataSize = datumA.getVectorLength(line);
				for (int i = 0; i < dataSize; i++) {
					final Datum d = datumA.indexVector(i);
					if (d.getType() == Datum.Type.PHONE) {
						phones.add(d.getPhone(line));
						if (flag)
							delimits.add(Word.SyllableDelim.NULL);
						else
							flag = true;
					} else if (d.getType() == Datum.Type.STRING) {
						flag = false;
						switch (d.getString(line)) {
							case ".":
								delimits.add(Word.SyllableDelim.DELIM);
								break;
							case "+":
								delimits.add(Word.SyllableDelim.MORPHEME);
								break;
							default:
								throw new SonoRuntimeException(
										"Value <" + d.getDebugString(line) + "> is not applicable as a word delimiter",
										line);
						}
					}
				}
				return new Datum(new Word(phones, delimits));
			} else if (datumA.getType() == Datum.Type.STRING) {
				return new Datum(interpreter.getManager().interpretSequence(datumA.getString(line)));
			}
			throw new SonoRuntimeException("Value <" + datumA.getDebugString(line) + "> cannot be converted to a Word.",
					line);
		}

		@Override
		public String toString() {
			return "word " + a.toString();
		}
	}

	public static class ListDec extends Unary {
		public ListDec(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.LIST_DEC, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			Datum[] list = null;
			final Datum datumA = a.evaluate(scope);
			int i;
			switch (datumA.getType()) {
				case MATRIX:
					list = new Datum[datumA.getMatrix(line).size()];
					i = 0;
					for (final Feature p : datumA.getMatrix(line))
						list[i++] = new Datum(p);
					break;
				case STRING:
					final String s = datumA.getString(line);
					list = new Datum[s.length()];
					for (i = 0; i < s.length(); i++)
						list[i] = new Datum(String.valueOf(s.charAt(i)));
					break;
				case WORD:
					final List<Datum> tempList = new ArrayList<>();
					for (i = 0; i < datumA.getWord(line).size(); i++) {
						if (datumA.getWord(line).getDelim(i) != Word.SyllableDelim.NULL)
							tempList.add(new Datum(datumA.getWord(line).getDelim(i).toString()));
						tempList.add(new Datum(datumA.getWord(line).get(i)));
					}
					list = tempList.toArray(new Datum[0]);
					break;
				case VECTOR:
					return datumA;
				case STRUCTURE:
					return datumA.getStructure(line).getScope().getVariable(interpreter.GET_LIST, interpreter, line)
							.getFunction(Datum.Type.ANY, line).execute(null, line);
				default:
					throw new SonoRuntimeException(
							"Cannot convert value <" + datumA.getDebugString(line) + "> into a List", line);
			}
			return new Datum(list);
		}

		@Override
		public String toString() {
			return "vec " + a.toString();
		}
	}

	public static class StringDec extends Unary {
		public StringDec(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.STR_DEC, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final String s = a.evaluate(scope).toRawStringTrace(line);
			return new Datum(s);
		}

		@Override
		public String toString() {
			return "str " + a.toString();
		}
	}

	public static class TypeConvert extends Unary {
		public TypeConvert(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.TYPE_CONVERT, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			return new Datum(datumA.getTypeString());
		}

		@Override
		public String toString() {
			return "type " + a.toString();
		}
	}

	public static class Hash extends Unary {
		public Hash(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.HASH, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			return new Datum(datumA.hashCode());
		}

		@Override
		public String toString() {
			return "type " + a.toString();
		}
	}

	public static class MatConvert extends Unary {
		public MatConvert(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.MATRIX_CONVERT, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {

			final Datum datumA = a.evaluate(scope);
			if (datumA.getType() == Datum.Type.VECTOR) {
				final int listSize = datumA.getVectorLength(line);
				final Matrix m = new Matrix(interpreter.getManager());
				for (int i = 0; i < listSize; i++) {
					final Feature p = datumA.indexVector(i).getFeature(line);
					m.put(p.getKey(), p.getQuality());
				}
				return new Datum(m);
			} else if (datumA.getType() == Datum.Type.PHONE) {
				return new Datum(datumA.getPhone(line).getMatrix());
			}
			throw new SonoRuntimeException("Cannot convert value <" + datumA.getDebugString(line) + "> to a Matrix.",
					line);
		}

		@Override
		public String toString() {
			return "mat " + a.toString();
		}
	}

	public static class Find extends Binary {
		public Find(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.FIND_DEC, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {

			final Matrix matrix = a.evaluate(scope).getMatrix(line);
			final Datum datumB = b.evaluate(scope);
			final int dataSize = datumB.getVectorLength(line);
			final List<Phone> phones = new ArrayList<>();
			for (int i = 0; i < dataSize; i++)
				phones.add(datumB.indexVector(i).getPhone(line));
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
		public NumConvert(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.NUMBER_CONVERT, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			if (datumA.getType() == Datum.Type.NUMBER) {
				return datumA;
			} else if (datumA.getType() == Datum.Type.STRING) {
				try {
					return new Datum((double) Double.valueOf(datumA.getString(line)));
				} catch (final Exception e) {
					return new Datum();
				}
			} else {
				throw new SonoRuntimeException(
						"Cannot convert value <" + datumA.getDebugString(line) + "> to a Number.", line);
			}
		}

		@Override
		public String toString() {
			return "num " + a.toString();
		}
	}

	public static class Code extends Unary {
		public Code(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.CODE, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final String s = a.evaluate(scope).getString(line);
			if (s.length() != 1)
				throw new SonoRuntimeException("Value <" + s + "> is not a single char.", line);
			return new Datum((int) s.charAt(0));
		}

		@Override
		public String toString() {
			return "code " + a.toString();
		}
	}

	public static class Char extends Unary {
		public Char(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.CHAR, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			try {
				final int i = (int) datumA.getNumber(line);
				return new Datum(String.valueOf((char) i));
			} catch (final Exception e) {
				throw new SonoRuntimeException("Value <" + datumA.getDebugString(line) + "> is not of type `Number`",
						line);
			}
		}

		@Override
		public String toString() {
			return "code " + a.toString();
		}
	}

	public static class Length extends Unary {
		public Length(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.LEN, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			switch (datumA.getType()) {
				case STRING:
					return new Datum(datumA.getString(line).length());
				case WORD:
					return new Datum(datumA.getWord(line).size());
				case VECTOR:
					return new Datum(datumA.getVectorLength(line));
				case MATRIX:
					return new Datum(datumA.getMatrix(line).size());
				case STRUCTURE:
					return datumA.getStructure(line).getScope().getVariable(interpreter.GET_LEN, interpreter, line)
							.getFunction(Datum.Type.ANY, line).execute(null, line);
				default:
					throw new SonoRuntimeException("Cannot get length of value <" + datumA.getDebugString(line) + ">",
							line);
			}
		}

		@Override
		public String toString() {
			return "len " + a.toString();
		}
	}

	public static class Return extends Unary {
		public Return(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.RETURN, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			datumA.setRet(true);
			return datumA;
		}

		@Override
		public String toString() {
			return "return " + a.toString();
		}
	}

	public static class Refer extends Unary {
		public Refer(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.REFER, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			datumA.setRefer(true);
			return datumA;
		}

		@Override
		public String toString() {
			return "refer " + a.toString();
		}
	}

	public static class Add extends Binary {
		public Add(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.ADD, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			if (datumA.getType() != datumB.getType())
				throw new SonoRuntimeException("Cannot add values <" + datumA.getDebugString(line) + "> and <"
						+ datumB.getDebugString(line) + ">", line);
			switch (datumA.getType()) {
				case NUMBER:
					return new Datum(datumA.getNumber(line) + datumB.getNumber(line));
				case VECTOR:
					return Datum.arrayConcat(datumA, datumB);
				case MATRIX:
					final Matrix newMatrix = new Matrix(interpreter.getManager());
					newMatrix.putAll(datumA.getMatrix(line));
					newMatrix.putAll(datumB.getMatrix(line));
					return new Datum(newMatrix);
				case WORD:
					final Word newWord = new Word();
					newWord.addAll(datumA.getWord(line));
					newWord.addAll(datumB.getWord(line));
					return new Datum(newWord);
				case STRING:
					return new Datum(datumA.getString(line) + datumB.getString(line));
				default:
					throw new SonoRuntimeException("Values of type <" + datumA.getType() + "> cannot be added.", line);
			}
		}

		@Override
		public String toString() {
			return a.toString() + " + " + b.toString();
		}
	}

	public static class Sub extends Binary {
		public Sub(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.SUB, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			return new Datum(datumA.getNumber(line) - datumB.getNumber(line));
		}

		@Override
		public String toString() {
			return a.toString() + " - " + b.toString();
		}
	}

	public static class Mul extends Binary {
		public Mul(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.MUL, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			return new Datum(datumA.getNumber(line) * datumB.getNumber(line));
		}

		@Override
		public String toString() {
			return a.toString() + " * " + b.toString();
		}
	}

	public static class Div extends Binary {
		public Div(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.DIV, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			try {
				return new Datum(datumA.getNumber(line) / datumB.getNumber(line));
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
		public Mod(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.MOD, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			try {
				return new Datum(datumA.getNumber(line) % datumB.getNumber(line));
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
		public Pow(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.POW, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			try {
				return new Datum(Math.pow(datumA.getNumber(line), datumB.getNumber(line)));
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
		public Index(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.INDEX, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			if (datumA.getType() == Datum.Type.VECTOR) {
				try {
					return datumA.indexVector((int) datumB.getNumber(line));
				} catch (final Exception e) {
					throw new SonoRuntimeException(
							"Cannot index List <" + datumA.getDebugString(line) + "> with value <"
									+ datumB.getDebugString(line) + ">; Length: " + datumA.getVectorLength(line),
							line);
				}
			} else if (datumA.getType() == Datum.Type.STRUCTURE) {
				return datumA.getStructure(line).getScope().getVariable(interpreter.GET_INDEX, interpreter, line)
						.getFunction(Datum.Type.ANY, line).execute(new Datum[] { datumB }, line);
			}
			throw new SonoRuntimeException("Cannot index value <" + datumA.getDebugString(line) + ">", line);
		}

		@Override
		public String toString() {
			return a.toString() + "[" + b.toString() + "]";
		}
	}

	public static class Equal extends Binary {
		public Equal(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.EQUAL, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			return new Datum(datumA.isEqual(datumB, line));
		}

		@Override
		public String toString() {
			return a.toString() + " == " + b.toString();
		}
	}

	public static class PureEqual extends Binary {
		public PureEqual(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.P_EQUALS, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			return new Datum(datumA.isEqualPure(datumB, line));
		}

		@Override
		public String toString() {
			return a.toString() + " === " + b.toString();
		}
	}

	public static class NEqual extends Binary {
		public NEqual(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.NOT_EQUAL, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			return new Datum(!datumA.isEqual(datumB, line));
		}

		@Override
		public String toString() {
			return a.toString() + " != " + b.toString();
		}
	}

	public static class PureNEqual extends Binary {
		public PureNEqual(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.P_NOT_EQUAL, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			return new Datum(!datumA.isEqualPure(datumB, line));
		}

		@Override
		public String toString() {
			return a.toString() + " !== " + b.toString();
		}
	}

	public static class Contrast extends Binary {
		public Contrast(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.CONTRAST, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {

			return new Datum(interpreter.getManager().getContrast(a.evaluate(scope).getPhone(line),
					b.evaluate(scope).getPhone(line)));
		}

		@Override
		public String toString() {
			return a.toString() + " ?> " + b.toString();
		}
	}

	public static class Common extends Unary {
		public Common(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.COMMON, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {

			final Datum datumA = a.evaluate(scope);
			final int dataSize = datumA.getVectorLength(line);
			final List<Phone> phones = new ArrayList<>();
			for (int i = 0; i < dataSize; i++)
				phones.add(datumA.indexVector(i).getPhone(line));
			return new Datum(interpreter.getManager().getCommon(phones));
		}

		@Override
		public String toString() {
			return "com " + a.toString();
		}

	}

	public static class VarDec extends Operator {
		private final int varName;

		public VarDec(final Interpreter interpreter, final Token line, final int varName) {
			super(interpreter, Type.VAR_DEC, line);
			this.varName = varName;
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return scope.setVariable(interpreter, varName, null, line);
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
		public Break(final Interpreter interpreter, final Token line) {
			super(interpreter, Type.BREAK, line);
		}

		@Override
		public Datum evaluate(final Scope scope) {
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

		public IfElse(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.IF_ELSE, line, a, b);
			this.c = null;
		}

		public void setElse(final Operator c) {
			this.c = c;
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum condition = a.evaluate(scope);
			if (condition.getBool(line))
				return b.evaluate(scope);
			else if (c != null)
				return c.evaluate(scope);
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
		public Lambda(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.LAMBDA, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
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
				final Datum t = ((TypeDec) a).getA().evaluate(scope);
				if (!t.isPrototypic())
					throw new SonoRuntimeException(
							"Value <" + t.getDebugString(line) + "> cannot be used to designate an objective function.",
							line);
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
		public Execute(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.EXECUTE, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumB = b.evaluate(scope);
			final int pValuesSize = datumB.getVectorLength(line);
			Datum[] pValues = null;
			Function f = null;
			if (a.type == Type.INNER) {
				final Datum datumA = ((Inner) a).getA().evaluate(scope);
				if (datumA.getType() != Datum.Type.STRUCTURE) {
					final Datum[] tempValues = new Datum[pValuesSize + 1];
					tempValues[0] = datumA;
					for (int i = 0; i < pValuesSize; i++)
						tempValues[i + 1] = datumB.indexVector(i);
					pValues = tempValues;
					final Datum functionB = ((Inner) a).getB().evaluate(scope);
					f = functionB.getFunction(datumA.getType(), line);
					if (f == null)
						f = functionB.getFunction(Datum.Type.ANY, line);
				} else {
					pValues = datumB.getVector(line);
					f = a.evaluate(scope).getFunction(Datum.Type.ANY, line);
				}
			} else {
				pValues = datumB.getVector(line);
				final Datum fDatum = a.evaluate(scope);
				if (pValuesSize != 0)
					f = fDatum.getFunction(datumB.indexVector(0).getType(), line);
				if (f == null)
					f = fDatum.getFunction(Datum.Type.ANY, line);
			}
			if (f == null)
				throw new SonoRuntimeException("No function found", line);
			return f.execute(pValues, line);
		}

		@Override
		public String toString() {
			return a.toString() + Interpreter.stringFromList(((Sequence) b).getVector(), "(", ")");
		}
	}

	public static class Less extends Binary {
		public Less(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.LESS, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			return new Datum(datumA.getNumber(line) < datumB.getNumber(line));
		}

		@Override
		public String toString() {
			return a.toString() + " < " + b.toString();
		}
	}

	public static class More extends Binary {
		public More(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.LESS, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			return new Datum(datumA.getNumber(line) > datumB.getNumber(line));
		}

		@Override
		public String toString() {
			return a.toString() + " > " + b.toString();
		}
	}

	public static class ELess extends Binary {
		public ELess(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.E_LESS, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			return new Datum(datumA.getNumber(line) <= datumB.getNumber(line));
		}

		@Override
		public String toString() {
			return a.toString() + " <= " + b.toString();
		}
	}

	public static class EMore extends Binary {
		public EMore(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.E_MORE, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			final Datum datumB = b.evaluate(scope);
			return new Datum(datumA.getNumber(line) >= datumB.getNumber(line));
		}

		@Override
		public String toString() {
			return a.toString() + " >= " + b.toString();
		}
	}

	public static class And extends Binary {
		public And(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.AND, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			if (datumA.getBool(line)) {
				final Datum datumB = b.evaluate(scope);
				if (datumB.getBool(line))
					return new Datum(true);
			}
			return new Datum(false);
		}

		@Override
		public String toString() {
			return a.toString() + " && " + b.toString();
		}
	}

	public static class Or extends Binary {
		public Or(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.OR, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum datumA = a.evaluate(scope);
			if (datumA.getBool(line)) {
				return new Datum(true);
			} else {
				final Datum datumB = b.evaluate(scope);
				if (datumB.getBool(line))
					return new Datum(true);
			}
			return new Datum(false);
		}

		@Override
		public String toString() {
			return a.toString() + " || " + b.toString();
		}
	}

	public static class Inner extends Binary {
		public Inner(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.INNER, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Datum object = a.evaluate(scope);
			if (object.type != Datum.Type.STRUCTURE) {
				if (!object.isPrototypic())
					throw new SonoRuntimeException("Value <" + object.getDebugString(line)
							+ "> is not prototypic and therefore cannot extract objective methods.", line);
				final Datum fHolder = b.evaluate(scope);
				return new Datum(object.getType(), fHolder.getFunction(object.getType(), line));
			} else {
				final Structure s = object.getStructure(line);
				if (s.perusable())
					return b.evaluate(object.getStructure(line).getScope());
				throw new SonoRuntimeException("Class <" + s.getName() + "> is not perusable.", line);
			}
		}

		@Override
		public String toString() {
			return a.toString() + "." + b.toString();
		}
	}

	public static class OuterCall extends Binary {
		public OuterCall(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.OUTER_CALL, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			return interpreter.getCommandManager().execute(a.evaluate(scope).getString(line), b.evaluate(scope), line,
					interpreter);
		}

		@Override
		public String toString() {
			return a.toString() + " _OUTER_CALL_ " + b.toString();
		}
	}

	public static class ClassDec extends Binary {
		public ClassDec(final Interpreter interpreter, final Token line, final Operator a, final Operator b) {
			super(interpreter, Type.CLASS_DEC, line, a, b);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			Structure.Type stype = Structure.Type.STRUCT;
			Operator objectOperator = null;
			Structure extending = null;
			if (a.type == Type.EXTENDS) {
				objectOperator = ((Extends) a).getA();
				extending = ((Extends) a).getB().evaluate(scope).getStructure(line);
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
				main = new SoftList(interpreter, line, new Operator[] { extending.getMain(), main });
			final Structure structure = new Structure(scope.getStructure(), stype, scope, main, varName, interpreter);
			if (stype == Structure.Type.STATIC)
				b.evaluate(structure.getScope());
			return scope.setVariable(interpreter, varName, new Datum(structure), line);
		}

		@Override
		public String toString() {
			return a.toString() + " class " + b.toString();
		}
	}

	public static class NewDec extends Unary {
		public NewDec(final Interpreter interpreter, final Token line, final Operator a) {
			super(interpreter, Type.NEW_DEC, line, a);
		}

		@Override
		public Datum evaluate(final Scope scope) {
			final Structure struct = ((Execute) a).getA().evaluate(scope).getStructure(line);
			final Datum[] params = ((Execute) a).getB().evaluate(scope).getVector(line);
			return struct.instantiate(params, line);
		}

		@Override
		public String toString() {
			return "new " + a.toString();
		}
	}

	public static class Switch extends Unary {
		private final Map<Datum, Operator> map;
		private Operator c;

		public Switch(final Interpreter interpreter, final Token line, final Operator a,
				final Map<Datum, Operator> map) {
			super(interpreter, Type.SWITCH, line, a);
			this.map = map;
			this.c = null;
		}

		public void setElse(final Operator c) {
			this.c = c;
		}

		@Override
		public Datum evaluate(final Scope scope) {

			final Datum key = a.evaluate(scope);
			final Operator b = map.get(key);
			if (b == null) {
				if (c == null)
					return new Datum();
				else
					return c.evaluate(scope);
			}
			return map.get(key).evaluate(scope);
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

		public SwitchCase(final Interpreter interpreter, final Token line, final Datum key, final Operator seq) {
			super(interpreter, Type.SWITCH_CASE, line);
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
		public Datum evaluate(final Scope scope) {
			throw new SonoRuntimeException("Cannot evaluate uncontrolled goto statement", line);
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

	public Operator(final Interpreter interpreter, final Type type, final Token line) {
		this.interpreter = interpreter;
		this.type = type;
		this.line = line;
	}

	public abstract Operator[] getChildren();

	public abstract void condense();

	public abstract Datum evaluate(Scope scope);

	public abstract String toString();
}