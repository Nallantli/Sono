package main.base;

import java.util.List;
import java.util.Map;

import main.sono.err.SonoRuntimeException;

public class Library {
	private Map<String, Command> commands;

	protected void setCommands(final Map<String, Command> commands) {
		this.commands = commands;
	}

	public Map<String, Command> getCommands() {
		return commands;
	}

	protected SonoRuntimeException error(final String message, final List<String> trace) {
		return new SonoRuntimeException(message, trace);
	}
}