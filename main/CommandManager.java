package main;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sonolang.Datum;

interface Command {
	public Datum execute(Datum datum);
}

public class CommandManager {
	private Map<String, Command> commands;

	public CommandManager() {
		commands = new HashMap<>();
		commands.put("CONSOLE.PRINT", (Datum datum) -> {
			System.out.println(datum.getString());
			return new Datum();
		});
		commands.put("CONSOLE.REGEX", (Datum datum) -> {
			Pattern pattern = Pattern.compile(datum.getList().get(0).getString());
			String line = datum.getList().get(1).getString();
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
	}

	public Datum execute(String key, Datum datum) {
		return commands.get(key).execute(datum);
	}
}