package main.phl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoneManager {
	private final Map<Matrix, String> phoneLibrary;
	private final List<Phone> baseLibrary;
	private final List<String> baseValues;

	private final List<Integer> featureNames = new ArrayList<>();

	private final Map<Integer, List<Integer>> majorClasses = new HashMap<>();

	private final PhoneLoader loader;

	public PhoneManager(final PhoneLoader loader) {
		this.loader = loader;
		phoneLibrary = new HashMap<>();
		baseLibrary = new ArrayList<>();
		baseValues = new ArrayList<>();
	}

	public List<Integer> getFeatureNames() {
		return this.featureNames;
	}

	public Map<Integer, List<Integer>> getMajorClasses() {
		return this.majorClasses;
	}

	public Word interpretSequence(final String s) {
		final List<Phone> phones = new ArrayList<>();
		final List<Word.SyllableDelim> delimits = new ArrayList<>();
		int i = 0;
		while (i < s.length()) {
			switch (s.charAt(i)) {
				case '+':
					delimits.add(Word.SyllableDelim.MORPHEME);
					i++;
					break;
				case '.':
					delimits.add(Word.SyllableDelim.DELIM);
					i++;
					break;
				default:
					delimits.add(Word.SyllableDelim.NULL);
					break;
			}
			final StringBuilder curr = new StringBuilder();
			while (i < s.length() && (s.charAt(i) == '_' || !baseValues.contains(curr.toString())))
				curr.append(s.charAt(i++));
			while (i < s.length() && loader.isSecondary(s.charAt(i)))
				curr.append(s.charAt(i++));
			if (curr.length() > 0)
				phones.add(interpretSegment(curr.toString()));
		}
		return new Word(phones, delimits);
	}

	public Phone interpretSegment(String s) {
		String segment;
		if (s.length() >= 3 && s.charAt(1) == '_') {
			segment = s.substring(0, 3);
			s = s.substring(3);
		} else {
			segment = s.substring(0, 1);
			s = s.substring(1);
		}
		Matrix base = null;
		if (phoneLibrary.containsValue(segment)) {
			for (final Map.Entry<Matrix, String> e : phoneLibrary.entrySet()) {
				if (e.getValue().equals(segment)) {
					base = e.getKey();
					break;
				}
			}
		}
		if (base == null)
			throw new IllegalArgumentException(
					"Cannot interpret [" + segment + s + "], no base phone found from data.");

		Matrix newMatrix = base;
		final List<PhoneLoader.Secondary> applied = new ArrayList<>();
		for (int i = 0; i < s.length(); i++) {
			boolean flag = false;
			for (final Map.Entry<PhoneLoader.Secondary, SecondaryArticulation> e : loader.getSecondaryLibrary()
					.entrySet()) {
				if (s.charAt(i) == e.getValue().getSegment().charAt(0)) {
					flag = true;
					applied.add(e.getKey());
					newMatrix = newMatrix.transform(loader.getSecondaryLibrary().get(e.getKey()).getMatrix());
					break;
				}
			}
			if (!flag) {
				throw new IllegalArgumentException(
						"Cannot interpret [" + segment + s + "], secondary articulation is unknown: " + s.charAt(i));
			}
		}
		return this.validate(newMatrix);
	}

	public Feature interpretFeature(final String s) {
		final String[] split = s.split("\\|");
		final int quality = Hasher.hash(split[0]);
		final int value = Hasher.hash(split[1]);
		int feature = -1;
		for (final int f : featureNames) {
			if (f == value) {
				feature = f;
				break;
			}
		}

		if (feature == -1)
			return null;

		return new Feature(feature, quality);
	}

	public Matrix fuzzySearch(final Matrix m) {
		final Matrix features = m;
		for (final Map.Entry<Matrix, String> pl : phoneLibrary.entrySet()) {
			final Matrix temp = pl.getKey();
			boolean flag = true;
			for (final Feature e : features) {
				if (e.getQuality() == Hasher.ZERO)
					continue;
				if (e.getQuality() != temp.getQuality(e.getKey())) {
					flag = false;
					break;
				}
			}
			if (flag)
				return pl.getKey();
		}
		return null;
	}

	public int inMajorClass(final int feature) {
		for (final Map.Entry<Integer, List<Integer>> e : majorClasses.entrySet())
			if (e.getValue().contains(feature))
				return e.getKey();
		return -1;
	}

	public List<Phone> getPhones(final List<Phone> library, final Matrix map) {
		final List<Phone> phones = new ArrayList<>();

		for (int i = 0; i < library.size(); i++)
			if (library.get(i).hasFeatures(map))
				phones.add(library.get(i));

		return phones;
	}

	public boolean contains(final Matrix matrix) {
		return phoneLibrary.containsKey(matrix);
	}

	public void add(final Phone phone, final boolean validate) {
		if (validate) {
			if (!phoneLibrary.containsKey(phone.getMatrix())
					|| phoneLibrary.get(phone.getMatrix()).length() > phone.getSegment().length())
				phoneLibrary.put(phone.getMatrix(), phone.getSegment());
		} else if (!phone.getSegment().equals("*")) {
			phoneLibrary.put(phone.getMatrix(), phone.getSegment());
			if (!baseValues.contains(phone.getSegment())
					&& ((phone.getSegment().length() == 3 && phone.getSegment().charAt(1) == '_')
							|| phone.getSegment().length() == 1)) {
				baseValues.add(phone.getSegment());
				baseLibrary.add(phone);
			}
		}
	}

	public Matrix getCommon(final List<Phone> phones) {
		final Matrix common = new Matrix(this);
		for (int i = 0; i < featureNames.size(); i++) {
			final int f = phones.get(0).getFeatureQuality(featureNames.get(i));
			boolean flag = true;
			for (int j = 1; j < phones.size(); j++)
				if (phones.get(j).getFeatureQuality(featureNames.get(i)) != f) {
					flag = false;
					break;
				}
			if (flag && f != Hasher.ZERO)
				common.put(featureNames.get(i), f);
		}
		return common;
	}

	public Matrix getContrast(final Phone a, final Phone b) {
		final Matrix contrast = new Matrix(this);
		for (int i = 0; i < featureNames.size(); i++)
			if (a.getFeatureQuality(featureNames.get(i)) != b.getFeatureQuality(featureNames.get(i))
					&& b.getFeatureQuality(featureNames.get(i)) != Hasher.ZERO)
				contrast.put(featureNames.get(i), b.getFeatureQuality(featureNames.get(i)));

		return contrast;
	}

	public List<Phone> getAllPhones() {
		final List<Phone> phones = new ArrayList<>();
		for (final Map.Entry<Matrix, String> e : phoneLibrary.entrySet())
			phones.add(new Phone(this, e.getValue(), e.getKey(), false));

		return phones;
	}

	public List<Phone> getBasePhones() {
		return baseLibrary;
	}

	public Phone registerNewPhone(final String segment, final Matrix features) {
		if (validate(features) != null)
			return null;
		phoneLibrary.put(features, segment);
		return new Phone(this, segment, features, false);
	}

	public Phone validate(final Matrix m) {
		if (m == null)
			return null;
		if (phoneLibrary.containsKey(m))
			return new Phone(this, phoneLibrary.get(m), m, true);
		else
			return new Phone(this, "*", m, false);
	}
}