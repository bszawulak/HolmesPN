package holmes.petrinet.data;

import holmes.petrinet.elements.extensions.TransitionSPNExtension;

/**
 * Klasa kontener, przechowuje dane stochastyczne tranzycji
 */
public class SPNtransitionData {
	public String ST_function = "";
	public int IM_priority = 0;
	public int DET_delay = 0;
	public String SCH_start = "";
	public int SCH_rep = 0;
	public String SCH_end = "";
	public TransitionSPNExtension.StochaticsType sType = TransitionSPNExtension.StochaticsType.ST;
	
	//TMP:
	public int tmp_DET_counter = 0;
	
	public SPNtransitionData() {
		
	}
	
	public SPNtransitionData(String value, TransitionSPNExtension.StochaticsType sType) {
		this.ST_function = value;
		this.sType = sType;
	}
	
	public String returnSaveVector() {
		String data = "";
		data += (ST_function+";");
		data += (IM_priority+";");
		data += (DET_delay+";");
		data += (SCH_start+";");
		data += (SCH_rep+";");
		data += (SCH_end+";");
		data += (sType);
		return data;
	}
}
