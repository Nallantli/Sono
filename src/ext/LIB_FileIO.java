package ext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import main.base.Library;
import main.sono.Datum;

import java.util.List;

public class LIB_FileIO extends Library {
	public LIB_FileIO() {
		super();
		commands.put("LIB_FileIO.INIT", (final Datum datum, final List<String> trace) -> {
			final File file = new File(datum.getString(trace));
			return new Datum((Object) file);
		});
		commands.put("LIB_FileIO.EXISTS", (final Datum datum, final List<String> trace) -> {
			final File file = (File) datum.getPointer(trace);
			if (file.exists())
				return new Datum(BigDecimal.ONE);
			else
				return new Datum(BigDecimal.ZERO);
		});
		commands.put("LIB_FileIO.CREATE", (final Datum datum, final List<String> trace) -> {
			final File file = (File) datum.getPointer(trace);
			try {
				if (file.createNewFile())
					return new Datum(BigDecimal.ONE);
				else
					return new Datum(BigDecimal.ZERO);
			} catch (final IOException e) {
				throw error("Cannot create file <" + file.toString() + ">", trace);
			}
		});
		commands.put("LIB_FileIO.READER.INIT", (final Datum datum, final List<String> trace) -> {
			final File file = (File) datum.getPointer(trace);
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(file));
				return new Datum((Object) br);
			} catch (final FileNotFoundException e) {
				throw error("Cannot initiate reader for file <" + file.toString() + ">", trace);
			}
		});
		commands.put("LIB_FileIO.READER.GETLINE", (final Datum datum, final List<String> trace) -> {
			final BufferedReader br = (BufferedReader) datum.getPointer(trace);
			try {
				final String s = br.readLine();
				if (s == null)
					return new Datum();
				else
					return new Datum(s);
			} catch (final IOException e) {
				throw error("Cannot read from reader <" + br.toString() + ">", trace);
			}
		});
		commands.put("LIB_FileIO.READER.GET", (final Datum datum, final List<String> trace) -> {
			final BufferedReader br = (BufferedReader) datum.getPointer(trace);
			try {
				final int c = br.read();
				if (c == -1)
					return new Datum();
				else
					return new Datum(String.valueOf((char) c));
			} catch (final IOException e) {
				throw error("Cannot read from reader <" + br.toString() + ">", trace);
			}
		});
		commands.put("LIB_FileIO.READER.CLOSE", (final Datum datum, final List<String> trace) -> {
			final BufferedReader br = (BufferedReader) datum.getPointer(trace);
			try {
				br.close();
				return new Datum();
			} catch (final IOException e) {
				throw error("Cannot close reader <" + br.toString() + ">", trace);
			}
		});
		commands.put("LIB_FileIO.WRITER.INIT", (final Datum datum, final List<String> trace) -> {
			final File file = (File) datum.getPointer(trace);
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter(file));
				return new Datum((Object) bw);
			} catch (final IOException e) {
				throw error("Cannot initiate writer for file <" + file.toString() + ">", trace);
			}
		});
		commands.put("LIB_FileIO.WRITER.WRITE", (final Datum datum, final List<String> trace) -> {
			final BufferedWriter bw = (BufferedWriter) datum.getVector(trace).get(0).getPointer(trace);
			try {
				bw.write(datum.getVector(trace).get(1).getString(trace));
				return new Datum();
			} catch (final IOException e) {
				throw error("Cannot write from write <" + bw.toString() + ">", trace);
			}
		});
		commands.put("LIB_FileIO.WRITER.CLOSE", (final Datum datum, final List<String> trace) -> {
			final BufferedWriter bw = (BufferedWriter) datum.getPointer(trace);
			try {
				bw.close();
				return new Datum();
			} catch (final IOException e) {
				throw error("Cannot close reader <" + bw.toString() + ">", trace);
			}
		});
	}
}