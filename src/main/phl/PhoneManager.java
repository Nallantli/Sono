package main.phl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoneManager {
	private final Map<Matrix, String> phoneLibrary;
	private final List<Phone> baseLibrary;
	private final List<String> baseValues;

	public List<Integer> featureNames = new ArrayList<>();

	public Map<Integer, List<Integer>> majorClasses = new HashMap<>();

	public Hasher hasher;

	public PhoneManager() {
		phoneLibrary = new HashMap<>();
		baseLibrary = new ArrayList<>();
		baseValues = new ArrayList<>();
		hasher = new Hasher();
	}

	public Word interpretSequence(final String s) {
		final List<Phone> phones = new ArrayList<>();
		final List<Word.SyllableDelim> delims = new ArrayList<>();
		int i = 0;
		while (i < s.length()) {
			if (s.charAt(i) == '+') {
				delims.add(Word.SyllableDelim.MORPHEME);
				i++;
			} else if (s.charAt(i) == '.') {
				delims.add(Word.SyllableDelim.DELIM);
				i++;
			} else {
				delims.add(Word.SyllableDelim.NULL);
			}
			final StringBuilder curr = new StringBuilder();
			while (i < s.length() && (s.charAt(i) == '_' || !baseValues.contains(curr.toString()))) {
				curr.append(s.charAt(i++));
			}
			while (i < s.length() && PhoneLoader.isSecondary(s.charAt(i))) {
				curr.append(s.charAt(i++));
			}
			if (curr.length() > 0) {
				phones.add(interpretSegment(curr.toString()));
			}
		}
		return new Word(phones, delims);
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
			for (Map.Entry<Matrix, String> e : phoneLibrary.entrySet()) {
				if (e.getValue().equals(segment)) {
					base = e.getKey();
					break;
				}
			}
		}
		if (base == null)
			throw new IllegalArgumentException(
					"Cannot interpret [" + segment + s + "], no base phone found from data.");

		// String newSegment = base.getSegment();
		Matrix newMatrix = base;
		final List<PhoneLoader.Secondary> applied = new ArrayList<>();
		for (int i = 0; i < s.length(); i++) {
			boolean flag = false;
			for (final Map.Entry<PhoneLoader.Secondary, SecondaryArticulation> e : PhoneLoader.secondaryLibrary
					.entrySet()) {
				if (s.charAt(i) == e.getValue().getSegment().charAt(0)) {
					flag = true;
					applied.add(e.getKey());
					// newSegment += PhoneLoader.secondaryLibrary.get(e.getKey()).getSegment();
					newMatrix = newMatrix.transform(this, PhoneLoader.secondaryLibrary.get(e.getKey()).getMatrix());
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

	public Pair interpretFeature(final String s) {
		final String[] split = s.split("\\|");
		final String quality = split[0];
		final int value = hasher.hash(split[1]);
		int feature = -1;
		for (final int f : featureNames) {
			if (f == value) {
				feature = f;
				break;
			}
		}

		if (feature == -1 || quality == null)
			return null;

		return new Pair(this, feature, quality);
	}

	public Matrix fuzzySearch(final Matrix m) {
		final Matrix features = m;
		for (Map.Entry<Matrix, String> pl : phoneLibrary.entrySet()) {
			final Matrix temp = pl.getKey();
			boolean flag = true;
			for (final Pair e : features) {
				if (e.getQuality().equals("0"))
					continue;
				if (!e.getQuality().equals(temp.getQuality(e.getFeature()))) {
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
		for (final Map.Entry<Integer, List<Integer>> e : majorClasses.entrySet()) {
			if (e.getValue().contains(feature))
				return e.getKey();
		}
		return -1;
	}

	public List<Phone> getPhones(final List<Phone> library, final Matrix map) {
		final List<Phone> phones = new ArrayList<>();

		for (int i = 0; i < library.size(); i++) {
			if (library.get(i).hasFeatures(map))
				phones.add(library.get(i));
		}

		return phones;
	}

	public boolean contains(final Matrix matrix) {
		return phoneLibrary.containsKey(matrix);
	}

	public void add(final Phone phone, final boolean validate) {
		if (validate) {
			if (!phone.getSegment().equals("*")) {
				if (!phoneLibrary.containsKey(phone.getMatrix())) {
					phoneLibrary.put(phone.getMatrix(), phone.getSegment());
				} else if (phoneLibrary.get(phone.getMatrix()).length() > phone.getSegment().length()) {
					phoneLibrary.put(phone.getMatrix(), phone.getSegment());
				}
			}
		} else {
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
		final Matrix common = new Matrix();
		for (int i = 0; i < featureNames.size(); i++) {
			final String f = phones.get(0).getFeatureQuality(featureNames.get(i));
			boolean flag = true;
			for (int j = 1; j < phones.size(); j++)
				if (!phones.get(j).getFeatureQuality(featureNames.get(i)).equals(f)) {
					flag = false;
					break;
				}
			if (flag && !f.equals("0"))
				common.put(this, featureNames.get(i), f);
		}
		return common;
	}

	public Matrix getContrast(final Phone a, final Phone b) {
		final Matrix contrast = new Matrix();
		for (int i = 0; i < featureNames.size(); i++) {
			if (!a.getFeatureQuality(featureNames.get(i)).equals(b.getFeatureQuality(featureNames.get(i)))
					&& !b.getFeatureQuality(featureNames.get(i)).equals("0")) {
				contrast.put(this, featureNames.get(i), b.getFeatureQuality(featureNames.get(i)));
			}
		}
		return contrast;
	}

	public List<Phone> getAllPhones() {
		List<Phone> phones = new ArrayList<>();
		for (Map.Entry<Matrix, String> e : phoneLibrary.entrySet()) {
			phones.add(new Phone(this, e.getValue(), e.getKey(), false));
		}
		return phones;
	}

	public List<Phone> getBasePhones() {
		return baseLibrary;
	}

	public Phone validate(final Matrix m) {
		return new Phone(this, phoneLibrary.get(m), m, true);
	}
}