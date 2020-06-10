import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

class secondaryArticulation {
	private String segment;
	private Matrix matrix;
	private List<Matrix> requirements;
	private Phone.Secondary restrictions[];

	public secondaryArticulation(String segment, Matrix matrix, Phone.Secondary restrictions[],
			List<Matrix> requirements) {
		this.matrix = matrix;
		this.restrictions = restrictions;
		this.segment = segment;
		this.requirements = requirements;
	}

	public secondaryArticulation(String segment, Phone.Feature feature, Phone.Quality value,
			Phone.Secondary restrictions[], List<Matrix> requirements) {
		this(segment, new Matrix(entry(feature, value)), restrictions, requirements);
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
			for (Map.Entry<Phone.Feature, Phone.Quality> feature : r.entrySet()) {
				if (phone.getFeatureQuality(feature.getKey()) != feature.getValue()) {
					flag = false;
					break;
				}
			}
			if (flag == true)
				return true;
		}

		return false;
	}

	public String getSegment() {
		return segment;
	}
}

public class Phone implements Comparable<Phone> {
	private PhoneLibrary pm;

	public static enum Quality {
		TRUE {
			public String toString() {
				return "+";
			}
		},
		FALSE {
			public String toString() {
				return "-";
			}
		},
		NULL {
			public String toString() {
				return "0";
			}
		},
		// RULE ONLY
		ALPHA {
			public String toString() {
				return "α-";
			}
		},
		BETA {
			public String toString() {
				return "β-";
			}
		},
		GAMMA {
			public String toString() {
				return "γ-";
			}
		}
	}

	public static enum Feature {
		STRESS {
			public String toString() {
				return "stress";
			}
		},
		LONG {
			public String toString() {
				return "long";
			}
		},
		SYLLABIC {
			public String toString() {
				return "syl";
			}
		},
		CONSONANTAL {
			public String toString() {
				return "cons";
			}
		},
		APPROXIMANT {
			public String toString() {
				return "approx";
			}
		},
		SONORANT {
			public String toString() {
				return "son";
			}
		},
		CONTINUANT {
			public String toString() {
				return "con";
			}
		},
		DELAYED_RELEASE {
			public String toString() {
				return "del";
			}
		},
		NASAL {
			public String toString() {
				return "nasal";
			}
		},
		STRIDENT {
			public String toString() {
				return "str";
			}
		},
		VOICE {
			public String toString() {
				return "voice";
			}
		},
		SPREAD_GLOTTIS {
			public String toString() {
				return "s.g.";
			}
		},
		CONSTRICTED_GLOTTIS {
			public String toString() {
				return "c.g.";
			}
		},
		LABIAL {
			public String toString() {
				return "LAB";
			}
		},
		ROUND {
			public String toString() {
				return "round";
			}
		},
		LABIODENTAL {
			public String toString() {
				return "l.d.";
			}
		},
		CORONAL {
			public String toString() {
				return "COR";
			}
		},
		ANTERIOR {
			public String toString() {
				return "ant";
			}
		},
		DISTRIBUTED {
			public String toString() {
				return "dist";
			}
		},
		LATERAL {
			public String toString() {
				return "LAT";
			}
		},
		DORSAL {
			public String toString() {
				return "DOR";
			}
		},
		HIGH {
			public String toString() {
				return "high";
			}
		},
		LOW {
			public String toString() {
				return "low";
			}
		},
		FRONT {
			public String toString() {
				return "front";
			}
		},
		BACK {
			public String toString() {
				return "back";
			}
		},
		TENSE {
			public String toString() {
				return "tense";
			}
		}
	}

	public static enum Secondary {
		VOCALIC, RETRACTED, ADVANCED, PALATOALVEOLAR, DENTAL, DEVOICING, NASALIZATION, LABIALIZATION, PALATALIZATION,
		VELARIZATION, PHARYNGEALIZATION, ASPIRATION, LENGTH
	}

	static final Map<Feature, List<Feature>> majorClasses = Map.ofEntries(
			entry(Feature.LABIAL, List.of(Feature.ROUND, Feature.LABIODENTAL)),
			entry(Feature.CORONAL, List.of(Feature.ANTERIOR, Feature.DISTRIBUTED)), entry(Feature.DORSAL,
					List.of(Feature.HIGH, Feature.LOW, Feature.FRONT, Feature.BACK, Feature.TENSE)));

	static final Map<Secondary, secondaryArticulation> secondaryLibrary = Map.ofEntries(
			entry(Secondary.VOCALIC,
					new secondaryArticulation("̩", Feature.SYLLABIC, Quality.TRUE, new Secondary[] {},
							List.of(new Matrix(entry(Feature.CONSONANTAL, Quality.TRUE),
									entry(Feature.SONORANT, Quality.TRUE))))),
			entry(Secondary.RETRACTED,
					new secondaryArticulation("̠",
							new Matrix(entry(Feature.FRONT, Quality.FALSE), entry(Feature.BACK, Quality.TRUE)),
							new Secondary[] { Secondary.ADVANCED },
							List.of(new Matrix(entry(Feature.CORONAL, Quality.TRUE))))),
			entry(Secondary.ADVANCED,
					new secondaryArticulation("̟",
							new Matrix(entry(Feature.FRONT, Quality.TRUE), entry(Feature.BACK, Quality.FALSE)),
							new Secondary[] { Secondary.RETRACTED },
							List.of(new Matrix(entry(Feature.CORONAL, Quality.TRUE))))),
			entry(Secondary.DENTAL,
					new secondaryArticulation("̪",
							new Matrix(entry(Feature.ANTERIOR, Quality.TRUE), entry(Feature.DISTRIBUTED, Quality.TRUE)),
							new Secondary[] { Secondary.PALATOALVEOLAR },
							List.of(new Matrix(entry(Feature.CORONAL, Quality.TRUE))))),
			entry(Secondary.PALATOALVEOLAR,
					new secondaryArticulation("̺",
							new Matrix(entry(Feature.ANTERIOR, Quality.FALSE),
									entry(Feature.DISTRIBUTED, Quality.TRUE)),
							new Secondary[] { Secondary.DENTAL },
							List.of(new Matrix(entry(Feature.CORONAL, Quality.TRUE))))),
			entry(Secondary.DEVOICING,
					new secondaryArticulation("̥", Feature.VOICE, Quality.FALSE, new Secondary[] {},
							List.of(new Matrix(entry(Feature.VOICE, Quality.TRUE))))),
			entry(Secondary.NASALIZATION,
					new secondaryArticulation("̃", Feature.NASAL, Quality.TRUE, new Secondary[] {},
							List.of(new Matrix(entry(Feature.SONORANT, Quality.TRUE))))),
			entry(Secondary.LABIALIZATION,
					new secondaryArticulation("ʷ",
							new Matrix(entry(Feature.LABIAL, Quality.TRUE), entry(Feature.ROUND, Quality.TRUE)),
							new Secondary[] {},
							List.of(
									new Matrix(entry(Feature.CONSONANTAL, Quality.TRUE),
											entry(Feature.SYLLABIC, Quality.FALSE)),
									new Matrix(entry(Feature.CONSONANTAL, Quality.FALSE),
											entry(Feature.SYLLABIC, Quality.FALSE))))),
			entry(Secondary.PALATALIZATION,
					new secondaryArticulation("ʲ",
							new Matrix(entry(Feature.DORSAL, Quality.TRUE), entry(Feature.HIGH, Quality.TRUE),
									entry(Feature.LOW, Quality.FALSE), entry(Feature.FRONT, Quality.TRUE),
									entry(Feature.BACK, Quality.FALSE)),
							new Secondary[] { Secondary.VELARIZATION, Secondary.PHARYNGEALIZATION },
							List.of(
									new Matrix(entry(Feature.CONSONANTAL, Quality.TRUE),
											entry(Feature.SYLLABIC, Quality.FALSE)),
									new Matrix(entry(Feature.CONSONANTAL, Quality.FALSE),
											entry(Feature.SYLLABIC, Quality.FALSE))))),
			entry(Secondary.VELARIZATION,
					new secondaryArticulation("ˠ",
							new Matrix(entry(Feature.DORSAL, Quality.TRUE), entry(Feature.HIGH, Quality.TRUE),
									entry(Feature.LOW, Quality.FALSE), entry(Feature.FRONT, Quality.FALSE),
									entry(Feature.BACK, Quality.TRUE)),
							new Secondary[] { Secondary.PALATALIZATION, Secondary.PHARYNGEALIZATION },
							List.of(
									new Matrix(entry(Feature.CONSONANTAL, Quality.TRUE),
											entry(Feature.SYLLABIC, Quality.FALSE)),
									new Matrix(entry(Feature.CONSONANTAL, Quality.FALSE),
											entry(Feature.SYLLABIC, Quality.FALSE))))),
			entry(Secondary.PHARYNGEALIZATION,
					new secondaryArticulation("ˤ",
							new Matrix(entry(Feature.DORSAL, Quality.TRUE), entry(Feature.HIGH, Quality.FALSE),
									entry(Feature.LOW, Quality.TRUE), entry(Feature.FRONT, Quality.FALSE),
									entry(Feature.BACK, Quality.TRUE)),
							new Secondary[] { Secondary.VELARIZATION, Secondary.PALATALIZATION },
							List.of(
									new Matrix(entry(Feature.CONSONANTAL, Quality.TRUE),
											entry(Feature.SYLLABIC, Quality.FALSE)),
									new Matrix(entry(Feature.CONSONANTAL, Quality.FALSE),
											entry(Feature.SYLLABIC, Quality.FALSE))))),
			entry(Secondary.ASPIRATION,
					new secondaryArticulation("ʰ",
							new Matrix(entry(Feature.SPREAD_GLOTTIS, Quality.TRUE),
									entry(Feature.CONSTRICTED_GLOTTIS, Quality.FALSE)),
							new Secondary[] {},
							List.of(
									new Matrix(entry(Feature.CONSONANTAL, Quality.TRUE),
											entry(Feature.SYLLABIC, Quality.FALSE)),
									new Matrix(entry(Feature.CONSONANTAL, Quality.FALSE),
											entry(Feature.SYLLABIC, Quality.FALSE))))),
			entry(Secondary.LENGTH,
					new secondaryArticulation("ː", Feature.LONG, Quality.TRUE, new Secondary[] {}, List.of())));

	private String segment;
	private Matrix features;

	public Phone(PhoneLibrary pm, String segment, Matrix features) {
		this.pm = pm;
		this.segment = segment;
		this.features = features;

		pm.add(this);
	}

	static boolean isSecondary(char c) {
		for (Map.Entry<Secondary, secondaryArticulation> e : secondaryLibrary.entrySet()) {
			if (e.getValue().getSegment().charAt(0) == c)
				return true;
		}
		return false;
	}
	
	public boolean hasFeatures(Matrix map) {
		for (Map.Entry<Feature, Quality> e : map.entrySet()) {
			if (getFeatureQuality(e.getKey()) != e.getValue()) {
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
		map.putAll(features);

		return map;
	}

	private Quality[] getQualityArray() {
		Quality[] values = new Quality[features.size()];

		int i = 0;
		for (Feature v : Feature.values())
			values[i++] = getFeatureQuality(v);

		return values;
	}

	public String getDataString(String split) {
		String s = segment;
		Quality values[] = getQualityArray();
		for (int i = 0; i < values.length; i++) {
			s += split;
			switch (values[i]) {
				case FALSE:
					s += "-";
					break;
				case NULL:
					s += "0";
					break;
				case TRUE:
					s += "+";
					break;
				default:
					break;
			}
		}
		return s;
	}

	public boolean canApply(Secondary secondary, List<Secondary> applied) {
		return secondaryLibrary.get(secondary).canApply(this, applied);
	}

	private Feature inMajorClass(Feature feature) {
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

		for (Map.Entry<Feature, Quality> e : matrix.entrySet()) {
			new_features.put(e.getKey(), e.getValue());
			Feature im = inMajorClass(e.getKey());
			if (im != null && e.getValue() != Quality.NULL) {
				new_features.put(im, Quality.TRUE);
				for (Feature f : majorClasses.get(im)) {
					if (new_features.get(f) == Quality.NULL)
						new_features.put(f, Quality.FALSE);
				}
			} else if (majorClasses.containsKey(e.getKey()) && e.getValue() == Quality.TRUE) {
				for (Feature f : majorClasses.get(e.getKey())) {
					if (new_features.get(f) == Quality.NULL)
						new_features.put(f, Quality.FALSE);
				}
			} else if (majorClasses.containsKey(e.getKey()) && e.getValue() == Quality.FALSE) {
				for (Feature f : majorClasses.get(e.getKey())) {
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
			} else
				return p;
		}
		return pm.validate(p);
	}

	public Phone apply(Secondary secondary) {
		secondaryArticulation sa = secondaryLibrary.get(secondary);
		return transform(getSegment() + sa.getSegment(), sa.getMatrix(), false);
	}

	@Override
	public String toString() {
		return segment;
	}

	@Override
	public boolean equals(Object o) {
		if (o.getClass() != this.getClass())
			return false;
		Phone p = (Phone) o;

		for (Map.Entry<Feature, Quality> e : p.features.entrySet()) {
			if (e.getValue() != getFeatureQuality(e.getKey())) {
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