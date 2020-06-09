public class RulePMMP extends Rule {
	public RulePMMP(Phone search_phone, Matrix trans_matrix, Matrix init_matrix, Phone fin_phone, boolean assimilate) {
		this.search_phone = search_phone;
		this.search_matrix = null;
		this.searchType = SegmentType.PHONE;
		this.trans_matrix = trans_matrix;
		this.trans_phone = null;
		this.transType = SegmentType.MATRIX;
		this.init_phone = null;
		this.init_matrix = init_matrix;
		this.initType = SegmentType.MATRIX;
		this.fin_phone = fin_phone;
		this.fin_matrix = null;
		this.finType = SegmentType.PHONE;
		this.assimilate = assimilate;
	}
}