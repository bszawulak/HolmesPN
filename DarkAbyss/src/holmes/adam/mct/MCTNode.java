package holmes.adam.mct;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MCTNode implements Comparable<MCTNode>
{
	private static Pattern crg = Pattern.compile("^([0-9]+)\\..*");
	private final Integer no;
	public final String id;
	public final MCTPetriNet petriNet;
	
	public MCTNode(String id, MCTPetriNet petriNet)
	{
		this.id = id;
		this.petriNet = petriNet;
		Matcher mt = crg.matcher(id);
		if (mt.matches())
		{
			no = Integer.parseInt(mt.group(1));
		} else {
			no = null;
		}
	}
	
	public int compareTo(MCTNode o)
	{
		if (no != null && o.no != null)
			return no.compareTo(o.no);

		return id.compareTo(o.id);
	}
	
	public String getNodeShort(boolean latexMode) {
		return String.valueOf(no);
	}
}
