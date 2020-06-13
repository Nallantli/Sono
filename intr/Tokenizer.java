package intr;

import java.util.Map;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import phl.Phone;

import static java.util.Map.entry;

public class Tokenizer {
	private Tokenizer() {
		throw new IllegalStateException("Utility class");
	}

	protected static final Map<String, Integer> operators = Map.ofEntries(entry("load", -13), entry("let", -13),
			entry("return", 2), entry("=>", 12), entry("len", -11), entry("word", -11), entry("str", -11),
			entry("from", 10), entry("mat", -11), entry("num", -11), entry("list", -11), entry("com", -11),
			entry(".negative", -11), entry(".positive", -11), entry(".index", -15), entry("&", -15), entry(".exec", -15), entry(".", 14),
			entry("**", -10), entry("*", 9), entry("/", 9), entry("%", 9), entry("+", 8), entry("-", 8), entry(">>", 8),
			entry("?>", 8), entry("==", 6), entry("!=", 6), entry("<", 6), entry(">", 6), entry("<=", 6),
			entry(">=", 6), entry("&&", 5), entry("||", 5), entry("=", -4), entry("->", 7), entry("//", 6),
			entry("~", 7), entry("in", 3), entry("switch", 3), entry("do", 3), entry(":", 5), entry("until", 4),
			entry("then", 3), entry("else", -3), entry(",", 2), entry(";", -1));

	public static List<String> tokenize(String str) {
		char pChar = 0;
		int mode = 0;
		char lastChar = 0;
		boolean comment = false;
		List<StringBuilder> tokens = new ArrayList<>();
		tokens.add(new StringBuilder("#["));

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
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
							int newChar = (c * 100) + (str.charAt(i + 1) * 10) + (str.charAt(i + 2));
							i += 2;
							tokens.get(tokens.size() - 1).append(newChar);
							lastChar = (char) newChar;
							continue;
						} else {
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

		List<String> newTokens = new ArrayList<>();
		String last = "+";
		for (StringBuilder raw : tokens) {
			String t = raw.toString();
			if (t.equals("#[") || t.equals("#]"))
				continue;
			if (t.equals("[")
					&& ((Character.isLetterOrDigit(last.charAt(0)) || last.charAt(0) == '\"' || last.charAt(0) == '`')
							|| last.equals(")") || last.equals("]") || last.equals("}"))
					&& !operators.containsKey(last))
				newTokens.add(".index");
			if (t.equals("(") && ((Character.isLetterOrDigit(last.charAt(0)) || last.charAt(0) == '`')
					|| last.equals(")") || last.equals("]") || last.equals("}")) && !operators.containsKey(last))
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
				String temp = newTokens.get(newTokens.size() - 1) + "." + t + "D";
				newTokens.set(newTokens.size() - 1, temp);
				last = t;
				continue;
			}
			newTokens.add(t);
			last = t;
		}

		Deque<String> postFeatures = new ArrayDeque<>();
		for (int i = 0; i < newTokens.size(); i++) {
			String t = newTokens.get(i);
			if (t.equals(".positive") || t.equals(".negative") || t.equals("~")) {
				String t2 = newTokens.get(i + 1);
				boolean flag = false;
				for (int j = 0; j < Phone.Feature.values().length; j++) {
					if (t2.equals(Phone.Feature.values()[j].toString())) {
						flag = true;
						break;
					}
				}
				if (flag) {
					postFeatures.add("@" + (t.equals(".positive") ? "+" : "-") + t2);
					i++;
				} else {
					postFeatures.add(t);
				}
			} else if (t.equals("A") || t.equals("B") || t.equals("C")) {
				String t2 = newTokens.get(i + 1);
				String t3 = newTokens.get(i + 2);
				boolean flag = false;
				for (int j = 0; j < Phone.Feature.values().length; j++) {
					if (t3.equals(Phone.Feature.values()[j].toString())) {
						flag = true;
						break;
					}
				}
				if (t2.equals("-") && flag) {
					postFeatures.add("@" + t + t2 + t3);
					i += 2;
				} else {
					postFeatures.add(t);
				}
			} else {
				postFeatures.add(t);
			}
		}

		return new ArrayList<>(postFeatures);
	}

	public static List<String> infixToPostfix(List<String> tokens) {
		Deque<String> stack = new ArrayDeque<>();
		Deque<String> output = new ArrayDeque<>();
		int pCount = 0;
		int bCount = 0;
		int cCount = 0;

		for (String token : tokens) {
			if ((Character.isLetterOrDigit(token.charAt(0)) || token.charAt(0) == '_' || token.charAt(0) == '\"' || token.charAt(0) == '@'
					|| token.charAt(0) == '\'' || token.charAt(0) == '`') && !operators.containsKey(token))
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
							throw new SonoCompilationException("Unmatched paranthesis!");
						while (!stack.peekLast().equals("(")) {
							output.addLast(stack.peekLast());
							stack.pollLast();
							if (stack.isEmpty())
								throw new SonoCompilationException("Unmatched paranthesis!");
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
			throw new SonoCompilationException("Unmatched paranthesis!");
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

	private static int getPrecedence(String token) {
		if (operators.containsKey(token))
			return operators.get(token);

		return 999;
	}
}