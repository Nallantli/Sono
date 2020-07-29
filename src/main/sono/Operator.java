package main.sono;

public abstract class Operator {

	public enum Type {
		VARIABLE, DATUM, SET, TRANSFORM, SOFT_LIST, HARD_LIST, RULE_DEC, ARROW, SLASH, UNDERSCORE, MATRIX_DEC, SEQ_DEC,
		COMMON, ADD, SUB, MUL, DIV, MOD, INDEX, EQUAL, NOT_EQUAL, LESS, MORE, E_LESS, E_MORE, MATRIX_CONVERT,
		NUMBER_CONVERT, CONTRAST, VAR_DEC, LIST_DEC, ITERATOR, LOOP, RANGE_UNTIL, BREAK, IF_ELSE, LAMBDA, RETURN,
		JOIN_DEC, STR_DEC, FIND_DEC, AND, OR, LEN, INNER, REF_DEC, TYPE_CONVERT, TYPE_DEC, STRUCT_DEC, STATIC_DEC,
		CLASS_DEC, NEW_DEC, POW, FEAT_DEC, THROW, TRY_CATCH, CHAR, ALLOC, FINAL, REGISTER, CODE, REFER, SWITCH, HASH,
		P_EQUALS, P_NOT_EQUAL, ABSTRACT_DEC, EXTENDS, XOR,

		// INTERPRETER USE
		UNARY, BINARY, SEQUENCE, EXECUTE, OUTER_CALL, SWITCH_CASE
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

	public abstract Datum evaluate(Scope scope);

	public abstract String toString();

	public Type getType() {
		return this.type;
	}
}