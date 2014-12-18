package abyss.adam.mct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

public class Runner {
	private PrintWriter out = null;

	private void print(String s) throws FileNotFoundException
	{
		if (out == null)
			out = new PrintWriter(new File("output/output.txt"));
		//System.out.print(s);
		out.print(s);
		out.flush();
	}
	
	private void print(PrintWriter pw, String s) throws FileNotFoundException
	{
		if (pw == null)
			return;
		pw.print(s);
		pw.flush();
	}
	
	private void println(String s) throws FileNotFoundException
	{
		if (out == null)
			out = new PrintWriter(new File("output/output.txt"));
		//System.out.println(s);
		out.println(s);
		out.flush();
	}
	
	private void println(PrintWriter pw, String s) throws FileNotFoundException
	{
		if (pw == null)
			return;
		print(pw, s);
		pw.println();
		pw.flush();
	}
	
	private static class ProgramSettings {
		String inputFilePath = "input/input.csv";
		String outputFilePath = "output/output.txt";
		String csvOutputFilePath = "output/output.csv";
		String mctRenameFile = null;
		List<SortedSet<String>> clusters = null;
		boolean latexMode = false;
		boolean includeId = true;
		boolean includeVectors = false;
		boolean boolValues = false;
		boolean showAll = true;
		Double showAsCommonTransitionThreshold = 1.0;
		boolean showClusterAsVector = false;
		
		public ProgramSettings(String args[]) {
			for (int i = 0; i < args.length; i++) {
				String param = args[i];
				
				//parametry bezargumentowe
				if (param.equals("-latex")) {
					latexMode = true;
					continue;
				} 
				
				if (param.equals("-excludeId")) {
					includeId = false;
					continue;
				} 
				
				if (param.equals("-includeVectors")) {
					includeVectors = true;
					continue;
				}
				
				if (param.equals("-boolValues")) {
					boolValues = true;
					continue;
				}
				
				if (param.equals("-showAll")) {
					showAll = true;
					continue;
				}
				
				if (param.equals("-showClusterAsVector")) {
					showClusterAsVector = true;
					continue;
				} 
				
				if (param.startsWith("-")) {
					if (i == args.length - 1) {
						printHelp();
						throw new RuntimeException("Wrong parameter list");
					}
				} else {
					inputFilePath = param;
					if (outputFilePath.equals("output/output.txt")) {
						outputFilePath = inputFilePath + ".analysed.txt";
					}
					
					if (csvOutputFilePath.equals("output/output.csv")) {
						csvOutputFilePath = inputFilePath + ".csv";
					}
					continue;
				}
				
				// parametry argumentowe
				if (param.equals("-o")) {
					outputFilePath = args[++i];
				} else if (param.equals("-csvo")) {
					csvOutputFilePath = args[++i];
				} else if (param.equals("-mct")) {
					mctRenameFile = args[++i];
				} else if (param.equals("-cr")) {
					setClusters(args[++i]);
				} else if (param.equals("-sactt")) {
					String threshold = args[++i];
					try {
						showAsCommonTransitionThreshold = Double.parseDouble(threshold);
					} catch (NumberFormatException nfe) {
						throw new NumberFormatException("Error in format of common transition threshold value (-scatt arg.): " + threshold);
					}
				} else {
					throw new IllegalArgumentException("Wrong parameter: " + param);
				}
			}
		}
		
		/**
		 * Parsuje ci¹g zakresów klastrów. Zak³ada siê, ¿e kolejnoœæ inwariantów odpowiada kolejnoœci podanej w analizowanym pliku z t-inwariantami.
		 * @param tclusters - ci¹g zakresów klastrów rozdzielonych œrednikami, przy czym zakresy mog¹ byæ roz³¹czne i rozdzielone œrednikami
		 *  np. 1-2,5;3-4,6-8;9-50;51-90;91-129
		 */
		private void setClusters(String tclusters) {
			String tInvariantsInClusters[] = tclusters.split(";");
			clusters = new ArrayList<SortedSet<String>>(tInvariantsInClusters.length);
			for (int j = 0; j < tInvariantsInClusters.length; j++) {
				String clusterDescriptor = tInvariantsInClusters[j];
				String ranges[] = clusterDescriptor.split(",");
				SortedSet<String> ss = new TreeSet<String>(new Utils.HybridStringNumberComparator());
				
				for (String range : ranges) {
					// dla ew. id tinwariantów innych ni¿ liczbowe
					if (range.indexOf('-') < 0 || !range.matches("[0-9]+-[0-9]+") ) {
						ss.add(range);
						continue;
					}
					
					String limits[] = range.split("-");
					try {
						int from = Integer.parseInt(limits[0]);
						int to = limits.length > 1 ? Integer.parseInt(limits[1]) : from;
						for (int index = from; index <= to; index++) {
							ss.add(String.valueOf(index));
						}
					} catch (NumberFormatException nfe) {
						throw new NumberFormatException("Error in cluster range format: " + range);
					}
				}
				clusters.add(ss);
			}
		}
		
		private static void printHelp() {
			try {
				InputStream is = Runner.class.getClassLoader().getResourceAsStream("abyss/adam/mct/help.txt");
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				//String line;
				//while ((line = br.readLine()) != null) {
					//System.out.println(line);
				//}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void activate(String[] args) throws FileNotFoundException, IOException
	{
		ProgramSettings ps = null;
		try {
			ps = new ProgramSettings(args);
		} catch (RuntimeException re) {
			//System.out.println(re.getMessage());
			ProgramSettings.printHelp();
			return;
		}
		String fileName = ps.inputFilePath;
		File file = new File(fileName);
		InputStream is = new FileInputStream(file);
		out = new PrintWriter(ps.outputFilePath);
		PrintWriter vOut = fileName.endsWith(".csv") ? null : new PrintWriter(ps.csvOutputFilePath);
		SortedSet<TInvariant> tInvariants;
		MCTPetriNet petriNet = new MCTPetriNet();
		if (file.getName().toLowerCase().endsWith(".inv"))
			tInvariants = Utils.readFromCharlie(is, petriNet);
		else if (file.getName().toLowerCase().endsWith(".csv"))
			tInvariants = Utils.readFromCSV(is, petriNet);
		else
			tInvariants = Utils.readTInvariants(is, true, petriNet);
		Map <String, String> renameMap = ps.mctRenameFile != null ? Utils.readMCTNameMap(ps.mctRenameFile) : null;
		
		SortedSet<MCTSet> mctSets = Utils.buildMCT(tInvariants, renameMap);
		SortedSet<MCTSet> properMctSets = new TreeSet<MCTSet>();
		SortedSet<MCTSet> single = new TreeSet<MCTSet>();
		for (MCTSet mct : mctSets)
			if (mct.getTransitionSet().size() > 1)
				properMctSets.add(mct);
			else
				single.add(mct);
		
		if (ps.showAll) {
			println("---Invariants---------------------------");
			println(Utils.invariantsToString(tInvariants));
		}
		println("---Invariants[VECTORS]---------------------------");
		String header = ";";
		for (MCTTransition t : petriNet.getTransitions())
		{
			header += t.id + ";";
		}
		println(header);
		println(vOut, header);
		String vectors = Utils.invariantsToCsvVectors(tInvariants, false, true, ps.latexMode);
		println(vectors);
		println(vOut, vectors);
		println("");
		println("---Invariants[IN MCT]---------------------------");
		println(header);
		println(Utils.invariantsWithMCT(tInvariants, properMctSets, ps.includeId, ps.includeVectors, ps.boolValues, ps.latexMode, ps.clusters));
		println("");
		println("Proper MCT sets (size > 1): ");
		//int i = 1;
		for (MCTSet mct : properMctSets)
		{
			if (ps.latexMode)
				println(" [" + mct.getTransitionSet().size() + "]. " + mct.toString(true));
			else 
				println((mct.getPrintName(false)) + " [" + mct.getTransitionSet().size() + "]. " + mct);
		}
		println("Single MCT sets (size = 1): ");
		for (MCTSet mct : single)
		{
			if (ps.latexMode)
				println(mct.toString(true));
			else
				println((mct.getPrintName(ps.latexMode)) + ". " + mct);
		}
		
		//wydruk zbiorów MCT i tranzycji wspólnych dla klastra
		if (ps.clusters != null) {
			println("");
			println("List of common cluster MCT sets and");
			List<SortedMap<MCTTransition, Integer>> transitionCounts = Utils.computeTransitionCoverance(ps.clusters, tInvariants);
			for (int i = 0; i < transitionCounts.size(); i++) {
				int clusterSize = ps.clusters.get(i).size();
				SortedMap<MCTTransition, Integer> transitionCount = transitionCounts.get(i);
				String percent = String.valueOf((1000*clusterSize/tInvariants.size())/10.0).replaceFirst("\\.(.).*$", ".$1");
				if (ps.latexMode)
					print("$c_{" + (i + 1) + "}$ & " + clusterSize + " (" + percent + "\\%) & ");
				else
					print("c" + (i + 1) + " [" + clusterSize + " (" + percent + "%)]: ");
				if (ps.showClusterAsVector) {
					for (MCTTransition t : transitionCount.keySet()) {
						Integer tcount = transitionCount.get(t);
						print(tcount + ", ");
					}
					println("");
				} else {
					SortedSet<MCTSet> mctSetsInClusterForThreshold = new TreeSet<MCTSet>();
					SortedSet<MCTTransition> singleTransitionsInClusterForThreshold = new TreeSet<MCTTransition>();
					
					SortedSet<MCTSet> mctSetsInClusterBelowThreshold = new TreeSet<MCTSet>();
					SortedSet<MCTTransition> singleTransitionsInClusterBelowThreshold = new TreeSet<MCTTransition>();
					
					for (MCTTransition t : transitionCount.keySet()) {
						Integer tcount = transitionCount.get(t);
						double percentage = ((double)tcount)/clusterSize;
						if (percentage >= ps.showAsCommonTransitionThreshold) {
							MCTSet mct = t.getMCTSet();
							if (mct != null && mct.getTransitionSet().size() > 1) {
								mctSetsInClusterForThreshold.add(mct);
							} else {
								singleTransitionsInClusterForThreshold.add(t);
							}
						} else if (percentage > 0.5) {
							MCTSet mct = t.getMCTSet();
							if (mct != null && mct.getTransitionSet().size() > 1) {
								mctSetsInClusterBelowThreshold.add(mct);
							} else {
								singleTransitionsInClusterBelowThreshold.add(t);
							}
						}
					}
					
					// WYPISANIE TRANZYCJI DLA KLASTRA POWY¯EJ PROGU
					if (!ps.latexMode) print("MCT = [");
					for (MCTSet mct : mctSetsInClusterForThreshold) {
						print(mct.getPrintName(ps.latexMode) + ", ");
					}
					if (!ps.latexMode) print("], transitions = [");
					for (MCTTransition t : singleTransitionsInClusterForThreshold) {
						print(t.getNodeShort(ps.latexMode) + ", ");
					}
					if (ps.latexMode)
						println("	&	");
					else
						println("]");
					
					// WYPISANIE TRANZYCJI DLA KLASTRA PONI¯EJ PROGU
					if (!ps.latexMode) print("MCT = [");
					for (MCTSet mct : mctSetsInClusterBelowThreshold) {
						print(mct.getPrintName(ps.latexMode) + ", ");
					}
					if (!ps.latexMode) print("], transitions = [");
					for (MCTTransition t : singleTransitionsInClusterBelowThreshold) {
						print(t.getNodeShort(ps.latexMode) + ", ");
					}
					
					if (ps.latexMode)
						println("	\\\\ \\hline");
					else
						println("]");
				}
			}
		}
	}
}
