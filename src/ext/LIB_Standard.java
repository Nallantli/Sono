package ext;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.base.Library;
import main.sono.Datum;
import main.sono.Function;
import main.sono.Interpreter;
import main.sono.Token;
import main.sono.io.Output;

import java.util.List;
import java.util.NoSuchElementException;

class FThread extends Thread {
	final Function f;
	final Datum[] params;
	final Token line;
	final Object[] overrides;

	public FThread(final Function f, final Datum[] params, final Token line, final Object[] overrides) {
		this.f = f;
		this.params = params;
		this.line = line;
		this.overrides = overrides;
	}

	@Override
	public void run() {
		f.execute(params, line, overrides);
	}
}

public class LIB_Standard extends Library {
	public LIB_Standard(final Interpreter interpreter) {
		super(interpreter);
	}

	public Datum PRINT(final Datum[] data, final Token line, final Object[] overrides) {
		if (overrides != null)
			((Output) overrides[0]).print(data[0].getString(line, overrides));
		else
			interpreter.print(data[0].getString(line, overrides));
		return new Datum();
	}

	public Datum REGEX(final Datum[] data, final Token line, final Object[] overrides) {
		final Pattern pattern = Pattern.compile(data[0].getString(line, overrides));
		final String s = data[1].getString(line, overrides);
		final Matcher m = pattern.matcher(s);
		final List<Datum> list = new ArrayList<>();
		while (m.find()) {
			final Datum[] temp = new Datum[] { new Datum(m.start()), new Datum(m.end()) };
			list.add(new Datum(temp));
		}
		return new Datum(list.toArray(new Datum[0]));
	}

	public Datum GET_LINE(final Token line, final Object[] overrides) {
		try {
			final String s = interpreter.getLine();
			return new Datum(s);
		} catch (final NoSuchElementException e) {
			throw error("Cannot read input", line);
		} catch (final IllegalStateException e) {
			throw error("Input mechanism has been closed", line);
		}
	}

	public Datum GET_NUMBER(final Token line, final Object[] overrides) {
		try {
			final double i = interpreter.getNumber();
			return new Datum(i);
		} catch (final InputMismatchException e) {
			throw error("Input is not a Number", line);
		} catch (final NoSuchElementException e) {
			throw error("Cannot read input", line);
		} catch (final IllegalStateException e) {
			throw error("Input mechanism has been closed", line);
		}
	}

	public Datum TIME(final Token line, final Object[] overrides) {
		final double i = System.currentTimeMillis();
		return new Datum(i);
	}

	public Datum SLEEP(final Datum[] data, final Token line, final Object[] overrides) {
		try {
			Thread.sleep((long) data[0].getNumber(line, overrides));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw error("Cannot pause thread execution", line);
		}
		return new Datum();
	}

	public Datum RAND(final Token line, final Object[] overrides) {
		final double i = Math.random();
		return new Datum(i);
	}

	public Datum EXIT(final Token line, final Object[] overrides) {
		try {
			System.exit(0);
		} catch (final SecurityException e) {
			throw error("Unable to terminate interpreter", line);
		}
		return null;
	}

	public Datum LOG(final Datum[] data, final Token line, final Object[] overrides) {
		final double i = Math.log(data[0].getNumber(line, overrides));
		return new Datum(i);
	}

	public Datum FLOOR(final Datum[] data, final Token line, final Object[] overrides) {
		final double i = Math.floor(data[0].getNumber(line, overrides));
		return new Datum(i);
	}

	public Datum CEIL(final Datum[] data, final Token line, final Object[] overrides) {
		final double i = Math.ceil(data[0].getNumber(line, overrides));
		return new Datum(i);
	}

	public Datum ROUND(final Datum[] data, final Token line, final Object[] overrides) {
		final double i = Math.round(data[0].getNumber(line, overrides));
		return new Datum(i);
	}

	public Datum MATH_COS(final Datum[] data, final Token line, final Object[] overrides) {
		final double i = Math.cos(data[0].getNumber(line, overrides));
		return new Datum(i);
	}

	public Datum MATH_SIN(final Datum[] data, final Token line, final Object[] overrides) {
		final double i = Math.sin(data[0].getNumber(line, overrides));
		return new Datum(i);
	}

	public Datum MATH_TAN(final Datum[] data, final Token line, final Object[] overrides) {
		final double i = Math.tan(data[0].getNumber(line, overrides));
		return new Datum(i);
	}

	public Datum MATH_COSH(final Datum[] data, final Token line, final Object[] overrides) {
		final double i = Math.cosh(data[0].getNumber(line, overrides));
		return new Datum(i);
	}

	public Datum MATH_SINH(final Datum[] data, final Token line, final Object[] overrides) {
		final double i = Math.sinh(data[0].getNumber(line, overrides));
		return new Datum(i);
	}

	public Datum MATH_TANH(final Datum[] data, final Token line, final Object[] overrides) {
		final double i = Math.tanh(data[0].getNumber(line, overrides));
		return new Datum(i);
	}

	public Datum THREAD_INIT(final Datum[] data, final Token line, final Object[] overrides) {
		final Function f = data[0].getFunction(Datum.Type.ANY, line, overrides);
		final Datum[] params = data[1].getVector(line, overrides);
		final FThread thread = new FThread(f, params, line, overrides);
		return new Datum((Object) thread);
	}

	public Datum THREAD_START(final Datum[] data, final Token line, final Object[] overrides) {
		final FThread thread = (FThread) data[0].getPointer(line, overrides);
		thread.start();
		return new Datum();
	}

	public Datum THREAD_JOIN(final Datum[] data, final Token line, final Object[] overrides) {
		final FThread thread = (FThread) data[0].getPointer(line, overrides);
		try {
			thread.join();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw error("Cannot join thread", line);
		}
		return new Datum();
	}
}