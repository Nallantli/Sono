package main.phl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rule {
	public enum Type {
		SIMPLE, A_FORWARD, A_BACKWARD
	}

	public enum Variants {
		WORD_INITIAL {
			@Override
			public String toString() {
				return "^";
			}
		},
		WORD_FINAL {
			@Override
			public String toString() {
				return "#";
			}
		},
		SYLLABLE {
			@Override
			public String toString() {
				return "$";
			}
		},
		MORPHEME {
			@Override
			public String toString() {
				return "+";
			}
		}
	}

	private final Object search;
	private final List<Object> init;
	private final List<Object> fin;
	private final List<Object> trans;

	private Type type;

	public static class DeleteForward extends Rule {
		public DeleteForward(final Object search, final List<Object> trans, final List<Object> init,
				final List<Object> fin) {
			super(search, trans, init, fin, Type.A_FORWARD);
		}
	}

	public static class DeleteBackward extends Rule {
		public DeleteBackward(final Object search, final List<Object> trans, final List<Object> init,
				final List<Object> fin) {
			super(search, trans, init, fin, Type.A_BACKWARD);
		}
	}

	public static class Simple extends Rule {
		public Simple(final Object search, final List<Object> trans, final List<Object> init, final List<Object> fin) {
			super(search, trans, init, fin, Type.SIMPLE);
		}
	}

	private Rule(final Object search, final List<Object> trans, final List<Object> init, final List<Object> fin,
			final Type type) {
		this.search = search;
		this.trans = trans;
		this.init = init;
		this.fin = fin;
		this.type = type;
	}

	public Word transform(final Word sequence) {
		List<Phone> phones = new ArrayList<>();
		List<Word.SyllableDelim> delims = new ArrayList<>();
		boolean assimilateFlag = false;
		for (int i = 0; i < sequence.size(); i++) {
			if (assimilateFlag) {
				assimilateFlag = false;
				continue;
			}
			Map<Integer, Matrix> assimilationMaps = new HashMap<>();
			Phone phone = sequence.get(i);
			Word.SyllableDelim delim = sequence.getDelim(i);

			Object tempSearch = search;
			List<Object> tempInit = new ArrayList<>();
			List<Object> tempFin = new ArrayList<>();
			List<Object> tempTrans = new ArrayList<>();

			int currentIndex = 0;
			if (search != null && search.getClass() == Matrix.class) {
				tempSearch = new Matrix();
				((Matrix) tempSearch).putAll((Matrix) search);
				for (Pair se : (Matrix) tempSearch) {
					if (se.getQuality().equals(String.valueOf(currentIndex))) {
						if (assimilationMaps.get(currentIndex) == null)
							assimilationMaps.put(currentIndex, new Matrix());
						assimilationMaps.get(currentIndex).put(se.getFeature(),
								phone.getFeatureQuality(se.getFeature()));
					}
				}
			}
			currentIndex++;
			for (int j = 0; j < init.size(); j++) {
				Phone target = sequence.get((i - init.size()) + j);
				Object e = init.get(j);
				if (target != null && target.getClass() == Phone.class && e.getClass() == Matrix.class) {
					Matrix temp = new Matrix();
					temp.putAll((Matrix) e);
					tempInit.add(0, temp);
					for (Pair ie : temp) {
						if (ie.getQuality().equals(String.valueOf(currentIndex))) {
							if (assimilationMaps.get(currentIndex) == null)
								assimilationMaps.put(currentIndex, new Matrix());
							assimilationMaps.get(currentIndex).put(ie.getFeature(),
									target.getFeatureQuality(ie.getFeature()));
						}
					}
					currentIndex++;
				} else if (e.getClass() == Phone.class) {
					tempInit.add(0, e);
					currentIndex++;
				} else {
					tempInit.add(0, e);
				}
			}
			for (int j = 0; j < fin.size(); j++) {
				Phone target = sequence.get(i + j + (search != null ? 1 : 0));
				Object e = fin.get(j);
				if (target != null && target.getClass() == Phone.class && e.getClass() == Matrix.class) {
					Matrix temp = new Matrix();
					temp.putAll((Matrix) e);
					tempFin.add(temp);
					for (Pair fe : temp) {
						if (fe.getQuality().equals(String.valueOf(currentIndex))) {
							if (assimilationMaps.get(currentIndex) == null)
								assimilationMaps.put(currentIndex, new Matrix());
							assimilationMaps.get(currentIndex).put(fe.getFeature(),
									target.getFeatureQuality(fe.getFeature()));
						}
					}
					currentIndex++;
				} else if (e.getClass() == Phone.class) {
					tempFin.add(e);
					currentIndex++;
				} else {
					tempFin.add(e);
				}
			}
			for (int j = 0; j < trans.size(); j++) {
				Object e = trans.get(j);
				if (e.getClass() == Matrix.class) {
					Matrix temp = new Matrix();
					temp.putAll((Matrix) e);
					tempTrans.add(temp);
				} else {
					tempTrans.add(e);
				}
			}

			for (Map.Entry<Integer, Matrix> e : assimilationMaps.entrySet()) {
				if (tempSearch != null && tempSearch.getClass() == Matrix.class)
					for (Pair feature : e.getValue())
						if (((Matrix) tempSearch).get(feature.getFeature()).equals(String.valueOf(e.getKey())))
							((Matrix) tempSearch).put(feature.getFeature(), feature.getQuality());
				for (Object m : tempInit)
					if (m.getClass() == Matrix.class)
						for (Pair feature : e.getValue())
							if (((Matrix) m).get(feature.getFeature()).equals(String.valueOf(e.getKey())))
								((Matrix) m).put(feature.getFeature(), feature.getQuality());
				for (Object m : tempFin)
					if (m.getClass() == Matrix.class)
						for (Pair feature : e.getValue())
							if (((Matrix) m).get(feature.getFeature()).equals(String.valueOf(e.getKey())))
								((Matrix) m).put(feature.getFeature(), feature.getQuality());
				for (Object m : tempTrans)
					if (m.getClass() == Matrix.class)
						for (Pair feature : e.getValue())
							if (((Matrix) m).get(feature.getFeature()).equals(String.valueOf(e.getKey())))
								((Matrix) m).put(feature.getFeature(), feature.getQuality());
			}

			boolean flag = false;
			if (tempSearch == null || applicable(i, sequence, tempSearch)) {
				flag = true;
				int initOffset = 0;
				for (int j = 0; j < tempInit.size(); j++) {
					if (tempInit.get(j).getClass() == Phone.class || tempInit.get(j).getClass() == Matrix.class) {
						initOffset++;
					}
					if (!applicable(i - initOffset, sequence, tempInit.get(j))) {
						flag = false;
						break;
					}
				}
				int finOffset = (search == null ? -1 : 0);
				for (int j = 0; j < tempFin.size(); j++) {
					if (tempFin.get(j).getClass() == Phone.class || tempFin.get(j).getClass() == Matrix.class) {
						finOffset++;
					}
					if (!applicable(i + finOffset, sequence, tempFin.get(j))) {
						flag = false;
						break;
					}
				}
			}

			if (flag) {
				int c = 0;
				if (type == Type.A_BACKWARD) {
					phones.remove(phones.size() - 1);
					delims.remove(delims.size() - 1);
				}
				for (Object e : tempTrans) {
					Phone addition = null;
					if (e.getClass() == Matrix.class) {
						addition = phone.transform((Matrix) e, true);
					} else if (e.getClass() == Phone.class) {
						addition = (Phone) e;
					}
					phones.add(addition);
					if (c == 0)
						delims.add(delim);
					else if (c >= 1)
						delims.add(Word.SyllableDelim.NULL);
					if (search == null) {
						phones.add(phone);
						delims.add(Word.SyllableDelim.NULL);
					}
					c++;
				}
				if (type == Type.A_FORWARD)
					assimilateFlag = true;
			} else {
				phones.add(phone);
				delims.add(delim);
			}
		}
		return new Word(phones, delims);
	}

	private boolean applicable(int index, Word sequence, Object o) {
		if (sequence.get(index) != null && (o.getClass() == Phone.class || o.getClass() == Matrix.class)) {
			if (o.getClass() == Matrix.class) {
				return ((Phone) sequence.get(index)).hasFeatures((Matrix) o);
			} else if (o.getClass() == Phone.class) {
				return ((Phone) sequence.get(index)).equals(o);
			}
		} else if (o.getClass() != Matrix.class && o.getClass() != Phone.class) {
			switch ((Variants) o) {
				case WORD_FINAL:
					return index == sequence.size() - 1;
				case WORD_INITIAL:
					return index == 0;
				case SYLLABLE:
					return index == sequence.size() - 1 || index == 0
							|| sequence.getDelim(index) == Word.SyllableDelim.DELIM
							|| sequence.getDelim(index) == Word.SyllableDelim.MORPHEME;
				case MORPHEME:
					return index == sequence.size() - 1 || index == 0
							|| sequence.getDelim(index) == Word.SyllableDelim.MORPHEME;
				default:
					return false;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		switch (type) {
			case A_BACKWARD:
				s.append("Ab : ");
				break;
			case A_FORWARD:
				s.append("Af : ");
				break;
			case SIMPLE:
				s.append("S : ");
				break;
		}
		if (search != null)
			s.append(search.toString());
		else
			s.append("null");
		s.append(" -> ");
		s.append(trans.toString());
		s.append(" // ");
		s.append(init.toString());
		s.append(" ~ ");
		s.append(fin.toString());
		return s.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o.getClass() != this.getClass())
			return false;
		Rule r = (Rule) o;
		if (type != r.type)
			return false;
		if (search != null && !search.equals(r.search))
			return false;
		else if (search == null && r.search != null)
			return false;
		for (int i = 0; i < trans.size(); i++)
			if (!trans.get(i).equals(r.trans.get(i)))
				return false;
		for (int i = 0; i < init.size(); i++)
			if (!init.get(i).equals(r.init.get(i)))
				return false;
		for (int i = 0; i < fin.size(); i++)
			if (!fin.get(i).equals(r.fin.get(i)))
				return false;
		return true;
	}
}