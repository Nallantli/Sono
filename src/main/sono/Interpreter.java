package main.sono;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.base.CommandManager;
import main.phl.Pair;
import main.phl.Phone;
import main.phl.PhoneManager;
import main.phl.Rule;
import main.phl.Word;
import main.SonoWrapper;
import main.sono.err.SonoCompilationException;
import main.sono.err.SonoException;
import main.sono.io.Input;
import main.sono.io.Output;

public class Interpreter {
	private final Scope main;
	private final PhoneManager pl;
	private final CommandManager console;
	private final List<String> variableHash;
	private final Output stdout;
	private final Output stderr;
	private final Input stdin;

	private final List<String> loadedFiles;

	public final int INIT;
	public final int THIS;
	public final int GET_STR;
	public final int GET_LEN;
	public final int GET_INDEX;
	public final int GET_LIST;
	public final int ERROR;
	public final int TRACE;
	public final int S_RULE;
	public final int AF_RULE;
	public final int AB_RULE;
	public final int SQUARE_BRACKET;
	public final int CURLY_BRACKET;
	public final int PARENTHESES;
	public final int ALL;
	public final int BASE;

	public Interpreter(final Scope main, final PhoneManager pl, final CommandManager console, final Output stdout,
			final Output stderr, final Input stdin) {
		this.stdout = stdout;
		this.stderr = stderr;
		this.stdin = stdin;
		this.main = main;
		this.pl = pl;
		this.loadedFiles = new ArrayList<>();
		this.console = console;

		variableHash = new ArrayList<>();

		INIT = hashVariable("init");
		THIS = hashVariable("this");
		GET_STR = hashVariable("getStr");
		GET_LEN = hashVariable("getLen");
		GET_INDEX = hashVariable("getIndex");
		GET_LIST = hashVariable("getVec");
		ERROR = hashVariable("_e");
		TRACE = hashVariable("_trace");
		S_RULE = hashVariable("S");
		AF_RULE = hashVariable("Af");
		AB_RULE = hashVariable("Ab");
		SQUARE_BRACKET = hashVariable("[");
		CURLY_BRACKET = hashVariable("{");
		PARENTHESES = hashVariable("(");
		ALL = hashVariable("_all");
		BASE = hashVariable("_base");

		if (this.pl != null) {
			final List<Datum> data = new ArrayList<>();
			for (final Phone p : pl.getAllPhones()) {
				data.add(new Datum(p));
			}
			final List<Datum> dataBase = new ArrayList<>();
			for (final Phone p : pl.getBasePhones()) {
				dataBase.add(new Datum(p));
			}
			final Datum d = new Datum(data.toArray(new Datum[0]));
			d.setMutable(false);
			final Datum db = new Datum(dataBase.toArray(new Datum[0]));
			db.setMutable(false);

			main.setVariable(this, ALL, d, new ArrayList<>());
			main.setVariable(this, BASE, db, new ArrayList<>());
		}
	}

	public Scope getScope() {
		return this.main;
	}

	private int hashVariable(final String key) {
		if (!variableHash.contains(key))
			variableHash.add(key);
		return variableHash.indexOf(key);
	}

	public String deHash(final int key) {
		return variableHash.get(key);
	}

	public Datum runCode(final String directory, final String code) {
		return evaluate(parse(directory, tokenize(code)));
	}

	public Datum evaluate(final Operator o) {
		try {
			return o.evaluate(main, new ArrayList<>());
		} catch (final SonoException e) {
			stderr.println(e.getMessage());
			stderr.print(e.getStackString());
			return new Datum();
		}
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
				file = new File(SonoWrapper.getGlobalOption("PATH"), "lib/" + filename);

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
				if (token.equals("!")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.NEqual(this, new Operator.Container(this, new Datum(1)), a));
				}
				if (token.equals(".negative")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Sub(this, new Operator.Container(this, new Datum(0)), a));
				}
				if (token.equals(".positive")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Add(this, new Operator.Container(this, new Datum(0)), a));
				}
				if (token.equals("new")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.NewDec(this, a));
				}
				if (token.equals("len")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Length(this, a));
				}
				if (token.equals("return")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Return(this, a));
				}
				if (token.equals("refer")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Refer(this, a));
				}
				if (token.equals("var")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.VarDec(this, ((Operator.Variable) a).getKey()));
				}
				if (token.equals("struct")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.StructDec(this, ((Operator.Variable) a).getKey()));
				}
				if (token.equals("static")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.StaticDec(this, ((Operator.Variable) a).getKey()));
				}
				if (token.equals("ref")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Ref(this, ((Operator.Variable) a).getKey()));
				}
				if (token.equals("final")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Final(this, ((Operator.Variable) a).getKey()));
				}
				if (token.equals("com")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Common(this, a));
				}
				if (token.equals("word")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.SeqDec(this, a));
				}
				if (token.equals("type")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.TypeConvert(this, a));
				}
				if (token.equals("vec")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.ListDec(this, a));
				}
				if (token.equals("mat")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.MatConvert(this, a));
				}
				if (token.equals("num")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.NumConvert(this, a));
				}
				if (token.equals("str")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.StringDec(this, a));
				}
				if (token.equals("char")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Char(this, a));
				}
				if (token.equals("code")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Code(this, a));
				}
				if (token.equals("feat")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.FeatDec(this, a));
				}
				if (token.equals("alloc")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Allocate(this, a));
				}
				if (token.equals("throw")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.Throw(this, a));
				}
				if (token.equals("try")) {
					final Operator a = o.pollLast();
					o.addLast(new Operator.TryCatch(this, a));
				}
				if (token.equals("register")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Register(this, a, b));
				}
				if (token.equals("=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(this, a, b));
				}
				if (token.equals("::")) {
					Operator b = o.pollLast();
					final Operator a = o.pollLast();
					if (b.type == Operator.Type.SOFT_LIST)
						b = new Operator.HardList(this, ((Operator.Sequence) b).getVector());
					o.addLast(new Operator.TypeDec(this, a, b));
				}
				if (token.equals("->")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Arrow(this, a, b));
				}
				if (token.equals("~")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Underscore(this, a, b));
				}
				if (token.equals("//")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Slash(this, a, b));
				}
				if (token.equals(">>")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Transform(this, a, b));
				}
				if (token.equals(">>=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(this, a, new Operator.Transform(this, a, b)));
				}
				if (token.equals("+")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Add(this, a, b));
				}
				if (token.equals("+=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(this, a, new Operator.Add(this, a, b)));
				}
				if (token.equals("-")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Sub(this, a, b));
				}
				if (token.equals("-=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(this, a, new Operator.Sub(this, a, b)));
				}
				if (token.equals("*")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Mul(this, a, b));
				}
				if (token.equals("*=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(this, a, new Operator.Mul(this, a, b)));
				}
				if (token.equals("/")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Div(this, a, b));
				}
				if (token.equals("/=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(this, a, new Operator.Div(this, a, b)));
				}
				if (token.equals("%")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Mod(this, a, b));
				}
				if (token.equals("%=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(this, a, new Operator.Mod(this, a, b)));
				}
				if (token.equals("**")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Pow(this, a, b));
				}
				if (token.equals("**=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Set(this, a, new Operator.Pow(this, a, b)));
				}
				if (token.equals("class")) {
					Operator b = o.pollLast();
					final Operator a = o.pollLast();
					b = new Operator.SoftList(this, ((Operator.Sequence) b).getVector());
					o.addLast(new Operator.ClassDec(this, a, b));
				}
				if (token.equals(".index")) {
					final Operator b = ((Operator.MatrixDec) o.pollLast()).operators.get(0);
					final Operator a = o.pollLast();
					o.addLast(new Operator.Index(this, a, b));
				}
				if (token.equals("?>")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Contrast(this, a, b));
				}
				if (token.equals("==")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Equal(this, a, b));
				}
				if (token.equals("!=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.NEqual(this, a, b));
				}
				if (token.equals("<")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Less(this, a, b));
				}
				if (token.equals(">")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.More(this, a, b));
				}
				if (token.equals("<=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.ELess(this, a, b));
				}
				if (token.equals(">=")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.EMore(this, a, b));
				}
				if (token.equals("&&")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.And(this, a, b));
				}
				if (token.equals("||")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Or(this, a, b));
				}
				if (token.equals(".")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Inner(this, a, b));
				}
				if (token.equals("from")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Find(this, a, b));
				}
				if (token.equals("in")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.Iterator(this, a, b));
				}
				if (token.equals("until")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.RangeUntil(this, a, b));
				}
				if (token.equals("do")) {
					Operator b = o.pollLast();
					if (b.type == Operator.Type.HARD_LIST)
						b = new Operator.SoftList(this, b.getChildren());
					final Operator a = o.pollLast();
					o.addLast(new Operator.Loop(this, a, b));
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
							a = new Operator.HardList(this,
									((Operator.Sequence) ((Operator.Execute) a).getB()).getVector());
							o.addLast(new Operator.Set(this,
									new Operator.VarDec(this, ((Operator.Variable) fName).getKey()),
									new Operator.Lambda(this, new Operator.TypeDec(this, typeDec, a), b)));
						} else {
							a = new Operator.HardList(this,
									((Operator.Sequence) ((Operator.Execute) a).getB()).getVector());
							o.addLast(new Operator.Set(this,
									new Operator.VarDec(this, ((Operator.Variable) name).getKey()),
									new Operator.Lambda(this, a, b)));
						}
					} else {
						if (a.type != Operator.Type.TYPE_DEC)
							a = new Operator.HardList(this, ((Operator.Sequence) a).getVector());
						o.addLast(new Operator.Lambda(this, a, b));
					}
				}
				if (token.equals(".exec")) {
					Operator b = o.pollLast();
					final Operator a = o.pollLast();
					b = new Operator.HardList(this, ((Operator.Sequence) b).getVector());
					o.addLast(new Operator.Execute(this, a, b));
				}
				if (token.equals("goto")) {
					final Operator b = o.pollLast();
					final Datum a = o.pollLast().evaluate(null, new ArrayList<>());
					o.addLast(new Operator.SwitchCase(this, a, b));
				}
				if (token.equals("switch")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					final Map<Datum, Operator> ops = new HashMap<>();
					for (final Operator r : b.getChildren()) {
						final Operator.SwitchCase c = (Operator.SwitchCase) r;
						ops.put(c.getKey(), c.getOperator());
					}
					o.addLast(new Operator.Switch(this, a, ops));
				}
				if (token.equals("then")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					o.addLast(new Operator.IfElse(this, a, b));
				}
				if (token.equals("else")) {
					final Operator b = o.pollLast();
					final Operator a = o.pollLast();
					if (a.type == Operator.Type.IF_ELSE)
						((Operator.IfElse) a).setElse(b);
					else if (a.type == Operator.Type.SWITCH)
						((Operator.Switch) a).setElse(b);
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
					o.addLast(new Operator.OuterCall(this, a, b));
				}
				if (token.equals("|>")) {
					Rule.Type rType = null;
					final Operator b = o.pollLast();
					final int a = ((Operator.Variable) o.pollLast()).getKey();
					if (a == S_RULE)
						rType = Rule.Type.SIMPLE;
					if (a == AF_RULE)
						rType = Rule.Type.A_FORWARD;
					if (a == AB_RULE)
						rType = Rule.Type.A_BACKWARD;
					o.addLast(new Operator.RuleDec(this, rType, b));
				}
			} else if (token.equals("]")) {
				final List<Operator> list = new ArrayList<>();
				Operator curr = o.pollLast();
				while (!(curr.type == Operator.Type.VARIABLE
						&& ((Operator.Variable) curr).getKey() == this.SQUARE_BRACKET)) {
					list.add(0, curr);
					curr = o.pollLast();
				}
				o.addLast(new Operator.MatrixDec(this, list));
			} else if (token.equals(")")) {
				final List<Operator> list = new ArrayList<>();
				Operator curr = o.pollLast();
				while (!(curr.type == Operator.Type.VARIABLE
						&& ((Operator.Variable) curr).getKey() == this.PARENTHESES)) {
					list.add(0, curr);
					curr = o.pollLast();
				}
				o.addLast(new Operator.SoftList(this, list));
			} else if (token.equals("}")) {
				final List<Operator> list = new ArrayList<>();
				Operator curr = o.pollLast();
				while (!(curr.type == Operator.Type.VARIABLE
						&& ((Operator.Variable) curr).getKey() == this.CURLY_BRACKET)) {
					list.add(0, curr);
					curr = o.pollLast();
				}
				o.addLast(new Operator.HardList(this, list));
			} else if (token.charAt(0) == '\'') {
				if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
					throw new SonoCompilationException(
							"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.");
				final Phone p = pl.interpretSegment(token.substring(1));
				o.addLast(new Operator.Container(this, new Datum(p)));
			} else if (token.charAt(0) == '@') {
				if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
					throw new SonoCompilationException(
							"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.");
				final Pair p = pl.interpretFeature(token.substring(1));
				o.addLast(new Operator.Container(this, new Datum(p)));
			} else if (token.charAt(0) == '`') {
				if (SonoWrapper.getGlobalOption("LING").equals("FALSE"))
					throw new SonoCompilationException(
							"Cannot conduct phonological-based operations, the modifier `-l` has disabled these.");
				final Word p = pl.interpretSequence(token.substring(1));
				o.addLast(new Operator.Container(this, new Datum(p)));
			} else if (token.charAt(0) == '\"') {
				final String s = token.substring(1);
				o.addLast(new Operator.Container(this, new Datum(s)));
			} else if (Character.isDigit(token.charAt(0))) {
				if (token.charAt(token.length() - 1) == 'D')
					token = token.substring(0, token.length() - 1);
				o.addLast(new Operator.Container(this, new Datum((double) Double.valueOf(token))));
			} else if (token.equals("null")) {
				o.addLast(new Operator.Container(this, new Datum()));
			} else if (token.equals("Vector")) {
				o.addLast(new Operator.Container(this, new Datum(Datum.Type.VECTOR)));
			} else if (token.equals("Number")) {
				o.addLast(new Operator.Container(this, new Datum(Datum.Type.NUMBER)));
			} else if (token.equals("Function")) {
				o.addLast(new Operator.Container(this, new Datum(Datum.Type.FUNCTION)));
			} else if (token.equals("String")) {
				o.addLast(new Operator.Container(this, new Datum(Datum.Type.STRING)));
			} else if (token.equals("Phone")) {
				o.addLast(new Operator.Container(this, new Datum(Datum.Type.PHONE)));
			} else if (token.equals("Feature")) {
				o.addLast(new Operator.Container(this, new Datum(Datum.Type.PAIR)));
			} else if (token.equals("Matrix")) {
				o.addLast(new Operator.Container(this, new Datum(Datum.Type.MATRIX)));
			} else if (token.equals("Rule")) {
				o.addLast(new Operator.Container(this, new Datum(Datum.Type.RULE)));
			} else if (token.equals("Word")) {
				o.addLast(new Operator.Container(this, new Datum(Datum.Type.WORD)));
			} else if (token.equals("true")) {
				o.addLast(new Operator.Container(this, new Datum(1)));
			} else if (token.equals("false")) {
				o.addLast(new Operator.Container(this, new Datum(0)));
			} else if (token.equals("break")) {
				o.addLast(new Operator.Break(this));
			} else {
				o.addLast(new Operator.Variable(this, hashVariable(token)));
			}
		}

		final Operator m = new Operator.SoftList(this, Arrays.asList(o.toArray(new Operator[0])));
		m.condense();
		return m;
	}

	public static <T> String stringFromList(final T[] list, final String init, final String fin) {
		final StringBuilder s = new StringBuilder(init);
		for (int i = 0; i < list.length; i++) {
			if (i > 0)
				s.append(", ");
			s.append(list[i].toString());
		}
		s.append(fin);
		return s.toString();
	}

	public static <E> boolean containsInstance(final List<E> list, final Class<? extends E> clazz) {
		return list.stream().anyMatch(clazz::isInstance);
	}

	public PhoneManager getManager() {
		return this.pl;
	}

	public CommandManager getCommandManager() {
		return this.console;
	}

	public void print(final String str) {
		stdout.print(str);
	}

	public String getLine() {
		return stdin.getLine();
	}

	public double getNumber() {
		return stdin.getNumber();
	}

	public static Object stringFromList(final int[] list, final String init, final String fin) {
		final StringBuilder s = new StringBuilder(init);
		for (int i = 0; i < list.length; i++) {
			if (i > 0)
				s.append(", ");
			s.append(Integer.toString(list[i]));
		}
		s.append(fin);
		return s.toString();
	}
}