package holmes.analyse;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Transition.TransitionType;

/**
 * Klasa odpowiedzialna za działania związane z czasem w sieci Petriego.
 */
public class TimeComputations {
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
			GUIManager.getDefaultGUIManager().log("Error: t-invariant and transition set sizes do not match!", "error", true);
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
			boolean tpnStatus = trans.getTPNstatus();
			boolean dpnStatus = trans.getDPNstatus();
			
			if(tpnStatus && dpnStatus) {
				tdpnTrans++;
				
				eftTotalTime += trans.getEFT();
				lftTotalTime += trans.getLFT();
				dpnTotalTime += trans.getDPNduration();
			} else if(tpnStatus) {
				tpnPureTrans++;
				
				eftTotalTime += trans.getEFT();
				lftTotalTime += trans.getLFT();
			} else if(dpnStatus) {
				dpnPureTrans++;
				
				dpnTotalTime += trans.getDPNduration();
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
