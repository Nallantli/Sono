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
	public LIB_FileIO(final Interpreter interpreter) {
		super(interpreter);
	}

	public Datum INIT(final Datum[] data, final Token line, final Object[] overrides) {
		final File file = new File(data[0].getString(line, overrides));
		return new Datum((Object) file);
	}

	public Datum EXISTS(final Datum[] data, final Token line, final Object[] overrides) {
		final File file = (File) data[0].getPointer(line, overrides);
		if (file.exists())
			return new Datum(true);
		else
			return new Datum(false);
	}

	public Datum CREATE(final Datum[] data, final Token line, final Object[] overrides) {
		if (SonoWrapper.getGlobalOption("WEB").equals("TRUE"))
			throw error("Write permissions are disabled for this interpreter.", line);
		final File file = (File) data[0].getPointer(line, overrides);
		try {
			if (file.createNewFile())
				return new Datum(true);
			else
				return new Datum(false);
		} catch (final IOException e) {
			throw error("Cannot create file <" + file.toString() + ">", line);
		}
	}

	public Datum READER_INIT(final Datum[] data, final Token line, final Object[] overrides) {
		final File file = (File) data[0].getPointer(line, overrides);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			return new Datum((Object) br);
		} catch (final FileNotFoundException e) {
			throw error("Cannot initiate reader for file <" + file.toString() + ">", line);
		}
	}

	public Datum READER_GETLINE(final Datum[] data, final Token line, final Object[] overrides) {
		final BufferedReader br = (BufferedReader) data[0].getPointer(line, overrides);
		try {
			final String s = br.readLine();
			if (s == null)
				return new Datum();
			else
				return new Datum(s);
		} catch (final IOException e) {
			throw error("Cannot read from reader <" + br.toString() + ">", line);
		}
	}

	public Datum READER_GET(final Datum[] data, final Token line, final Object[] overrides) {
		final BufferedReader br = (BufferedReader) data[0].getPointer(line, overrides);
		try {
			final int c = br.read();
			if (c == -1)
				return new Datum();
			else
				return new Datum(String.valueOf((char) c));
		} catch (final IOException e) {
			throw error("Cannot read from reader <" + br.toString() + ">", line);
		}
	}

	public Datum READER_CLOSE(final Datum[] data, final Token line, final Object[] overrides) {
		final BufferedReader br = (BufferedReader) data[0].getPointer(line, overrides);
		try {
			br.close();
			return new Datum();
		} catch (final IOException e) {
			throw error("Cannot close reader <" + br.toString() + ">", line);
		}
	}

	public Datum WRITER_INIT(final Datum[] data, final Token line, final Object[] overrides) {
		if (SonoWrapper.getGlobalOption("WEB").equals("TRUE"))
			throw error("Write permissions are disabled for this interpreter.", line);
		final File file = (File) data[0].getPointer(line, overrides);
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			return new Datum((Object) bw);
		} catch (final IOException e) {
			throw error("Cannot initiate writer for file <" + file.toString() + ">", line);
		}
	}

	public Datum WRITER_WRITE(final Datum[] data, final Token line, final Object[] overrides) {
		final BufferedWriter bw = (BufferedWriter) data[0].getPointer(line, overrides);
		try {
			bw.write(data[1].getString(line, overrides));
			return new Datum();
		} catch (final IOException e) {
			throw error("Cannot write from write <" + bw.toString() + ">", line);
		}
	}

	public Datum WRITER_CLOSE(final Datum[] data, final Token line, final Object[] overrides) {
		final BufferedWriter bw = (BufferedWriter) data[0].getPointer(line, overrides);
		try {
			bw.close();
			return new Datum();
		} catch (final IOException e) {
			throw error("Cannot close reader <" + bw.toString() + ">", line);
		}
	}
}