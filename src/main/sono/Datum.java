package main.sono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import main.phl.Feature;
import main.phl.Matrix;
import main.phl.Phone;
import main.phl.PhoneManager;
import main.phl.Rule;
import main.phl.Word;
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
		FEATURE {
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
		BOOL {
			@Override
			public String toString() {
				return "Boolean";
			}
		},

		// INTERPRETER USE
		I_BREAK, ANY
	}

	private double valueNumber = 0;
	private boolean valueBool = false;
	private String valueString = null;

	private Datum[] valueVector = null;
	private Phone valuePhone = null;
	private Feature valueFeature = null;
	private Matrix valueMatrix = null;
	private Rule valueRule = null;
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

	public Datum(final PhoneManager pm, final Datum datum, final Scope parent, final Token line) {
		this.set(pm, datum, line);
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

	public Datum(final Feature valueFeature) {
		this.type = Type.FEATURE;
		this.valueFeature = valueFeature;
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

	public Datum(final boolean valueBool) {
		this.type = Type.BOOL;
		this.valueBool = valueBool;
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

	public Phone getPhone(final Token line) {
		if (type != Type.PHONE)
			throw new SonoRuntimeException("Value <" + this.getDebugString(line) + "> is not a Phone.", line);
		return this.valuePhone;
	}

	public Matrix getMatrix(final Token line) {
		if (type != Type.MATRIX)
			throw new SonoRuntimeException("Value <" + this.getDebugString(line) + "> is not a Matrix.", line);
		return this.valueMatrix;
	}

	public Rule getRule(final Token line) {
		if (type != Type.RULE)
			throw new SonoRuntimeException("Value <" + this.getDebugString(line) + "> is not a Rule.", line);
		return this.valueRule;
	}

	public String getString(final Token line) {
		if (type != Type.STRING)
			throw new SonoRuntimeException("Value <" + this.getDebugString(line) + "> is not a String.", line);
		return this.valueString;
	}

	public double getNumber(final Token line) {
		if (type != Type.NUMBER)
			throw new SonoRuntimeException("Value <" + this.getDebugString(line) + "> is not a Number.", line);
		return this.valueNumber;
	}

	public Structure getStructure(final Token line) {
		if (type != Type.STRUCTURE)
			throw new SonoRuntimeException("Value <" + this.getDebugString(line) + "> is not a Structure.", line);
		return this.valueStructure;
	}

	public Function getFunction(final Type fType, final Token line) {
		if (type != Type.FUNCTION)
			throw new SonoRuntimeException("Value <" + this.getDebugString(line) + "> is not a Function.", line);
		if (this.valueFunction.containsKey(fType))
			return this.valueFunction.get(fType);
		else
			return null;
	}

	public Object getPointer(final Token line) {
		if (type != Type.POINTER)
			throw new SonoRuntimeException("Value <" + this.getDebugString(line) + "> is not a Pointer.", line);
		return this.valuePointer;
	}

	public Word getWord(final Token line) {
		if (type != Type.WORD)
			throw new SonoRuntimeException("Value <" + this.getDebugString(line) + "> is not a Word.", line);
		return this.valueWord;
	}

	public boolean getBool(final Token line) {
		if (type != Type.BOOL)
			throw new SonoRuntimeException("Value <" + this.getDebugString(line) + "> is not a Boolean.", line);
		return this.valueBool;
	}

	public void setMutable(final boolean mutable) {
		this.mutable = mutable;
	}

	public void set(final PhoneManager pm, final Datum datum, final Token line) {
		if (!mutable)
			throw new SonoRuntimeException("You cannot set the value of a constant <" + this.getDebugString(line)
					+ "> (to value <" + datum.getDebugString(line) + ">)", line);
		this.type = datum.getType();
		switch (type) {
			case VECTOR:
				this.valueMatrix = null;
				this.valueFeature = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				final Datum[] dVec = datum.valueVector;
				this.valueVector = new Datum[dVec.length];
				for (int i = 0; i < dVec.length; i++) {
					final Datum n = new Datum();
					n.set(pm, dVec[i], line);
					this.valueVector[i] = n;
				}
				break;
			case MATRIX:
				this.valueVector = null;
				this.valueFeature = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valueMatrix = new Matrix(pm);
				for (final Feature p : datum.valueMatrix)
					this.valueMatrix.put(p.getKey(), p.getQuality());
				break;
			case FEATURE:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				final Feature temp = datum.valueFeature;
				this.valueFeature = new Feature(temp.getKey(), temp.getQuality());
				break;
			case PHONE:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valueFeature = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valuePhone = datum.valuePhone;
				break;
			case RULE:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valueFeature = null;
				this.valuePhone = null;
				this.valueString = null;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valueRule = datum.valueRule;
				break;
			case STRING:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valueFeature = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valueString = datum.valueString;
				break;
			case NUMBER:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valueFeature = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valueNumber = datum.valueNumber;
				break;
			case BOOL:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valueFeature = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valueBool = datum.valueBool;
				break;
			case FUNCTION:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valueFeature = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
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
				this.valueFeature = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueFunction = null;
				this.valueStructure = null;
				this.valuePointer = null;

				this.valueWord = datum.valueWord;
				break;
			case STRUCTURE:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valueFeature = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueFunction = null;
				this.valueWord = null;
				this.valuePointer = null;

				this.valueStructure = datum.valueStructure;
				break;
			case POINTER:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valueFeature = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;

				this.valuePointer = datum.valuePointer;
				break;
			case NULL:
				this.valueVector = null;
				this.valueMatrix = null;
				this.valueFeature = null;
				this.valuePhone = null;
				this.valueRule = null;
				this.valueString = null;
				this.valueFunction = null;
				this.valueWord = null;
				this.valueStructure = null;
				this.valuePointer = null;
				break;
			default:
				break;
		}
	}

	public String toStringTrace(final Token line) {
		if (prototypic)
			return "OBJ-" + type;
		switch (type) {
			case VECTOR:
				return Interpreter.stringFromList(valueVector, "{", "}");
			case MATRIX:
				return valueMatrix.toString();
			case NULL:
				return "null";
			case FEATURE:
				return valueFeature.toString();
			case PHONE:
				return "'" + valuePhone.toString() + "'";
			case RULE:
				return valueRule.toString();
			case STRING:
				return "\"" + valueString + "\"";
			case NUMBER:
				try {
					return BigDecimal.valueOf(valueNumber).stripTrailingZeros().toPlainString();
				} catch (final Exception e) {
					return String.valueOf(valueNumber);
				}
			case BOOL:
				return valueBool ? "true" : "false";
			case FUNCTION:
				return valueFunction.toString();
			case WORD:
				return "`" + valueWord.toString() + "`";
			case STRUCTURE:
				return valueStructure.toStringTrace(line);
			case POINTER:
				return valuePointer.toString();
			default:
				return "undefined";
		}
	}

	public String toRawStringTrace(final Token line) {
		if (prototypic)
			return "OBJ-" + type;
		switch (type) {
			case VECTOR:
				return Interpreter.stringFromList(valueVector, "{", "}");
			case MATRIX:
				return valueMatrix.toString();
			case NULL:
				return "null";
			case FEATURE:
				return valueFeature.toString();
			case PHONE:
				return valuePhone.toString();
			case RULE:
				return valueRule.toString();
			case STRING:
				return valueString;
			case NUMBER:
				try {
					return BigDecimal.valueOf(valueNumber).stripTrailingZeros().toPlainString();
				} catch (final Exception e) {
					return String.valueOf(valueNumber);
				}
			case BOOL:
				return valueBool ? "true" : "false";
			case FUNCTION:
				return valueFunction.toString();
			case WORD:
				return valueWord.toString();
			case STRUCTURE:
				return valueStructure.toStringTrace(line);
			case POINTER:
				return valuePointer.toString();
			default:
				return "undefined";
		}
	}

	@Override
	public String toString() {
		return this.toStringTrace(null);
	}

	public Feature getFeature(final Token line) {
		if (type != Type.FEATURE)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(line) + "> is not a Feature.", line);
		return valueFeature;
	}

	public Datum[] getVector(final Token line) {
		if (type != Type.VECTOR)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(line) + "> is not a Vector.", line);
		return valueVector;
	}

	public void setVector(final Datum[] valueVector) {
		this.type = Type.VECTOR;
		this.valueVector = valueVector;
	}

	public Datum indexVector(final int i) {
		return valueVector[i];
	}

	public int getVectorLength(final Token line) {
		if (type != Type.VECTOR)
			throw new SonoRuntimeException("Value <" + this.toStringTrace(line) + "> is not a Vector.", line);
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
				return valueMatrix.hashCode();
			case NUMBER:
				return String.valueOf(valueNumber).hashCode();
			case BOOL:
				return Boolean.hashCode(valueBool);
			case FEATURE:
				return valueFeature.toString().hashCode();
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

	public boolean isEqual(final Datum d, final Token line) {
		if (type != d.getType())
			return false;
		if (prototypic && d.isPrototypic())
			return true;

		switch (type) {
			case VECTOR:
				if (valueVector.length != d.valueVector.length)
					return false;
				for (int i = 0; i < valueVector.length; i++)
					if (!valueVector[i].isEqual(d.valueVector[i], line))
						return false;
				return true;
			case MATRIX:
				return valueMatrix.equals(d.valueMatrix);
			case NULL:
				return d.getType() == Type.NULL;
			case NUMBER:
				return valueNumber == d.valueNumber;
			case BOOL:
				return valueBool == d.valueBool;
			case FEATURE:
				return valueFeature.equals(d.valueFeature);
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
				return valueStructure.isEqual(d.valueStructure, line);
			case POINTER:
				return valuePointer.equals(d.valuePointer);
			default:
				return false;
		}
	}

	public boolean isEqualPure(final Datum d, final Token line) {
		if (type != d.getType())
			return false;
		if (prototypic && d.isPrototypic())
			return true;

		switch (type) {
			case STRUCTURE:
				return valueStructure == d.valueStructure;
			default:
				return this.isEqual(d, line);
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;

		return this.isEqual((Datum) o, null);
	}

	public static Datum arrayConcat(final Datum a, final Datum b) {
		final Datum[] newList = new Datum[a.valueVector.length + b.valueVector.length];
		System.arraycopy(a.valueVector, 0, newList, 0, a.valueVector.length);
		System.arraycopy(b.valueVector, 0, newList, a.valueVector.length, b.valueVector.length);
		return new Datum(newList);
	}

	public String getDebugString(final Token line) {
		return getTypeString() + ":" + toStringTrace(line);
	}
}