package ext;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.base.Library;
import main.sono.Datum;
import main.sono.Interpreter;

import java.util.List;

public class LIB_Console extends Library {
	public LIB_Console() {
		super();
		commands.put("LIB_Console.PRINT",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					interpreter.print(datum.getString(trace));
					return new Datum();
				});
		commands.put("LIB_Console.REGEX",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final Pattern pattern = Pattern.compile(datum.getVector(trace)[0].getString(trace));
					final String line = datum.getVector(trace)[1].getString(trace);
					final Matcher m = pattern.matcher(line);
					final List<Datum> list = new ArrayList<>();
					while (m.find()) {
						final Datum[] temp = new Datum[] { new Datum(m.start()), new Datum(m.end()) };
						list.add(new Datum(temp));
					}
					return new Datum(list.toArray(new Datum[0]));
				});
		commands.put("LIB_Console.GET.LINE",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final String s = interpreter.getLine();
					return new Datum(s);
				});
		commands.put("LIB_Console.GET.NUMBER",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final double i = interpreter.getNumber();
					return new Datum(i);
				});
		commands.put("LIB_Console.TIME",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final double i = System.currentTimeMillis();
					return new Datum(i);
				});
		commands.put("LIB_Console.RAND",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final double i = Math.random();
					return new Datum(i);
				});
		commands.put("LIB_Console.EXIT",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					System.exit(0);
					return datum;
				});
		commands.put("LIB_Console.LOG",
				(final Datum datum, final List<String> trace, final Interpreter interpreter) -> {
					final double i = Math.log(datum.getNumber(trace));
					return new Datum(i);
				});
	}
}