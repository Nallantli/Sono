package main.phl;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

import java.io.Serializable;

class SecondaryArticulation implements Serializable {
	private static final long serialVersionUID = 1L;

	private String segment;
	private Matrix matrix;
	private List<Matrix> requirements;
	private Phone.Secondary[] restrictions;

	public SecondaryArticulation(String segment, Matrix matrix, Phone.Secondary[] restrictions,
			List<Matrix> requirements) {
		this.matrix = matrix;
		this.restrictions = restrictions;
		this.segment = segment;
		this.requirements = requirements;
	}

	public SecondaryArticulation(String segment, Phone.Feature feature, String value,
			Phone.Secondary[] restrictions, List<Matrix> requirements) {
		this(segment, new Matrix(new Pair(feature, value)), restrictions, requirements);
	}

	public Matrix getMatrix() {
		return matrix;
	}

	public boolean canApply(Phone phone, List<Phone.Secondary> applied) {
		for (int i = 0; i < restrictions.length; i++)
			if (applied.contains(restrictions[i]))
				return false;

		if (requirements.isEmpty())
			return true;

		for (int i = 0; i < requirements.size(); i++) {
			boolean flag = true;
			Matrix r = requirements.get(i);
			for (Pair feature : r) {
				if (!phone.getFeatureQuality(feature.getFeature()).equals(feature.getQuality())) {
					flag = false;
					break;
				}
			}
			if (flag)
				return true;
		}

		return false;
	}

	public String getSegment() {
		return segment;
	}
}

public class Phone implements Comparable<Phone>, Serializable {
	private static final long serialVersionUID = 1L;

	private PhoneManager pm;

	/*public enum String {
		TRUE {
			@Override
			public String toString() {
				return "+";
			}
		},
		FALSE {
			@Override
			public String toString() {
				return "-";
			}
		},
		NULL {
			@Override
			public String toString() {
				return "0";
			}
		},
		// RULE ONLY
		ALPHA {
			@Override
			public String toString() {
				return "A-";
			}
		},
		BETA {
			@Override
			public String toString() {
				return "B-";
			}
		},
		GAMMA {
			@Override
			public String toString() {
				return "C-";
			}
		},

		// MATRIX ADDITION
		ANY {
			@Override
			public String toString() {
				return "~";
			}
		}
	}*/

	public enum Feature {
		STRESS {
			@Override
			public String toString() {
				return "stress";
			}
		},
		LONG {
			@Override
			public String toString() {
				return "long";
			}
		},
		SYLLABIC {
			@Override
			public String toString() {
				return "syl";
			}
		},
		CONSONANTAL {
			@Override
			public String toString() {
				return "cons";
			}
		},
		APPROXIMANT {
			@Override
			public String toString() {
				return "approx";
			}
		},
		SONORANT {
			@Override
			public String toString() {
				return "son";
			}
		},
		CONTINUANT {
			@Override
			public String toString() {
				return "cont";
			}
		},
		DELAYED_RELEASE {
			@Override
			public String toString() {
				return "del";
			}
		},
		NASAL {
			@Override
			public String toString() {
				return "nasal";
			}
		},
		STRIDENT {
			@Override
			public String toString() {
				return "str";
			}
		},
		VOICE {
			@Override
			public String toString() {
				return "voice";
			}
		},
		SPREAD_GLOTTIS {
			@Override
			public String toString() {
				return "sg";
			}
		},
		CONSTRICTED_GLOTTIS {
			@Override
			public String toString() {
				return "cg";
			}
		},
		LABIAL {
			@Override
			public String toString() {
				return "LAB";
			}
		},
		ROUND {
			@Override
			public String toString() {
				return "round";
			}
		},
		LABIODENTAL {
			@Override
			public String toString() {
				return "ld";
			}
		},
		CORONAL {
			@Override
			public String toString() {
				return "COR";
			}
		},
		ANTERIOR {
			@Override
			public String toString() {
				return "ant";
			}
		},
		DISTRIBUTED {
			@Override
			public String toString() {
				return "dist";
			}
		},
		LATERAL {
			@Override
			public String toString() {
				return "LAT";
			}
		},
		DORSAL {
			@Override
			public String toString() {
				return "DOR";
			}
		},
		HIGH {
			@Override
			public String toString() {
				return "high";
			}
		},
		LOW {
			@Override
			public String toString() {
				return "low";
			}
		},
		FRONT {
			@Override
			public String toString() {
				return "front";
			}
		},
		BACK {
			@Override
			public String toString() {
				return "back";
			}
		},
		TENSE {
			@Override
			public String toString() {
				return "tense";
			}
		}
	}

	public enum Secondary {
		VOCALIC, RETRACTED, ADVANCED, PALATOALVEOLAR, DENTAL, DEVOICING, NASALIZATION, LABIALIZATION, PALATALIZATION,
		VELARIZATION, PHARYNGEALIZATION, ASPIRATION, LENGTH
	}

	static final Map<Feature, List<Feature>> majorClasses = Map.ofEntries(
			entry(Feature.LABIAL, List.of(Feature.ROUND, Feature.LABIODENTAL)),
			entry(Feature.CORONAL, List.of(Feature.ANTERIOR, Feature.DISTRIBUTED)),
			entry(Feature.DORSAL, List.of(Feature.HIGH, Feature.LOW, Feature.FRONT, Feature.BACK, Feature.TENSE)));

	static final Map<Secondary, SecondaryArticulation> secondaryLibrary = Map.ofEntries(
			entry(Secondary.VOCALIC,
					new SecondaryArticulation("̩", Feature.SYLLABIC, "+", new Secondary[] {},
							List.of(new Matrix(new Pair(Feature.CONSONANTAL, "+"),
									new Pair(Feature.SONORANT, "+"))))),
			entry(Secondary.RETRACTED,
					new SecondaryArticulation("̠",
							new Matrix(new Pair(Feature.FRONT, "-"), new Pair(Feature.BACK, "+")),
							new Secondary[] { Secondary.ADVANCED },
							List.of(new Matrix(new Pair(Feature.CORONAL, "+"))))),
			entry(Secondary.ADVANCED,
					new SecondaryArticulation("̟",
							new Matrix(new Pair(Feature.FRONT, "+"), new Pair(Feature.BACK, "-")),
							new Secondary[] { Secondary.RETRACTED },
							List.of(new Matrix(new Pair(Feature.CORONAL, "+"))))),
			entry(Secondary.DENTAL,
					new SecondaryArticulation("̪", new Matrix(new Pair(Feature.ANTERIOR, "+"),
							new Pair(Feature.DISTRIBUTED, "+")), new Secondary[] { Secondary.PALATOALVEOLAR },
							List.of(new Matrix(new Pair(Feature.CORONAL, "+"))))),
			entry(Secondary.PALATOALVEOLAR,
					new SecondaryArticulation("̺",
							new Matrix(new Pair(Feature.ANTERIOR, "-"),
									new Pair(Feature.DISTRIBUTED, "+")),
							new Secondary[] { Secondary.DENTAL },
							List.of(new Matrix(new Pair(Feature.CORONAL, "+"))))),
			entry(Secondary.DEVOICING,
					new SecondaryArticulation("̥", Feature.VOICE, "-", new Secondary[] {},
							List.of(new Matrix(new Pair(Feature.VOICE, "+"))))),
			entry(Secondary.NASALIZATION,
					new SecondaryArticulation("̃", Feature.NASAL, "+", new Secondary[] {},
							List.of(new Matrix(new Pair(Feature.SONORANT, "+"))))),
			entry(Secondary.LABIALIZATION,
					new SecondaryArticulation("ʷ",
							new Matrix(new Pair(Feature.LABIAL, "+"), new Pair(Feature.ROUND, "+")),
							new Secondary[] {},
							List.of(new Matrix(new Pair(Feature.CONSONANTAL, "+"),
									new Pair(Feature.SYLLABIC, "-")),
									new Matrix(new Pair(Feature.CONSONANTAL, "-"),
											new Pair(Feature.SYLLABIC, "-"))))),
			entry(Secondary.PALATALIZATION,
					new SecondaryArticulation("ʲ",
							new Matrix(new Pair(Feature.DORSAL, "+"), new Pair(Feature.HIGH, "+"),
									new Pair(Feature.LOW, "-"), new Pair(Feature.FRONT, "+"),
									new Pair(Feature.BACK, "-")),
							new Secondary[] { Secondary.VELARIZATION, Secondary.PHARYNGEALIZATION },
							List.of(new Matrix(new Pair(Feature.CONSONANTAL, "+"),
									new Pair(Feature.SYLLABIC, "-")),
									new Matrix(new Pair(Feature.CONSONANTAL, "-"),
											new Pair(Feature.SYLLABIC, "-"))))),
			entry(Secondary.VELARIZATION,
					new SecondaryArticulation("ˠ",
							new Matrix(new Pair(Feature.DORSAL, "+"), new Pair(Feature.HIGH, "+"),
									new Pair(Feature.LOW, "-"), new Pair(Feature.FRONT, "-"),
									new Pair(Feature.BACK, "+")),
							new Secondary[] { Secondary.PALATALIZATION, Secondary.PHARYNGEALIZATION },
							List.of(new Matrix(new Pair(Feature.CONSONANTAL, "+"),
									new Pair(Feature.SYLLABIC, "-")),
									new Matrix(new Pair(Feature.CONSONANTAL, "-"),
											new Pair(Feature.SYLLABIC, "-"))))),
			entry(Secondary.PHARYNGEALIZATION,
					new SecondaryArticulation("ˤ",
							new Matrix(new Pair(Feature.DORSAL, "+"), new Pair(Feature.HIGH, "-"),
									new Pair(Feature.LOW, "+"), new Pair(Feature.FRONT, "-"),
									new Pair(Feature.BACK, "+")),
							new Secondary[] { Secondary.VELARIZATION, Secondary.PALATALIZATION },
							List.of(new Matrix(new Pair(Feature.CONSONANTAL, "+"),
									new Pair(Feature.SYLLABIC, "-")),
									new Matrix(new Pair(Feature.CONSONANTAL, "-"),
											new Pair(Feature.SYLLABIC, "-"))))),
			entry(Secondary.ASPIRATION,
					new SecondaryArticulation("ʰ",
							new Matrix(new Pair(Feature.SPREAD_GLOTTIS,
									"+"), new Pair(Feature.CONSTRICTED_GLOTTIS, "-")),
							new Secondary[] {},
							List.of(new Matrix(new Pair(Feature.CONSONANTAL, "+"),
									new Pair(Feature.SYLLABIC, "-")),
									new Matrix(new Pair(Feature.CONSONANTAL, "-"),
											new Pair(Feature.SYLLABIC, "-"))))),
			entry(Secondary.LENGTH,
					new SecondaryArticulation("ː", Feature.LONG, "+", new Secondary[] {}, List.of())));

	private String segment;
	private Matrix features;

	public Phone(PhoneManager pm, String segment, Matrix features) {
		this.pm = pm;
		this.segment = segment;
		this.features = features;

		pm.add(this);
	}

	static boolean isSecondary(char c) {
		for (Map.Entry<Secondary, SecondaryArticulation> e : secondaryLibrary.entrySet()) {
			if (e.getValue().getSegment().charAt(0) == c)
				return true;
		}
		return false;
	}

	public boolean hasFeatures(Matrix map) {
		for (Pair e : map) {
			if (!getFeatureQuality(e.getFeature()).equals(e.getQuality()) && !e.getQuality().equals("~")) {
				return false;
			}
		}
		return true;
	}

	public String getFeatureQuality(Feature feature) {
		return features.get(feature);
	}

	public Matrix getMatrix() {
		Matrix map = new Matrix();
		for (Pair p : features)
			if (!p.getQuality().equals("0"))
				map.put(p);
		return map;
	}

	private String[] getQualityArray() {
		String[] values = new String[Feature.values().length];

		int i = 0;
		for (Feature v : Feature.values())
			values[i++] = getFeatureQuality(v);

		return values;
	}

	public String getDataString(String split) {
		StringBuilder s = new StringBuilder(segment);
		String[] values = getQualityArray();
		for (int i = 0; i < values.length; i++) {
			s.append(split);
			s.append(values[i]);
		}
		return s.toString();
	}

	public boolean canApply(Secondary secondary, List<Secondary> applied) {
		return secondaryLibrary.get(secondary).canApply(this, applied);
	}

	public static Feature inMajorClass(Feature feature) {
		for (Map.Entry<Feature, List<Feature>> e : majorClasses.entrySet()) {
			if (e.getValue().contains(feature))
				return e.getKey();
		}
		return null;
	}

	public Phone transform(Matrix matrix, boolean search) {
		return transform("*", matrix, search);
	}

	private Phone transform(String segment, Matrix matrix, boolean search) {
		Matrix new_features = getMatrix();

		for (Pair e : matrix) {
			new_features.put(e.getFeature(), e.getQuality());
			Feature im = inMajorClass(e.getFeature());
			if (im != null && !e.getQuality().equals("0")) {
				new_features.put(im, "+");
				for (Feature f : majorClasses.get(im)) {
					if (new_features.get(f).equals("0"))
						new_features.put(f, "-");
				}
			} else if (majorClasses.containsKey(e.getFeature()) && e.getQuality().equals("+")) {
				for (Feature f : majorClasses.get(e.getFeature())) {
					if (new_features.get(f).equals("0"))
						new_features.put(f, "-");
				}
			} else if (majorClasses.containsKey(e.getFeature()) && e.getQuality().equals("-")) {
				for (Feature f : majorClasses.get(e.getFeature())) {
					new_features.put(f, "0");
				}
			}
		}

		Phone p = new Phone(pm, segment, new_features);
		if (!pm.contains(p)) {
			if (search) {
				Phone fuzzy = pm.fuzzySearch(p);
				if (fuzzy != null)
					return fuzzy;
				else
					return p;
			} else {
				return p;
			}
		}
		return pm.validate(p);
	}

	public Phone apply(Secondary secondary) {
		SecondaryArticulation sa = secondaryLibrary.get(secondary);
		return transform(getSegment() + sa.getSegment(), sa.getMatrix(), false);
	}

	@Override
	public String toString() {
		return segment;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		Phone p = (Phone) o;

		for (Pair pair : p.getMatrix())
			if (!pair.getQuality().equals(this.getFeatureQuality(pair.getFeature())))
				return false;

		return true;
	}

	public String getSegment() {
		return segment;
	}

	@Override
	public int compareTo(Phone o) {
		/*if (o.getFeatureQuality(Feature.SYLLABIC) != getFeatureQuality(Feature.SYLLABIC))
			return (getFeatureQuality(Feature.SYLLABIC) == "+" ? -1 : 1);
		if (o.getFeatureQuality(Feature.CONSONANTAL) != getFeatureQuality(Feature.CONSONANTAL))
			return (getFeatureQuality(Feature.CONSONANTAL) == "-" ? -1 : 1);
		if (o.getFeatureQuality(Feature.APPROXIMANT) != getFeatureQuality(Feature.APPROXIMANT))
			return (getFeatureQuality(Feature.APPROXIMANT) == "+" ? -1 : 1);
		if (o.getFeatureQuality(Feature.SONORANT) != getFeatureQuality(Feature.SONORANT))
			return (getFeatureQuality(Feature.SONORANT) == "+" ? -1 : 1);
		if (o.getFeatureQuality(Feature.CONTINUANT) != getFeatureQuality(Feature.CONTINUANT))
			return (getFeatureQuality(Feature.CONTINUANT) == "+" ? -1 : 1);
		if (o.getFeatureQuality(Feature.DELAYED_RELEASE) != getFeatureQuality(Feature.DELAYED_RELEASE))
			return (getFeatureQuality(Feature.DELAYED_RELEASE) == "+" ? -1 : 1);*/
		return segment.compareTo(o.segment);
	}
}