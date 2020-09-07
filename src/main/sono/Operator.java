package main.sono;

import main.base.ConsoleColors;
import main.sono.err.SonoRuntimeException;

public abstract class Operator {

	public enum Type {
		VARIABLE, CONTAINER, SET, TRANSFORM, SOFT_LIST, HARD_LIST, DEC_RULE, ARROW, SLASH, UNDERSCORE, DEC_MATRIX,
		TO_WORD, COMMON, ADD, SUB, MUL, DIV, MOD, INDEX, EQUALS, NOT_EQUALS, LESS, MORE, E_LESS, E_MORE, TO_MATRIX,
		TO_NUMBER, CONTRAST, DEC_VARIABLE, TO_VECTOR, ITERATOR, LOOP, RANGE_UNTIL, BREAK, IF_ELSE, DEC_LAMBDA, RETURN,
		JOIN_DEC, TO_STRING, FIND, AND, OR, LENGTH, INNER, CAST_REFERENCE, TO_TYPE_STRING, DEC_OBJECTIVE, CAST_STRUCT,
		CAST_STATIC, DEC_CLASS, DEC_NEW, POW, TO_FEATURE, THROW, TRY_CATCH, TO_CHAR, ALLOCATE, CAST_FINAL, REGISTER,
		TO_CHAR_CODE, REFER, SWITCH, TO_HASH, EQUALS_PURE, NOT_EQUALS_PURE, CAST_ABSTRACT, EXTENDS, XOR, IF_ELSE_INLINE,
		EXECUTE, OUTER_CALL, SWITCH_CASE, EVAL, DEC_RAW_OBJECT, DEC_ENTRY,

		// INTERPRETER USE
		UNARY, BINARY, SEQUENCE
	}

	protected Type type;
	protected Interpreter interpreter;
	protected Token line;

	public Operator(final Interpreter interpreter, final Type type, final Token line) {
		this.interpreter = interpreter;
		this.type = type;
		this.line = line;
	}

	public abstract Operator[] getChildren();

	public abstract void condense();

	public void checkInterrupt() throws InterruptedException {
		if (!Thread.interrupted())
			throw new InterruptedException();
	}

	public Datum evaluate(Scope scope, Object[] overrides) throws InterruptedException {
		checkInterrupt();
		return null;
	}

	public abstract String toString();

	public Type getType() {
		return this.type;
	}

	public Token getLine() {
		return this.line;
	}

	public Interpreter getInterpreter() {
		return this.interpreter;
	}

	protected String getInfo() {
		return "";
	}

	public void printTree(String indent, boolean last) {
		System.out.print(indent);
		if (last) {
			System.out.print("└─");
			indent += "  ";
		} else {
			System.out.print("├─");
			indent += "│ ";
		}
		System.out.println(this.type.toString() + ConsoleColors.CYAN + getInfo() + ConsoleColors.RESET);

		for (int i = 0; i < getChildren().length; i++)
			getChildren()[i].printTree(indent, i == getChildren().length - 1);
	}
}