package phl;

public class Rule {
	private enum SegmentType {
		PHONE, MATRIX
	}

	public enum Type {
		SIMPLE, A_FORWARD, A_BACKWARD
	}

	private SegmentType initType;
	private SegmentType searchType;
	private SegmentType finType;
	private SegmentType transType;
	private Matrix initMatrix;
	private Phone initPhone;
	private Matrix searchMatrix;
	private Phone searchPhone;
	private Matrix finMatrix;
	private Phone finPhone;
	private Matrix transMatrix;
	private Phone transPhone;

	private Type type;

	public static class DeleteForward extends Rule {
		public DeleteForward(Object search, Object trans, Object init, Object fin) {
			super(search, trans, init, fin, Type.A_FORWARD);
		}
	}

	public static class Simple extends Rule {
		public Simple(Object search, Object trans, Object init, Object fin) {
			super(search, trans, init, fin, Type.SIMPLE);
		}
	}

	private Rule(Object search, Object trans, Object init, Object fin, Type type) {
		if (search == null || search.getClass() == Matrix.class) {
			this.searchType = SegmentType.MATRIX;
			this.searchMatrix = (Matrix) search;
			this.searchPhone = null;
		} else {
			this.searchType = SegmentType.PHONE;
			this.searchMatrix = null;
			this.searchPhone = (Phone) search;
		}

		if (trans == null || trans.getClass() == Matrix.class) {
			this.transType = SegmentType.MATRIX;
			this.transMatrix = (Matrix) trans;
			this.transPhone = null;
		} else {
			this.transType = SegmentType.PHONE;
			this.transMatrix = null;
			this.transPhone = (Phone) trans;
		}

		if (init == null || init.getClass() == Matrix.class) {
			this.initType = SegmentType.MATRIX;
			this.initMatrix = (Matrix) init;
			this.initPhone = null;
		} else {
			this.initType = SegmentType.PHONE;
			this.initMatrix = null;
			this.initPhone = (Phone) search;
		}

		if (fin == null || fin.getClass() == Matrix.class) {
			this.finType = SegmentType.MATRIX;
			this.finMatrix = (Matrix) fin;
			this.finPhone = null;
		} else {
			this.finType = SegmentType.PHONE;
			this.finMatrix = null;
			this.finPhone = (Phone) fin;
		}

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
			Phone p = sequence.get(i);
			Phone last = null;
			if (i > 0)
				last = sequence.get(i - 1);
			Phone next = null;
			if (i < sequence.size() - 1)
				next = sequence.get(i + 1);

			Matrix tempSearchMatrix = null;
			Matrix tempInitMatrix = null;
			Matrix tempFinMatrix = null;

			if (searchMatrix != null) {
				tempSearchMatrix = new Matrix();
				tempSearchMatrix.putAll(searchMatrix);
				for (Pair se : tempSearchMatrix)
					if (se.getQuality() == Phone.Quality.ALPHA)
						alphaMap.put(se.getFeature(), p.getFeatureQuality(se.getFeature()));
			}
			if (initMatrix != null && last != null) {
				tempInitMatrix = new Matrix();
				tempInitMatrix.putAll(initMatrix);
				for (Pair ie : tempInitMatrix)
					if (ie.getQuality() == Phone.Quality.BETA)
						betaMap.put(ie.getFeature(), last.getFeatureQuality(ie.getFeature()));
			}
			if (finMatrix != null && next != null) {
				tempFinMatrix = new Matrix();
				tempFinMatrix.putAll(finMatrix);
				for (Pair fe : tempFinMatrix)
					if (fe.getQuality() == Phone.Quality.GAMMA)
						gammaMap.put(fe.getFeature(), next.getFeatureQuality(fe.getFeature()));
			}

			if (!alphaMap.isEmpty()) {
				if (tempSearchMatrix != null)
					for (Pair e : alphaMap)
						if (tempSearchMatrix.get(e.getFeature()) == Phone.Quality.ALPHA)
							tempSearchMatrix.put(e.getFeature(), e.getQuality());
				if (tempInitMatrix != null)
					for (Pair e : alphaMap)
						if (tempInitMatrix.get(e.getFeature()) == Phone.Quality.ALPHA)
							tempInitMatrix.put(e.getFeature(), e.getQuality());
				if (tempFinMatrix != null)
					for (Pair e : alphaMap)
						if (tempFinMatrix.get(e.getFeature()) == Phone.Quality.ALPHA)
							tempFinMatrix.put(e.getFeature(), e.getQuality());
			}

			if (!betaMap.isEmpty()) {
				if (tempSearchMatrix != null)
					for (Pair e : betaMap)
						if (tempSearchMatrix.get(e.getFeature()) == Phone.Quality.BETA)
							tempSearchMatrix.put(e.getFeature(), e.getQuality());
				if (tempInitMatrix != null)
					for (Pair e : betaMap)
						if (tempInitMatrix.get(e.getFeature()) == Phone.Quality.BETA)
							tempInitMatrix.put(e.getFeature(), e.getQuality());
				if (tempFinMatrix != null)
					for (Pair e : betaMap)
						if (tempFinMatrix.get(e.getFeature()) == Phone.Quality.BETA)
							tempFinMatrix.put(e.getFeature(), e.getQuality());
			}

			if (!gammaMap.isEmpty()) {
				if (tempSearchMatrix != null)
					for (Pair e : gammaMap)
						if (tempSearchMatrix.get(e.getFeature()) == Phone.Quality.GAMMA)
							tempSearchMatrix.put(e.getFeature(), e.getQuality());
				if (tempInitMatrix != null)
					for (Pair e : gammaMap)
						if (tempInitMatrix.get(e.getFeature()) == Phone.Quality.GAMMA)
							tempInitMatrix.put(e.getFeature(), e.getQuality());
				if (tempFinMatrix != null)
					for (Pair e : gammaMap)
						if (tempFinMatrix.get(e.getFeature()) == Phone.Quality.GAMMA)
							tempFinMatrix.put(e.getFeature(), e.getQuality());
			}

			boolean flag = false;
			switch (searchType) {
				case MATRIX:
					if (p.hasFeatures(tempSearchMatrix)) {
						flag = true;
						switch (initType) {
							case MATRIX:
								if (initMatrix != null && (last == null || !last.hasFeatures(tempInitMatrix)))
									flag = false;
								break;
							case PHONE:
								if (initPhone != null && (last == null || !last.equals(initPhone)))
									flag = false;
								break;
						}
						switch (finType) {
							case MATRIX:
								if (finMatrix != null && (next == null || !next.hasFeatures(tempFinMatrix)))
									flag = false;
								break;
							case PHONE:
								if (finPhone != null && (next == null || !next.equals(finPhone)))
									flag = false;
								break;
						}
					}
					break;
				case PHONE:
					if (p.equals(searchPhone)) {
						flag = true;
						switch (initType) {
							case MATRIX:
								if (initMatrix != null && (last == null || !last.hasFeatures(tempInitMatrix)))
									flag = false;
								break;
							case PHONE:
								if (initPhone != null && (last == null || !last.equals(initPhone)))
									flag = false;
								break;
						}
						switch (finType) {
							case MATRIX:
								if (finMatrix != null && (next == null || !next.hasFeatures(tempFinMatrix)))
									flag = false;
								break;
							case PHONE:
								if (finPhone != null && (next == null || !next.equals(finPhone)))
									flag = false;
								break;
						}
					}
					break;

			}
			if (flag) {
				switch (transType) {
					case MATRIX:
						Matrix tempTransMatrix = new Matrix();
						for (Pair te : transMatrix) {
							switch (te.getQuality()) {
								case ALPHA:
									tempTransMatrix.put(te.getFeature(), alphaMap.get(te.getFeature()));
									break;
								case BETA:
									tempTransMatrix.put(te.getFeature(), betaMap.get(te.getFeature()));
									break;
								case GAMMA:
									tempTransMatrix.put(te.getFeature(), gammaMap.get(te.getFeature()));
									break;
								default:
									tempTransMatrix.put(te.getFeature(), te.getQuality());
									break;
							}
						}
						result.add(p.transform(tempTransMatrix, true));
						break;
					case PHONE:
						result.add(transPhone);
						break;
				}
				if (type != Type.SIMPLE)
					assimilateFlag = true;
			} else {
				result.add(p);
			}
		}
		return result;
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
		switch (searchType) {
			case MATRIX:
				s.append(searchMatrix);
				break;
			case PHONE:
				s.append(searchPhone);
				break;
		}
		s.append(" -> ");
		switch (transType) {
			case MATRIX:
				s.append(transMatrix);
				break;
			case PHONE:
				s.append(transPhone);
				break;
		}
		s.append(" // ");
		switch (initType) {
			case MATRIX:
				s.append(initMatrix);
				break;
			case PHONE:
				s.append(initPhone);
				break;
		}
		s.append(" ~ ");
		switch (finType) {
			case MATRIX:
				s.append(finMatrix);
				break;
			case PHONE:
				s.append(finPhone);
				break;
		}
		return s.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o.getClass() != this.getClass())
			return false;
		Rule r = (Rule) o;
		if (type != r.type)
			return false;
		if (searchMatrix == null && r.searchMatrix != null)
			return false;
		if (searchPhone == null && r.searchPhone != null)
			return false;
		if (transMatrix == null && r.transMatrix != null)
			return false;
		if (transPhone == null && r.transPhone != null)
			return false;
		if (initMatrix == null && r.initMatrix != null)
			return false;
		if (initPhone == null && r.initPhone != null)
			return false;
		if (finMatrix == null && r.finMatrix != null)
			return false;
		if (finPhone == null && r.finPhone != null)
			return false;
		switch (searchType) {
			case MATRIX:
				if (!searchMatrix.equals(r.searchMatrix))
					return false;
				break;
			case PHONE:
				if (!searchPhone.equals(r.searchPhone))
					return false;
				break;
		}
		switch (transType) {
			case MATRIX:
				if (!transMatrix.equals(r.transMatrix))
					return false;
				break;
			case PHONE:
				if (!transPhone.equals(r.transPhone))
					return false;
				break;
		}
		switch (initType) {
			case MATRIX:
				if (!initMatrix.equals(r.initMatrix))
					return false;
				break;
			case PHONE:
				if (!initPhone.equals(r.initPhone))
					return false;
				break;
		}
		switch (finType) {
			case MATRIX:
				if (!finMatrix.equals(r.finMatrix))
					return false;
				break;
			case PHONE:
				if (!finPhone.equals(r.finPhone))
					return false;
				break;
		}
		return true;
	}
}