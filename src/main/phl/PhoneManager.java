package main.phl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhoneManager implements Serializable {
	private static final long serialVersionUID = 1L;

	private final List<Phone> phoneLibrary;
	private final List<String> baseValues;

	public PhoneManager() {
		phoneLibrary = new ArrayList<>();
		baseValues = new ArrayList<>();
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
			while (i < s.length() && Phone.isSecondary(s.charAt(i))) {
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
		Phone base = null;
		for (final Phone p : phoneLibrary) {
			if (p.getSegment().equals(segment)) {
				base = p;
				break;
			}
		}
		if (base == null)
			throw new IllegalArgumentException(
					"Cannot interpret [" + segment + s + "], no base phone found from data.");

		final List<Phone.Secondary> applied = new ArrayList<>();
		for (int i = 0; i < s.length(); i++) {
			boolean flag = false;
			for (final Map.Entry<Phone.Secondary, SecondaryArticulation> e : Phone.secondaryLibrary.entrySet()) {
				if (s.charAt(i) == e.getValue().getSegment().charAt(0)) {
					flag = true;
					if (e.getValue().canApply(base, applied)) {
						applied.add(e.getKey());
						base = base.apply(e.getKey());
					} else {
						throw new IllegalArgumentException("Cannot interpret [" + segment + s
								+ "], secondary articulation is restricted from application to competing phonological features: "
								+ e.getKey());
					}
					break;
				}
			}
			if (!flag) {
				throw new IllegalArgumentException(
						"Cannot interpret [" + segment + s + "], secondary articulation is unknown: " + s.charAt(i));
			}
		}
		return base;
	}

	public Pair interpretFeature(final String s) {
		final String[] split = s.split("\\|");
		final String quality = split[0];
		final String value = split[1];
		Phone.Feature feature = null;
		for (final Phone.Feature f : Phone.Feature.values()) {
			if (f.toString().equals(value)) {
				feature = f;
				break;
			}
		}

		if (feature == null || quality == null)
			return null;

		return new Pair(feature, quality);
	}

	public Phone fuzzySearch(final Phone p) {
		final Matrix features = p.getMatrix();
		for (int i = 0; i < phoneLibrary.size(); i++) {
			final Phone temp = phoneLibrary.get(i);
			boolean flag = true;
			for (final Pair e : features) {
				if (e.getQuality().equals("0"))
					continue;
				if (!e.getQuality().equals(temp.getFeatureQuality(e.getFeature()))) {
					flag = false;
					break;
				}
			}
			if (flag)
				return phoneLibrary.get(i);
		}
		return null;
	}

	public List<Phone> getPhones(final List<Phone> library, final Matrix map) {
		final List<Phone> phones = new ArrayList<>();

		for (int i = 0; i < library.size(); i++) {
			if (library.get(i).hasFeatures(map))
				phones.add(library.get(i));
		}

		return phones;
	}

	public boolean contains(final Phone phone) {
		return phoneLibrary.contains(phone);
	}

	public void add(final Phone phone) {
		if (!phone.getSegment().equals("*")) {
			if (!phoneLibrary.contains(phone)) {
				phoneLibrary.add(phone);
			} else if (phoneLibrary.get(phoneLibrary.indexOf(phone)).getSegment().length() > phone.getSegment()
					.length()) {
				phoneLibrary.remove(phone);
				phoneLibrary.add(phone);
			}

			if (!baseValues.contains(phone.getSegment())
					&& ((phone.getSegment().length() == 3 && phone.getSegment().charAt(1) == '_')
							|| phone.getSegment().length() == 1))
				baseValues.add(phone.getSegment());
		}
	}

	public Matrix getCommon(final List<Phone> phones) {
		final Matrix common = new Matrix();
		for (int i = 0; i < Phone.Feature.values().length; i++) {
			final String f = phones.get(0).getFeatureQuality(Phone.Feature.values()[i]);
			boolean flag = true;
			for (int j = 1; j < phones.size(); j++)
				if (!phones.get(j).getFeatureQuality(Phone.Feature.values()[i]).equals(f)) {
					flag = false;
					break;
				}
			if (flag && !f.equals("0"))
				common.put(Phone.Feature.values()[i], f);
		}
		return common;
	}

	public Matrix getContrast(final Phone a, final Phone b) {
		final Matrix contrast = new Matrix();
		for (int i = 0; i < Phone.Feature.values().length; i++) {
			if (!a.getFeatureQuality(Phone.Feature.values()[i]).equals(b.getFeatureQuality(Phone.Feature.values()[i]))
					&& !b.getFeatureQuality(Phone.Feature.values()[i]).equals("0")) {
				contrast.put(Phone.Feature.values()[i], b.getFeatureQuality(Phone.Feature.values()[i]));
			}
		}
		return contrast;
	}

	public List<Phone> getAllPhones() {
		return phoneLibrary;
	}

	public Phone validate(final Phone p) {
		return phoneLibrary.get(phoneLibrary.indexOf(p));
	}
}