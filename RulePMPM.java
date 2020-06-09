public class RulePMPM extends Rule {
	public RulePMPM(Phone search_phone, Matrix trans_matrix, Phone init_phone, Matrix fin_matrix, boolean assimilate) {
		this.search_phone = search_phone;
		this.search_matrix = null;
		this.searchType = SegmentType.PHONE;
		this.trans_matrix = trans_matrix;
		this.trans_phone = null;
		this.transType = SegmentType.MATRIX;
		this.init_phone = init_phone;
		this.init_matrix = null;
		this.initType = SegmentType.PHONE;
		this.fin_phone = null;
		this.fin_matrix = fin_matrix;
		this.finType = SegmentType.MATRIX;
		this.assimilate = assimilate;
	}
}