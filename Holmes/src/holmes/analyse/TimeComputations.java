package holmes.analyse;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Transition.TransitionType;

/**
 * Klasa odpowiedzialna za działania związane z czasem w sieci Petriego.
 */
public class TimeComputations {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	/**
	 * Metoda zwraca wektor danych czasowych dla t-inwariantu.
	 * @param invariant ArrayList[Integer] - t-inwariant
	 * @param transitions ArrayList[Transition] - wektor tranzycji
	 * @return ArrayList[Double]:<br>
	 * 		[0] - eftTotalTime <br>
	 * 		[1] - lftTotalTime <br>
	 * 		[2] - avgTime (= (eft+lft)/2 ) <br>
	 * 		[3] - dpnTotalTime <br>
	 * 		[4] - normalTrans <br>
	 * 		[5] - tpnPureTrans <br>
	 * 		[6] - dpnPureTrans <br>
	 * 		[7] - tdpnTrans <br>
	 */
	public static ArrayList<Double> getT_InvTimeValues(ArrayList<Integer> invariant, ArrayList<Transition> transitions) {
		double eftTotalTime = 0;
		double lftTotalTime = 0;
		double avgTime = 0;
		double dpnTotalTime = 0;
		int normalTrans = 0;
		int tpnPureTrans = 0;
		int dpnPureTrans = 0;
		int tdpnTrans = 0;
		if(invariant.size() != transitions.size()) {
			overlord.log(lang.getText("TC_entry001"), "error", true);
			return null;
		}
		
		for(int t=0; t<invariant.size(); t++) {
			int value = invariant.get(t);
			if(value <= 0)
				continue;
			
			Transition trans = transitions.get(t);
			
			if(trans.getTransType() != TransitionType.TPN) {
				normalTrans++;
				continue;
			}
			boolean tpnStatus = trans.timeExtension.isTPN();
			boolean dpnStatus = trans.timeExtension.isDPN();
			
			if(tpnStatus && dpnStatus) {
				tdpnTrans++;
				
				eftTotalTime += trans.timeExtension.getEFT();
				lftTotalTime += trans.timeExtension.getLFT();
				dpnTotalTime += trans.timeExtension.getDPNduration();
			} else if(tpnStatus) {
				tpnPureTrans++;
				
				eftTotalTime += trans.timeExtension.getEFT();
				lftTotalTime += trans.timeExtension.getLFT();
			} else if(dpnStatus) {
				dpnPureTrans++;
				
				dpnTotalTime += trans.timeExtension.getDPNduration();
			} else {
				normalTrans++; //niby TPN, ale oba parametry wyłączone
			}
		}
		avgTime += ((eftTotalTime + lftTotalTime) / 2);
		
		ArrayList<Double> result = new ArrayList<Double>();
		result.add(eftTotalTime);
		result.add(lftTotalTime);
		result.add(avgTime);
		result.add(dpnTotalTime);
		result.add((double)normalTrans);
		result.add((double)tpnPureTrans);
		result.add((double)dpnPureTrans);
		result.add((double)tdpnTrans);

		return result;
		
	}
}
