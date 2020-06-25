package main.phl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.Main;

import static java.util.Map.entry;

public class PhoneLoader {
	public enum Secondary {
		VOCALIC, RETRACTED, ADVANCED, PALATOALVEOLAR, DENTAL, DEVOICING, NASALIZATION, LABIALIZATION, PALATALIZATION,
		VELARIZATION, PHARYNGEALIZATION, ASPIRATION, LENGTH
	}

	public static Map<Secondary, SecondaryArticulation> secondaryLibrary;

	private PhoneManager pm;
	private Map<Matrix, ArrayList<String>> loadedPhones;

	private void readFile(final PhoneManager pm, final String directory, final String filename, final String del)
			throws IOException {
		final File file = new File(directory, filename);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			String major = null;
			while ((line = br.readLine()) != null) {
				final String[] split = line.split(del);
				final String segment = split[0];
				if (segment.equals("SEGMENT")) {
					for (int i = 1; i < split.length; i++) {
						//System.out.println("\tAdding Feature <" + split[i] + ">");
						if (Character.isUpperCase(split[i].charAt(0))) {
							pm.majorClasses.put(pm.hasher.hash(split[i]), new ArrayList<>());
							major = split[i];
						}
						pm.featureNames.add(pm.hasher.hash(split[i]));
						if (major != null && !major.equals(split[i])) {
							pm.majorClasses.get(pm.hasher.hash(major)).add(pm.hasher.hash(split[i]));
						}
					}
					continue;
				}
				final Matrix features = new Matrix();
				int i = 1;
				for (final int f : pm.featureNames) {
					features.put(pm, f, split[i]);
					i++;
				}
				//System.out.println("\tBase Phone <" + segment + ">");

				if (!loadedPhones.containsKey(features))
					loadedPhones.put(features, new ArrayList<>());
				loadedPhones.get(features).add(segment);
				// new Phone(pm, segment, features);
			}
		}
	}

	private static void initalizeSecondary(PhoneManager pm) {
		secondaryLibrary = Map.ofEntries(
				entry(Secondary.VOCALIC, new SecondaryArticulation(pm, "̩", pm.hasher.hash("syl"), "+",
						new Secondary[] {},
						List.of(new Matrix(new Pair(pm, pm.hasher.hash("cons"), "+"),
								new Pair(pm, pm.hasher.hash("son"), "+"), new Pair(pm, pm.hasher.hash("syl"), "-"))))),
				entry(Secondary.RETRACTED, new SecondaryArticulation("̠",
						new Matrix(new Pair(pm, pm.hasher.hash("front"), "-"),
								new Pair(pm, pm.hasher.hash("back"), "+")),
						new Secondary[] { Secondary.ADVANCED },
						List.of(new Matrix(new Pair(pm, pm.hasher.hash("COR"), "+"))))),
				entry(Secondary.ADVANCED, new SecondaryArticulation("̟",
						new Matrix(new Pair(pm, pm.hasher.hash("front"), "+"),
								new Pair(pm, pm.hasher.hash("back"), "-")),
						new Secondary[] { Secondary.RETRACTED },
						List.of(new Matrix(new Pair(pm, pm.hasher.hash("COR"), "+"))))),
				entry(Secondary.DENTAL,
						new SecondaryArticulation("̪",
								new Matrix(new Pair(pm, pm.hasher.hash("ant"), "+"),
										new Pair(pm, pm.hasher.hash("dist"), "+")),
								new Secondary[] { Secondary.PALATOALVEOLAR },
								List.of(new Matrix(new Pair(pm, pm.hasher.hash("COR"), "+"))))),
				entry(Secondary.PALATOALVEOLAR,
						new SecondaryArticulation("̺",
								new Matrix(new Pair(pm, pm.hasher.hash("ant"), "-"),
										new Pair(pm, pm.hasher.hash("dist"), "+")),
								new Secondary[] { Secondary.DENTAL },
								List.of(new Matrix(new Pair(pm, pm.hasher.hash("COR"), "+"))))),
				entry(Secondary.DEVOICING, new SecondaryArticulation(pm, "̥", pm.hasher.hash("voice"), "-",
						new Secondary[] {}, List.of(new Matrix(new Pair(pm, pm.hasher.hash("voice"), "+"))))),
				entry(Secondary.NASALIZATION,
						new SecondaryArticulation(
								pm, "̃", pm.hasher.hash("nasal"), "+", new Secondary[] {},
								List.of(new Matrix(new Pair(pm, pm.hasher.hash("son"), "+"),
										new Pair(pm, pm.hasher.hash("nasal"), "-"))))),
				entry(Secondary.LABIALIZATION, new SecondaryArticulation("ʷ",
						new Matrix(
								new Pair(pm, pm.hasher.hash("LAB"), "+"), new Pair(pm, pm.hasher.hash("round"), "+")),
						new Secondary[] {},
						List.of(new Matrix(new Pair(pm, pm.hasher.hash("cons"), "+"),
								new Pair(pm, pm.hasher.hash("syl"), "-"), new Pair(pm, pm.hasher.hash("LAB"), "-")),
								new Matrix(new Pair(pm, pm.hasher.hash("cons"), "-"),
										new Pair(pm, pm.hasher.hash("syl"), "-"),
										new Pair(pm, pm.hasher.hash("LAB"), "-"))))),
				entry(Secondary.PALATALIZATION,
						new SecondaryArticulation("ʲ", new Matrix(new Pair(pm, pm.hasher.hash("DOR"), "+"),
								new Pair(pm, pm.hasher.hash("high"), "+"), new Pair(pm, pm.hasher.hash("low"), "-"),
								new Pair(pm, pm.hasher.hash("front"), "+"), new Pair(pm, pm.hasher.hash("back"), "-")),
								new Secondary[] { Secondary.VELARIZATION, Secondary.PHARYNGEALIZATION },
								List.of(new Matrix(new Pair(pm, pm.hasher.hash("cons"), "+"),
										new Pair(pm, pm.hasher.hash("syl"), "-"),
										new Pair(pm, pm.hasher.hash("back"), "-")),
										new Matrix(new Pair(pm, pm.hasher.hash("cons"), "-"),
												new Pair(pm, pm.hasher.hash("syl"), "-"),
												new Pair(pm, pm.hasher.hash("back"), "-")),
										new Matrix(new Pair(pm, pm.hasher.hash("cons"), "+"),
												new Pair(pm, pm.hasher.hash("syl"), "-"),
												new Pair(pm, pm.hasher.hash("back"), "0")),
										new Matrix(new Pair(pm, pm.hasher.hash("cons"), "-"),
												new Pair(pm, pm.hasher.hash("syl"), "-"),
												new Pair(pm, pm.hasher.hash("back"), "0"))))),
				entry(Secondary.VELARIZATION,
						new SecondaryArticulation("ˠ", new Matrix(new Pair(pm, pm.hasher.hash("DOR"), "+"),
								new Pair(pm, pm.hasher.hash("high"), "+"), new Pair(pm, pm.hasher.hash("low"), "-"),
								new Pair(pm, pm.hasher.hash("front"), "-"), new Pair(pm, pm.hasher.hash("back"), "+")),
								new Secondary[] { Secondary.PALATALIZATION, Secondary.PHARYNGEALIZATION },
								List.of(new Matrix(new Pair(pm, pm.hasher.hash("cons"), "+"),
										new Pair(pm, pm.hasher.hash("syl"), "-"),
										new Pair(pm, pm.hasher.hash("back"), "-")),
										new Matrix(new Pair(pm, pm.hasher.hash("cons"), "-"),
												new Pair(pm, pm.hasher.hash("syl"), "-"),
												new Pair(pm, pm.hasher.hash("back"), "-")),
										new Matrix(new Pair(pm, pm.hasher.hash("cons"), "+"),
												new Pair(pm, pm.hasher.hash("syl"), "-"),
												new Pair(pm, pm.hasher.hash("back"), "0")),
										new Matrix(new Pair(pm, pm.hasher.hash("cons"), "-"),
												new Pair(pm, pm.hasher.hash("syl"), "-"),
												new Pair(pm, pm.hasher.hash("back"), "0"))))),
				entry(Secondary.PHARYNGEALIZATION,
						new SecondaryArticulation("ˤ", new Matrix(new Pair(pm, pm.hasher.hash("DOR"), "+"),
								new Pair(pm, pm.hasher.hash("high"), "-"), new Pair(pm, pm.hasher.hash("low"), "+"),
								new Pair(pm, pm.hasher.hash("front"), "-"), new Pair(pm, pm.hasher.hash("back"), "+")),
								new Secondary[] { Secondary.VELARIZATION, Secondary.PALATALIZATION },
								List.of(new Matrix(new Pair(pm, pm.hasher.hash("cons"), "+"),
										new Pair(pm, pm.hasher.hash("syl"), "-")),
										new Matrix(new Pair(pm, pm.hasher.hash("cons"), "-"),
												new Pair(pm, pm.hasher.hash("syl"), "-"))))),
				entry(Secondary.ASPIRATION,
						new SecondaryArticulation("ʰ", new Matrix(
								new Pair(pm, pm.hasher.hash("sg"), "+"), new Pair(pm, pm.hasher.hash("cg"), "-")),
								new Secondary[] {},
								List.of(new Matrix(new Pair(pm, pm.hasher.hash("cons"), "+"),
										new Pair(pm, pm.hasher.hash("syl"), "-")),
										new Matrix(new Pair(pm, pm.hasher.hash("cons"), "-"),
												new Pair(pm, pm.hasher.hash("syl"), "-"))))),
				entry(Secondary.LENGTH, new SecondaryArticulation(pm, "ː", pm.hasher.hash("long"), "+",
						new Secondary[] {}, List.of(new Matrix(new Pair(pm, pm.hasher.hash("long"), "-"))))));
	}

	private void initCache(final String baseFilename) throws IOException {
		String cacheFilename = baseFilename.replaceFirst(".*[\\\\\\/]", "");
		System.out.println("Cache not initialized...");
		try {
			System.out.println("Reading file <" + baseFilename + ">...");
			readFile(pm, null, baseFilename, "\t");

			System.out.println("Generating variants...");

			final Map<Matrix, ArrayList<String>> base = new HashMap<>();
			base.putAll(loadedPhones);

			for (final Map.Entry<Matrix, ArrayList<String>> e : base.entrySet()) {
				System.out.println("\tGenerating Variants of <" + e.getValue().get(0) + ">");
				recursiveApply(e.getValue().get(0), e.getKey(), new ArrayList<>(),
						new ArrayList<>(List.of(Secondary.values())));
			}

			List<Phone> phones = new ArrayList<>();

			for (final Map.Entry<Matrix, ArrayList<String>> e : loadedPhones.entrySet()) {
				String shortest = e.getValue().get(0);
				for (final String s : e.getValue())
					if (s.length() <= shortest.length())
						shortest = s;
				phones.add(new Phone(pm, shortest, e.getKey(), false));
			}

			final File directory = new File(Main.getGlobalOption("PATH"), ".config/cache");
			if (!directory.exists())
				directory.mkdir();

			System.out.println("Saving cache (" + loadedPhones.size() + ") to " + cacheFilename + ".data...");

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(directory, cacheFilename + ".data")));
			bw.write("SEGMENT");
			for (int i : pm.featureNames) {
				bw.write("\t" + pm.hasher.deHash(i));
			}
			bw.write("\n");
			for (Phone p : phones) {
				bw.write(p.getDataString("\t") + "\n");
			}
			bw.close();
		} catch (final Exception e2) {
			e2.printStackTrace();
			throw new IOException("File <" + baseFilename + "> not in directory, cannot load base phones");
		}
	}

	public PhoneLoader(final String baseFilename, final boolean force) throws IOException {
		loadedPhones = new HashMap<>();
		this.pm = new PhoneManager();
		initalizeSecondary(pm);
		if (force)
			initCache(baseFilename);
		else {
			String cacheFilename = baseFilename.replaceFirst(".*[\\\\\\/]", "");
			final File directory = new File(Main.getGlobalOption("PATH"), ".config/cache");
			System.out.println("Loading Cache for <" + baseFilename + ">...");
			try {
				readFile(pm, directory.getAbsolutePath(), cacheFilename + ".data", "\t");
				System.out.println("Setting Phones...");
				for (final Map.Entry<Matrix, ArrayList<String>> e : loadedPhones.entrySet()) {
					new Phone(pm, e.getValue().get(0), e.getKey(), false);
				}
			} catch (Exception e) {
				System.out.println("Cache not found <" + directory.getAbsolutePath() + "\\" + cacheFilename + ".data> generating new cache");
				initCache(baseFilename);
			}
		}
	}

	private void recursiveApply(final String segment, final Matrix m, final ArrayList<Secondary> applied,
			final ArrayList<Secondary> list) {
		if (list.isEmpty())
			return;

		for (int i = 0; i < list.size(); i++) {
			if (!secondaryLibrary.get(list.get(i)).canApply(m, applied))
				continue;

			final Matrix newMatrix = m.transform(pm, secondaryLibrary.get(list.get(i)).getMatrix());
			final String newSegment = segment + secondaryLibrary.get(list.get(i)).getSegment();

			if (!loadedPhones.containsKey(newMatrix))
				loadedPhones.put(newMatrix, new ArrayList<>());
			loadedPhones.get(newMatrix).add(newSegment);

			final ArrayList<Secondary> newList = new ArrayList<>();
			newList.addAll(list);
			for (int j = 0; j <= i; j++)
				newList.remove(0);

			final ArrayList<Secondary> newListA = new ArrayList<>();
			newListA.addAll(applied);
			newListA.add(list.get(i));

			recursiveApply(newSegment, newMatrix, newListA, newList);
		}
	}

	static boolean isSecondary(final char c) {
		for (final Map.Entry<Secondary, SecondaryArticulation> e : secondaryLibrary.entrySet()) {
			if (e.getValue().getSegment().charAt(0) == c)
				return true;
		}
		return false;
	}

	public PhoneManager getManager() {
		return pm;
	}
}