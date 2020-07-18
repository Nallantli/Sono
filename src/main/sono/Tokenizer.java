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
			new SimpleImmutableEntry<String, Integer>("_OUTER_CALL_", 20),
			new SimpleImmutableEntry<String, Integer>("load", -13),
			new SimpleImmutableEntry<String, Integer>("import", -13),
			new SimpleImmutableEntry<String, Integer>("new", -14),
			new SimpleImmutableEntry<String, Integer>("var", -13),
			new SimpleImmutableEntry<String, Integer>("struct", -14),
			new SimpleImmutableEntry<String, Integer>("static", -14),
			new SimpleImmutableEntry<String, Integer>("class", 13),
			new SimpleImmutableEntry<String, Integer>("return", 2),
			new SimpleImmutableEntry<String, Integer>("refer", 2),
			new SimpleImmutableEntry<String, Integer>("throw", 2), new SimpleImmutableEntry<String, Integer>("=>", 12),
			new SimpleImmutableEntry<String, Integer>("!", -12), new SimpleImmutableEntry<String, Integer>("len", -11),
			new SimpleImmutableEntry<String, Integer>("word", -11),
			new SimpleImmutableEntry<String, Integer>("str", -11),
			new SimpleImmutableEntry<String, Integer>("alloc", -11),
			new SimpleImmutableEntry<String, Integer>("char", -11),
			new SimpleImmutableEntry<String, Integer>("code", -11),
			new SimpleImmutableEntry<String, Integer>("feat", -11),
			new SimpleImmutableEntry<String, Integer>("hash", -11),
			new SimpleImmutableEntry<String, Integer>("type", -11),
			new SimpleImmutableEntry<String, Integer>("from", 10),
			new SimpleImmutableEntry<String, Integer>("register", 10),
			new SimpleImmutableEntry<String, Integer>("mat", -11),
			new SimpleImmutableEntry<String, Integer>("num", -11),
			new SimpleImmutableEntry<String, Integer>("vec", -11),
			new SimpleImmutableEntry<String, Integer>("com", -11), new SimpleImmutableEntry<String, Integer>("::", 16),
			new SimpleImmutableEntry<String, Integer>(".negative", -11),
			new SimpleImmutableEntry<String, Integer>(".positive", -11),
			new SimpleImmutableEntry<String, Integer>(".index", 15),
			new SimpleImmutableEntry<String, Integer>("ref", -15),
			new SimpleImmutableEntry<String, Integer>("final", -15),
			new SimpleImmutableEntry<String, Integer>(".exec", 15), new SimpleImmutableEntry<String, Integer>(".", 15),
			new SimpleImmutableEntry<String, Integer>("**", -10), new SimpleImmutableEntry<String, Integer>("*", 9),
			new SimpleImmutableEntry<String, Integer>("/", 9), new SimpleImmutableEntry<String, Integer>("%", 9),
			new SimpleImmutableEntry<String, Integer>("+", 8), new SimpleImmutableEntry<String, Integer>("-", 8),
			new SimpleImmutableEntry<String, Integer>(">>", 8), new SimpleImmutableEntry<String, Integer>("?>", 8),
			new SimpleImmutableEntry<String, Integer>("===", 6),
			new SimpleImmutableEntry<String, Integer>("!==", 6),
			new SimpleImmutableEntry<String, Integer>("==", 6), new SimpleImmutableEntry<String, Integer>("!=", 6),
			new SimpleImmutableEntry<String, Integer>("<", 6), new SimpleImmutableEntry<String, Integer>(">", 6),
			new SimpleImmutableEntry<String, Integer>("<=", 6), new SimpleImmutableEntry<String, Integer>(">=", 6),
			new SimpleImmutableEntry<String, Integer>("&&", 5), new SimpleImmutableEntry<String, Integer>("||", 5),
			new SimpleImmutableEntry<String, Integer>("=", -4), new SimpleImmutableEntry<String, Integer>("+=", -4),
			new SimpleImmutableEntry<String, Integer>("-=", -4), new SimpleImmutableEntry<String, Integer>("*=", -4),
			new SimpleImmutableEntry<String, Integer>("/=", -4), new SimpleImmutableEntry<String, Integer>("%=", -4),
			new SimpleImmutableEntry<String, Integer>(">>=", -4), new SimpleImmutableEntry<String, Integer>("->", 7),
			new SimpleImmutableEntry<String, Integer>("//", 6), new SimpleImmutableEntry<String, Integer>("~", 7),
			new SimpleImmutableEntry<String, Integer>("in", 3), new SimpleImmutableEntry<String, Integer>("do", 3),
			new SimpleImmutableEntry<String, Integer>("|>", 5), new SimpleImmutableEntry<String, Integer>("until", 4),
			new SimpleImmutableEntry<String, Integer>("then", 3),
			new SimpleImmutableEntry<String, Integer>("switch", 3),
			new SimpleImmutableEntry<String, Integer>("goto", 3), new SimpleImmutableEntry<String, Integer>("else", -3),
			new SimpleImmutableEntry<String, Integer>("try", -4), new SimpleImmutableEntry<String, Integer>("catch", 3),
			new SimpleImmutableEntry<String, Integer>(",", 2), new SimpleImmutableEntry<String, Integer>(";", -1),
			new SimpleImmutableEntry<String, Integer>("|", 999));

	public static List<String> tokenize(final String str) {
		char pChar = 0;
		int mode = 0;
		char lastChar = 0;
		boolean comment = false;
		final List<StringBuilder> tokens = new ArrayList<>();
		tokens.add(new StringBuilder("#["));

		for (int i = 0; i < str.length(); i++) {
			final char c = str.charAt(i);
			if (comment) {
				if (c == '\n' || c == '\r') {
					comment = false;
					mode = 0;
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
					mode = 0;
					continue;
				} else if (c == '\"' || c == '\'' || c == '`') {
					mode = 0;
					pChar = c;
					tokens.add(new StringBuilder(String.valueOf(c)));
					continue;
				} else if (c == '{') {
					tokens.add(new StringBuilder("{"));
					tokens.add(new StringBuilder("#["));
					lastChar = '(';
					mode = 0;
					continue;
				} else if (c == '}') {
					tokens.add(new StringBuilder("#]"));
					tokens.add(new StringBuilder("}"));
					tokens.add(new StringBuilder("#]"));
					tokens.add(new StringBuilder("#["));
					lastChar = '(';
					mode = 0;
					continue;
				}
				switch (mode) {
					case 0:
						tokens.add(new StringBuilder(String.valueOf(c)));
						break;
					case 1:
						if (Character.isLetterOrDigit(c) || c == '_')
							tokens.get(tokens.size() - 1).append(c);
						else
							tokens.add(new StringBuilder(String.valueOf(c)));
						break;
					case 2:
						if (!(Character.isLetterOrDigit(c) || c == '_')
								&& operators.containsKey(tokens.get(tokens.size() - 1).toString() + String.valueOf(c)))
							tokens.get(tokens.size() - 1).append(c);
						else
							tokens.add(new StringBuilder(String.valueOf(c)));
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

		final List<String> newTokens = new ArrayList<>();
		String last = "+";
		for (final StringBuilder raw : tokens) {
			final String t = raw.toString();
			if (t.equals("#[") || t.equals("#]"))
				continue;
			if (last.equals("}") && !t.equals("else") && !t.equals("[")
					&& (!operators.containsKey(t) || (operators.containsKey(t) && operators.get(t) < 0)))
				newTokens.add(";");
			if (t.equals("[")
					&& ((Character.isLetterOrDigit(last.charAt(0)) || last.charAt(0) == '_' || last.charAt(0) == '`')
							|| last.equals(")") || last.equals("]") || last.equals("}"))
					&& !operators.containsKey(last))
				newTokens.add(".index");
			if (t.equals("(") && ((Character.isLetterOrDigit(last.charAt(0)) || last.charAt(0) == '_')
					|| last.equals(")") || last.equals("]")) && !operators.containsKey(last))
				newTokens.add(".exec");
			if (t.equals("-")
					&& (operators.containsKey(last) || last.equals("(") || last.equals("[") || last.equals("{"))) {
				newTokens.add(".negative");
				last = t;
				continue;
			}
			if (t.equals("+")
					&& (operators.containsKey(last) || last.equals("(") || last.equals("[") || last.equals("{"))) {
				newTokens.add(".positive");
				last = t;
				continue;
			}
			if (Character.isDigit(t.charAt(0)) && last.equals(".")
					&& Character.isDigit(newTokens.get(newTokens.size() - 2).charAt(0))) {
				newTokens.remove(newTokens.size() - 1);
				final String temp = newTokens.get(newTokens.size() - 1) + "." + t + "D";
				newTokens.set(newTokens.size() - 1, temp);
				last = t;
				continue;
			}
			newTokens.add(t);
			last = t;
		}

		final Deque<String> postFeatures = new ArrayDeque<>();
		for (int i = 0; i < newTokens.size(); i++) {
			final String t = newTokens.get(i);
			if (t.equals("|")) {
				String t1 = postFeatures.pollLast();
				final String t2 = newTokens.get(i + 1);
				if (t1.equals(".positive"))
					t1 = "+";
				else if (t1.equals(".negative"))
					t1 = "-";
				postFeatures.add("@" + t1 + "|" + t2);
				i++;
			} else {
				postFeatures.add(t);
			}
		}

		return new ArrayList<>(postFeatures);
	}

	public static List<String> infixToPostfix(final List<String> tokens) {
		final Deque<String> stack = new ArrayDeque<>();
		final Deque<String> output = new ArrayDeque<>();
		int pCount = 0;
		int bCount = 0;
		int cCount = 0;

		for (final String token : tokens) {
			if ((Character.isLetterOrDigit(token.charAt(0)) || token.charAt(0) == '_' || token.charAt(0) == '\"'
					|| token.charAt(0) == '@' || token.charAt(0) == '\'' || token.charAt(0) == '`')
					&& !operators.containsKey(token))
				output.addLast(token);
			else if (operators.containsKey(token)) {
				if (!stack.isEmpty()) {
					String sb = stack.peekLast();
					while ((Math.abs(getPrecedence(token)) < Math.abs(getPrecedence(sb))
							|| (getPrecedence(sb) > 0 && Math.abs(getPrecedence(token)) == Math.abs(getPrecedence(sb))))
							&& (!sb.equals("(") && !sb.equals("[") && !sb.equals("{"))) {
						output.addLast(sb);
						stack.pollLast();
						if (stack.isEmpty())
							break;
						else
							sb = stack.peekLast();
					}
				}
				stack.addLast(token);
			} else if (token.equals("(") || token.equals("[") || token.equals("{")) {
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
			} else if (token.equals(")") || token.equals("]") || token.equals("}")) {
				switch (token.charAt(0)) {
					case ')':
						pCount--;
						if (stack.isEmpty())
							throw new SonoCompilationException("Unmatched parenthesis!");
						while (!stack.peekLast().equals("(")) {
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
						while (!stack.peekLast().equals("[")) {
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
						while (!stack.peekLast().equals("{")) {
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