package main;

import java.io.IOException;
import java.util.Scanner;

import intr.*;
import phl.*;

public class Main {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println(
					"Please specify a TSV file to generate phoneme data from: ex. `sono \"data.tsv\"`");
			System.exit(1);
		}

		try {
			PhoneLoader pl = new PhoneLoader(args[0]);
			Interpreter sono = new Interpreter(new Scope(null), pl.getManager());

			try (Scanner sc = new Scanner(System.in)) {
				while (true) {
					System.out.print("> ");
					String line = sc.nextLine();
					try {
						Datum result = sono.runCode(".", line);
						if (result.getType() == Datum.Type.LIST) {
							int i = 0;
							for (Datum d : result.getList())
								System.out.println("\t" + (i++) + ":\t" + d.toString());
						} else {
							System.out.println("\t" + result.toString());
						}
					} catch (SonoException e) {
						System.err.println(e.getMessage());
					}
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}