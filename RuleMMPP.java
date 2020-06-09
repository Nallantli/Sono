public class RuleMMPP extends Rule {
	public RuleMMPP(Matrix search_matrix, Matrix trans_matrix,
			Phone init_phone, Phone fin_phone, boolean assimilate) {
		this.search_matrix = search_matrix;
		this.search_phone = null;
		this.searchType = SegmentType.MATRIX;
		this.trans_matrix = trans_matrix;
		this.trans_phone = null;
		this.transType = SegmentType.MATRIX;
		this.init_phone = init_phone;
		this.init_matrix = null;
		this.initType = SegmentType.PHONE;
		this.fin_phone = fin_phone;
		this.fin_matrix = null;
		this.finType = SegmentType.PHONE;
		this.assimilate = assimilate;
	}
}