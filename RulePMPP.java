public class RulePMPP extends Rule {
	public RulePMPP(Phone search_phone, Matrix trans_matrix,
			Phone init_phone, Phone fin_phone, boolean assimilate) {
		this.search_phone = search_phone;
		this.search_matrix = null;
		this.searchType = SegmentType.PHONE;
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