package abyss.math;

import java.util.ArrayList;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class MinCutSetData {
	
	private ArrayList<ArrayList<Set>> mcsDataCore;

	public MinCutSetData() {
		mcsDataCore = new ArrayList<ArrayList<Set>>();
		
	}
	
	public ArrayList<ArrayList<Set>> accessMCSdata() {
		return mcsDataCore;
	}
}
