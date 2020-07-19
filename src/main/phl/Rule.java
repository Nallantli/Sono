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
		SYLLABLE_INIT {
			@Override
			public String toString() {
				return "$";
			}
		},
		SYLLABLE_END {
			@Override
			public String toString() {
				return "&";
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

	private final Type type;

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

	public Word transform(final PhoneManager pm, final Word sequence) {
		final List<Phone> phones = new ArrayList<>();
		final List<Word.SyllableDelim> delimits = new ArrayList<>();
		boolean assimilateFlag = false;
		int dOffset = 0;
		for (int i = 0; i < sequence.size(); i++) {
			final Phone phone = sequence.get(i);
			final Word.SyllableDelim delim = sequence.getDelim(i + dOffset);
			if (assimilateFlag) {
				assimilateFlag = false;
				if (delim != Word.SyllableDelim.NULL) {
					delimits.add(delim);
					dOffset++;
				}
				continue;
			}
			final Map<Integer, Matrix> assimilationMaps = new HashMap<>();

			Object tempSearch = search;
			final List<Object> tempInit = new ArrayList<>();
			final List<Object> tempFin = new ArrayList<>();
			final List<Object> tempTrans = new ArrayList<>();

			int currentIndex = 1;
			if (search != null && search.getClass() == Matrix.class) {
				tempSearch = new Matrix();
				((Matrix) tempSearch).putAll(pm, (Matrix) search);
				for (final Pair se : (Matrix) tempSearch) {
					if (Hasher.deHash(se.getQuality()).equals(String.valueOf(currentIndex))) {
						if (assimilationMaps.get(currentIndex) == null)
							assimilationMaps.put(currentIndex, new Matrix());
						assimilationMaps.get(currentIndex).put(pm, se.getFeature(),
								phone.getFeatureQuality(se.getFeature()));
					}
				}
			}
			currentIndex++;
			for (int j = 0; j < init.size(); j++) {
				final Phone target = sequence.get((i - init.size()) + j);
				final Object e = init.get(j);
				if (target != null && target.getClass() == Phone.class && e.getClass() == Matrix.class) {
					final Matrix temp = new Matrix();
					temp.putAll(pm, (Matrix) e);
					tempInit.add(0, temp);
					for (final Pair ie : temp) {
						if (Hasher.deHash(ie.getQuality()).equals(String.valueOf(currentIndex))) {
							if (assimilationMaps.get(currentIndex) == null)
								assimilationMaps.put(currentIndex, new Matrix());
							assimilationMaps.get(currentIndex).put(pm, ie.getFeature(),
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
				final Phone target = sequence.get(i + j + (search != null ? 1 : 0));
				final Object e = fin.get(j);
				if (target != null && target.getClass() == Phone.class && e.getClass() == Matrix.class) {
					final Matrix temp = new Matrix();
					temp.putAll(pm, (Matrix) e);
					tempFin.add(temp);
					for (final Pair fe : temp) {
						if (Hasher.deHash(fe.getQuality()).equals(String.valueOf(currentIndex))) {
							if (assimilationMaps.get(currentIndex) == null)
								assimilationMaps.put(currentIndex, new Matrix());
							assimilationMaps.get(currentIndex).put(pm, fe.getFeature(),
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
				final Object e = trans.get(j);
				if (e.getClass() == Matrix.class) {
					final Matrix temp = new Matrix();
					temp.putAll(pm, (Matrix) e);
					tempTrans.add(temp);
				} else {
					tempTrans.add(e);
				}
			}

			for (final Map.Entry<Integer, Matrix> e : assimilationMaps.entrySet()) {
				if (tempSearch != null && tempSearch.getClass() == Matrix.class)
					for (final Pair feature : e.getValue())
						if (Hasher.deHash(((Matrix) tempSearch).getQuality(feature.getFeature()))
								.equals(String.valueOf(e.getKey())))
							((Matrix) tempSearch).put(pm, feature.getFeature(), feature.getQuality());
				for (final Object m : tempInit)
					if (m.getClass() == Matrix.class)
						for (final Pair feature : e.getValue())
							if (Hasher.deHash(((Matrix) m).getQuality(feature.getFeature()))
									.equals(String.valueOf(e.getKey())))
								((Matrix) m).put(pm, feature.getFeature(), feature.getQuality());
				for (final Object m : tempFin)
					if (m.getClass() == Matrix.class)
						for (final Pair feature : e.getValue())
							if (Hasher.deHash(((Matrix) m).getQuality(feature.getFeature()))
									.equals(String.valueOf(e.getKey())))
								((Matrix) m).put(pm, feature.getFeature(), feature.getQuality());
				for (final Object m : tempTrans)
					if (m.getClass() == Matrix.class)
						for (final Pair feature : e.getValue())
							if (Hasher.deHash(((Matrix) m).getQuality(feature.getFeature()))
									.equals(String.valueOf(e.getKey())))
								((Matrix) m).put(pm, feature.getFeature(), feature.getQuality());
			}

			boolean flag = false;
			if (tempSearch == null || applicable(i, sequence, tempSearch)) {
				flag = true;
				int initOffset = 0;
				for (int j = 0; j < tempInit.size(); j++) {
					if (tempInit.get(j).getClass() == Phone.class || tempInit.get(j).getClass() == Matrix.class)
						initOffset++;

					if (!applicable(i - initOffset, sequence, tempInit.get(j))) {
						flag = false;
						break;
					}
				}
				int finOffset = (search == null ? -1 : 0);
				for (int j = 0; j < tempFin.size(); j++) {
					if (tempFin.get(j).getClass() == Phone.class || tempFin.get(j).getClass() == Matrix.class)
						finOffset++;

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
					delimits.remove(delimits.size() - 1);
				}
				if (tempTrans.isEmpty() && !delimits.isEmpty() && sequence.getDelim(i + 1) != Word.SyllableDelim.NULL) {
					delimits.remove(delimits.size() - 1);
					delimits.add(delim);
				}
				for (final Object e : tempTrans) {
					Phone addition = null;
					if (e.getClass() == Matrix.class)
						addition = phone.transform((Matrix) e, true);
					else if (e.getClass() == Phone.class)
						addition = (Phone) e;

					phones.add(addition);
					if (phones.size() > delimits.size()) {
						if (c == 0)
							delimits.add(delim);
						else if (c >= 1)
							delimits.add(Word.SyllableDelim.NULL);

						if (search == null) {
							phones.add(phone);
							if (delimits.size() > 2) {
								delimits.remove(delimits.size() - 1);
								delimits.remove(delimits.size() - 1);
								delimits.add(Word.SyllableDelim.DELIM);
								delimits.add(Word.SyllableDelim.NULL);
								delimits.add(Word.SyllableDelim.DELIM);
							} else {
								delimits.add(Word.SyllableDelim.DELIM);
							}
						}
					}
					c++;
				}
				if (type == Type.A_FORWARD)
					assimilateFlag = true;
			} else {
				phones.add(phone);
				delimits.add(delim);
			}
		}
		return new Word(phones, delimits);
	}

	private boolean applicable(final int index, final Word sequence, final Object o) {
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
				case SYLLABLE_INIT:
					return index == sequence.size() || index == 0
							|| sequence.getDelim(index) == Word.SyllableDelim.DELIM
							|| sequence.getDelim(index) == Word.SyllableDelim.MORPHEME;
				case SYLLABLE_END:
					return index == sequence.size() - 1 || index == 0
							|| sequence.getDelim(index + 1) == Word.SyllableDelim.DELIM
							|| sequence.getDelim(index + 1) == Word.SyllableDelim.MORPHEME;
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
		final StringBuilder s = new StringBuilder();
		switch (type) {
			case A_BACKWARD:
				s.append("Ab |> ");
				break;
			case A_FORWARD:
				s.append("Af |> ");
				break;
			case SIMPLE:
				s.append("S |> ");
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
		s.append(" .. ");
		s.append(fin.toString());
		return s.toString();
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(final Object o) {
		if (o.getClass() != this.getClass())
			return false;
		final Rule r = (Rule) o;
		if (type != r.type)
			return false;
		if ((search != null && !search.equals(r.search)) || (search == null && r.search != null))
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