import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Rule {
	protected static enum SegmentType {
		PHONE, MATRIX
	}

	protected SegmentType initType;
	protected SegmentType searchType;
	protected SegmentType finType;
	protected SegmentType transType;
	protected Matrix init_matrix;
	protected Phone init_phone;
	protected Matrix search_matrix;
	protected Phone search_phone;
	protected Matrix fin_matrix;
	protected Phone fin_phone;
	protected Matrix trans_matrix;
	protected Phone trans_phone;

	protected boolean assimilate;

	
	public Rule(Object search, Object trans, Object init, Object fin, boolean assimilate) {
		if (search == null || search.getClass() == Matrix.class) {
			this.searchType = SegmentType.MATRIX;
			this.search_matrix = (Matrix) search;
			this.search_phone = null;
		} else {
			this.searchType = SegmentType.PHONE;
			this.search_matrix = null;
			this.search_phone = (Phone) search;
		}

		if (trans == null || trans.getClass() == Matrix.class) {
			this.transType = SegmentType.MATRIX;
			this.trans_matrix = (Matrix) trans;
			this.trans_phone = null;
		} else {
			this.transType = SegmentType.PHONE;
			this.trans_matrix = null;
			this.trans_phone = (Phone) trans;
		}

		if (init == null || init.getClass() == Matrix.class) {
			this.initType = SegmentType.MATRIX;
			this.init_matrix = (Matrix) init;
			this.init_phone = null;
		} else {
			this.initType = SegmentType.PHONE;
			this.init_matrix = null;
			this.init_phone = (Phone) search;
		}

		if (fin == null || fin.getClass() == Matrix.class) {
			this.finType = SegmentType.MATRIX;
			this.fin_matrix = (Matrix) fin;
			this.fin_phone = null;
		} else {
			this.finType = SegmentType.PHONE;
			this.fin_matrix = null;
			this.fin_phone = (Phone) fin;
		}

		this.assimilate = assimilate;
	}

	public List<Phone> transform(List<Phone> sequence) {
		List<Phone> result = new ArrayList<Phone>();
		boolean a_flag = false;
		for (int i = 0; i < sequence.size(); i++) {
			if (a_flag) {
				a_flag = false;
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

			Matrix temp_search_matrix = null;
			Matrix temp_init_matrix = null;
			Matrix temp_fin_matrix = null;

			if (search_matrix != null) {
				temp_search_matrix = new Matrix();
				temp_search_matrix.putAll(search_matrix);
				for (Map.Entry<Phone.Feature, Phone.Quality> se : temp_search_matrix.entrySet())
					if (se.getValue() == Phone.Quality.ALPHA)
						alphaMap.put(se.getKey(), p.getFeatureQuality(se.getKey()));
			}
			if (init_matrix != null && last != null) {
				temp_init_matrix = new Matrix();
				temp_init_matrix.putAll(init_matrix);
				for (Map.Entry<Phone.Feature, Phone.Quality> ie : temp_init_matrix.entrySet())
					if (ie.getValue() == Phone.Quality.BETA)
						betaMap.put(ie.getKey(), last.getFeatureQuality(ie.getKey()));
			}
			if (fin_matrix != null && next != null) {
				temp_fin_matrix = new Matrix();
				temp_fin_matrix.putAll(fin_matrix);
				for (Map.Entry<Phone.Feature, Phone.Quality> fe : temp_fin_matrix.entrySet())
					if (fe.getValue() == Phone.Quality.GAMMA)
						gammaMap.put(fe.getKey(), next.getFeatureQuality(fe.getKey()));
			}

			if (!alphaMap.isEmpty()) {
				if (temp_search_matrix != null)
					for (Map.Entry<Phone.Feature, Phone.Quality> e : alphaMap.entrySet())
						if (temp_search_matrix.get(e.getKey()) == Phone.Quality.ALPHA)
							temp_search_matrix.put(e.getKey(), e.getValue());
				if (temp_init_matrix != null)
					for (Map.Entry<Phone.Feature, Phone.Quality> e : alphaMap.entrySet())
						if (temp_init_matrix.get(e.getKey()) == Phone.Quality.ALPHA)
							temp_init_matrix.put(e.getKey(), e.getValue());
				if (temp_fin_matrix != null)
					for (Map.Entry<Phone.Feature, Phone.Quality> e : alphaMap.entrySet())
						if (temp_fin_matrix.get(e.getKey()) == Phone.Quality.ALPHA)
							temp_fin_matrix.put(e.getKey(), e.getValue());
			}

			if (!betaMap.isEmpty()) {
				if (temp_search_matrix != null)
					for (Map.Entry<Phone.Feature, Phone.Quality> e : betaMap.entrySet())
						if (temp_search_matrix.get(e.getKey()) == Phone.Quality.BETA)
							temp_search_matrix.put(e.getKey(), e.getValue());
				if (temp_init_matrix != null)
					for (Map.Entry<Phone.Feature, Phone.Quality> e : betaMap.entrySet())
						if (temp_init_matrix.get(e.getKey()) == Phone.Quality.BETA)
							temp_init_matrix.put(e.getKey(), e.getValue());
				if (temp_fin_matrix != null)
					for (Map.Entry<Phone.Feature, Phone.Quality> e : betaMap.entrySet())
						if (temp_fin_matrix.get(e.getKey()) == Phone.Quality.BETA)
							temp_fin_matrix.put(e.getKey(), e.getValue());
			}

			if (!gammaMap.isEmpty()) {
				if (temp_search_matrix != null)
					for (Map.Entry<Phone.Feature, Phone.Quality> e : gammaMap.entrySet())
						if (temp_search_matrix.get(e.getKey()) == Phone.Quality.GAMMA)
							temp_search_matrix.put(e.getKey(), e.getValue());
				if (temp_init_matrix != null)
					for (Map.Entry<Phone.Feature, Phone.Quality> e : gammaMap.entrySet())
						if (temp_init_matrix.get(e.getKey()) == Phone.Quality.GAMMA)
							temp_init_matrix.put(e.getKey(), e.getValue());
				if (temp_fin_matrix != null)
					for (Map.Entry<Phone.Feature, Phone.Quality> e : gammaMap.entrySet())
						if (temp_fin_matrix.get(e.getKey()) == Phone.Quality.GAMMA)
							temp_fin_matrix.put(e.getKey(), e.getValue());
			}

			boolean flag = false;
			switch (searchType) {
				case MATRIX:
					if (p.hasFeatures(temp_search_matrix)) {
						flag = true;
						switch (initType) {
							case MATRIX:
								if (init_matrix != null) {
									if (last == null)
										flag = false;
									else if (!last.hasFeatures(temp_init_matrix))
										flag = false;
								}
								break;
							case PHONE:
								if (init_phone != null) {
									if (last == null)
										flag = false;
									else if (!last.equals(init_phone))
										flag = false;
								}
								break;
						}
						switch (finType) {
							case MATRIX:
								if (fin_matrix != null) {
									if (next == null)
										flag = false;
									else if (!next.hasFeatures(temp_fin_matrix))
										flag = false;
								}
								break;
							case PHONE:
								if (fin_phone != null) {
									if (next == null)
										flag = false;
									else if (!next.equals(fin_phone))
										flag = false;
								}
								break;
						}
					}
					break;
				case PHONE:
					if (p.equals(search_phone)) {
						flag = true;
						switch (initType) {
							case MATRIX:
								if (init_matrix != null) {
									if (last == null)
										flag = false;
									else if (!last.hasFeatures(temp_init_matrix))
										flag = false;
								}
								break;
							case PHONE:
								if (init_phone != null) {
									if (last == null)
										flag = false;
									else if (!last.equals(init_phone))
										flag = false;
								}
								break;
						}
						switch (finType) {
							case MATRIX:
								if (fin_matrix != null) {
									if (next == null)
										flag = false;
									else if (!next.hasFeatures(temp_fin_matrix))
										flag = false;
								}
								break;
							case PHONE:
								if (fin_phone != null) {
									if (next == null)
										flag = false;
									else if (!next.equals(fin_phone))
										flag = false;
								}
								break;
						}
					}
					break;

			}
			if (flag) {
				switch (transType) {
					case MATRIX:
						Matrix temp_trans_matrix = new Matrix();
						for (Map.Entry<Phone.Feature, Phone.Quality> te : trans_matrix.entrySet()) {
							switch (te.getValue()) {
								case ALPHA:
									temp_trans_matrix.put(te.getKey(), alphaMap.get(te.getKey()));
									break;
								case BETA:
									temp_trans_matrix.put(te.getKey(), betaMap.get(te.getKey()));
									break;
								case GAMMA:
									temp_trans_matrix.put(te.getKey(), gammaMap.get(te.getKey()));
									break;
								default:
									temp_trans_matrix.put(te.getKey(), te.getValue());
									break;
							}
						}
						result.add(p.transform(temp_trans_matrix, true));
						break;
					case PHONE:
						result.add(trans_phone);
						break;
				}
				if (assimilate)
					a_flag = true;
			} else
				result.add(p);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		if (assimilate)
			s.append("(A) ");
		switch (searchType) {
			case MATRIX:
				s.append(search_matrix);
				break;
			case PHONE:
				s.append(search_phone);
				break;
		}
		s.append(" -> ");
		switch (transType) {
			case MATRIX:
				s.append(trans_matrix);
				break;
			case PHONE:
				s.append(trans_phone);
				break;
		}
		s.append(" / ");
		switch (initType) {
			case MATRIX:
				s.append(init_matrix);
				break;
			case PHONE:
				s.append(init_phone);
				break;
		}
		s.append(" _ ");
		switch (finType) {
			case MATRIX:
				s.append(fin_matrix);
				break;
			case PHONE:
				s.append(fin_phone);
				break;
		}
		return s.toString();
	}
}