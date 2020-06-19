package main.phl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhoneManager implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<Phone> phoneLibrary;
	private List<String> baseValues;

	public PhoneManager() {
		phoneLibrary = new ArrayList<>();
		baseValues = new ArrayList<>();
	}

	public Word interpretSequence(String s) {
		List<Phone> phones = new ArrayList<>();
		List<Word.SyllableDelim> delims = new ArrayList<>();
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
			StringBuilder curr = new StringBuilder();
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
		for (Phone p : phoneLibrary) {
			if (p.getSegment().equals(segment)) {
				base = p;
				break;
			}
		}
		if (base == null)
			throw new IllegalArgumentException(
					"Cannot interpret [" + segment + s + "], no base phone found from data.");

		List<Phone.Secondary> applied = new ArrayList<>();
		for (int i = 0; i < s.length(); i++) {
			boolean flag = false;
			for (Map.Entry<Phone.Secondary, SecondaryArticulation> e : Phone.secondaryLibrary.entrySet()) {
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

	public Pair interpretFeature(String s) {
		String[] split = s.split("\\|");
		String quality = split[0];
		String value = split[1];
		Phone.Feature feature = null;
		for (Phone.Feature f : Phone.Feature.values()) {
			if (f.toString().equals(value)) {
				feature = f;
				break;
			}
		}

		if (feature == null || quality == null)
			return null;

		return new Pair(feature, quality);
	}

	public Phone fuzzySearch(Phone p) {
		Matrix features = p.getMatrix();
		for (int i = 0; i < phoneLibrary.size(); i++) {
			Phone temp = phoneLibrary.get(i);
			boolean flag = true;
			for (Pair e : features) {
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

	public List<Phone> getPhones(List<Phone> library, Matrix map) {
		List<Phone> phones = new ArrayList<>();

		for (int i = 0; i < library.size(); i++) {
			if (library.get(i).hasFeatures(map))
				phones.add(library.get(i));
		}

		return phones;
	}

	public boolean contains(Phone phone) {
		return phoneLibrary.contains(phone);
	}

	public void add(Phone phone) {
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

	public Matrix getCommon(List<Phone> phones) {
		Matrix common = new Matrix();
		for (int i = 0; i < Phone.Feature.values().length; i++) {
			String f = phones.get(0).getFeatureQuality(Phone.Feature.values()[i]);
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

	public Matrix getContrast(Phone a, Phone b) {
		Matrix contrast = new Matrix();
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

	public Phone validate(Phone p) {
		return phoneLibrary.get(phoneLibrary.indexOf(p));
	}
}