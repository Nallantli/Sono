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

	public SecondaryArticulation(String segment, Phone.Feature feature, Phone.Quality value,
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
				if (phone.getFeatureQuality(feature.getFeature()) != feature.getQuality()) {
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

	public enum Quality {
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
	}

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
					new SecondaryArticulation("̩", Feature.SYLLABIC, Quality.TRUE, new Secondary[] {},
							List.of(new Matrix(new Pair(Feature.CONSONANTAL, Quality.TRUE),
									new Pair(Feature.SONORANT, Quality.TRUE))))),
			entry(Secondary.RETRACTED,
					new SecondaryArticulation("̠",
							new Matrix(new Pair(Feature.FRONT, Quality.FALSE), new Pair(Feature.BACK, Quality.TRUE)),
							new Secondary[] { Secondary.ADVANCED },
							List.of(new Matrix(new Pair(Feature.CORONAL, Quality.TRUE))))),
			entry(Secondary.ADVANCED,
					new SecondaryArticulation("̟",
							new Matrix(new Pair(Feature.FRONT, Quality.TRUE), new Pair(Feature.BACK, Quality.FALSE)),
							new Secondary[] { Secondary.RETRACTED },
							List.of(new Matrix(new Pair(Feature.CORONAL, Quality.TRUE))))),
			entry(Secondary.DENTAL,
					new SecondaryArticulation("̪", new Matrix(new Pair(Feature.ANTERIOR, Quality.TRUE),
							new Pair(Feature.DISTRIBUTED, Quality.TRUE)), new Secondary[] { Secondary.PALATOALVEOLAR },
							List.of(new Matrix(new Pair(Feature.CORONAL, Quality.TRUE))))),
			entry(Secondary.PALATOALVEOLAR,
					new SecondaryArticulation("̺",
							new Matrix(new Pair(Feature.ANTERIOR, Quality.FALSE),
									new Pair(Feature.DISTRIBUTED, Quality.TRUE)),
							new Secondary[] { Secondary.DENTAL },
							List.of(new Matrix(new Pair(Feature.CORONAL, Quality.TRUE))))),
			entry(Secondary.DEVOICING,
					new SecondaryArticulation("̥", Feature.VOICE, Quality.FALSE, new Secondary[] {},
							List.of(new Matrix(new Pair(Feature.VOICE, Quality.TRUE))))),
			entry(Secondary.NASALIZATION,
					new SecondaryArticulation("̃", Feature.NASAL, Quality.TRUE, new Secondary[] {},
							List.of(new Matrix(new Pair(Feature.SONORANT, Quality.TRUE))))),
			entry(Secondary.LABIALIZATION,
					new SecondaryArticulation("ʷ",
							new Matrix(new Pair(Feature.LABIAL, Quality.TRUE), new Pair(Feature.ROUND, Quality.TRUE)),
							new Secondary[] {},
							List.of(new Matrix(new Pair(Feature.CONSONANTAL, Quality.TRUE),
									new Pair(Feature.SYLLABIC, Quality.FALSE)),
									new Matrix(new Pair(Feature.CONSONANTAL, Quality.FALSE),
											new Pair(Feature.SYLLABIC, Quality.FALSE))))),
			entry(Secondary.PALATALIZATION,
					new SecondaryArticulation("ʲ",
							new Matrix(new Pair(Feature.DORSAL, Quality.TRUE), new Pair(Feature.HIGH, Quality.TRUE),
									new Pair(Feature.LOW, Quality.FALSE), new Pair(Feature.FRONT, Quality.TRUE),
									new Pair(Feature.BACK, Quality.FALSE)),
							new Secondary[] { Secondary.VELARIZATION, Secondary.PHARYNGEALIZATION },
							List.of(new Matrix(new Pair(Feature.CONSONANTAL, Quality.TRUE),
									new Pair(Feature.SYLLABIC, Quality.FALSE)),
									new Matrix(new Pair(Feature.CONSONANTAL, Quality.FALSE),
											new Pair(Feature.SYLLABIC, Quality.FALSE))))),
			entry(Secondary.VELARIZATION,
					new SecondaryArticulation("ˠ",
							new Matrix(new Pair(Feature.DORSAL, Quality.TRUE), new Pair(Feature.HIGH, Quality.TRUE),
									new Pair(Feature.LOW, Quality.FALSE), new Pair(Feature.FRONT, Quality.FALSE),
									new Pair(Feature.BACK, Quality.TRUE)),
							new Secondary[] { Secondary.PALATALIZATION, Secondary.PHARYNGEALIZATION },
							List.of(new Matrix(new Pair(Feature.CONSONANTAL, Quality.TRUE),
									new Pair(Feature.SYLLABIC, Quality.FALSE)),
									new Matrix(new Pair(Feature.CONSONANTAL, Quality.FALSE),
											new Pair(Feature.SYLLABIC, Quality.FALSE))))),
			entry(Secondary.PHARYNGEALIZATION,
					new SecondaryArticulation("ˤ",
							new Matrix(new Pair(Feature.DORSAL, Quality.TRUE), new Pair(Feature.HIGH, Quality.FALSE),
									new Pair(Feature.LOW, Quality.TRUE), new Pair(Feature.FRONT, Quality.FALSE),
									new Pair(Feature.BACK, Quality.TRUE)),
							new Secondary[] { Secondary.VELARIZATION, Secondary.PALATALIZATION },
							List.of(new Matrix(new Pair(Feature.CONSONANTAL, Quality.TRUE),
									new Pair(Feature.SYLLABIC, Quality.FALSE)),
									new Matrix(new Pair(Feature.CONSONANTAL, Quality.FALSE),
											new Pair(Feature.SYLLABIC, Quality.FALSE))))),
			entry(Secondary.ASPIRATION,
					new SecondaryArticulation("ʰ",
							new Matrix(new Pair(Feature.SPREAD_GLOTTIS,
									Quality.TRUE), new Pair(Feature.CONSTRICTED_GLOTTIS, Quality.FALSE)),
							new Secondary[] {},
							List.of(new Matrix(new Pair(Feature.CONSONANTAL, Quality.TRUE),
									new Pair(Feature.SYLLABIC, Quality.FALSE)),
									new Matrix(new Pair(Feature.CONSONANTAL, Quality.FALSE),
											new Pair(Feature.SYLLABIC, Quality.FALSE))))),
			entry(Secondary.LENGTH,
					new SecondaryArticulation("ː", Feature.LONG, Quality.TRUE, new Secondary[] {}, List.of())));

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
			if (getFeatureQuality(e.getFeature()) != e.getQuality() && e.getQuality() != Quality.ANY) {
				return false;
			}
		}
		return true;
	}

	public Quality getFeatureQuality(Feature feature) {
		return features.get(feature);
	}

	public Matrix getMatrix() {
		Matrix map = new Matrix();
		for (Pair p : features)
			if (p.getQuality() != Quality.NULL)
				map.put(p);
		return map;
	}

	private Quality[] getQualityArray() {
		Quality[] values = new Quality[Feature.values().length];

		int i = 0;
		for (Feature v : Feature.values())
			values[i++] = getFeatureQuality(v);

		return values;
	}

	public String getDataString(String split) {
		StringBuilder s = new StringBuilder(segment);
		Quality[] values = getQualityArray();
		for (int i = 0; i < values.length; i++) {
			s.append(split);
			switch (values[i]) {
				case FALSE:
					s.append("-");
					break;
				case NULL:
					s.append("0");
					break;
				case TRUE:
					s.append("+");
					break;
				default:
					break;
			}
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
			if (im != null && e.getQuality() != Quality.NULL) {
				new_features.put(im, Quality.TRUE);
				for (Feature f : majorClasses.get(im)) {
					if (new_features.get(f) == Quality.NULL)
						new_features.put(f, Quality.FALSE);
				}
			} else if (majorClasses.containsKey(e.getFeature()) && e.getQuality() == Quality.TRUE) {
				for (Feature f : majorClasses.get(e.getFeature())) {
					if (new_features.get(f) == Quality.NULL)
						new_features.put(f, Quality.FALSE);
				}
			} else if (majorClasses.containsKey(e.getFeature()) && e.getQuality() == Quality.FALSE) {
				for (Feature f : majorClasses.get(e.getFeature())) {
					new_features.put(f, Quality.NULL);
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

		for (Pair e : p.features) {
			if (e.getQuality() != getFeatureQuality(e.getFeature())) {
				return false;
			}
		}
		return true;
	}

	public String getSegment() {
		return segment;
	}

	@Override
	public int compareTo(Phone o) {
		if (o.getFeatureQuality(Feature.SYLLABIC) != getFeatureQuality(Feature.SYLLABIC))
			return (getFeatureQuality(Feature.SYLLABIC) == Quality.TRUE ? -1 : 1);
		if (o.getFeatureQuality(Feature.CONSONANTAL) != getFeatureQuality(Feature.CONSONANTAL))
			return (getFeatureQuality(Feature.CONSONANTAL) == Quality.FALSE ? -1 : 1);
		if (o.getFeatureQuality(Feature.APPROXIMANT) != getFeatureQuality(Feature.APPROXIMANT))
			return (getFeatureQuality(Feature.APPROXIMANT) == Quality.TRUE ? -1 : 1);
		if (o.getFeatureQuality(Feature.SONORANT) != getFeatureQuality(Feature.SONORANT))
			return (getFeatureQuality(Feature.SONORANT) == Quality.TRUE ? -1 : 1);
		if (o.getFeatureQuality(Feature.CONTINUANT) != getFeatureQuality(Feature.CONTINUANT))
			return (getFeatureQuality(Feature.CONTINUANT) == Quality.TRUE ? -1 : 1);
		if (o.getFeatureQuality(Feature.DELAYED_RELEASE) != getFeatureQuality(Feature.DELAYED_RELEASE))
			return (getFeatureQuality(Feature.DELAYED_RELEASE) == Quality.TRUE ? -1 : 1);
		return segment.compareTo(o.segment);
	}
}