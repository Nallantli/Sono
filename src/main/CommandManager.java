package src.main;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import src.sono.Datum;

interface Command {
	public Datum execute(Datum datum, List<String> trace);
}

public class CommandManager {
	private Map<String, Command> commands;

	public CommandManager() {
		commands = new HashMap<>();
		commands.put("CONSOLE.PRINT", (Datum datum, List<String> trace) -> {
			System.out.println(datum.getString(trace));
			return new Datum();
		});
		commands.put("CONSOLE.REGEX", (Datum datum, List<String> trace) -> {
			Pattern pattern = Pattern.compile(datum.getVector(trace).get(0).getString(trace));
			String line = datum.getVector(trace).get(1).getString(trace);
			Matcher m = pattern.matcher(line);
			List<Datum> list = new ArrayList<>();
			while (m.find()) {
				List<Datum> temp = new ArrayList<>();
				temp.add(new Datum(BigDecimal.valueOf(m.start())));
				temp.add(new Datum(BigDecimal.valueOf(m.end())));
				list.add(new Datum(temp));
			}
			return new Datum(list);
		});
		commands.put("CONSOLE.EXIT", (Datum datum, List<String> trace) -> {
			System.exit(0);
			return datum;
		});
	}

	public Datum execute(String key, Datum datum, List<String> trace) {
		return commands.get(key).execute(datum, trace);
	}
}