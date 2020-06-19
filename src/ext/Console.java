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
		Map<String, Command> commands = new HashMap<>();
		commands.put("Console.PRINT", (Datum datum, List<String> trace) -> {
			System.out.print(datum.getString(trace));
			return new Datum();
		});
		commands.put("Console.REGEX", (Datum datum, List<String> trace) -> {
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
		commands.put("Console.GET.LINE", (Datum datum, List<String> trace) -> {
			if (Main.getScanner() == null)
				Main.initScanner();
			String s = Main.getScanner().nextLine();
			return new Datum(s);
		});
		commands.put("Console.GET.NUMBER", (Datum datum, List<String> trace) -> {
			if (Main.getScanner() == null)
				Main.initScanner();
			BigDecimal i = Main.getScanner().nextBigDecimal();
			return new Datum(i);
		});
		commands.put("Console.EXIT", (Datum datum, List<String> trace) -> {
			System.exit(0);
			return datum;
		});
		setCommands(commands);
	}
}