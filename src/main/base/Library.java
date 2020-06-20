package main.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.base.CommandManager.Command;
import main.sono.err.SonoRuntimeException;

public class Library {
	protected Map<String, Command> commands;

	protected Library() {
		this.commands = new HashMap<>();
	}

	public Map<String, Command> getCommands() {
		return commands;
	}

	protected SonoRuntimeException error(final String message, final List<String> trace) {
		return new SonoRuntimeException(message, trace);
	}
}