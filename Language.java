import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static java.util.Map.entry;

public class Language {
	public static void main(String[] args) {
		try {
			File file = new File("data.txt");

			BufferedReader br = new BufferedReader(new FileReader(file));

			String line;
			while ((line = br.readLine()) != null) {
				String split[] = line.split("\t");
				String segment = split[0];
				if (segment.equals("SEGMENT"))
					continue;
				Matrix features = new Matrix();
				int i = 1;
				for (Phone.Feature f : Phone.Feature.values()) {
					switch (split[i].charAt(0)) {
						case '-':
							features.put(f, Phone.Quality.FALSE);
							break;
						case '+':
							features.put(f, Phone.Quality.TRUE);
							break;
						case '0':
							features.put(f, Phone.Quality.NULL);
							break;
					}
					i++;
				}
				Phone p = new Phone(segment, features);
				System.out.println("Adding " + p);
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		List<Phone> base = new ArrayList<Phone>();
		base.addAll(Phone.phoneLibrary);

		for (Phone p : base) {
			System.out.println("Adding variants of " + p);
			recursiveApply(p, new ArrayList<Phone.Secondary>(),
					new ArrayList<Phone.Secondary>(Arrays.asList(Phone.Secondary.values())));
		}

		for (Phone p : base) {
			System.out.println(p);
			for (Phone.Feature type : Phone.Feature.values()) {
				Phone newP1 = p.transform("*", new Matrix(entry(type, Phone.Quality.TRUE)), false);
				if (!p.equals(newP1) && !newP1.getSegment().equals("*")) {
					System.out.print("[+" + type + "]\t");
					System.out.println(newP1);
				}

				Phone newP2 = p.transform("*", new Matrix(entry(type, Phone.Quality.FALSE)), false);
				if (!p.equals(newP2) && !newP2.getSegment().equals("*")) {
					System.out.print("[-" + type + "]\t");
					System.out.println(newP2);
				}
			}
			System.out.println();
		}

		Rule general_palate = new RuleMMMM(
				new Matrix(
					entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE)),
				new Matrix(
					entry(Phone.Feature.DORSAL, Phone.Quality.TRUE),
					entry(Phone.Feature.HIGH, Phone.Quality.TRUE),
					entry(Phone.Feature.LOW, Phone.Quality.FALSE),
					entry(Phone.Feature.FRONT, Phone.Quality.TRUE), 
					entry(Phone.Feature.BACK, Phone.Quality.FALSE)),
				null, 
				new Matrix(
					entry(Phone.Feature.SYLLABIC, Phone.Quality.TRUE),
					entry(Phone.Feature.HIGH, Phone.Quality.TRUE),
					entry(Phone.Feature.FRONT, Phone.Quality.TRUE)), false);

			Rule yod_palate = new RuleMMMM(
					new Matrix(
						entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE)),
					new Matrix(
						entry(Phone.Feature.DORSAL, Phone.Quality.TRUE),
						entry(Phone.Feature.HIGH, Phone.Quality.TRUE),
						entry(Phone.Feature.LOW, Phone.Quality.FALSE),
						entry(Phone.Feature.FRONT, Phone.Quality.TRUE), 
						entry(Phone.Feature.BACK, Phone.Quality.FALSE)),
					null, 
					new Matrix(
						entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE),
						entry(Phone.Feature.HIGH, Phone.Quality.TRUE),
						entry(Phone.Feature.FRONT, Phone.Quality.TRUE)), true);

		Rule devoicing = new RuleMMMM(
			new Matrix(
				entry(Phone.Feature.SYLLABIC, Phone.Quality.TRUE),
				entry(Phone.Feature.VOICE, Phone.Quality.TRUE),
				entry(Phone.Feature.HIGH, Phone.Quality.TRUE)),
			new Matrix(
				entry(Phone.Feature.VOICE, Phone.Quality.FALSE)),
			new Matrix(
				entry(Phone.Feature.VOICE, Phone.Quality.FALSE),
				entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE)), 
			new Matrix(
				entry(Phone.Feature.VOICE, Phone.Quality.FALSE),
				entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE)), false);

		Rule vowel_length = new RuleMMMM(
			new Matrix(
				entry(Phone.Feature.SYLLABIC, Phone.Quality.TRUE),
				entry(Phone.Feature.BACK, Phone.Quality.ALPHA),
				entry(Phone.Feature.FRONT, Phone.Quality.ALPHA)),
			new Matrix(
				entry(Phone.Feature.LONG, Phone.Quality.TRUE)),
			null, 
			new Matrix(
				entry(Phone.Feature.SYLLABIC, Phone.Quality.TRUE),
				entry(Phone.Feature.BACK, Phone.Quality.ALPHA),
				entry(Phone.Feature.FRONT, Phone.Quality.ALPHA)
			), true);

		Rule nasal_n = new RulePMMM(
			Phone.interpretSegment("ɴ"),
			new Matrix(
				entry(Phone.Feature.LABIAL, Phone.Quality.GAMMA),
				entry(Phone.Feature.ROUND, Phone.Quality.GAMMA),
				entry(Phone.Feature.LABIODENTAL, Phone.Quality.GAMMA),
				entry(Phone.Feature.CORONAL, Phone.Quality.GAMMA),
				entry(Phone.Feature.ANTERIOR, Phone.Quality.GAMMA),
				entry(Phone.Feature.DISTRIBUTED, Phone.Quality.GAMMA),
				entry(Phone.Feature.DORSAL, Phone.Quality.GAMMA),
				entry(Phone.Feature.HIGH, Phone.Quality.GAMMA),
				entry(Phone.Feature.LOW, Phone.Quality.GAMMA),
				entry(Phone.Feature.FRONT, Phone.Quality.GAMMA),
				entry(Phone.Feature.BACK, Phone.Quality.GAMMA)),
			null, 
			new Matrix(
				entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE),
				entry(Phone.Feature.CONSONANTAL, Phone.Quality.TRUE),
				entry(Phone.Feature.LABIAL, Phone.Quality.GAMMA),
				entry(Phone.Feature.ROUND, Phone.Quality.GAMMA),
				entry(Phone.Feature.LABIODENTAL, Phone.Quality.GAMMA),
				entry(Phone.Feature.CORONAL, Phone.Quality.GAMMA),
				entry(Phone.Feature.ANTERIOR, Phone.Quality.GAMMA),
				entry(Phone.Feature.DISTRIBUTED, Phone.Quality.GAMMA),
				entry(Phone.Feature.DORSAL, Phone.Quality.GAMMA),
				entry(Phone.Feature.HIGH, Phone.Quality.GAMMA),
				entry(Phone.Feature.LOW, Phone.Quality.GAMMA),
				entry(Phone.Feature.FRONT, Phone.Quality.GAMMA),
				entry(Phone.Feature.BACK, Phone.Quality.GAMMA)), false);

		List<Phone> sequence = Phone.interpretSequence("osjɯɯkeɴdati");

		System.out.println(sequence);

		System.out.println(general_palate);
		sequence = general_palate.transform(sequence);
		System.out.println(sequence);

		System.out.println(yod_palate);
		sequence = yod_palate.transform(sequence);
		System.out.println(sequence);

		System.out.println(vowel_length);
		sequence = vowel_length.transform(sequence);
		System.out.println(sequence);

		System.out.println(nasal_n);
		sequence = nasal_n.transform(sequence);
		System.out.println(sequence);

		try {
			List<Phone> sorted = new ArrayList<Phone>();
			sorted.addAll(Phone.phoneLibrary);
			Collections.sort(sorted);
			BufferedWriter out;
			out = new BufferedWriter(new FileWriter("out.txt"));
			for (Phone p : sorted) {
				out.write(p.getDataString() + "\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void recursiveApply(Phone p, List<Phone.Secondary> applied, List<Phone.Secondary> list) {
		if (list.isEmpty())
			return;

		for (int i = 0; i < list.size(); i++) {
			if (!p.canApply(list.get(i), applied))
				continue;
			Phone newP = p.apply(list.get(i));
			List<Phone.Secondary> newList = new ArrayList<Phone.Secondary>();
			newList.addAll(list);
			for (int j = 0; j <= i; j++)
				newList.remove(0);
			List<Phone.Secondary> newListA = new ArrayList<Phone.Secondary>();
			newListA.addAll(applied);
			newListA.add(list.get(i));
			recursiveApply(newP, newListA, newList);
		}
	}
}