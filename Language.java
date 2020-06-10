import static java.util.Map.entry;

import java.util.List;

public class Language {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Please specify a TSV file to generate phoneme data from: ex. `java Language \"data\"`");
			System.exit(1);
		}

		PhoneLibrary pm = new PhoneLibrary(args[0]);

		Rule general_palate = new Rule(new Matrix(entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE)),
				new Matrix(entry(Phone.Feature.DORSAL, Phone.Quality.TRUE),
						entry(Phone.Feature.HIGH, Phone.Quality.TRUE), entry(Phone.Feature.LOW, Phone.Quality.FALSE),
						entry(Phone.Feature.FRONT, Phone.Quality.TRUE), entry(Phone.Feature.BACK, Phone.Quality.FALSE)),
				null, new Matrix(entry(Phone.Feature.SYLLABIC, Phone.Quality.TRUE),
						entry(Phone.Feature.HIGH, Phone.Quality.TRUE)),
				false);

		Rule neutralization = new Rule(
				new Matrix(entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE),
						entry(Phone.Feature.ANTERIOR, Phone.Quality.TRUE),
						entry(Phone.Feature.DISTRIBUTED, Phone.Quality.FALSE)),
				new Matrix(entry(Phone.Feature.DELAYED_RELEASE, Phone.Quality.TRUE),
						entry(Phone.Feature.STRIDENT, Phone.Quality.TRUE),
						entry(Phone.Feature.DORSAL, Phone.Quality.FALSE)),
				null, pm.interpretSegment("ɨ"), false);

		Rule yod_palate = new Rule(new Matrix(entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE)),
				new Matrix(entry(Phone.Feature.DORSAL, Phone.Quality.TRUE),
						entry(Phone.Feature.HIGH, Phone.Quality.TRUE), entry(Phone.Feature.LOW, Phone.Quality.FALSE),
						entry(Phone.Feature.FRONT, Phone.Quality.TRUE), entry(Phone.Feature.BACK, Phone.Quality.FALSE)),
				null,
				new Matrix(entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE),
						entry(Phone.Feature.APPROXIMANT, Phone.Quality.TRUE),
						entry(Phone.Feature.HIGH, Phone.Quality.TRUE), entry(Phone.Feature.FRONT, Phone.Quality.TRUE)),
				true);

		Rule u_reduction = new Rule(pm.interpretSegment("ɯ"), pm.interpretSegment("ɨ"),
				new Matrix(entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE),
						entry(Phone.Feature.HIGH, Phone.Quality.TRUE), entry(Phone.Feature.FRONT, Phone.Quality.TRUE)),
				null, false);

		Rule vowel_length = new Rule(new Matrix(entry(Phone.Feature.SYLLABIC, Phone.Quality.TRUE),
				entry(Phone.Feature.BACK, Phone.Quality.ALPHA), entry(Phone.Feature.FRONT, Phone.Quality.ALPHA)),
				new Matrix(entry(Phone.Feature.LONG, Phone.Quality.TRUE)), null,
				new Matrix(entry(Phone.Feature.SYLLABIC, Phone.Quality.TRUE),
						entry(Phone.Feature.BACK, Phone.Quality.ALPHA),
						entry(Phone.Feature.FRONT, Phone.Quality.ALPHA)),
				true);

		Rule nasal_n = new Rule(pm.interpretSegment("ɴ"), new Matrix(
				entry(Phone.Feature.LABIAL, Phone.Quality.GAMMA), entry(Phone.Feature.ROUND, Phone.Quality.GAMMA),
				entry(Phone.Feature.LABIODENTAL, Phone.Quality.GAMMA),
				entry(Phone.Feature.CORONAL, Phone.Quality.GAMMA), entry(Phone.Feature.ANTERIOR, Phone.Quality.GAMMA),
				entry(Phone.Feature.DISTRIBUTED, Phone.Quality.GAMMA), entry(Phone.Feature.DORSAL, Phone.Quality.GAMMA),
				entry(Phone.Feature.HIGH, Phone.Quality.GAMMA), entry(Phone.Feature.LOW, Phone.Quality.GAMMA),
				entry(Phone.Feature.FRONT, Phone.Quality.GAMMA), entry(Phone.Feature.BACK, Phone.Quality.GAMMA)), null,
				new Matrix(entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE),
						entry(Phone.Feature.CONSONANTAL, Phone.Quality.TRUE),
						entry(Phone.Feature.LABIAL, Phone.Quality.GAMMA),
						entry(Phone.Feature.ROUND, Phone.Quality.GAMMA),
						entry(Phone.Feature.LABIODENTAL, Phone.Quality.GAMMA),
						entry(Phone.Feature.CORONAL, Phone.Quality.GAMMA),
						entry(Phone.Feature.ANTERIOR, Phone.Quality.GAMMA),
						entry(Phone.Feature.DISTRIBUTED, Phone.Quality.GAMMA),
						entry(Phone.Feature.DORSAL, Phone.Quality.GAMMA),
						entry(Phone.Feature.HIGH, Phone.Quality.GAMMA), entry(Phone.Feature.LOW, Phone.Quality.GAMMA),
						entry(Phone.Feature.FRONT, Phone.Quality.GAMMA),
						entry(Phone.Feature.BACK, Phone.Quality.GAMMA)),
				false);

		Rule spirantize = new Rule(new Matrix(entry(Phone.Feature.CONSONANTAL, Phone.Quality.TRUE),
				entry(Phone.Feature.SYLLABIC, Phone.Quality.FALSE), entry(Phone.Feature.SONORANT, Phone.Quality.FALSE),
				entry(Phone.Feature.CORONAL, Phone.Quality.TRUE), entry(Phone.Feature.HIGH, Phone.Quality.TRUE),
				entry(Phone.Feature.LOW, Phone.Quality.FALSE), entry(Phone.Feature.FRONT, Phone.Quality.TRUE),
				entry(Phone.Feature.BACK, Phone.Quality.FALSE)),
				new Matrix(entry(Phone.Feature.DELAYED_RELEASE, Phone.Quality.TRUE),
						entry(Phone.Feature.STRIDENT, Phone.Quality.TRUE),
						entry(Phone.Feature.DISTRIBUTED, Phone.Quality.TRUE),
						entry(Phone.Feature.FRONT, Phone.Quality.FALSE), entry(Phone.Feature.BACK, Phone.Quality.TRUE)),
				null, new Matrix(entry(Phone.Feature.SYLLABIC, Phone.Quality.TRUE)), false);

		Rule h_frication = new Rule(pm.interpretSegment("hʲ"), pm.interpretSegment("ç"), null,
				new Matrix(entry(Phone.Feature.SYLLABIC, Phone.Quality.TRUE)), false);

		Rule n_alveolarization = new Rule(pm.interpretSegment("nʲ"), pm.interpretSegment("ɲ"), null,
				new Matrix(entry(Phone.Feature.SYLLABIC, Phone.Quality.TRUE)), false);

		List<Phone> sequence = pm.interpretSequence("tɯsihinjaɴzjaɴkiɴeidɯɴ");

		System.out.println(sequence + "\n");

		System.out.println(vowel_length);
		sequence = vowel_length.transform(sequence);
		System.out.println(sequence + "\n");

		System.out.println(general_palate);
		sequence = general_palate.transform(sequence);
		System.out.println(sequence + "\n");

		System.out.println(yod_palate);
		sequence = yod_palate.transform(sequence);
		System.out.println(sequence + "\n");

		System.out.println(u_reduction);
		sequence = u_reduction.transform(sequence);
		System.out.println(sequence + "\n");

		System.out.println(neutralization);
		sequence = neutralization.transform(sequence);
		System.out.println(sequence + "\n");

		System.out.println(spirantize);
		sequence = spirantize.transform(sequence);
		System.out.println(sequence + "\n");

		System.out.println(h_frication);
		sequence = h_frication.transform(sequence);
		System.out.println(sequence + "\n");

		System.out.println(n_alveolarization);
		sequence = n_alveolarization.transform(sequence);
		System.out.println(sequence + "\n");

		System.out.println(nasal_n);
		sequence = nasal_n.transform(sequence);
		System.out.println(sequence + "\n");
	}
}