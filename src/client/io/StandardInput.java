package client.io;

import java.math.BigDecimal;
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
	public BigDecimal getNumber() {
		return sc.nextBigDecimal();
	}

}