package main.phl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.SonoWrapper;

public class PhoneLoader {
	public enum Secondary {
		VOCALIC, RETRACTED, ADVANCED, PALATOALVEOLAR, DENTAL, DEVOICING, NASALIZATION, LABIALIZATION, PALATALIZATION,
		VELARIZATION, PHARYNGEALIZATION, ASPIRATION, LENGTH
	}

	private final Map<Secondary, SecondaryArticulation> secondaryLibrary = new EnumMap<>(Secondary.class);

	private final PhoneManager pm;
	private final Map<Matrix, ArrayList<String>> loadedPhones;

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
						if (Character.isUpperCase(split[i].charAt(0))) {
							pm.getMajorClasses().put(Hasher.hash(split[i]), new ArrayList<>());
							major = split[i];
						}
						pm.getFeatureNames().add(Hasher.hash(split[i]));
						if (major != null && !major.equals(split[i])) {
							pm.getMajorClasses().get(Hasher.hash(major)).add(Hasher.hash(split[i]));
						}
					}
					System.out.println("FEATURES\t" + pm.getFeatureNames());
					continue;
				}
				final Matrix features = new Matrix();
				int i = 1;
				for (final int f : pm.getFeatureNames()) {
					features.put(pm, f, Hasher.hash(split[i]));
					i++;
				}

				if (!loadedPhones.containsKey(features))
					loadedPhones.put(features, new ArrayList<>());
				loadedPhones.get(features).add(segment);
			}
		}
	}

	private void initCache(final String baseFilename) throws IOException {
		final String cacheFilename = baseFilename.replaceFirst(".*[\\\\\\/]", "");
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

			final List<Phone> phones = new ArrayList<>();

			for (final Map.Entry<Matrix, ArrayList<String>> e : loadedPhones.entrySet()) {
				String shortest = e.getValue().get(0);
				for (final String s : e.getValue())
					if (s.length() <= shortest.length())
						shortest = s;
				phones.add(new Phone(pm, shortest, e.getKey(), false));
			}

			final File directory = new File(SonoWrapper.getGlobalOption("PATH"), ".config/cache");
			if (!directory.exists())
				directory.mkdir();

			System.out.println("Saving cache (" + loadedPhones.size() + ") to " + directory.getAbsolutePath() + "/"
					+ cacheFilename + ".data...");

			try (BufferedWriter bw = new BufferedWriter(
					new FileWriter(new File(directory, cacheFilename + ".data")));) {
				bw.write("SEGMENT");
				for (final int i : pm.getFeatureNames())
					bw.write("\t" + Hasher.deHash(i));
				bw.write("\n");
				for (final Phone p : phones)
					bw.write(p.getDataString("\t") + "\n");
			}
		} catch (final Exception e2) {
			e2.printStackTrace();
			throw new IOException("File <" + baseFilename + "> not in directory, cannot load base phones");
		}
	}

	public PhoneLoader(final String baseFilename, final boolean force) throws IOException {
		loadedPhones = new HashMap<>();
		this.pm = new PhoneManager(this);
		secondaryLibrary.put(Secondary.VOCALIC,
				new SecondaryArticulation("̩", Hasher.hash("syl"), Hasher.TRUE, Collections.emptyList(),
						List.of(new Matrix(new Pair(Hasher.hash("cons"), Hasher.TRUE),
								new Pair(Hasher.hash("son"), Hasher.TRUE),
								new Pair(Hasher.hash("syl"), Hasher.FALSE)))));
		secondaryLibrary.put(Secondary.RETRACTED,
				new SecondaryArticulation("̠",
						new Matrix(new Pair(Hasher.hash("front"), Hasher.FALSE),
								new Pair(Hasher.hash("back"), Hasher.TRUE)),
						List.of(Secondary.ADVANCED), List.of(new Matrix(new Pair(Hasher.hash("COR"), Hasher.TRUE)))));
		secondaryLibrary.put(Secondary.ADVANCED,
				new SecondaryArticulation("̟",
						new Matrix(new Pair(Hasher.hash("front"), Hasher.TRUE),
								new Pair(Hasher.hash("back"), Hasher.FALSE)),
						List.of(Secondary.RETRACTED), List.of(new Matrix(new Pair(Hasher.hash("COR"), Hasher.TRUE)))));
		secondaryLibrary.put(Secondary.DENTAL,
				new SecondaryArticulation("̪",
						new Matrix(new Pair(Hasher.hash("ant"), Hasher.TRUE),
								new Pair(Hasher.hash("dist"), Hasher.TRUE)),
						List.of(Secondary.PALATOALVEOLAR),
						List.of(new Matrix(new Pair(Hasher.hash("COR"), Hasher.TRUE)))));
		secondaryLibrary.put(Secondary.PALATOALVEOLAR,
				new SecondaryArticulation("̺",
						new Matrix(new Pair(Hasher.hash("ant"), Hasher.FALSE),
								new Pair(Hasher.hash("dist"), Hasher.TRUE)),
						List.of(Secondary.DENTAL), List.of(new Matrix(new Pair(Hasher.hash("COR"), Hasher.TRUE)))));
		secondaryLibrary.put(Secondary.DEVOICING, new SecondaryArticulation("̥", Hasher.hash("voice"), Hasher.FALSE,
				Collections.emptyList(), List.of(new Matrix(new Pair(Hasher.hash("voice"), Hasher.TRUE)))));
		secondaryLibrary.put(Secondary.NASALIZATION,
				new SecondaryArticulation("̃", Hasher.hash("nasal"), Hasher.TRUE, Collections.emptyList(),
						List.of(new Matrix(new Pair(Hasher.hash("son"), Hasher.TRUE),
								new Pair(Hasher.hash("nasal"), Hasher.FALSE)))));
		secondaryLibrary.put(Secondary.LABIALIZATION,
				new SecondaryArticulation("ʷ",
						new Matrix(
								new Pair(Hasher.hash("LAB"), Hasher.TRUE), new Pair(Hasher.hash("round"), Hasher.TRUE)),
						Collections.emptyList(),
						List.of(new Matrix(new Pair(Hasher.hash("cons"), Hasher.TRUE),
								new Pair(Hasher.hash("syl"), Hasher.FALSE)),
								new Matrix(new Pair(Hasher.hash("cons"), Hasher.FALSE),
										new Pair(Hasher.hash("syl"), Hasher.FALSE)))));
		secondaryLibrary.put(Secondary.PALATALIZATION, new SecondaryArticulation("ʲ",
				new Matrix(new Pair(Hasher.hash("DOR"), Hasher.TRUE), new Pair(Hasher.hash("high"), Hasher.TRUE),
						new Pair(Hasher.hash("low"), Hasher.FALSE), new Pair(Hasher.hash("front"), Hasher.TRUE),
						new Pair(Hasher.hash("back"), Hasher.FALSE)),
				List.of(Secondary.VELARIZATION, Secondary.PHARYNGEALIZATION),
				List.of(new Matrix(new Pair(Hasher.hash("cons"), Hasher.TRUE),
						new Pair(Hasher.hash("syl"), Hasher.FALSE), new Pair(Hasher.hash("back"), Hasher.FALSE)),
						new Matrix(new Pair(Hasher.hash("cons"), Hasher.FALSE),
								new Pair(Hasher.hash("syl"), Hasher.FALSE),
								new Pair(Hasher.hash("back"), Hasher.FALSE)),
						new Matrix(new Pair(Hasher.hash("cons"), Hasher.TRUE),
								new Pair(Hasher.hash("syl"), Hasher.FALSE), new Pair(Hasher.hash("back"), Hasher.ZERO)),
						new Matrix(new Pair(Hasher.hash("cons"), Hasher.FALSE),
								new Pair(Hasher.hash("syl"), Hasher.FALSE),
								new Pair(Hasher.hash("back"), Hasher.ZERO)))));
		secondaryLibrary.put(Secondary.VELARIZATION, new SecondaryArticulation("ˠ",
				new Matrix(new Pair(Hasher.hash("DOR"), Hasher.TRUE), new Pair(Hasher.hash("high"), Hasher.TRUE),
						new Pair(Hasher.hash("low"), Hasher.FALSE), new Pair(Hasher.hash("front"), Hasher.FALSE),
						new Pair(Hasher.hash("back"), Hasher.TRUE)),
				List.of(Secondary.PALATALIZATION, Secondary.PHARYNGEALIZATION),
				List.of(new Matrix(new Pair(Hasher.hash("cons"), Hasher.TRUE),
						new Pair(Hasher.hash("syl"), Hasher.FALSE), new Pair(Hasher.hash("back"), Hasher.FALSE)),
						new Matrix(new Pair(Hasher.hash("cons"), Hasher.FALSE),
								new Pair(Hasher.hash("syl"), Hasher.FALSE),
								new Pair(Hasher.hash("back"), Hasher.FALSE)),
						new Matrix(new Pair(Hasher.hash("cons"), Hasher.TRUE),
								new Pair(Hasher.hash("syl"), Hasher.FALSE), new Pair(Hasher.hash("back"), Hasher.ZERO)),
						new Matrix(new Pair(Hasher.hash("cons"), Hasher.FALSE),
								new Pair(Hasher.hash("syl"), Hasher.FALSE),
								new Pair(Hasher.hash("back"), Hasher.ZERO)))));
		secondaryLibrary.put(Secondary.PHARYNGEALIZATION,
				new SecondaryArticulation("ˤ", new Matrix(new Pair(Hasher.hash("DOR"), Hasher.TRUE),
						new Pair(Hasher.hash("high"), Hasher.FALSE), new Pair(Hasher.hash("low"), Hasher.TRUE),
						new Pair(Hasher.hash("front"), Hasher.FALSE), new Pair(Hasher.hash("back"), Hasher.TRUE)),
						List.of(Secondary.VELARIZATION, Secondary.PALATALIZATION),
						List.of(new Matrix(new Pair(Hasher.hash("cons"), Hasher.TRUE),
								new Pair(Hasher.hash("syl"), Hasher.FALSE)),
								new Matrix(new Pair(Hasher.hash("cons"), Hasher.FALSE),
										new Pair(Hasher.hash("syl"), Hasher.FALSE)))));
		secondaryLibrary.put(Secondary.ASPIRATION,
				new SecondaryArticulation("ʰ",
						new Matrix(new Pair(Hasher.hash("sg"), Hasher.TRUE), new Pair(Hasher.hash("cg"), Hasher.FALSE)),
						Collections.emptyList(),
						List.of(new Matrix(new Pair(Hasher.hash("cons"), Hasher.TRUE),
								new Pair(Hasher.hash("syl"), Hasher.FALSE)),
								new Matrix(new Pair(Hasher.hash("cons"), Hasher.FALSE),
										new Pair(Hasher.hash("syl"), Hasher.FALSE)))));
		secondaryLibrary.put(Secondary.LENGTH, new SecondaryArticulation("ː", Hasher.hash("long"), Hasher.TRUE,
				Collections.emptyList(), List.of(new Matrix(new Pair(Hasher.hash("long"), Hasher.FALSE)))));
		if (force)
			initCache(baseFilename);
		else {
			final String cacheFilename = baseFilename.replaceFirst(".*[\\\\\\/]", "");
			final File directory = new File(SonoWrapper.getGlobalOption("PATH"), ".config/cache");
			System.out.println("Loading Cache for <" + baseFilename + ">...");
			try {
				readFile(pm, directory.getAbsolutePath(), cacheFilename + ".data", "\t");
				System.out.println("Setting Phones...");
				for (final Map.Entry<Matrix, ArrayList<String>> e : loadedPhones.entrySet())
					new Phone(pm, e.getValue().get(0), e.getKey(), false);
			} catch (final Exception e) {
				System.out.println("Cache not found <" + directory.getAbsolutePath() + "\\" + cacheFilename
						+ ".data> generating new cache");
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

	public Map<Secondary, SecondaryArticulation> getSecondaryLibrary() {
		return this.secondaryLibrary;
	}

	public boolean isSecondary(final char c) {
		for (final Map.Entry<Secondary, SecondaryArticulation> e : secondaryLibrary.entrySet())
			if (e.getValue().getSegment().charAt(0) == c)
				return true;

		return false;
	}

	public PhoneManager getManager() {
		return pm;
	}
}