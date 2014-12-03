package abyss.math.parser;

import java.io.PrintWriter;
import java.util.ArrayList;

import abyss.math.Transition;

public class INAinvariantsWriter {

	String buffor = "transition sub/sur/invariants for net 0.";

	public void write(String path, ArrayList<ArrayList<Integer>> invariants,
			ArrayList<Transition> transitions) {
		try {
			PrintWriter pw = new PrintWriter(path + ".inv");
			buffor += getNazwaPliku(path);
			buffor += "\r\n";
			buffor += "\r\n";
			// Dod pokrycie
			buffor += "semipositive transition invariants =\r\n";
			buffor += "\r\n";
			buffor += "Nr.      ";

			//int[] tabTransitions = new int[transitions.size()];
			for (int i = 0; i < transitions.size(); i++) {
				
				if (i <= 9)
					buffor += " ";
				if (i <= 99)
					buffor += " ";
				if (i <= 999)
					buffor += " ";
				buffor += i;

				if (i == 16) {
					buffor += "\r\n";
					buffor += "        ";
				}
			}
			buffor += "\r\n";
			// if(transitions.size()>=17)
			// {
			// buffor +=
			// "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";

			// }else
			buffor += "~~~~~~~~~";
			for (int t = 0; t < transitions.size(); t++)
				buffor += "~~~~";
			buffor += "\r\n";

			for (int i = 0; i < invariants.size(); i++) {
				if (i <= 9)
					buffor += " ";
				if (i <= 99)
					buffor += " ";
				if (i <= 999)
					buffor += " ";
				buffor += i;
				buffor += " |   ";

				for (int t = 0; t < invariants.get(i).size(); t++) {
					int tr = invariants.get(i).get(t);
					if (tr <= 9)
						buffor += " ";
					if (tr <= 99)
						buffor += " ";
					if (tr <= 999)
						buffor += " ";
					buffor += tr;
					if (t == 16 && invariants.size() > 16) {
						buffor += "\r\n";
						buffor += "     |   ";
					}
				}
				buffor += "\r\n";

			}

			//
			buffor += "\r\n";
			buffor += "@";
			//System.out.println(buffor);
			
			pw.println(buffor);
			pw.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());

		}
	}

	public String getNazwaPliku(String sciezka) {

		String[] tablica = sciezka.split("\\\\");
		return tablica[tablica.length - 1];
	}

}
