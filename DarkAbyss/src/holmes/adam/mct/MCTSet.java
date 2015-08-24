package holmes.adam.mct;

import java.util.*;

public class MCTSet implements Comparable <MCTSet> {
	
	private static int seq = 1;
	
	private static class CompareMCT implements Comparator<MCTSet> {
		private final int bySize;
		@Override
		public int compare(MCTSet o1, MCTSet o2) {
			if (o1.name != null && o2.name != null) {
				return o1.name.compareTo(o2.name);
			} else {
				return (bySize*(o1.set.size() - o2.set.size())*2*seq) + (o1.id - o2.id);
			}
		}
		
		public CompareMCT(int bySize)
		{
			this.bySize = bySize;
		}
	};
	
	public static final Comparator<MCTSet> COMP_DEFAULT_BY_SEQ = new CompareMCT(0);
	public static final Comparator<MCTSet> COMP_BY_SIZE_DESCENDING = new CompareMCT(-1);
	public static final Comparator<MCTSet> COMP_BY_SIZE_ASCENDING = new CompareMCT(1);
	private static final String MCT_NAME_PREFIX = "m";
	
	public final int id;
	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private SortedSet<MCTTransition> set = new TreeSet<MCTTransition>();
	
	public MCTSet()
	{
		id = seq++;
	}
	
	public SortedSet<MCTTransition> getTransitionSet()
	{
		return set;
	}
	
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean latexMode)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getPrintName(latexMode));
		sb.append("={");
		for (MCTTransition tr : set)
		{
			sb.append(tr.id + ", ");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(sb.length() - 1);
		sb.append("}");
		return sb.toString();
	}
	
	public String getPrintName(boolean latexMode) {
		if (latexMode) {
			if (name == null) {
				return "$" + MCT_NAME_PREFIX + "_{" + id + "}$";
			} else {
				if (name.indexOf('.') < 0)
					return name;
				return "$" + name.replaceFirst("\\..*$", "") + "_{" + name.replaceFirst("^.*\\.", "") + "}$";
			}
		} else {
			return name == null ? MCT_NAME_PREFIX + id : name;	
		}
	}

	public int compareTo(MCTSet o) {
		return COMP_DEFAULT_BY_SEQ.compare(this, o);
	}
	
	
	private MCTSet(int id)
	{
		this.id = id;
	}
	
	public MCTPetriNet getPetriNet() {
		for (MCTTransition t : getTransitionSet()) {
			return t.petriNet;
		}
		return null;
	}
	
	public static SortedSet<MCTSet> rebuildMctOrder(SortedSet<MCTSet> mctSets, Comparator<MCTSet> comp)
	{
		ArrayList<MCTSet> bySize = new ArrayList<MCTSet>();
		for (MCTSet mct : mctSets)
		{
			bySize.add(mct);
		}
		int seq = 1;
		
		Collections.sort(bySize, comp);
		SortedSet<MCTSet> result = new TreeSet<MCTSet>();
		for (MCTSet mct : bySize)
		{
			MCTSet nmct = new MCTSet(seq++);
			nmct.setName(mct.getName());
			nmct.set.addAll(mct.set);
			result.add(nmct);
		}
		return result;
	}
	
	public static void renameMctSets(Set<MCTSet> mctSets, Map<String, String> renameMap) {
		MCTPetriNet pn = mctSets.iterator().next().getPetriNet();
		for (MCTSet mct : mctSets) {
			for (String tid : renameMap.keySet()) {
				MCTTransition t = pn.getTransition(tid);
				if (t == null) {
					continue;
				}
				if (mct.getTransitionSet().contains(t)) {
					mct.setName(renameMap.get(tid));
					break;
				}
			}
		}
	}
	
}
