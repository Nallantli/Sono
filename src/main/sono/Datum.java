package main.sono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import main.phl.*;
import main.sono.err.SonoRuntimeException;

public class Datum {
	public enum Type {
		NULL {
			@Override
			public String toString() {
				return "null";
			}
		},
		VECTOR {
			@Override
			public String toString() {
				return "Vector";
			}
		},
		STRING {
			@Override
			public String toString() {
				return "String";
			}
		},
		PHONE {
			@Override
			public String toString() {
				return "Phone";
			}
		},
		PAIR {
			@Override
			public String toString() {
				return "Feature";
			}
		},
		MATRIX {
			@Override
			public String toString() {
				return "Matrix";
			}
		},
		RULE {
			@Override
			public String toString() {
				return "Rule";
			}
		},
		NUMBER {
			@Override
			public String toString() {
				return "Number";
			}
		},
		FUNCTION {
			@Override
			public String toString() {
				return "Function";
			}
		},
		WORD {
			@Override
			public String toString() {
				return "Word";
			}
		},
		STRUCTURE {
			@Override
			public String toString() {
				return "Object";
			}
		},
		POINTER {
			@Override
			public String toString() {
				return "Pointer";
			}
		},

		// INTERPRETER USE
		I_BREAK, ANY
	}

	private Datum[] valueVector = null;
	private String valueString = null;
	private Phone valuePhone = null;
	private Pair valuePair = null;
	private Matrix valueMatrix = null;
	private Rule valueRule = null;
	private double valueNumber = 0;
	private Map<Type, Function> valueFunction = null;
	private Word valueWord = null;
	private Structure valueStructure = null;
	private Object valuePointer = null;

	protected Type type;

	private boolean ret = false;
	private boolean refer = false;
	private boolean mutable = true;
	private boolean prototypic = false;

	public Datum() {
		this.type = Type.NULL;
	}

	public Datum(final PhoneManager pm, final Datum datum, final Scope parent, final List<String> trace) {
		this.set(pm, datum, trace);
		if (this.type == Type.FUNCTION)
			for (final Map.Entry<Type, Function> e : valueFunction.entrySet())
				e.getValue().setParent(parent);
	}

	public Datum(final Type type) {
		this.type = type;
		this.mutable = false;
		this.prototypic = true;
	}

	public Datum(final Datum[] valueVector) {
		this.type = Type.VECTOR;
		this.valueVector = valueVector;
	}

	public Datum(final Structure valueStructure) {
		this.type = Type.STRUCTURE;
		this.valueStructure = valueStructure;
	}

	public Datum(final String valueString) {
		this.type = Type.STRING;
		this.valueString = valueString;
	}

	public Datum(final Phone valuePhone) {
		this.type = Type.PHONE;
		this.valuePhone = valuePhone;
	}

	public Datum(final Pair valuePair) {
		this.type = Type.PAIR;
		this.valuePair = valuePair;
	}

	public Datum(final Matrix valueMatrix) {
		this.type = Type.MATRIX;
		this.valueMatrix = valueMatrix;
	}

	public Datum(final Rule valueRule) {
		this.type = Type.RULE;
		this.valueRule = valueRule;
	}

	public Datum(final double valueNumber) {
		this.type = Type.NUMBER;
		this.valueNumber = valueNumber;
	}

	public Datum(final Type fType, final Function valueFunction) {
		this.type = Type.FUNCTION;
		this.valueFunction = new EnumMap<>(Type.class);
		this.valueFunction.put(fType, valueFunction);
	}

	public Datum(final Word valueWord) {
		this.type = Type.WORD;
		this.valueWord = valueWord;
	}

	public Datum(final Object valuePointer) {
		this.type = Type.POINTER;
		this.valuePointer = valuePointer;
	}

	public static class Break extends Datum {
		public Break() {
			this.type = Type.I_BREAK;
		}
	}

	public Type getType() {
		return this.type;
	}

	public String getTypeString() {
		if (this.type == Type.STRUCTURE)
			return this.valueStructure.getName();
		return this.type.toString();
	}

	public Phone getPhone(final List<String> trace) {
		if (type != Type.PHONE)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(trace) + "> is not a Phone.", trace);
		return this.valuePhone;
	}

	public Matrix getMatrix(final List<String> trace) {
		if (type != Type.MATRIX)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(trace) + "> is not a Matrix.", trace);
		return this.valueMatrix;
	}

	public Rule getRule(final List<String> trace) {
		if (type != Type.RULE)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(trace) + "> is not a Rule.", trace);
		return this.valueRule;
	}

	public String getString(final List<String> trace) {
		if (type != Type.STRING)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(trace) + "> is not a String.", trace);
		return this.valueString;
	}

	public double getNumber(final List<String> trace) {
		if (type != Type.NUMBER)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(trace) + "> is not a Number.", trace);
		return this.valueNumber;
	}

	public Structure getStructure(final List<String> trace) {
		if (type != Type.STRUCTURE)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(trace) + "> is not a Structure.", trace);
		return this.valueStructure;
	}

	public Function getFunction(final Type fType, final List<String> trace) {
		if (type != Type.FUNCTION)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(trace) + "> is not a Function.", trace);
		if (this.valueFunction.containsKey(fType))
			return this.valueFunction.get(fType);
		else
			return null;
	}

	public Object getPointer(final List<String> trace) {
		if (type != Type.POINTER)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(trace) + "> is not a Pointer.", trace);
		return this.valuePointer;
	}

	public Word getWord(final List<String> trace) {
		if (type != Type.WORD)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(trace) + "> is not a Word.", trace);
		return this.valueWord;
	}

	public void setMutable(final boolean mutable) {
		this.mutable = mutable;
	}

	public void set(final PhoneManager pm, final Datum datum, final List<String> trace) {
		if (datum == null) {
			throw new SonoRuntimeException("ERROR", trace);
		}
		if (!mutable)
			throw new SonoRuntimeException("You cannot set the value of a constant <" + this.toStringTrace(trace)
					+ "> (to value <" + datum.toStringTrace(trace) + ">)", trace);
		this.type = datum.getType();
		switch (type) {
			case VECTOR:
				this.valueMatrix = null;
				this.valuePair = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueNumber = 0;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				final Datum[] dVec = datum.valueVector;
				this.valueVector = new Datum[dVec.length];
				for (int i = 0; i < dVec.length; i++) {
					final Datum n = new Datum();
					n.set(pm, dVec[i], trace);
					this.valueVector[i] = n;
				}
				break;
			case MATRIX:
				this.valueVector = null;
				this.valuePair = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueNumber = 0;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valueMatrix = new Matrix();
				for (final Pair p : datum.valueMatrix)
					this.valueMatrix.put(pm, p.getFeature(), p.getQuality());
				break;
			case PAIR:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueNumber = 0;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				final Pair temp = datum.getPair(trace);
				this.valuePair = new Pair(temp.getFeature(), temp.getQuality());
				break;
			case PHONE:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valuePair = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueNumber = 0;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valuePhone = datum.getPhone(trace);
				break;
			case RULE:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valuePair = null;
				this.valuePhone = null;
				this.valueString = null;
				this.valueNumber = 0;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valueRule = datum.getRule(trace);
				break;
			case STRING:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valuePair = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueNumber = 0;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valueString = datum.getString(trace);
				break;
			case NUMBER:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valuePair = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valueNumber = datum.getNumber(trace);
				break;
			case FUNCTION:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valuePair = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueNumber = 0;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				if (this.valueFunction == null)
					this.valueFunction = new EnumMap<>(Type.class);
				this.valueFunction.putAll(datum.valueFunction);
				break;
			case WORD:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valuePair = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueNumber = 0;
				this.valueFunction = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valueWord = datum.getWord(trace);
				break;
			case STRUCTURE:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valuePair = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueNumber = 0;
				this.valueFunction = null;
				this.valueWord = null;
				this.valuePointer = null;

				this.valueStructure = datum.getStructure(trace);
				break;
			case POINTER:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valuePair = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueNumber = 0;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;

				this.valuePointer = datum.getPointer(trace);
				break;
			case NULL:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valuePair = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueNumber = 0;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;
				break;
			default:
				break;
		}
	}

	public String toStringTrace(final List<String> trace) {
		if (prototypic)
			return "OBJ-" + type;
		switch (type) {
			case VECTOR:
				return Interpreter.stringFromList(valueVector, "{", "}");
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
				return BigDecimal.valueOf(valueNumber).stripTrailingZeros().toPlainString();
			case FUNCTION:
				return valueFunction.toString();
			case WORD:
				return "`" + valueWord.toString() + "`";
			case STRUCTURE:
				return valueStructure.toStringTrace(trace);
			case POINTER:
				return valuePointer.toString();
			default:
				return "undefined";
		}
	}

	public String toRawStringTrace(final List<String> trace) {
		if (prototypic)
			return "OBJ-" + type;
		switch (type) {
			case VECTOR:
				return Interpreter.stringFromList(valueVector, "{", "}");
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
				return BigDecimal.valueOf(valueNumber).stripTrailingZeros().toPlainString();
			case FUNCTION:
				return valueFunction.toString();
			case WORD:
				return valueWord.toString();
			case STRUCTURE:
				return valueStructure.toStringTrace(trace);
			case POINTER:
				return valuePointer.toString();
			default:
				return "undefined";
		}
	}

	@Override
	public String toString() {
		return this.toStringTrace(new ArrayList<>());
	}

	public Pair getPair(final List<String> trace) {
		if (type != Type.PAIR)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(trace) + "> is not a Pair.", trace);
		return valuePair;
	}

	public Datum[] getVector(final List<String> trace) {
		if (type != Type.VECTOR)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(trace) + "> is not a Vector.", trace);
		return valueVector;
	}

	public Datum indexVector(final int i) {
		return valueVector[i];
	}

	public int getVectorLength(final List<String> trace) {
		if (type != Type.VECTOR)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(trace) + "> is not a Vector.", trace);
		return valueVector.length;
	}

	public void setRet(final boolean ret) {
		this.ret = ret;
	}

	public void setRefer(final boolean refer) {
		this.refer = refer;
	}

	public boolean getRet() {
		return ret;
	}

	public boolean getRefer() {
		return refer;
	}

	public boolean isPrototypic() {
		return prototypic;
	}

	public boolean isMutable() {
		return mutable;
	}

	@Override
	public int hashCode() {
		switch (type) {
			case FUNCTION:
				return valueFunction.hashCode();
			case MATRIX:
				return valueMatrix.toString().hashCode();
			case NUMBER:
				return String.valueOf(valueNumber).hashCode();
			case PAIR:
				return valuePair.toString().hashCode();
			case PHONE:
				return valuePhone.toString().hashCode();
			case POINTER:
				return valuePointer.hashCode();
			case RULE:
				return valueRule.toString().hashCode();
			case STRING:
				return valueString.hashCode();
			case STRUCTURE:
				return valueStructure.getHash();
			case VECTOR:
				return Arrays.deepHashCode(valueVector);
			case WORD:
				return valueWord.toString().hashCode();
			default:
				return 1;
		}
	}

	public boolean isEqual(final Datum d, final List<String> trace) {
		if (type != d.getType())
			return false;
		if (prototypic && d.isPrototypic())
			return true;

		switch (type) {
			case VECTOR:
				if (valueVector.length != d.valueVector.length)
					return false;
				for (int i = 0; i < valueVector.length; i++)
					if (!valueVector[i].isEqual(d.valueVector[i], trace))
						return false;
				return true;
			case MATRIX:
				return valueMatrix.equals(d.valueMatrix);
			case NULL:
				return d.getType() == Type.NULL;
			case NUMBER:
				return valueNumber == d.valueNumber;
			case PAIR:
				return valuePair.equals(d.valuePair);
			case PHONE:
				return valuePhone.equals(d.valuePhone);
			case RULE:
				return valueRule.equals(d.valueRule);
			case FUNCTION:
				return valueFunction.equals(d.valueFunction);
			case STRING:
				return valueString.equals(d.valueString);
			case WORD:
				return valueWord.equals(d.valueWord);
			case STRUCTURE:
				return valueStructure.isEqual(d.valueStructure, trace);
			case POINTER:
				return valuePointer.equals(d.valuePointer);
			default:
				return false;
		}
	}

	public boolean isEqualPure(final Datum d, final List<String> trace) {
		if (type != d.getType())
			return false;
		if (prototypic && d.isPrototypic())
			return true;

		switch (type) {
			case STRUCTURE:
				return valueStructure == d.valueStructure;
			default:
				return this.isEqual(d, trace);
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;

		return this.isEqual((Datum) o, Collections.emptyList());
	}

	public static Datum arrayConcat(final Datum a, final Datum b) {
		final Datum[] newList = new Datum[a.valueVector.length + b.valueVector.length];
		System.arraycopy(a.valueVector, 0, newList, 0, a.valueVector.length);
		System.arraycopy(b.valueVector, 0, newList, a.valueVector.length, b.valueVector.length);
		return new Datum(newList);
	}

	public String getDebugString(final List<String> trace) {
		return getTypeString() + ":" + toStringTrace(trace);
	}
}