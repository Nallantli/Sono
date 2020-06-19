package ext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Main;
import main.base.Command;
import main.base.Library;
import main.sono.Datum;

import java.util.List;

public class Console extends Library {
	public Console() {
		final Map<String, Command> commands = new HashMap<>();
		commands.put("Console.PRINT", (final Datum datum, final List<String> trace) -> {
			System.out.print(datum.getString(trace));
			return new Datum();
		});
		commands.put("Console.REGEX", (final Datum datum, final List<String> trace) -> {
			final Pattern pattern = Pattern.compile(datum.getVector(trace).get(0).getString(trace));
			final String line = datum.getVector(trace).get(1).getString(trace);
			final Matcher m = pattern.matcher(line);
			final List<Datum> list = new ArrayList<>();
			while (m.find()) {
				final List<Datum> temp = new ArrayList<>();
				temp.add(new Datum(BigDecimal.valueOf(m.start())));
				temp.add(new Datum(BigDecimal.valueOf(m.end())));
				list.add(new Datum(temp));
			}
			return new Datum(list);
		});
		commands.put("Console.GET.LINE", (final Datum datum, final List<String> trace) -> {
			if (Main.getScanner() == null)
				Main.initScanner();
			final String s = Main.getScanner().nextLine();
			return new Datum(s);
		});
		commands.put("Console.GET.NUMBER", (final Datum datum, final List<String> trace) -> {
			if (Main.getScanner() == null)
				Main.initScanner();
			final BigDecimal i = Main.getScanner().nextBigDecimal();
			return new Datum(i);
		});
		commands.put("Console.EXIT", (final Datum datum, final List<String> trace) -> {
			System.exit(0);
			return datum;
		});
		setCommands(commands);
	}
}