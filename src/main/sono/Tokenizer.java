package main.sono;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleImmutableEntry;

import main.sono.err.SonoCompilationException;

public class Tokenizer {
	private Tokenizer() {
		throw new IllegalStateException("Utility class");
	}

	protected static final Map<String, Integer> operators = Map.ofEntries(
			new SimpleImmutableEntry<String, Integer>("|", 999),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$_OUTER_CALL_, 20),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$OBJECTIVE, 16),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$EXEC, 15),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$INDEX, 15),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$INNER, 15),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$ABSTRACT, -15),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$FINAL, -15),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$REF, -15),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$STATIC, -15),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$STRUCT, -15),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$EXTENDS, 14),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$NEW, -14),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$CLASS, 13),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$IMPORT, -13),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$LOAD, -13),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$VAR, -13),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$LAMBDA, 12),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$UNARY_NOT, -12),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$CHAR, -11),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$CODE, -11),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$FEAT, -11),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$HASH, -11),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$LEN, -11),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$MAT, -11),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$NEGATIVE, -11),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$NUM, -11),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$POSITIVE, -11),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$STR, -11),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$VEC, -11),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$WORD, -11),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$FROM, 10),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$REGISTER, 10),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$POW, -10),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$DIV, 9),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$MOD, 9),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$MUL, 9),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$ADD, 8),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$CONTRAST, 8),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$SUB, 8),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$TRANSFORM, 8),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$ARROW, 7),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$UNDERSCORE, 7),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$E_LESS, 6),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$E_MORE, 6),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$EQUALS, 6),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$LESS, 6),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$MORE, 6),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$N_EQUALS, 6),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$PURE_EQUALS, 6),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$PURE_N_EQUALS, 6),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$SLASH, 6),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$AND, 5),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$OR, 5),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$RULE, 5),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$UNTIL, 4),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$ADD_SET, -4),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$DIV_SET, -4),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$MOD_SET, -4),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$MUL_SET, -4),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$SET, -4),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$SUB_SET, -4),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$TRANSFORM_SET, -4),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$TRY, -4),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$CATCH, 3),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$DO, 3),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$GOTO, 3),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$IN, 3),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$SWITCH, 3),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$THEN, 3),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$ELSE, -3),
			new SimpleImmutableEntry<String, Integer>(",", 2),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$REFER, 2),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$RETURN, 2),
			new SimpleImmutableEntry<String, Integer>(Interpreter.$THROW, 2),
			new SimpleImmutableEntry<String, Integer>(";", -1));

	public static List<Token> tokenize(final String str) {
		char pChar = 0;
		int mode = 0;
		char lastChar = 0;
		boolean comment = false;
		final List<Token> tokens = new ArrayList<>();
		tokens.add(new Token("#[", "", 0, 0));
		final String[] lines = str.split("[\\n\\r]");
		int lineIndex = 0;
		int cursorIndex = 0;

		for (int i = 0; i < str.length(); i++) {
			final char c = str.charAt(i);
			cursorIndex++;
			if (comment) {
				if (c == '\n' || c == '\r') {
					comment = false;
					mode = 0;
					lineIndex++;
					cursorIndex = 0;
				}
				continue;
			}
			if (pChar != 0) {
				if (c == pChar && lastChar != '\\')
					pChar = 0;
				else {
					if (lastChar == '\\') {
						tokens.get(tokens.size() - 1).substring(0, tokens.get(tokens.size() - 1).length() - 1);
						if (Character.isDigit(c)) {
							final int newChar = (c * 100) + (str.charAt(i + 1) * 10) + (str.charAt(i + 2));
							i += 2;
							tokens.get(tokens.size() - 1).append(newChar);
							lastChar = (char) newChar;
							continue;
						} else {
							tokens.get(tokens.size() - 1).deleteCharAt(tokens.get(tokens.size() - 1).length() - 1);
							switch (c) {
								case '\\':
									tokens.get(tokens.size() - 1).append('\\');
									lastChar = 0;
									continue;
								case 'n':
									tokens.get(tokens.size() - 1).append('\n');
									break;
								case 't':
									tokens.get(tokens.size() - 1).append('\t');
									break;
								case 'r':
									tokens.get(tokens.size() - 1).append('\r');
									break;
								case '\'':
									tokens.get(tokens.size() - 1).append('\'');
									break;
								case '0':
									tokens.get(tokens.size() - 1).append('\0');
									break;
								case '"':
									tokens.get(tokens.size() - 1).append('"');
									break;
								default:
									tokens.get(tokens.size() - 1).append('\\');
									tokens.get(tokens.size() - 1).append(c);
									break;
							}
						}
					} else {
						tokens.get(tokens.size() - 1).append(c);
					}
				}
			} else {
				if (c == '#') {
					comment = true;
					continue;
				}
				if (Character.isWhitespace(c)) {
					if (c == '\n' || c == '\r') {
						lineIndex++;
						cursorIndex = 0;
					}
					mode = 0;
					continue;
				} else if (c == '\"' || c == '\'' || c == '`') {
					mode = 0;
					pChar = c;
					tokens.add(new Token(String.valueOf(c), lines[lineIndex], cursorIndex, lineIndex));
					continue;
				} else if (c == '{') {
					tokens.add(new Token("{", lines[lineIndex], cursorIndex, lineIndex));
					tokens.add(new Token("#[", lines[lineIndex], cursorIndex, lineIndex));
					lastChar = '(';
					mode = 0;
					continue;
				} else if (c == '}') {
					tokens.add(new Token("#]", lines[lineIndex], cursorIndex, lineIndex));
					tokens.add(new Token("}", lines[lineIndex], cursorIndex, lineIndex));
					tokens.add(new Token("#]", lines[lineIndex], cursorIndex, lineIndex));
					tokens.add(new Token("#[", lines[lineIndex], cursorIndex, lineIndex));
					lastChar = '(';
					mode = 0;
					continue;
				}
				switch (mode) {
					case 0:
						tokens.add(new Token(String.valueOf(c), lines[lineIndex], cursorIndex, lineIndex));
						break;
					case 1:
						if (Character.isLetterOrDigit(c) || c == '_')
							tokens.get(tokens.size() - 1).append(c);
						else
							tokens.add(new Token(String.valueOf(c), lines[lineIndex], cursorIndex, lineIndex));
						break;
					case 2:
						if (!(Character.isLetterOrDigit(c) || c == '_')
								&& operators.containsKey(tokens.get(tokens.size() - 1).getKey() + String.valueOf(c)))
							tokens.get(tokens.size() - 1).append(c);
						else
							tokens.add(new Token(String.valueOf(c), lines[lineIndex], cursorIndex, lineIndex));
						break;
					default:
						throw new SonoCompilationException("Unknown tokenizer mode change.");
				}
				mode = (!(Character.isLetterOrDigit(c) || c == '_') ? 1 : 0) + 1;
			}
			lastChar = c;
		}

		if (pChar != 0)
			throw new SonoCompilationException("Unclosed string or char literal!");

		final List<Token> newTokens = new ArrayList<>();
		String last = "+";
		for (final Token raw : tokens) {
			final String t = raw.getKey();
			final String line = raw.getLine();
			final int cursor = raw.getCursor();
			if (t.equals("#[") || t.equals("#]"))
				continue;
			if (last.equals("}") && !t.equals(Interpreter.$ELSE) && !t.equals("[")
					&& (!operators.containsKey(t) || (operators.containsKey(t) && operators.get(t) < 0)))
				newTokens.add(new Token(";", line, cursor + 1, raw.getLineNumber()));
			if (t.equals("[")
					&& ((Character.isLetterOrDigit(last.charAt(0)) || last.charAt(0) == '_' || last.charAt(0) == '`')
							|| last.equals(")") || last.equals("]") || last.equals("}"))
					&& !operators.containsKey(last))
				newTokens.add(new Token(Interpreter.$INDEX, line, cursor, raw.getLineNumber()));
			if (t.equals("(") && ((Character.isLetterOrDigit(last.charAt(0)) || last.charAt(0) == '_')
					|| last.equals(")") || last.equals("]")) && !operators.containsKey(last))
				newTokens.add(new Token(Interpreter.$EXEC, line, cursor, raw.getLineNumber()));
			if (t.equals("-")
					&& (operators.containsKey(last) || last.equals("(") || last.equals("[") || last.equals("{"))) {
				newTokens.add(new Token(Interpreter.$NEGATIVE, line, cursor, raw.getLineNumber()));
				last = t;
				continue;
			}
			if (t.equals(Interpreter.$ADD)
					&& (operators.containsKey(last) || last.equals("(") || last.equals("[") || last.equals("{"))) {
				newTokens.add(new Token(Interpreter.$POSITIVE, line, cursor, raw.getLineNumber()));
				last = t;
				continue;
			}
			if (Character.isDigit(t.charAt(0)) && last.equals(Interpreter.$INNER)
					&& Character.isDigit(newTokens.get(newTokens.size() - 2).charAt(0))) {
				newTokens.remove(newTokens.size() - 1);
				final String temp = newTokens.get(newTokens.size() - 1).getKey() + "." + t + "D";
				newTokens.set(newTokens.size() - 1,
						new Token(temp, line, newTokens.get(newTokens.size() - 1).getCursor(), raw.getLineNumber()));
				last = t;
				continue;
			}
			newTokens.add(new Token(t, line, cursor, raw.getLineNumber()));
			last = t;
		}

		final Deque<Token> postFeatures = new ArrayDeque<>();
		for (int i = 0; i < newTokens.size(); i++) {
			final String t = newTokens.get(i).getKey();
			final String line = newTokens.get(i).getLine();
			final int cursor = newTokens.get(i).getCursor();
			final int lineN = newTokens.get(i).getLineNumber();
			if (t.equals("|")) {
				String t1 = postFeatures.pollLast().getKey();
				final String t2 = newTokens.get(i + 1).getKey();
				if (t1.equals(Interpreter.$POSITIVE))
					t1 = "+";
				else if (t1.equals(Interpreter.$NEGATIVE))
					t1 = "-";
				postFeatures.add(new Token("@" + t1 + "|" + t2, line, cursor, lineN));
				i++;
			} else {
				postFeatures.add(new Token(t, line, cursor, lineN));
			}
		}

		return new ArrayList<>(postFeatures);
	}

	public static List<Token> infixToPostfix(final List<Token> tokens) {
		final Deque<Token> stack = new ArrayDeque<>();
		final Deque<Token> output = new ArrayDeque<>();
		int pCount = 0;
		int bCount = 0;
		int cCount = 0;

		for (final Token token : tokens) {
			if ((Character.isLetterOrDigit(token.charAt(0)) || token.charAt(0) == '_' || token.charAt(0) == '\"'
					|| token.charAt(0) == '@' || token.charAt(0) == '\'' || token.charAt(0) == '`')
					&& !operators.containsKey(token.getKey()))
				output.addLast(token);
			else if (operators.containsKey(token.getKey())) {
				if (!stack.isEmpty()) {
					Token sb = stack.peekLast();
					while ((Math.abs(getPrecedence(token.getKey())) < Math.abs(getPrecedence(sb.getKey()))
							|| (getPrecedence(sb.getKey()) > 0
									&& Math.abs(getPrecedence(token.getKey())) == Math.abs(getPrecedence(sb.getKey()))))
							&& (!sb.getKey().equals("(") && !sb.getKey().equals("[") && !sb.getKey().equals("{"))) {
						output.addLast(sb);
						stack.pollLast();
						if (stack.isEmpty())
							break;
						else
							sb = stack.peekLast();
					}
				}
				stack.addLast(token);
			} else if (token.getKey().equals("(") || token.getKey().equals("[") || token.getKey().equals("{")) {
				stack.addLast(token);
				output.addLast(token);
				switch (token.charAt(0)) {
					case '(':
						pCount++;
						break;
					case '[':
						bCount++;
						break;
					case '{':
						cCount++;
						break;
					default:
						throw new SonoCompilationException("Unknown error.");
				}
			} else if (token.getKey().equals(")") || token.getKey().equals("]") || token.getKey().equals("}")) {
				switch (token.charAt(0)) {
					case ')':
						pCount--;
						if (stack.isEmpty())
							throw new SonoCompilationException("Unmatched parenthesis!");
						while (!stack.peekLast().getKey().equals("(")) {
							output.addLast(stack.peekLast());
							stack.pollLast();
							if (stack.isEmpty())
								throw new SonoCompilationException("Unmatched parenthesis!");
						}
						break;
					case ']':
						bCount--;
						if (stack.isEmpty())
							throw new SonoCompilationException("Unmatched bracket!");
						while (!stack.peekLast().getKey().equals("[")) {
							output.addLast(stack.peekLast());
							stack.pollLast();
							if (stack.isEmpty())
								throw new SonoCompilationException("Unmatched bracket!");
						}
						break;
					case '}':
						cCount--;
						if (stack.isEmpty())
							throw new SonoCompilationException("Unmatched scoping bracket!");
						while (!stack.peekLast().getKey().equals("{")) {
							output.addLast(stack.peekLast());
							stack.pollLast();
							if (stack.isEmpty())
								throw new SonoCompilationException("Unmatched scoping bracket!");
						}
						break;
					default:
						throw new SonoCompilationException("Unknown error.");
				}
				stack.pollLast();
				output.addLast(token);
			}
		}

		if (pCount != 0)
			throw new SonoCompilationException("Unmatched parenthesis!");
		if (bCount != 0)
			throw new SonoCompilationException("Unmatched bracket!");
		if (cCount != 0)
			throw new SonoCompilationException("Unmatched scoping bracket!");

		while (!stack.isEmpty()) {
			output.addLast(stack.peekLast());
			stack.pollLast();
		}

		return new ArrayList<>(output);
	}

	private static int getPrecedence(final String token) {
		if (operators.containsKey(token))
			return operators.get(token);

		return 999;
	}
}