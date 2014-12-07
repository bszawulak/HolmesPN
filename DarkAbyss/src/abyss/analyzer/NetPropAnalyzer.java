package abyss.analyzer;

import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;

public class NetPropAnalyzer {

	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	@SuppressWarnings("unused")
	private ArrayList<Node> nodes = new ArrayList<Node>();

	ArrayList<Node> checked = new ArrayList<Node>();

	public NetPropAnalyzer() {
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
	}

	public ArrayList<ArrayList<Object>> propAnalyze() {
		ArrayList<ArrayList<Object>> NetProps = new ArrayList<ArrayList<Object>>();

		if (places.size() > 0 && transitions.size() > 0 && arcs.size() > 0) {
			// PUR - pure net
			boolean isPure = true;
			// FT0 - a transition without pre place
			boolean isFT0 = false;
			// TF0- a transitions without post place
			boolean isTF0 = false;
			// FP0 - a place without pre transitions
			boolean isFP0 = false;
			// PF0 - a place without post transitions
			boolean isPF0 = false;

			for (Transition t : transitions) {
				boolean arcIn = false;
				boolean arcOut = false;
				for (ElementLocation el : t.getElementLocations()) {
					if (!el.getInArcs().isEmpty() && arcIn == false)
						arcIn = true;
					if (!el.getOutArcs().isEmpty() && arcOut == false)
						arcOut = true;
				}
				if (arcIn == false && arcOut == true)
					isFT0 = true;
				if (arcIn == true && arcOut == false)
					isTF0 = true;
			}

			for (Place p : places) {
				boolean arcIn = false;
				boolean arcOut = false;
				for (ElementLocation el : p.getElementLocations()) {
					if (!el.getInArcs().isEmpty() && arcIn == false)
						arcIn = true;
					if (!el.getOutArcs().isEmpty() && arcOut == false)
						arcOut = true;
				}
				if (arcIn == false && arcOut == true)
					isFP0 = true;
				if (arcIn == true && arcOut == false)
					isPF0 = true;
			}

			// Pure wlasciwe
			for (Transition t : transitions) {
				for (ElementLocation el : t.getElementLocations()) {
					for (Arc ar : el.getInArcs()) {
						for(ElementLocation el2 : ar.getStartNode().getElementLocations()){
							for (Arc ar2 : el2.getInArcs()) {
								if(ar2.getStartNode().getID()==t.getID())
									isPure=false;
							}
						}
					}
				}
			}

			ArrayList<Object> purProp = new ArrayList<Object>();
			purProp.add("PUR");
			purProp.add(isPure);
			NetProps.add(purProp);

			// ORD - ordinary net
			boolean isOrdinary = true;
			for (Arc a : arcs)
				if (a.getWeight() != 1)
					isOrdinary = false;

			ArrayList<Object> ordProp = new ArrayList<Object>();
			ordProp.add("ORD");
			ordProp.add(isOrdinary);
			NetProps.add(ordProp);

			// HOM - homogenous net
			boolean isHomogenous = true;
			for (Place p : places) {
				int val = 0;
				for (ElementLocation el : p.getElementLocations())
					for (Arc a : el.getOutArcs())
						if (val == 0)
							val = a.getWeight();
						else if (val != a.getWeight())
							isHomogenous = false;
			}

			ArrayList<Object> homProp = new ArrayList<Object>();
			homProp.add("HOM");
			homProp.add(isHomogenous);
			NetProps.add(homProp);

			// NBM - non blocking multiplicity net
			boolean isNonBlockingMulti = true;
			for (Place p : places) {
				int valIn = Integer.MAX_VALUE;
				int valOut = 0;
				for (ElementLocation el : p.getElementLocations()) {
					for (Arc a : el.getInArcs())
						if (a.getWeight() < valIn)
							valIn = a.getWeight();
					for (Arc a : el.getOutArcs())
						if (a.getWeight() > valOut)
							valOut = a.getWeight();

				}
				if (valOut > valIn)
					isNonBlockingMulti = false;
			}

			ArrayList<Object> nbmProp = new ArrayList<Object>();
			nbmProp.add("NBM");
			nbmProp.add(isNonBlockingMulti);
			NetProps.add(nbmProp);

			// CSV - conservative net
			boolean isConservative = true;
			for (Transition t : transitions) {
				int arcIn = 0;
				int arcOut = 0;
				for (ElementLocation el : t.getElementLocations()) {
					for (Arc a : el.getInArcs())
						arcIn += a.getWeight();
					for (Arc a : el.getOutArcs())
						arcOut += a.getWeight();
				}
				if (arcIn != arcOut)
					isConservative = false;
			}
			ArrayList<Object> csvProp = new ArrayList<Object>();
			csvProp.add("CSV");
			csvProp.add(isConservative);
			NetProps.add(csvProp);

			// CON - connected net
			
			
			
			// SC - strongly connected net
			boolean isConnected = false;
			boolean isStronglyConnected = true;

			//checkConnectionExist(nodes.get(0));
			//if (checked.size() == nodes.size())
			//	isConnected = true;
/*
			for (Node n : nodes)
				for (Node n2 : nodes)
					if (n.getID() != n2.getID())
						if (!checkStronglyConnectionExist(n, n, n2, false))
							isStronglyConnected = false;
*/
			// SCF - static conflict free
			boolean isStaticConFree = true;

			for (Transition t : transitions)
				for (ElementLocation el : t.getElementLocations())
					for (Arc a1 : el.getInArcs())
						for (Transition t2 : transitions)
							if (t.getID() != t2.getID())
								for (ElementLocation el2 : t2
										.getElementLocations())
									for (Arc a2 : el2.getInArcs())
										if (a1.getStartNode().getID() == a2
												.getStartNode().getID())
											;
			isStaticConFree = false;

			System.out.println("====> " + isStaticConFree);

			ArrayList<Object> ft0Prop = new ArrayList<Object>();
			ft0Prop.add("FT0");
			ft0Prop.add(isFT0);
			NetProps.add(ft0Prop);
			ArrayList<Object> tf0Prop = new ArrayList<Object>();
			tf0Prop.add("TF0");
			tf0Prop.add(isTF0);
			NetProps.add(tf0Prop);
			ArrayList<Object> fp0Prop = new ArrayList<Object>();
			fp0Prop.add("FP0");
			fp0Prop.add(isFP0);
			NetProps.add(fp0Prop);
			ArrayList<Object> pf0Prop = new ArrayList<Object>();
			pf0Prop.add("PF0");
			pf0Prop.add(isPF0);
			NetProps.add(pf0Prop);
			// Przeniesc wyzej
			ArrayList<Object> scfProp = new ArrayList<Object>();
			scfProp.add("SCF");
			scfProp.add(isStaticConFree);
			NetProps.add(scfProp);

			ArrayList<Object> conProp = new ArrayList<Object>();
			conProp.add("CON");
			conProp.add(isConnected);
			NetProps.add(conProp);

			ArrayList<Object> scProp = new ArrayList<Object>();
			scProp.add("SC");
			scProp.add(isStronglyConnected);
			NetProps.add(scProp);

		} else {
			ArrayList<Object> pProp = new ArrayList<Object>();
			pProp.add("PUR");
			NetProps.add(new ArrayList<Object>(pProp));
			pProp = new ArrayList<Object>();
			pProp.add("ORD");
			NetProps.add(new ArrayList<Object>(pProp));
			pProp = new ArrayList<Object>();
			pProp.add("HOM");
			NetProps.add(new ArrayList<Object>(pProp));
			pProp = new ArrayList<Object>();
			pProp.add("NBM");
			NetProps.add(new ArrayList<Object>(pProp));
			pProp = new ArrayList<Object>();
			pProp.add("CSV");
			NetProps.add(new ArrayList<Object>(pProp));
			pProp = new ArrayList<Object>();
			pProp.add("FT0");
			NetProps.add(new ArrayList<Object>(pProp));
			pProp = new ArrayList<Object>();
			pProp.add("TF0");
			NetProps.add(new ArrayList<Object>(pProp));
			pProp = new ArrayList<Object>();
			pProp.add("FP0");
			NetProps.add(new ArrayList<Object>(pProp));
			pProp = new ArrayList<Object>();
			pProp.add("PF0");
			NetProps.add(new ArrayList<Object>(pProp));

		}
		return NetProps;
	}

	@SuppressWarnings("unused")
	private boolean checkStronglyConnectionExist(Node n1, Node n2, Node last, boolean mode) {
		if (mode == true && n1.getID() == n2.getID())
			return false;

		if (n1.getInArcs()!=null)
		{
			for (Arc a : n1.getInArcs())
				if (a.getStartNode().getID() == last.getID())
					return true;
			for (Arc a : n1.getInArcs())
				if (checkStronglyConnectionExist(a.getStartNode(), n2, last, true))
					return true;
		}else{
			return false;
		}
		
		return false;
	}

	@SuppressWarnings("unused")
	private void checkConnectionExist(Node n1) {

	}
}
