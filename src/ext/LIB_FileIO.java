package ext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import main.SonoWrapper;
import main.base.Library;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Token;

public class LIB_FileIO extends Library {
	public LIB_FileIO() {
		super();
		commands.put("LIB_FileIO.INIT", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final File file = new File(datum.getString(line));
			return new Datum((Object) file);
		});
		commands.put("LIB_FileIO.EXISTS", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final File file = (File) datum.getPointer(line);
			if (file.exists())
				return new Datum(true);
			else
				return new Datum(false);
		});
		commands.put("LIB_FileIO.CREATE", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			if (SonoWrapper.getGlobalOption("WRITE").equals("FALSE"))
				throw error("Write permissions are disabled for this interpreter.", line);
			final File file = (File) datum.getPointer(line);
			try {
				if (file.createNewFile())
					return new Datum(true);
				else
					return new Datum(false);
			} catch (final IOException e) {
				throw error("Cannot create file <" + file.toString() + ">", line);
			}
		});
		commands.put("LIB_FileIO.READER.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final File file = (File) datum.getPointer(line);
					BufferedReader br;
					try {
						br = new BufferedReader(new FileReader(file));
						return new Datum((Object) br);
					} catch (final FileNotFoundException e) {
						throw error("Cannot initiate reader for file <" + file.toString() + ">", line);
					}
				});
		commands.put("LIB_FileIO.READER.GETLINE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final BufferedReader br = (BufferedReader) datum.getPointer(line);
					try {
						final String s = br.readLine();
						if (s == null)
							return new Datum();
						else
							return new Datum(s);
					} catch (final IOException e) {
						throw error("Cannot read from reader <" + br.toString() + ">", line);
					}
				});
		commands.put("LIB_FileIO.READER.GET", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final BufferedReader br = (BufferedReader) datum.getPointer(line);
			try {
				final int c = br.read();
				if (c == -1)
					return new Datum();
				else
					return new Datum(String.valueOf((char) c));
			} catch (final IOException e) {
				throw error("Cannot read from reader <" + br.toString() + ">", line);
			}
		});
		commands.put("LIB_FileIO.READER.CLOSE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final BufferedReader br = (BufferedReader) datum.getPointer(line);
					try {
						br.close();
						return new Datum();
					} catch (final IOException e) {
						throw error("Cannot close reader <" + br.toString() + ">", line);
					}
				});
		commands.put("LIB_FileIO.WRITER.INIT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					if (SonoWrapper.getGlobalOption("WRITE").equals("FALSE"))
						throw error("Write permissions are disabled for this interpreter.", line);
					final File file = (File) datum.getPointer(line);
					BufferedWriter bw;
					try {
						bw = new BufferedWriter(new FileWriter(file));
						return new Datum((Object) bw);
					} catch (final IOException e) {
						throw error("Cannot initiate writer for file <" + file.toString() + ">", line);
					}
				});
		commands.put("LIB_FileIO.WRITER.WRITE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final BufferedWriter bw = (BufferedWriter) datum.getVector(line)[0].getPointer(line);
					try {
						bw.write(datum.getVector(line)[1].getString(line));
						return new Datum();
					} catch (final IOException e) {
						throw error("Cannot write from write <" + bw.toString() + ">", line);
					}
				});
		commands.put("LIB_FileIO.WRITER.CLOSE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final BufferedWriter bw = (BufferedWriter) datum.getPointer(line);
					try {
						bw.close();
						return new Datum();
					} catch (final IOException e) {
						throw error("Cannot close reader <" + bw.toString() + ">", line);
					}
				});
	}
}