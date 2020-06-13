package src.sono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import src.phl.*;
import src.sono.err.SonoRuntimeException;

public class Datum {
	public enum Type {
		NULL, LIST, STRING, PHONE, PAIR, MATRIX, RULE, NUMBER, FUNCTION, WORD,

		// INTERPRETER USE
		I_BREAK
	}

	private List<Datum> valueList;
	private String valueString;
	private Phone valuePhone;
	private Pair valuePair;
	private Matrix valueMatrix;
	private Rule valueRule;
	private BigDecimal valueNumber;
	private Function valueFunction;
	private Word valueWord;

	protected Type type;

	private boolean ret = false;
	private boolean mutable = true;

	public Datum() {
		this.type = Type.NULL;
	}

	public Datum(List<Datum> valueList) {
		this.type = Type.LIST;
		this.valueList = valueList;
	}

	public Datum(String valueString) {
		this.type = Type.STRING;
		this.valueString = valueString;
	}

	public Datum(Phone valuePhone) {
		this.type = Type.PHONE;
		this.valuePhone = valuePhone;
	}

	public Datum(Pair valuePair) {
		this.type = Type.PAIR;
		this.valuePair = valuePair;
	}

	public Datum(Matrix valueMatrix) {
		this.type = Type.MATRIX;
		this.valueMatrix = valueMatrix;
	}

	public Datum(Rule valueRule) {
		this.type = Type.RULE;
		this.valueRule = valueRule;
	}

	public Datum(BigDecimal valueNumber) {
		this.type = Type.NUMBER;
		this.valueNumber = valueNumber;
	}

	public Datum(Function valueFunction) {
		this.type = Type.FUNCTION;
		this.valueFunction = valueFunction;
	}

	public Datum(Word valueWord) {
		this.type = Type.WORD;
		this.valueWord = valueWord;
	}

	public static class Break extends Datum {
		public Break() {
			this.type = Type.I_BREAK;
		}
	}

	public Type getType() {
		return this.type;
	}

	public Phone getPhone() {
		if (type != Type.PHONE)
			throw new SonoRuntimeException("Value <" + this + "> is not a Phone.");
		return this.valuePhone;
	}

	public Matrix getMatrix() {
		if (type != Type.MATRIX)
			throw new SonoRuntimeException("Value <" + this + "> is not a Matrix.");
		return this.valueMatrix;
	}

	public Rule getRule() {
		if (type != Type.RULE)
			throw new SonoRuntimeException("Value <" + this + "> is not a Rule.");
		return this.valueRule;
	}

	public String getString() {
		if (type != Type.STRING)
			throw new SonoRuntimeException("Value <" + this + "> is not a String.");
		return this.valueString;
	}

	public BigDecimal getNumber() {
		if (type != Type.NUMBER)
			throw new SonoRuntimeException("Value <" + this + "> is not a Number.");
		return this.valueNumber;
	}

	public Function getFunction() {
		if (type != Type.FUNCTION)
			throw new SonoRuntimeException("Value <" + this + "> is not a Function.");
		return this.valueFunction;
	}

	public Word getWord() {
		if (type != Type.WORD)
			throw new SonoRuntimeException("Value <" + this + "> is not a Word.");
		return this.valueWord;
	}

	public void setMutable(boolean mutable) {
		this.mutable = mutable;
	}

	public void set(Datum datum) {
		if (!mutable)
			throw new SonoRuntimeException("You cannot set the value of a constant.");
		this.type = datum.getType();
		switch (type) {
			case LIST:
				this.valueList = new ArrayList<>();
				for (Datum d : datum.getList()) {
					Datum n = new Datum();
					n.set(d);
					this.valueList.add(n);
				}
				break;
			case MATRIX:
				this.valueMatrix = new Matrix();
				for (Pair p : datum.valueMatrix)
					this.valueMatrix.put(p.getFeature(), p.getQuality());
				break;
			case PAIR:
				Pair temp = datum.getPair();
				this.valuePair = new Pair(temp.getFeature(), temp.getQuality());
				break;
			case PHONE:
				this.valuePhone = datum.getPhone();
				break;
			case RULE:
				this.valueRule = datum.getRule();
				break;
			case STRING:
				this.valueString = datum.getString();
				break;
			case NUMBER:
				this.valueNumber = datum.getNumber();
				break;
			case FUNCTION:
				this.valueFunction = datum.getFunction();
				break;
			case WORD:
				this.valueWord = datum.getWord();
				break;
			case NULL:
				this.valueList = null;
				this.valueMatrix = null;
				this.valuePair = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueNumber = null;
				this.valueFunction = null;
				break;
			default:
				break;
		}
	}

	@Override
	public String toString() {
		switch (type) {
			case LIST:
				return Interpreter.stringFromList(valueList, "{", "}");
			case MATRIX:
				return valueMatrix.toString();
			case NULL:
				return "null";
			case PAIR:
				return valuePair.toString();
			case PHONE:
				return "'" + valuePhone.toString() + "'";
			case RULE:
				return valueRule.toString();
			case STRING:
				return "\"" + valueString + "\"";
			case NUMBER:
				return String.valueOf(valueNumber);
			case FUNCTION:
				return valueFunction.toString();
			case WORD:
				return "`" + valueWord.toString() + "`";
			default:
				throw new SonoRuntimeException("Unknown value.");
		}
	}

	public String toRawString() {
		switch (type) {
			case LIST:
				return Interpreter.stringFromList(valueList, "{", "}");
			case MATRIX:
				return valueMatrix.toString();
			case NULL:
				return "null";
			case PAIR:
				return valuePair.toString();
			case PHONE:
				return valuePhone.toString();
			case RULE:
				return valueRule.toString();
			case STRING:
				return valueString;
			case NUMBER:
				return String.valueOf(valueNumber);
			case FUNCTION:
				return valueFunction.toString();
			case WORD:
				return valueWord.toString();
			default:
				throw new SonoRuntimeException("Unknown value.");
		}
	}

	public Pair getPair() {
		if (type != Type.PAIR)
			throw new SonoRuntimeException("Value <" + this + "> is not a Pair.");
		return valuePair;
	}

	public List<Datum> getList() {
		if (type != Type.LIST)
			throw new SonoRuntimeException("Value <" + this + "> is not a List.");
		return valueList;
	}

	public void setRet(boolean ret) {
		this.ret = ret;
	}

	public boolean getRet() {
		return ret;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;

		Datum d = (Datum) o;
		if (type != d.getType())
			return false;
		switch (type) {
			case LIST:
				if (valueList.size() != d.getList().size())
					return false;
				for (int i = 0; i < valueList.size(); i++)
					if (!valueList.get(i).equals(d.getList().get(i)))
						return false;
				return true;
			case MATRIX:
				return valueMatrix.equals(d.getMatrix());
			case NULL:
				return d.getType() == Type.NULL;
			case NUMBER:
				return valueNumber.compareTo(d.getNumber()) == 0;
			case PAIR:
				return valuePair.equals(d.getPair());
			case PHONE:
				return valuePhone.equals(d.getPhone());
			case RULE:
				return valueRule.equals(d.getRule());
			case FUNCTION:
				return valueFunction == d.getFunction();
			case STRING:
				return valueString.equals(d.getString());
			case WORD:
				return valueWord.equals(d.getWord());
			default:
				return false;
		}
	}
}