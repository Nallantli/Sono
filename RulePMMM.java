public class RulePMMM extends Rule {
	public RulePMMM(Phone search_phone, Matrix trans_matrix,
			Matrix init_matrix, Matrix fin_matrix,
			boolean assimilate) {
		this.search_phone = search_phone;
		this.search_matrix = null;
		this.searchType = SegmentType.PHONE;
		this.trans_matrix = trans_matrix;
		this.trans_phone = null;
		this.transType = SegmentType.MATRIX;
		this.init_phone = null;
		this.init_matrix = init_matrix;
		this.initType = SegmentType.MATRIX;
		this.fin_phone = null;
		this.fin_matrix = fin_matrix;
		this.finType = SegmentType.MATRIX;
		this.assimilate = assimilate;
	}
}