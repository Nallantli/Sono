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
import main.base.ConsoleColors;
import main.phl.Hasher;
import main.phl.Feature;
import main.phl.Phone;
import main.phl.PhoneManager;
import main.phl.Rule;
import main.phl.Word;
import main.SonoWrapper;
import main.sono.err.SonoCompilationException;
import main.sono.err.SonoException;
import main.sono.io.Input;
import main.sono.io.Output;

import main.sono.ops.*;

public class Interpreter {
	protected static final String $_OUTER_CALL_ = "_OUTER_CALL_";
	protected static final String $ABSTRACT = "abstract";
	protected static final String $ADD = "+";
	protected static final String $ADD_SET = "+=";
	protected static final String $ALLOC = "alloc";
	protected static final String $AND = "&&";
	protected static final String $ARROW = "->";
	protected static final String $CATCH = "catch";
	protected static final String $CHAR = "char";
	protected static final String $CLASS = "class";
	protected static final String $CODE = "code";
	protected static final String $COM = "com";
	protected static final String $CONTRAST = "?>";
	protected static final String $DIV = "/";
	protected static final String $DIV_SET = "/=";
	protected static final String $DO = "do";
	protected static final String $E_LESS = "<=";
	protected static final String $E_MORE = ">=";
	protected static final String $ELSE = "else";
	protected static final String $EQUALS = "==";
	protected static final String $EXEC = ".exec";
	protected static final String $EXTENDS = "extends";
	protected static final String $FEAT = "feat";
	protected static final String $EVAL = "eval";
	protected static final String $FINAL = "final";
	protected static final String $FROM = "from";
	protected static final String $GOTO = "goto";
	protected static final String $HASH = "hash";
	protected static final String $IMPORT = "import";
	protected static final String $IN = "in";
	protected static final String $INDEX = ".index";
	protected static final String $INNER = ".";
	protected static final String $LAMBDA = "=>";
	protected static final String $LEN = "length";
	protected static final String $LESS = "<";
	protected static final String $LOAD = "load";
	protected static final String $MAT = "mat";
	protected static final String $MOD = "%";
	protected static final String $MOD_SET = "%=";
	protected static final String $MORE = ">";
	protected static final String $MUL = "*";
	protected static final String $MUL_SET = "*=";
	protected static final String $N_EQUALS = "!=";
	protected static final String $NEGATIVE = ".negative";
	protected static final String $NEW = "new";
	protected static final String $NUM = "num";
	protected static final String $OBJECTIVE = "::";
	protected static final String $OR = "||";
	protected static final String $POSITIVE = ".positive";
	protected static final String $POW = "**";
	protected static final String $POW_SET = "**=";
	protected static final String $PURE_EQUALS = "===";
	protected static final String $PURE_N_EQUALS = "!==";
	protected static final String $REF = "ref";
	protected static final String $REFER = "refer";
	protected static final String $REGISTER = "register";
	protected static final String $RETURN = "return";
	protected static final String $RULE = "|>";
	protected static final String $SET = "=";
	protected static final String $SLASH = "//";
	protected static final String $STATIC = "static";
	protected static final String $STR = "str";
	protected static final String $STRUCT = "struct";
	protected static final String $SUB = "-";
	protected static final String $SUB_SET = "-=";
	protected static final String $SWITCH = "switch";
	protected static final String $THEN = "then";
	protected static final String $THEN_INLINE = "?";
	protected static final String $THROW = "throw";
	protected static final String $TRANSFORM = ">>";
	protected static final String $TRANSFORM_SET = ">>=";
	protected static final String $TRY = "try";
	protected static final String $TYPE = "type";
	protected static final String $UNARY_NOT = "!";
	protected static final String $UNDERSCORE = "..";
	protected static final String $UNTIL = "until";
	protected static final String $VAR = "var";
	protected static final String $VEC = "vec";
	protected static final String $WORD = "word";
	protected static final String $XOR = "^";
	protected static final String $OBJECT_RAW_START = "@{";
	protected static final String $ENTRY = ":";

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
	public final int ISEQUALS;
	public final int GET_HASH;
	public final int ERROR;
	public final int S_RULE;
	public final int AF_RULE;
	public final int AB_RULE;
	public final int SQUARE_BRACKET;
	public final int CURLY_BRACKET;
	public final int OBJECT_CURLY_BRACKET;
	public final int PARENTHESES;
	public final int ALL;
	public final int BASE;
	public final int FEATURES;

	public Interpreter(final Scope main, final PhoneManager pl, final CommandManager console, final Output stdout,
			final Output stderr, final Input stdin) throws InterruptedException {
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
		GET_STR = hashVariable("getString");
		GET_LEN = hashVariable("getLength");
		GET_INDEX = hashVariable("getIndex");
		GET_LIST = hashVariable("getVector");
		ISEQUALS = hashVariable("equals");
		GET_HASH = hashVariable("getHash");
		ERROR = hashVariable("_e");
		S_RULE = hashVariable("S");
		AF_RULE = hashVariable("Af");
		AB_RULE = hashVariable("Ab");
		SQUARE_BRACKET = hashVariable("[");
		CURLY_BRACKET = hashVariable("{");
		OBJECT_CURLY_BRACKET = hashVariable($OBJECT_RAW_START);
		PARENTHESES = hashVariable("(");
		ALL = hashVariable("_all");
		BASE = hashVariable("_base");
		FEATURES = hashVariable("_features");

		if (this.pl != null) {
			final List<Datum> data = new ArrayList<>();
			for (final Phone p : pl.getAllPhones()) {
				data.add(new Datum(p));
			}
			final List<Datum> dataBase = new ArrayList<>();
			for (final Phone p : pl.getBasePhones()) {
				dataBase.add(new Datum(p));
			}
			final List<Datum> dataFeatures = new ArrayList<>();
			for (final int f : pl.getFeatureNames()) {
				dataFeatures.add(new Datum(Hasher.deHash(f)));
			}
			final Datum d = new Datum(data.toArray(new Datum[0]));
			d.setMutable(false);
			final Datum db = new Datum(dataBase.toArray(new Datum[0]));
			db.setMutable(false);
			final Datum df = new Datum(dataFeatures.toArray(new Datum[0]));
			df.setMutable(false);

			main.setVariable(this, ALL, d, null, null);
			main.setVariable(this, BASE, db, null, null);
			main.setVariable(this, FEATURES, df, null, null);
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

	public Datum runCode(final String directory, final String filename, final String code, final boolean drawTree, final Scope override, final Object[] outOverrides)
			throws InterruptedException {
		return evaluate(parse(directory, tokenize(code, filename)), drawTree, override, outOverrides);
	}

	public Datum evaluate(final Operator o, final boolean drawTree, final Scope override, final Object[] outOverrides)
			throws InterruptedException {
		try {
			if (drawTree)
				o.printTree("", true);
			if (override == null)
				return o.evaluate(main, outOverrides);
			return o.evaluate(override, outOverrides);
		} catch (final SonoException e) {
			if (outOverrides != null) {
				((Output)outOverrides[1]).print(e.getMessage() + "\n" + e.getLineNoColor());
			} else if (stderr != null) {
				if (SonoWrapper.getGlobalOption("WEB").equals("TRUE")) {
					stderr.print(e.getMessage() + "\n" + e.getLineNoColor());
				} else {
					stderr.print(ConsoleColors.RED + e.getMessage() + "\n" + e.getLine());
				}
			}
			return new Datum();
		}
	}

	private List<Token> tokenize(final String code, final String filename) {
		List<Token> tokens = null;
		tokens = Tokenizer.tokenize(code, filename);
		tokens = Tokenizer.infixToPostfix(tokens);
		return tokens;
	}

	public Operator loadFile(final String fullDirectory, final String filename, final String rawDir,
			final String path) throws InterruptedException {
		final StringBuilder contents = new StringBuilder();
		File file = new File(fullDirectory, filename);
		boolean flag = false;
		try {
			if (!file.exists()) {
				file = new File(SonoWrapper.getGlobalOption("PATH"),
						"lib" + File.separator + fullDirectory + File.separator + filename);
			}
			if (!file.exists()) {
				file = new File(SonoWrapper.getGlobalOption("PATH"), "lib" + File.separator + path);
				flag = true;
			}

			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				String line;
				while ((line = br.readLine()) != null)
					contents.append(line + "\n");
			}
		} catch (final Exception e) {
			throw new SonoCompilationException("File <" + file.toString() + "> does not exist.");
		}

		if (flag) {
			return parse(rawDir, tokenize(contents.toString(), file.getAbsolutePath()));
		} else {
			return parse(fullDirectory, tokenize(contents.toString(), file.getAbsolutePath()));
		}
	}

	public Operator parse(final String directory, final List<Token> tokens) throws InterruptedException {
		final Deque<Operator> o = new ArrayDeque<>();
		String path;
		String[] split;
		String filename;
		String rawDir;
		StringBuilder fileDirectory;
		Operator a;
		Operator b;

		for (int i = 0; i < tokens.size(); i++) {
			final Token line = tokens.get(i);
			String token = line.getKey();

			if (token.equals(";") || token.equals(","))
				continue;
			if (Tokenizer.operators.containsKey(token)) {
				switch (token) {
					case $LOAD:
						path = ((Container) o.pollLast()).getDatum().getString(null, null);
						split = (new StringBuilder(path)).reverse().toString().split("[\\\\\\/]", 2);
						filename = (new StringBuilder(split[0])).reverse().toString();
						rawDir = "";
						fileDirectory = new StringBuilder(directory);
						if (split.length > 1) {
							rawDir = (new StringBuilder(split[1])).reverse().toString();
							fileDirectory.append(File.separator + rawDir);
						}
						if (!loadedFiles.contains(fileDirectory.toString() + filename)) {
							loadedFiles.add(fileDirectory.toString() + filename);
							Operator temp = loadFile(fileDirectory.toString(), filename, rawDir, path);
							if (temp.type == Operator.Type.SOFT_LIST) {
								for (Operator o2 : temp.getChildren())
									o.addLast(o2);
							} else {
								o.addLast(temp);
							}
						}
						break;
					case $IMPORT:
						path = ((Container) o.pollLast()).getDatum().getString(null, null);
						split = (new StringBuilder(path)).reverse().toString().split("[\\\\\\/]", 2);
						filename = (new StringBuilder(split[0])).reverse().toString() + ".jar";
						rawDir = "";
						fileDirectory = new StringBuilder(directory);
						if (split.length > 1) {
							rawDir = (new StringBuilder(split[1])).reverse().toString();
							fileDirectory.append(File.separator + rawDir);
						}
						if (!loadedFiles.contains(fileDirectory.toString() + filename)) {
							loadedFiles.add(fileDirectory.toString() + filename);
							console.importLibrary(fileDirectory.toString(), filename, rawDir, path, "ext." + path,
									this);
						}
						break;
					case $UNARY_NOT:
						a = o.pollLast();
						o.addLast(new NotEquals(this, line, new Container(this, line, new Datum(true)), a));
						break;
					case $NEGATIVE:
						a = o.pollLast();
						o.addLast(new Sub(this, line, new Container(this, line, new Datum(0)), a));
						break;
					case $POSITIVE:
						a = o.pollLast();
						o.addLast(new Add(this, line, new Container(this, line, new Datum(0)), a));
						break;
					case $NEW:
						a = o.pollLast();
						o.addLast(new DecNew(this, line, a));
						break;
					case $LEN:
						a = o.pollLast();
						o.addLast(new Length(this, line, a));
						break;
					case $RETURN:
						a = o.pollLast();
						o.addLast(new Return(this, line, a));
						break;
					case $REFER:
						a = o.pollLast();
						o.addLast(new Refer(this, line, a));
						break;
					case $VAR:
						a = o.pollLast();
						o.addLast(new DecVariable(this, line, ((Variable) a).getKey()));
						break;
					case $ABSTRACT:
						a = o.pollLast();
						o.addLast(new CastAbstract(this, line, ((Variable) a).getKey()));
						break;
					case $STRUCT:
						a = o.pollLast();
						o.addLast(new CastStruct(this, line, ((Variable) a).getKey()));
						break;
					case $STATIC:
						a = o.pollLast();
						o.addLast(new CastStatic(this, line, ((Variable) a).getKey()));
						break;
					case $REF:
						a = o.pollLast();
						o.addLast(new CastReference(this, line, ((Variable) a).getKey()));
						break;
					case $FINAL:
						a = o.pollLast();
						o.addLast(new CastFinal(this, line, ((Variable) a).getKey()));
						break;
					case $WORD:
						a = o.pollLast();
						o.addLast(new ToWord(this, line, a));
						break;
					case $VEC:
						a = o.pollLast();
						o.addLast(new ToVector(this, line, a));
						break;
					case $MAT:
						a = o.pollLast();
						o.addLast(new ToMatrix(this, line, a));
						break;
					case $NUM:
						a = o.pollLast();
						o.addLast(new ToNumber(this, line, a));
						break;
					case $HASH:
						a = o.pollLast();
						o.addLast(new ToHash(this, line, a));
						break;
					case $STR:
						a = o.pollLast();
						o.addLast(new ToString(this, line, a));
						break;
					case $CHAR:
						a = o.pollLast();
						o.addLast(new ToChar(this, line, a));
						break;
					case $CODE:
						a = o.pollLast();
						o.addLast(new ToCharCode(this, line, a));
						break;
					case $FEAT:
						a = o.pollLast();
						o.addLast(new ToFeature(this, line, a));
						break;
					case $THROW:
						a = o.pollLast();
						o.addLast(new Throw(this, line, a));
						break;
					case $TRY:
						a = o.pollLast();
						o.addLast(new TryCatch(this, line, a));
						break;
					case $REGISTER:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Register(this, line, a, b));
						break;
					case $EXTENDS:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new CastExtends(this, line, a, b));
						break;
					case $SET:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Set(this, line, a, b));
						break;
					case $OBJECTIVE:
						b = o.pollLast();
						a = o.pollLast();
						if (b.type == Operator.Type.SOFT_LIST)
							b = new HardList(this, line, ((Sequence) b).getVector());
						o.addLast(new DecObjective(this, line, a, b));
						break;
					case $ARROW:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Arrow(this, line, a, b));
						break;
					case $UNDERSCORE:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Underscore(this, line, a, b));
						break;
					case $ENTRY:
						b = o.pollLast();
						a = o.pollLast();
						if (a.type == Operator.Type.IF_ELSE_INLINE) {
							((IfElseInline) a).setElse(b);
							o.addLast(a);
						} else {
							o.addLast(new DecEntry(this, line, a, b));
						}
						break;
					case $SLASH:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Slash(this, line, a, b));
						break;
					case $TRANSFORM:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Transform(this, line, a, b));
						break;
					case $TRANSFORM_SET:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Set(this, line, a, new Transform(this, line, a, b)));
						break;
					case $ADD:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Add(this, line, a, b));
						break;
					case $ADD_SET:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Set(this, line, a, new Add(this, line, a, b)));
						break;
					case $SUB:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Sub(this, line, a, b));
						break;
					case $SUB_SET:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Set(this, line, a, new Sub(this, line, a, b)));
						break;
					case $MUL:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Mul(this, line, a, b));
						break;
					case $MUL_SET:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Set(this, line, a, new Mul(this, line, a, b)));
						break;
					case $DIV:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Div(this, line, a, b));
						break;
					case $DIV_SET:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Set(this, line, a, new Div(this, line, a, b)));
						break;
					case $MOD:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Mod(this, line, a, b));
						break;
					case $MOD_SET:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Set(this, line, a, new Mod(this, line, a, b)));
						break;
					case $POW:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Pow(this, line, a, b));
						break;
					case $POW_SET:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Set(this, line, a, new Pow(this, line, a, b)));
						break;
					case $CLASS:
						b = o.pollLast();
						a = o.pollLast();
						b = new SoftList(this, line, ((Sequence) b).getVector());
						o.addLast(new DecClass(this, line, a, b));
						break;
					case $INDEX:
						b = ((DecMatrix) o.pollLast()).getChildren()[0];
						a = o.pollLast();
						o.addLast(new Index(this, line, a, b));
						break;
					case $CONTRAST:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Contrast(this, line, a, b));
						break;
					case $EQUALS:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Equals(this, line, a, b));
						break;
					case $PURE_EQUALS:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new EqualsPure(this, line, a, b));
						break;
					case $N_EQUALS:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new NotEquals(this, line, a, b));
						break;
					case $PURE_N_EQUALS:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new NotEqualsPure(this, line, a, b));
						break;
					case $LESS:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Less(this, line, a, b));
						break;
					case $MORE:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new More(this, line, a, b));
						break;
					case $E_LESS:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new ELess(this, line, a, b));
						break;
					case $E_MORE:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new EMore(this, line, a, b));
						break;
					case $AND:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new And(this, line, a, b));
						break;
					case $OR:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Or(this, line, a, b));
						break;
					case $XOR:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new XOr(this, line, a, b));
						break;
					case $INNER:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Inner(this, line, a, b));
						break;
					case $FROM:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Find(this, line, a, b));
						break;
					case $IN:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new Iterator(this, line, a, b));
						break;
					case $UNTIL:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new RangeUntil(this, line, a, b));
						break;
					case $DO:
						b = softenIfList(o.pollLast());
						a = o.pollLast();
						o.addLast(new Loop(this, line, a, b));
						break;
					case $LAMBDA:
						b = o.pollLast();
						if (b.type == Operator.Type.HARD_LIST)
							b = new SoftList(this, line, b.getChildren());
						a = o.pollLast();
						if (a.type == Operator.Type.EXECUTE) {
							final Operator name = ((Execute) a).getA();
							if (name.type == Operator.Type.DEC_OBJECTIVE) {
								final Operator typeDec = ((DecObjective) name).getA();
								final Operator fName = ((DecObjective) name).getB();
								a = new HardList(this, line, ((Sequence) ((Execute) a).getB()).getVector());
								o.addLast(new Set(this, line, new DecVariable(this, line, ((Variable) fName).getKey()),
										new DecLambda(this, line, new DecObjective(this, line, typeDec, a), b)));
							} else {
								a = new HardList(this, line, ((Sequence) ((Execute) a).getB()).getVector());
								o.addLast(new Set(this, line, new DecVariable(this, line, ((Variable) name).getKey()),
										new DecLambda(this, line, a, b)));
							}
						} else {
							if (a.type != Operator.Type.DEC_OBJECTIVE)
								a = new HardList(this, line, ((Sequence) a).getVector());
							o.addLast(new DecLambda(this, line, a, b));
						}
						break;
					case $EXEC:
						b = o.pollLast();
						a = o.pollLast();
						if (a.type == Operator.Type.VARIABLE) {
							final String key = deHash(((Variable) a).getKey());
							switch (key) {
								case $ALLOC:
									o.addLast(new Allocate(this, line, b));
									break;
								case $COM:
									o.addLast(new Common(this, line, b));
									break;
								case $TYPE:
									o.addLast(new ToTypeString(this, line, b));
									break;
								case $LEN:
									o.addLast(new Length(this, line, b));
									break;
								case $EVAL:
									o.addLast(new Eval(this, line, b));
									break;
								default:
									b = new HardList(this, line, ((Sequence) b).getVector());
									o.addLast(new Execute(this, line, a, b));
									break;
							}
						} else {
							b = new HardList(this, line, ((Sequence) b).getVector());
							o.addLast(new Execute(this, line, a, b));
						}
						break;
					case $GOTO:
						b = softenIfList(o.pollLast());
						final Datum datumA = o.pollLast().evaluate(null, null);
						o.addLast(new SwitchCase(this, line, datumA, b));
						break;
					case $SWITCH:
						b = o.pollLast();
						a = o.pollLast();
						final Map<Datum, Operator> ops = new HashMap<>();
						for (final Operator r : b.getChildren()) {
							final SwitchCase c = (SwitchCase) r;
							ops.put(c.getKey(), c.getOperator());
						}
						o.addLast(new Switch(this, line, a, ops));
						break;
					case $THEN:
						b = softenIfList(o.pollLast());
						a = o.pollLast();
						o.addLast(new IfElse(this, line, a, b));
						break;
					case $THEN_INLINE:
						b = o.pollLast();
						a = o.pollLast();
						o.addLast(new IfElseInline(this, line, a, b));
						break;
					case $ELSE:
						b = softenIfList(o.pollLast());
						a = o.pollLast();
						if (a.type == Operator.Type.IF_ELSE)
							((IfElse) a).setElse(b);
						else if (a.type == Operator.Type.SWITCH)
							((Switch) a).setElse(b);
						o.addLast(a);
						break;
					case $CATCH:
						b = softenIfList(o.pollLast());
						a = o.pollLast();
						((TryCatch) a).setCatch(b);
						o.addLast(a);
						break;
					case $_OUTER_CALL_:
						a = o.pollLast();
						final String clazz = ((Sequence) a).getChildren()[0].evaluate(null, null).getString(line, null);
						final String key = ((Sequence) a).getChildren()[1].evaluate(null, null).getString(line, null);
						final Operator[] newOps = new Operator[a.getChildren().length - 2];
						System.arraycopy(a.getChildren(), 2, newOps, 0, newOps.length);
						o.addLast(new OuterCall(this, line, clazz, key, new HardList(this, a.line, newOps)));
						break;
					case $RULE:
						Rule.Type rType = null;
						b = o.pollLast();
						final int r = ((Variable) o.pollLast()).getKey();
						if (r == S_RULE)
							rType = Rule.Type.SIMPLE;
						if (r == AF_RULE)
							rType = Rule.Type.A_FORWARD;
						if (r == AB_RULE)
							rType = Rule.Type.A_BACKWARD;
						o.addLast(new DecRule(this, line, rType, b));
						break;
					default:
						throw new SonoCompilationException("Unknown operator: " + token);
				}
			} else if (token.equals("]")) {
				final List<Operator> list = new ArrayList<>();
				Operator curr = o.pollLast();
				while (!(curr.type == Operator.Type.VARIABLE && ((Variable) curr).getKey() == this.SQUARE_BRACKET)) {
					list.add(0, curr);
					curr = o.pollLast();
				}
				o.addLast(new DecMatrix(this, line, list.toArray(new Operator[0])));
			} else if (token.equals(")")) {
				final List<Operator> list = new ArrayList<>();
				Operator curr = o.pollLast();
				while (!(curr.type == Operator.Type.VARIABLE && ((Variable) curr).getKey() == this.PARENTHESES)) {
					list.add(0, curr);
					curr = o.pollLast();
				}
				o.addLast(new SoftList(this, line, list.toArray(new Operator[0])));
			} else if (token.equals("}")) {
				final List<Operator> list = new ArrayList<>();
				Operator curr = o.pollLast();
				while (!(curr.type == Operator.Type.VARIABLE && (((Variable) curr).getKey() == this.CURLY_BRACKET
						|| ((Variable) curr).getKey() == this.OBJECT_CURLY_BRACKET))) {
					list.add(0, curr);
					curr = o.pollLast();
				}
				final Variable currV = (Variable) curr;
				if (currV.getKey() == this.CURLY_BRACKET)
					o.addLast(new HardList(this, line, list.toArray(new Operator[0])));
				else if (currV.getKey() == this.OBJECT_CURLY_BRACKET)
					o.addLast(new DecRawObject(this, line, list.toArray(new Operator[0])));
			} else if (token.charAt(0) == '\'') {
				final Phone p = pl.interpretSegment(token.substring(1));
				o.addLast(new Container(this, line, new Datum(p)));
			} else if (token.charAt(0) == '#') {
				final Feature p = pl.interpretFeature(token.substring(1));
				o.addLast(new Container(this, line, new Datum(p)));
			} else if (token.charAt(0) == '`') {
				final Word p = pl.interpretSequence(token.substring(1));
				o.addLast(new Container(this, line, new Datum(p)));
			} else if (token.charAt(0) == '\"') {
				final String s = token.substring(1);
				o.addLast(new Container(this, line, new Datum(s)));
			} else if (Character.isDigit(token.charAt(0))) {
				if (token.charAt(token.length() - 1) == 'D')
					token = token.substring(0, token.length() - 1);
				o.addLast(new Container(this, line, new Datum((double) Double.valueOf(token))));
			} else if (token.equals("null")) {
				o.addLast(new Container(this, line, new Datum()));
			} else if (token.equals("Vector")) {
				o.addLast(new Container(this, line, new Datum(Datum.Type.VECTOR)));
			} else if (token.equals("Number")) {
				o.addLast(new Container(this, line, new Datum(Datum.Type.NUMBER)));
			} else if (token.equals("Function")) {
				o.addLast(new Container(this, line, new Datum(Datum.Type.FUNCTION)));
			} else if (token.equals("String")) {
				o.addLast(new Container(this, line, new Datum(Datum.Type.STRING)));
			} else if (token.equals("Phone")) {
				o.addLast(new Container(this, line, new Datum(Datum.Type.PHONE)));
			} else if (token.equals("Feature")) {
				o.addLast(new Container(this, line, new Datum(Datum.Type.FEATURE)));
			} else if (token.equals("Matrix")) {
				o.addLast(new Container(this, line, new Datum(Datum.Type.MATRIX)));
			} else if (token.equals("Boolean")) {
				o.addLast(new Container(this, line, new Datum(Datum.Type.BOOL)));
			} else if (token.equals("Rule")) {
				o.addLast(new Container(this, line, new Datum(Datum.Type.RULE)));
			} else if (token.equals("Dictionary")) {
				o.addLast(new Container(this, line, new Datum(Datum.Type.DICTIONARY)));
			} else if (token.equals("Word")) {
				o.addLast(new Container(this, line, new Datum(Datum.Type.WORD)));
			} else if (token.equals("true")) {
				o.addLast(new Container(this, line, new Datum(true)));
			} else if (token.equals("false")) {
				o.addLast(new Container(this, line, new Datum(false)));
			} else if (token.equals("break")) {
				o.addLast(new Break(this, line));
			} else {
				o.addLast(new Variable(this, line, hashVariable(token)));
			}
		}

		final Operator m = new SoftList(this, null, o.toArray(new Operator[0]));
		m.condense();
		return m;
	}

	public static <T> String stringFromList(final T[] list, final String init, final String fin, final String delim) {
		final StringBuilder s = new StringBuilder(init);
		for (int i = 0; i < list.length; i++) {
			if (i > 0)
				s.append(delim + " ");
			s.append(list[i].toString());
		}
		s.append(fin);
		return s.toString();
	}

	public static <E> boolean containsInstance(final E[] list, final Class<? extends E> clazz) {
		return Arrays.stream(list).anyMatch(clazz::isInstance);
	}

	public PhoneManager getManager() {
		return this.pl;
	}

	public CommandManager getCommandManager() {
		return this.console;
	}

	public void print(final String str) {
		if (stdout != null)
			stdout.print(str);
	}

	public String getLine() {
		if (stdin != null)
			return stdin.getLine();
		return "";
	}

	public double getNumber() {
		if (stdin != null)
			return stdin.getNumber();
		return 0;
	}

	public static Object stringFromList(final int[] list, final String init, final String fin, final String delim) {
		final StringBuilder s = new StringBuilder(init);
		for (int i = 0; i < list.length; i++) {
			if (i > 0)
				s.append(delim + " ");
			s.append(Integer.toString(list[i]));
		}
		s.append(fin);
		return s.toString();
	}

	private static Operator softenIfList(final Operator o) {
		if (o.type == Operator.Type.HARD_LIST)
			return new SoftList((HardList) o);
		return o;
	}
}