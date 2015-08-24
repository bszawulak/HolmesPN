package holmes.adam.mct;

import java.util.*;

public class TInvariant implements Comparable<TInvariant> {
	
	public final String id;
	public final MCTPetriNet petriNet;
	
	private SortedMap<MCTTransition, Integer> map = new TreeMap<MCTTransition, Integer>();
	
	public TInvariant(String id, MCTPetriNet petriNet)
	{
		this.id = id;
		this.petriNet = petriNet;
	}
	
	public SortedMap<MCTTransition, Integer> getTransitionMap()
	{
		return map;
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(id);
		sb.append("=");
		for (MCTTransition t : map.keySet())
		{
			Integer count = map.get(t);
			sb.append(t.id);
			if (count.intValue() > 1)
			{
				sb.append(":");
				sb.append(count);
			}
			sb.append(",");
		}
		return sb.toString();
	}
	
	public String toVector(boolean boolValues)
	{
		SortedSet<MCTTransition> transitions = petriNet.getTransitions();
		StringBuilder sb = new StringBuilder();
		int c = 0;
		for (MCTTransition transition : transitions)
		{
			Integer cnt = map.get(transition);
			if (cnt == null)
				cnt = 0;
			else if (boolValues)
				cnt = 1;
			
			sb.append(cnt);
			c++;
			if (c < transitions.size())
				sb.append(";");
		}
		return sb.toString();
	}

	@Override
	public int compareTo(TInvariant o) {
		try {
			int thisid = Integer.parseInt(this.id);
			int cid = Integer.parseInt(o.id);
			return thisid - cid;
		} catch (Exception e)
		{
			return this.id.compareTo(o.id);
		}
	}
	
	public boolean contains(MCTTransition tr) {
		return map.get(tr) != null;
	}

	public String toMCTString(SortedSet<MCTSet> mctSets, boolean latexMode) {
		StringBuilder sb = new StringBuilder();
		String separator = latexMode ? "\t\t&\t\t" : ";";
		sb.append(latexMode ? "" : "[");
		for (MCTSet mct : mctSets)
		{
			if (contains(mct.getTransitionSet().first()))
			{
				sb.append(mct.getPrintName(latexMode));
				sb.append(", ");
			}
		}
		if (sb.length() > 1 && sb.charAt(sb.length()-2) == ',')
			sb.deleteCharAt(sb.length() - 2);
		sb.append(latexMode ? "" : "]");
		sb.append(separator);
		for (MCTTransition t : map.keySet())
		{
			boolean inMCT = false;
			for (MCTSet mct : mctSets)
			{
				if (mct.getTransitionSet().contains(t))
				{
					inMCT = true;
					break;
				}
			}
			if (!inMCT)
			{
				sb.append(t.getNodeShort(latexMode));
				sb.append(", ");
			}
		}

		return sb.toString().replaceFirst(", $", "");
	}
}
