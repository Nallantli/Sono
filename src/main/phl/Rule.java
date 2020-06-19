package main.phl;

import java.util.ArrayList;
import java.util.List;

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

	private Object search;
	private List<Object> init;
	private List<Object> fin;
	private List<Object> trans;

	private Type type;

	public static class DeleteForward extends Rule {
		public DeleteForward(Object search, List<Object> trans, List<Object> init, List<Object> fin) {
			super(search, trans, init, fin, Type.A_FORWARD);
		}
	}

	public static class DeleteBackward extends Rule {
		public DeleteBackward(Object search, List<Object> trans, List<Object> init, List<Object> fin) {
			super(search, trans, init, fin, Type.A_BACKWARD);
		}
	}

	public static class Simple extends Rule {
		public Simple(Object search, List<Object> trans, List<Object> init, List<Object> fin) {
			super(search, trans, init, fin, Type.SIMPLE);
		}
	}

	private Rule(Object search, List<Object> trans, List<Object> init, List<Object> fin, Type type) {
		this.search = search;
		this.trans = trans;
		this.init = init;
		this.fin = fin;
		this.type = type;
	}

	public Word transform(Word sequence) {
		Word result = new Word();
		boolean assimilateFlag = false;
		for (int i = 0; i < sequence.size(); i++) {
			if (assimilateFlag) {
				assimilateFlag = false;
				continue;
			}
			Matrix alphaMap = new Matrix();
			Matrix betaMap = new Matrix();
			Matrix gammaMap = new Matrix();
			Object o = sequence.get(i);

			Object tempSearch = search;
			List<Object> tempInit = new ArrayList<>();
			List<Object> tempFin = new ArrayList<>();

			if (o.getClass() == Phone.class) {
				Phone p = (Phone) o;
				if (search != null && search.getClass() == Matrix.class) {
					tempSearch = new Matrix();
					((Matrix) tempSearch).putAll((Matrix) search);
					for (Pair se : (Matrix) tempSearch)
						if (se.getQuality() == Phone.Quality.ALPHA)
							alphaMap.put(se.getFeature(), p.getFeatureQuality(se.getFeature()));
				}
				for (int j = 0; j < init.size(); j++) {
					Object target = sequence.get((i - init.size()) + j);
					Object e = init.get(j);
					if (target != null && target.getClass() == Phone.class && e.getClass() == Matrix.class) {
						Matrix temp = new Matrix();
						temp.putAll((Matrix) e);
						tempInit.add(temp);
						for (Pair ie : temp)
							if (ie.getQuality() == Phone.Quality.BETA)
								betaMap.put(ie.getFeature(), ((Phone) target).getFeatureQuality(ie.getFeature()));
					} else {
						tempInit.add(e);
					}
				}
				for (int j = 0; j < fin.size(); j++) {
					Object target = sequence.get(i + j + (search != null ? 1 : 0));
					Object e = fin.get(j);
					if (target != null && target.getClass() == Phone.class && e.getClass() == Matrix.class) {
						Matrix temp = new Matrix();
						temp.putAll((Matrix) e);
						tempFin.add(temp);
						for (Pair fe : temp)
							if (fe.getQuality() == Phone.Quality.GAMMA)
								gammaMap.put(fe.getFeature(), ((Phone) target).getFeatureQuality(fe.getFeature()));
					} else {
						tempFin.add(e);
					}
				}

				if (!alphaMap.isEmpty()) {
					if (tempSearch != null && tempSearch.getClass() == Matrix.class)
						for (Pair e : alphaMap)
							if (((Matrix) tempSearch).get(e.getFeature()) == Phone.Quality.ALPHA)
								((Matrix) tempSearch).put(e.getFeature(), e.getQuality());
					for (Object m : tempInit)
						if (m.getClass() == Matrix.class)
							for (Pair e : alphaMap)
								if (((Matrix) m).get(e.getFeature()) == Phone.Quality.ALPHA)
									((Matrix) m).put(e.getFeature(), e.getQuality());
					for (Object m : tempFin)
						if (m.getClass() == Matrix.class)
							for (Pair e : alphaMap)
								if (((Matrix) m).get(e.getFeature()) == Phone.Quality.ALPHA)
									((Matrix) m).put(e.getFeature(), e.getQuality());
				}

				if (!betaMap.isEmpty()) {
					if (tempSearch != null && tempSearch.getClass() == Matrix.class)
						for (Pair e : betaMap)
							if (((Matrix) tempSearch).get(e.getFeature()) == Phone.Quality.BETA)
								((Matrix) tempSearch).put(e.getFeature(), e.getQuality());
					for (Object m : tempInit)
						if (m.getClass() == Matrix.class)
							for (Pair e : betaMap)
								if (((Matrix) m).get(e.getFeature()) == Phone.Quality.BETA)
									((Matrix) m).put(e.getFeature(), e.getQuality());
					for (Object m : tempFin)
						if (m.getClass() == Matrix.class)
							for (Pair e : betaMap)
								if (((Matrix) m).get(e.getFeature()) == Phone.Quality.BETA)
									((Matrix) m).put(e.getFeature(), e.getQuality());
				}

				if (!gammaMap.isEmpty()) {
					if (tempSearch != null && tempSearch.getClass() == Matrix.class)
						for (Pair e : gammaMap)
							if (((Matrix) tempSearch).get(e.getFeature()) == Phone.Quality.GAMMA)
								((Matrix) tempSearch).put(e.getFeature(), e.getQuality());
					for (Object m : tempInit)
						if (m.getClass() == Matrix.class)
							for (Pair e : gammaMap)
								if (((Matrix) m).get(e.getFeature()) == Phone.Quality.GAMMA)
									((Matrix) m).put(e.getFeature(), e.getQuality());
					for (Object m : tempFin)
						if (m.getClass() == Matrix.class)
							for (Pair e : gammaMap)
								if (((Matrix) m).get(e.getFeature()) == Phone.Quality.GAMMA)
									((Matrix) m).put(e.getFeature(), e.getQuality());
				}

				boolean flag = false;
				if (tempSearch == null || applicable(i, sequence, tempSearch)) {
					flag = true;
					for (int j = 0; j < tempInit.size(); j++) {
						if (!applicable((i - tempInit.size()) + j, sequence, tempInit.get(j))) {
							flag = false;
							break;
						}
					}
					for (int j = 0; j < tempFin.size(); j++) {
						if (!applicable(i + j + (search != null ? 1 : 0), sequence, tempFin.get(j))) {
							flag = false;
							break;
						}
					}
				}

				if (flag) {
					if (type == Type.A_BACKWARD)
						sequence.remove(sequence.size() - 1);
					for (Object e : trans) {
						Phone addition = null;
						if (e.getClass() == Matrix.class) {
							Matrix tempTrans = new Matrix();
							for (Pair te : (Matrix) e) {
								switch (te.getQuality()) {
									case ALPHA:
										tempTrans.put(te.getFeature(), alphaMap.get(te.getFeature()));
										break;
									case BETA:
										tempTrans.put(te.getFeature(), betaMap.get(te.getFeature()));
										break;
									case GAMMA:
										tempTrans.put(te.getFeature(), gammaMap.get(te.getFeature()));
										break;
									default:
										tempTrans.put(te.getFeature(), te.getQuality());
										break;
								}
							}
							addition = p.transform(tempTrans, true);
						} else if (e.getClass() == Phone.class) {
							addition = (Phone) e;
						}
						result.add(addition);
						if (search == null)
							result.add(p);
					}
					if (type == Type.A_FORWARD)
						assimilateFlag = true;
				} else {
					result.add(p);
				}
			} else {
				result.add(o);
			}
		}
		return result;
	}

	private boolean applicable(int index, Word sequence, Object o) {
		if (sequence.get(index) != null && sequence.get(index).getClass() == Phone.class) {
			if (o.getClass() == Matrix.class) {
				return ((Phone) sequence.get(index)).hasFeatures((Matrix) o);
			} else if (o.getClass() == Phone.class) {
				return ((Phone) sequence.get(index)).equals(o);
			}
		} else if (o.getClass() != Matrix.class && o.getClass() != Phone.class) {
			switch ((Variants) o) {
				case WORD_FINAL:
					return index == sequence.size();
				case WORD_INITIAL:
					return index == -1;
				case SYLLABLE:
					return index == sequence.size() || index == -1 || sequence.get(index).equals(Word.SyllableDelim.DELIM) || sequence.get(index).equals(Word.SyllableDelim.MORPHEME);
				case MORPHEME:
					return index == sequence.size() || index == -1 || sequence.get(index).equals(Word.SyllableDelim.MORPHEME);
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