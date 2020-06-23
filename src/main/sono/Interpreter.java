package main.sono;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import main.base.CommandManager;
import main.Main;
import main.phl.*;
import main.sono.err.SonoCompilationException;

public class Interpreter {
	private final Scope main;
	private final PhoneManager pl;
	private final CommandManager console;
	private static List<String> variableHash = new ArrayList<>();

	private final List<String> loadedFiles;

	public static final int INIT = 0;
	public static final int THIS = 1;
	public static final int GETSTR = 2;
	public static final int GETLEN = 3;
	public static final int GETINDEX = 4;
	public static final int GETLIST = 5;
	public static final int ERROR = 6;
	public static final int TRACE = 7;
	public static final int SRULE = 8;
	public static final int AFRULE = 9;
	public static final int ABRULE = 10;
	public static final int SQUAREBRACKET = 11;
	public static final int CURLYBRACKET = 12;
	public static final int PARANTHESIS = 13;

	public Interpreter(final Scope main, final PhoneManager pl, final CommandManager console) {
		this.main = main;
		this.pl = pl;
		this.loadedFiles = new ArrayList<>();
		this.console = console;
		final List<Datum> data = new ArrayList<>();
		for (final Phone p : pl.getAllPhones()) {
			data.add(new Datum(p));
		}
		final List<Datum> dataBase = new ArrayList<>();
		for (final Phone p : pl.getBasePhones()) {
			dataBase.add(new Datum(p));
		}
		final Datum d = new Datum(data);
		d.setMutable(false);
		final Datum db = new Datum(dataBase);
		db.setMutable(false);

		hashVariable("init");
		hashVariable("this");
		hashVariable("getStr");
		hashVariable("getLen");
		hashVariable("getIndex");
		hashVariable("getList");
		hashVariable("_e");
		hashVariable("_trace");
		hashVariable("S");
		hashVariable("Af");
		hashVariable("Ab");
		hashVariable("[");
		hashVariable("{");
		hashVariable("(");

		main.setVariable(hashVariable("_all"), d, new ArrayList<>());
		main.setVariable(hashVariable("_base"), db, new ArrayList<>());
	}

	private static int hashVariable(final String key) {
		if (!variableHash.contains(key))
			variableHash.add(key);
		return variableHash.indexOf(key);
	}

	public static String deHash(final int key) {
		return variableHash.get(key);
	}

	public Datum runCode(final String directory, final String code) {
		return evaluate(parse(directory, tokenize(code)));
	}

	public Datum evaluate(final Operator o) {
		return o.evaluate(main, this, new ArrayList<>());
	}

	private List<String> tokenize(final String code) {
		List<String> tokens = null;
		tokens = Tokenizer.tokenize(code);
		tokens = Tokenizer.infixToPostfix(tokens);
		return tokens;
	}

	public List<String> loadFile(final String directory, final String filename) {
		final StringBuilder contents = new StringBuilder();
		File file = new File(directory, filename);
		try {
			if (!file.exists())
				file = new File(Main.getGlobalOption("PATH"), "lib/" + filename);

			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				String line;
				while ((line = br.readLine()) != null)
					contents.append(line + "\n");
			}
		} catch (final Exception e) {
			throw new SonoCompilationException("File <" + file.toString() + "> does not exist.");
		}
		return tokenize(contents.toString());
	}

	public Operator parse(final String directory, final List<String> tokens) {
		final Deque<Operator> o = new ArrayDeque<>();

		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if (token.equals(";") || token.equals(","))
				continue;
			if (Tokenizer.operators.containsKey(token)) {
				if (token.equals("load")) {
					final String path = ((Operator.Container) o.pollLast()).getDatum().getString(new ArrayList<>());
					final String[] split = (new StringBuilder(path)).reverse().toString().split("/", 2);
					final String filename = (new StringBuilder(split[0])).reverse().toString() + ".so";
					String fileDirectory = directory;
					if (split.length > 1)
						fileDirectory += "/" + (new StringBuilder(split[1])).reverse().toString();
					if (!loadedFiles.contains(fileDirectory + "/" + filename)) {
						loadedFiles.add(fileDirectory + "/" + filename);
						o.addLast(parse(fileDirectory, loadFile(fileDirectory, filename)));
					}
				}
				if (token.equals("import")) {
					final String path = ((Operator.Container) o.pollLast()).getDatum().getString(new ArrayList<>());
					final String[] split = (new StringBuilder(path)).reverse().toString().split("/", 2);
					final String filename = (new StringBuilder(split[0])).reverse().toString() + ".jar";
					String fileDirectory = directory;
					if (split.length > 1)
						fileDirectory += "/" + (new StringBuilder(split[1])).reverse().toString();
					if (!loadedFiles.contains(fileDirectory + "/" + filename)) {
						loadedFiles.add(fileDirectory + "/" + filename);
						console.importLibrary(fileDirectory, filename, "ext." + path);
					}
				}
				if (token.equals(".negative")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Sub(new Operator.Container(new Datum(new BigDecimal(0))), a));
				}
				if (token.equals(".positive")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Add(new Operator.Container(new Datum(new BigDecimal(0))), a));
				}
				if (token.equals("new")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.NewDec(a));
				}
				if (token.equals("len")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Length(a));
				}
				if (token.equals("return")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Return(a));
				}
				if (token.equals("var")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.VarDec(((Operator.Variable) a).getKey()));
				}
				if (token.equals("struct")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.StructDec(((Operator.Variable) a).getKey()));
				}
				if (token.equals("static")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.StaticDec(((Operator.Variable) a).getKey()));
				}
				if (token.equals("ref")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Ref(((Operator.Variable) a).getKey()));
				}
				if (token.equals("final")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Final(((Operator.Variable) a).getKey()));
				}
				if (token.equals("com")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Common(a));
				}
				if (token.equals("word")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.SeqDec(a));
				}
				if (token.equals("type")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.TypeConv(a));
				}
				if (token.equals("vec")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.ListDec(a));
				}
				if (token.equals("mat")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.MatConv(a));
				}
				if (token.equals("num")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.NumConv(a));
				}
				if (token.equals("str")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.StringDec(a));
				}
				if (token.equals("char")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Char(a));
				}
				if (token.equals("feat")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.FeatDec(a));
				}
				if (token.equals("alloc")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Alloc(a));
				}
				if (token.equals("throw")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Throw(a));
				}
				if (token.equals("try")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.TryCatch(a));
				}
				if (token.equals("=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(a, b));
				}
				if (token.equals("::")) {
					Operator b = o.pollLast();
					final Operator a = o.pollLast();
					if (b.type == Operator.Type.SOFT_LIST)
						b = new Operator.HardList(((Operator.Sequence) b).getVector());
					o.addLast(new Operator.TypeDec(a, b));
				}
				if (token.equals("->")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Arrow(a, b));
				}
				if (token.equals("~")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Underscore(a, b));
				}
				if (token.equals("//")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Slash(a, b));
				}
				if (token.equals(">>")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Transform(a, b));
				}
				if (token.equals("+")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Add(a, b));
				}
				if (token.equals("+=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(a, new Operator.Add(a, b)));
				}
				if (token.equals("-")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Sub(a, b));
				}
				if (token.equals("-=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(a, new Operator.Sub(a, b)));
				}
				if (token.equals("*")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Mul(a, b));
				}
				if (token.equals("*=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(a, new Operator.Mul(a, b)));
				}
				if (token.equals("/")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Div(a, b));
				}
				if (token.equals("/=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(a, new Operator.Div(a, b)));
				}
				if (token.equals("%")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Mod(a, b));
				}
				if (token.equals("%=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(a, new Operator.Mod(a, b)));
				}
				if (token.equals("**")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Pow(a, b));
				}
				if (token.equals("**=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(a, new Operator.Pow(a, b)));
				}
				if (token.equals("class")) {
					Operator b = o.pollLast();
					final Operator a = o.pollLast();
					b = new Operator.SoftList(((Operator.Sequence) b).getVector());
					o.addLast(new Operator.ClassDec(a, b));
				}
				if (token.equals(".index")) {
					final Operator b = new Operator.SoftList(((Operator.MatrixDec) o.pollLast()).operators);
					final Operator a = o.pollLast();
					o.addLast(new Operator.Index(a, b));
				}
				if (token.equals("?>")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Contrast(a, b));
				}
				if (token.equals("==")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Equal(a, b));
				}
				if (token.equals("!=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.NEqual(a, b));
				}
				if (token.equals("<")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Less(a, b));
				}
				if (token.equals(">")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.More(a, b));
				}
				if (token.equals("<=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.ELess(a, b));
				}
				if (token.equals(">=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.EMore(a, b));
				}
				if (token.equals("&&")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.And(a, b));
				}
				if (token.equals("||")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Or(a, b));
				}
				if (token.equals(".")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Inner(a, b));
				}
				if (token.equals("from")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Find(a, b));
				}
				if (token.equals("in")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Iterator(a, b));
				}
				if (token.equals("until")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.RangeUntil(a, b));
				}
				if (token.equals("do")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Loop(a, b));
				}
				if (token.equals("=>")) {
					// (((VAR) :: (pop)) .exec (params)) => {}
					final Operator b = o.pollLast();
					Operator a = o.pollLast();
					if (a.type == Operator.Type.EXECUTE) {
						final Operator name = ((Operator.Execute) a).getA();
						if (name.type == Operator.Type.TYPE_DEC) {
							final Operator typeDec = ((Operator.TypeDec) name).getA();
							final Operator fName = ((Operator.TypeDec) name).getB();
							a = new Operator.HardList(((Operator.Sequence) ((Operator.Execute) a).getB()).getVector());
							o.addLast(new Operator.Set(new Operator.VarDec(((Operator.Variable) fName).getKey()),
									new Operator.Lambda(new Operator.TypeDec(typeDec, a), b)));
						} else {
							a = new Operator.HardList(((Operator.Sequence) ((Operator.Execute) a).getB()).getVector());
							o.addLast(new Operator.Set(new Operator.VarDec(((Operator.Variable) name).getKey()),
									new Operator.Lambda(a, b)));
						}
					} else {
						if (a.type != Operator.Type.TYPE_DEC)
							a = new Operator.HardList(((Operator.Sequence) a).getVector());
						o.addLast(new Operator.Lambda(a, b));
					}
				}
				if (token.equals(".exec")) {
					Operator b = o.pollLast();
					final Operator a = o.pollLast();
					b = new Operator.HardList(((Operator.Sequence) b).getVector());
					o.addLast(new Operator.Execute(a, b));
				}
				if (token.equals("then")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.IfElse(a, b));
				}
				if (token.equals("else")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					((Operator.IfElse) a).setElse(b);
					o.addLast(a);
				}
				if (token.equals("catch")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					((Operator.TryCatch) a).setCatch(b);
					o.addLast(a);
				}
				if (token.equals("_OUTER_CALL_")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.OuterCall(a, b));
				}
				if (token.equals(":")) {
					Rule.Type rtype = null;
					final Operator b = o.pollLast();
					final int a = ((Operator.Variable) o.pollLast()).getKey();
					if (a == SRULE)
						rtype = Rule.Type.SIMPLE;
					if (a == AFRULE)
						rtype = Rule.Type.A_FORWARD;
					if (a == ABRULE)
						rtype = Rule.Type.A_BACKWARD;
					o.addLast(new Operator.RuleDec(rtype, b));
				}
			} else if (token.equals("]")) {
				final List<Operator> list = new ArrayList<>();
				Operator curr = o.pollLast();
				while (!(curr.type == Operator.Type.VARIABLE
						&& ((Operator.Variable) curr).getKey() == Interpreter.SQUAREBRACKET)) {
					list.add(0, curr);
					curr = o.pollLast();
				}
				o.addLast(new Operator.MatrixDec(list));
			} else if (token.equals(")")) {
				final List<Operator> list = new ArrayList<>();
				Operator curr = o.pollLast();
				while (!(curr.type == Operator.Type.VARIABLE
						&& ((Operator.Variable) curr).getKey() == Interpreter.PARANTHESIS)) {
					list.add(0, curr);
					curr = o.pollLast();
				}
				o.addLast(new Operator.SoftList(list));
			} else if (token.equals("}")) {
				final List<Operator> list = new ArrayList<>();
				Operator curr = o.pollLast();
				while (!(curr.type == Operator.Type.VARIABLE
						&& ((Operator.Variable) curr).getKey() == Interpreter.CURLYBRACKET)) {
					list.add(0, curr);
					curr = o.pollLast();
				}
				o.addLast(new Operator.HardList(list));
			} else if (token.charAt(0) == '\'') {
				if (Main.getGlobalOption("LING").equals("FALSE"))
					throw new SonoCompilationException(
							"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.");
				final Phone p = pl.interpretSegment(token.substring(1));
				o.addLast(new Operator.Container(new Datum(p)));
			} else if (token.charAt(0) == '@') {
				if (Main.getGlobalOption("LING").equals("FALSE"))
					throw new SonoCompilationException(
							"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.");
				final Pair p = pl.interpretFeature(token.substring(1));
				o.addLast(new Operator.Container(new Datum(p)));
			} else if (token.charAt(0) == '`') {
				if (Main.getGlobalOption("LING").equals("FALSE"))
					throw new SonoCompilationException(
							"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.");
				final Word p = pl.interpretSequence(token.substring(1));
				o.addLast(new Operator.Container(new Datum(p)));
			} else if (token.charAt(0) == '\"') {
				final String s = token.substring(1);
				o.addLast(new Operator.Container(new Datum(s)));
			} else if (Character.isDigit(token.charAt(0))) {
				if (token.charAt(token.length() - 1) == 'D')
					token = token.substring(0, token.length() - 1);
				o.addLast(new Operator.Container(new Datum(new BigDecimal(token))));
			} else if (token.equals("null")) {
				o.addLast(new Operator.Container(new Datum()));
			} else if (token.equals("Vector")) {
				o.addLast(new Operator.Container(new Datum(Datum.Type.VECTOR)));
			} else if (token.equals("Number")) {
				o.addLast(new Operator.Container(new Datum(Datum.Type.NUMBER)));
			} else if (token.equals("Function")) {
				o.addLast(new Operator.Container(new Datum(Datum.Type.FUNCTION)));
			} else if (token.equals("String")) {
				o.addLast(new Operator.Container(new Datum(Datum.Type.STRING)));
			} else if (token.equals("Phone")) {
				o.addLast(new Operator.Container(new Datum(Datum.Type.PHONE)));
			} else if (token.equals("Feature")) {
				o.addLast(new Operator.Container(new Datum(Datum.Type.PAIR)));
			} else if (token.equals("Matrix")) {
				o.addLast(new Operator.Container(new Datum(Datum.Type.MATRIX)));
			} else if (token.equals("Rule")) {
				o.addLast(new Operator.Container(new Datum(Datum.Type.RULE)));
			} else if (token.equals("Word")) {
				o.addLast(new Operator.Container(new Datum(Datum.Type.WORD)));
			} else if (token.equals("true")) {
				o.addLast(new Operator.Container(new Datum(BigDecimal.valueOf(1))));
			} else if (token.equals("false")) {
				o.addLast(new Operator.Container(new Datum(BigDecimal.valueOf(0))));
			} else if (token.equals("break")) {
				o.addLast(new Operator.Break());
			} else {
				o.addLast(new Operator.Variable(hashVariable(token)));
			}
		}

		return new Operator.SoftList(Arrays.asList(o.toArray(new Operator[0])));
	}

	public static <T> String stringFromList(final List<T> list, final String init, final String fin) {
		final StringBuilder s = new StringBuilder(init);
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