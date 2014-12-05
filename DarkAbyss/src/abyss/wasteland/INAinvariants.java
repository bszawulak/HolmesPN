package abyss.wasteland;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Nieuzywana, zawartosc przeniesiono do INAprotocols 5.12.2014
 * @author Students
 */
public class INAinvariants {

	private ArrayList<ArrayList<Integer>> listaInvariantow = new ArrayList<ArrayList<Integer>>();
	private ArrayList<Integer> listaNodow = new ArrayList<Integer>();
	//private int invarianNumber = 0;

	public ArrayList<ArrayList<Integer>> getInvariantsList() {
		return listaInvariantow;
	}  

	public void read(String sciezka) {
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(
					sciezka));
			@SuppressWarnings("resource")
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			String wczytanaLinia = buffer.readLine();
			// T-invariany dla P nie ma, bo nie mam na czy sie wzorowac, a INA
			// mnie nie slucha
			
			// brzydka, niedobra INA...
			
			if (wczytanaLinia
					.contains("transition sub/sur/invariants for net 0.t               :")) {
			}
			buffer.readLine();
			if (wczytanaLinia.contains("semipositive transition invariants =")) {
			}
			buffer.readLine();
			//System.out.println("Etap I");
			// Etap I - ilosc tranzycji/miejsc
			while (!(wczytanaLinia = buffer.readLine()).endsWith("~~~~~~~~~~~~~~~~~~~~~~~~")) {
				if(wczytanaLinia.endsWith("~~~~~~~~~~~~~~~~~~~~~~~~")){break;}
				String[] sformatowanaLinia = wczytanaLinia.split(" ");
				//System.out.println(wczytanaLinia);
				for (int j = 0; j < sformatowanaLinia.length; j++) {
					if ((sformatowanaLinia[j].isEmpty())
							|| sformatowanaLinia[j].contains("Nr.")) {
					} else {
						getListaNodow().add(Integer.parseInt(sformatowanaLinia[j]));
					}
				}
			}
			// Etap II - lista T/P - invariantow
			ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
			while ((wczytanaLinia = buffer.readLine()) != null) {
				if(wczytanaLinia.contains("@")||wczytanaLinia.isEmpty()){break;}
				String[] sformatowanaLinia = wczytanaLinia.split("\\|");

				sformatowanaLinia = sformatowanaLinia[1].split(" ");

				for(int i = 0; i<sformatowanaLinia.length;i++)
				{
					if(sformatowanaLinia[i].isEmpty()){}else
					{
						tmpInvariant.add(Integer.parseInt(sformatowanaLinia[i]));
					}
				}
				if(tmpInvariant.size()==getListaNodow().size())
				{
					listaInvariantow.add(tmpInvariant);
					tmpInvariant = new ArrayList<Integer>();
				}
			}
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	public ArrayList<Integer> getListaNodow() {
		return listaNodow;
	}

}
