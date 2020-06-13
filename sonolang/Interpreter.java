package sonolang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import main.CommandManager;
import phl.*;

public class Interpreter {
	private Scope main;
	private PhoneManager pl;
	private CommandManager console;

	List<String> loadedFiles;

	public Interpreter(Scope main, PhoneManager pl, CommandManager console) {
		this.main = main;
		this.pl = pl;
		this.loadedFiles = new ArrayList<>();
		this.console = console;
		List<Datum> data = new ArrayList<>();
		for (Phone p : pl.getAllPhones()) {
			data.add(new Datum(p));
		}
		Datum d = new Datum(data);
		d.setMutable(false);
		main.setVariable("all", d);
	}

	public Datum runCode(String directory, String code) throws IOException {
		return evaluate(parse(directory, tokenize(code)));
	}

	public Datum evaluate(Operator o) {
		return o.evaluate(main, this);
	}

	private List<String> tokenize(String code) {
		List<String> tokens = null;
		tokens = Tokenizer.tokenize(code);
		System.out.println(tokens);
		tokens = Tokenizer.infixToPostfix(tokens);
		System.out.println(tokens);
		return tokens;
	}

	public List<String> loadFile(String directory, String filename) throws IOException {
		File file = new File(directory, filename);

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			StringBuilder contents = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null)
				contents.append(line + "\n");
			return tokenize(contents.toString());
		}
	}

	public Operator parse(String directory, List<String> tokens) throws IOException {
		Deque<Operator> o = new ArrayDeque<>();

		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if (token.equals(";") || token.equals(","))
				continue;
			if (Tokenizer.operators.containsKey(token)) {
				if (token.equals("load")) {
					String path = ((Operator.Container) o.pollLast()).getDatum().getString();
					String[] split = (new StringBuilder(path)).reverse().toString().split("/", 2);
					String filename = (new StringBuilder(split[0])).reverse().toString() + ".sn";
					String fileDirectory = directory;
					if (split.length > 1)
						fileDirectory += "/" + (new StringBuilder(split[1])).reverse().toString();
					if (!loadedFiles.contains(fileDirectory + "/" + filename)) {
						loadedFiles.add(fileDirectory + "/" + filename);
						try {
							o.addLast(parse(fileDirectory, loadFile(fileDirectory, filename)));
						} catch (IOException e) {
							throw new IOException("Cannot load file <" + fileDirectory + "/" + filename + ">");
						}
					}
				}
				if (token.equals(".negative")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.Sub(new Operator.Container(new Datum(new BigDecimal(0))), a));
				}
				if (token.equals(".positive")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.Add(new Operator.Container(new Datum(new BigDecimal(0))), a));
				}
				if (token.equals("len")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.Length(a));
				}
				if (token.equals("return")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.Return(a));
				}
				if (token.equals("let")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.VarDec(((Operator.Variable) a).getKey()));
				}
				if (token.equals("&")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.Ref(((Operator.Variable) a).getKey()));
				}
				if (token.equals("com")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.Common(a));
				}
				if (token.equals("word")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.SeqDec(a));
				}
				if (token.equals("join")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.Join(a));
				}
				if (token.equals("list")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.ListDec(a));
				}
				if (token.equals("mat")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.MatConv(a));
				}
				if (token.equals("num")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.NumConv(a));
				}
				if (token.equals("str")) {
					Operator a = o.pollLast();
					o.addLast(new Operator.StringDec(a));
				}
				if (token.equals("=")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Set(a, b));
				}
				if (token.equals("->")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Arrow(a, b));
				}
				if (token.equals("~")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Underscore(a, b));
				}
				if (token.equals("//")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Slash(a, b));
				}
				if (token.equals(">>")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Transform(a, b));
				}
				if (token.equals("+")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Add(a, b));
				}
				if (token.equals("-")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Sub(a, b));
				}
				if (token.equals("*")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Mul(a, b));
				}
				if (token.equals("/")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Div(a, b));
				}
				if (token.equals("%")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Mod(a, b));
				}
				if (token.equals(".index")) {
					Operator b = new Operator.SoftList(((Operator.MatrixDec) o.pollLast()).operators);
					Operator a = o.pollLast();
					o.addLast(new Operator.Index(a, b));
				}
				if (token.equals("?>")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Contrast(a, b));
				}
				if (token.equals("==")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Equal(a, b));
				}
				if (token.equals("!=")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.NEqual(a, b));
				}
				if (token.equals("<")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Less(a, b));
				}
				if (token.equals(">")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.More(a, b));
				}
				if (token.equals("<=")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.ELess(a, b));
				}
				if (token.equals(">=")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.EMore(a, b));
				}
				if (token.equals("&&")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.And(a, b));
				}
				if (token.equals("||")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Or(a, b));
				}
				if (token.equals(".")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Inner(a, b));
				}
				if (token.equals("from")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Find(a, b));
				}
				if (token.equals("in")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Iterator(a, b));
				}
				if (token.equals("until")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.RangeUntil(a, b));
				}
				if (token.equals("do")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.Loop(a, b));
				}
				if (token.equals("=>")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					a = new Operator.HardList(((Operator.Sequence) a).getList());
					o.addLast(new Operator.Lambda(a, b));
				}
				if (token.equals(".exec")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					b = new Operator.HardList(((Operator.Sequence) b).getList());
					o.addLast(new Operator.Execute(a, b));
				}
				if (token.equals("then")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.IfElse(a, b));
				}
				if (token.equals("else")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					((Operator.IfElse) a).setElse(b);
					o.addLast(a);
				}
				if (token.equals("_OUTER_CALL_")) {
					Operator b = o.pollLast();
					Operator a = o.pollLast();
					o.addLast(new Operator.OuterCall(a, b));
				}
				if (token.equals(":")) {
					Rule.Type rtype = null;
					Operator b = o.pollLast();
					String a = ((Operator.Variable) o.pollLast()).getKey();
					if (a.equals("S"))
						rtype = Rule.Type.SIMPLE;
					if (a.equals("Af"))
						rtype = Rule.Type.A_FORWARD;
					if (a.equals("Ab"))
						rtype = Rule.Type.A_BACKWARD;
					o.addLast(new Operator.RuleDec(rtype, b));
				}
			} else if (token.equals("]")) {
				List<Operator> list = new ArrayList<>();
				Operator curr = null;
				while (!(curr = o.pollLast()).toString().equals("[")) {
					list.add(0, curr);
				}
				o.addLast(new Operator.MatrixDec(list));
			} else if (token.equals(")")) {
				List<Operator> list = new ArrayList<>();
				Operator curr = null;
				while (!(curr = o.pollLast()).toString().equals("(")) {
					list.add(0, curr);
				}
				o.addLast(new Operator.SoftList(list));
			} else if (token.equals("}")) {
				List<Operator> list = new ArrayList<>();
				Operator curr = null;
				while (!(curr = o.pollLast()).toString().equals("{")) {
					list.add(0, curr);
				}
				o.addLast(new Operator.HardList(list));
			} else if (token.charAt(0) == '\'') {
				Phone p = pl.interpretSegment(token.substring(1));
				o.addLast(new Operator.Container(new Datum(p)));
			} else if (token.charAt(0) == '@') {
				Pair p = pl.interpretFeature(token.substring(1));
				o.addLast(new Operator.Container(new Datum(p)));
			} else if (token.charAt(0) == '`') {
				Word p = pl.interpretSequence(token.substring(1));
				o.addLast(new Operator.Container(new Datum(p)));
			} else if (token.charAt(0) == '\"') {
				String s = token.substring(1);
				o.addLast(new Operator.Container(new Datum(s)));
			} else if (Character.isDigit(token.charAt(0))) {
				if (token.charAt(token.length() - 1) == 'D')
					token = token.substring(0, token.length() - 1);
				o.addLast(new Operator.Container(new Datum(new BigDecimal(token))));
			} else if (token.equals("null")) {
				o.addLast(new Operator.Container(new Datum()));
			} else if (token.equals("true")) {
				o.addLast(new Operator.Container(new Datum(BigDecimal.valueOf(1))));
			} else if (token.equals("false")) {
				o.addLast(new Operator.Container(new Datum(BigDecimal.valueOf(0))));
			} else if (token.equals("break")) {
				o.addLast(new Operator.Break());
			} else {
				o.addLast(new Operator.Variable(token));
			}
		}

		return new Operator.SoftList(Arrays.asList(o.toArray(new Operator[0])));
	}

	public static <T> String stringFromList(List<T> list, String init, String fin) {
		StringBuilder s = new StringBuilder(init);
		for (int i = 0; i < list.size(); i++) {
			if (i > 0)
				s.append(", ");
			s.append(list.get(i).toString());
		}
		s.append(fin);
		return s.toString();
	}

	public PhoneManager getManager() {
		return this.pl;
	}

	public CommandManager getCommandManager() {
		return this.console;
	}
}