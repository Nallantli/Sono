public class RuleMMMM extends Rule {
	public RuleMMMM(Matrix search_matrix, Matrix trans_matrix,
			Matrix init_matrix, Matrix fin_matrix, boolean assimilate) {
		this.search_matrix = search_matrix;
		this.search_phone = null;
		this.searchType = SegmentType.MATRIX;
		this.trans_matrix = trans_matrix;
		this.trans_phone = null;
		this.transType = SegmentType.MATRIX;
		this.init_matrix = init_matrix;
		this.init_phone = null;
		this.initType = SegmentType.MATRIX;
		this.fin_matrix = fin_matrix;
		this.fin_phone = null;
		this.finType = SegmentType.MATRIX;
		this.assimilate = assimilate;
	}
}