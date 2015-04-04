package abyss.files.Snoopy;

import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.Arc.TypesOfArcs;

public class SnoopyToolClass {
	
	public static int getNormalizedY(int posY) {
		return posY+20;
	}
	
	public static ArrayList<Integer> getArcClassCount() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		int normal = 0;
		int readArc = 0;
		int inhibitor = 0;
		int reset = 0;
		int equal = 0;
		for(Arc a : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs()) {
			if(a.getArcType() == TypesOfArcs.NORMAL)
				normal++;
			else if(a.getArcType() == TypesOfArcs.READARC)
				readArc++;
			else if(a.getArcType() == TypesOfArcs.INHIBITOR)
				inhibitor++;
			else if(a.getArcType() == TypesOfArcs.RESET)
				reset++;
			else if(a.getArcType() == TypesOfArcs.EQUAL)
				equal++;
		}
		result.add(normal);
		result.add(readArc);
		result.add(inhibitor);
		result.add(reset);
		result.add(equal);
		
		return result;
	}
}
