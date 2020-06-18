package ext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import main.base.Command;
import main.base.Library;
import main.sono.Datum;

import java.util.List;

public class FileIO extends Library {
	public FileIO() {
		Map<String, Command> commands = new HashMap<>();
		commands.put("FileIO.INIT", (Datum datum, List<String> trace) -> {
			File file = new File(datum.getString(trace));
			return new Datum((Object) file);
		});
		commands.put("FileIO.EXISTS", (Datum datum, List<String> trace) -> {
			File file = (File) datum.getPointer(trace);
			if (file.exists())
				return new Datum(BigDecimal.ONE);
			else
				return new Datum(BigDecimal.ZERO);
		});
		commands.put("FileIO.CREATE", (Datum datum, List<String> trace) -> {
			File file = (File) datum.getPointer(trace);
			try {
				if (file.createNewFile())
					return new Datum(BigDecimal.ONE);
				else
					return new Datum(BigDecimal.ZERO);
			} catch (IOException e) {
				throw error("Cannot create file <" + file.toString() + ">", trace);
			}
		});
		commands.put("FileIO.READER.INIT", (Datum datum, List<String> trace) -> {
			File file = (File) datum.getPointer(trace);
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(file));
				return new Datum((Object)br);
			} catch (FileNotFoundException e) {
				throw error("Cannot initiate reader for file <" + file.toString() + ">", trace);
			}
		});
		commands.put("FileIO.READER.GETLINE", (Datum datum, List<String> trace) -> {
			BufferedReader br = (BufferedReader) datum.getPointer(trace);
			try {
				String s = br.readLine();
				if (s == null)
					return new Datum();
				else
					return new Datum(s);
			} catch (IOException e) {
				throw error("Cannot read from reader <" + br.toString() + ">", trace);
			}
		});
		commands.put("FileIO.READER.GET", (Datum datum, List<String> trace) -> {
			BufferedReader br = (BufferedReader) datum.getPointer(trace);
			try {
				int c = br.read();
				if (c == -1)
					return new Datum();
				else
					return new Datum(String.valueOf((char)c));
			} catch (IOException e) {
				throw error("Cannot read from reader <" + br.toString() + ">", trace);
			}
		});
		commands.put("FileIO.READER.CLOSE", (Datum datum, List<String> trace) -> {
			BufferedReader br = (BufferedReader) datum.getPointer(trace);
			try {
				br.close();
				return new Datum();
			} catch (IOException e) {
				throw error("Cannot close reader <" + br.toString() + ">", trace);
			}
		});
		commands.put("FileIO.WRITER.INIT", (Datum datum, List<String> trace) -> {
			File file = (File) datum.getPointer(trace);
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter(file));
				return new Datum((Object)bw);
			} catch (IOException e) {
				throw error("Cannot initiate writer for file <" + file.toString() + ">", trace);
			}
		});
		commands.put("FileIO.WRITER.WRITE", (Datum datum, List<String> trace) -> {
			BufferedWriter bw = (BufferedWriter) datum.getVector(trace).get(0).getPointer(trace);
			try {
				bw.write(datum.getVector(trace).get(1).getString(trace));
				return new Datum();
			} catch (IOException e) {
				throw error("Cannot write from write <" + bw.toString() + ">", trace);
			}
		});
		commands.put("FileIO.WRITER.CLOSE", (Datum datum, List<String> trace) -> {
			BufferedWriter bw = (BufferedWriter) datum.getPointer(trace);
			try {
				bw.close();
				return new Datum();
			} catch (IOException e) {
				throw error("Cannot close reader <" + bw.toString() + ">", trace);
			}
		});
		setCommands(commands);
	}
}