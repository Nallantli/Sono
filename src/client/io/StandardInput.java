package client.io;

import java.util.Scanner;

import main.sono.io.Input;

public class StandardInput extends Input {
	Scanner sc;

	public StandardInput(Scanner sc) {
		if (sc == null)
			sc = new Scanner(System.in);
		else
			this.sc = sc;
	}

	@Override
	public String getLine() {
		return sc.nextLine();
	}

	@Override
	public double getNumber() {
		return sc.nextDouble();
	}

}