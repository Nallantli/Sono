package main.base;

import java.util.List;

import main.sono.Datum;

public interface Command {
	public Datum execute(Datum datum, List<String> trace);
}
