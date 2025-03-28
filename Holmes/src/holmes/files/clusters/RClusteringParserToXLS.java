package holmes.files.clusters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import holmes.clusters.ClusterRepresentation;
import holmes.utilities.ByExt;

/**
 * Klasa odpowiedzialna za export wskazanego katalogu z plikami klastrowań do
 * arkusza excela.
 * @author AR
 *
 */
public class RClusteringParserToXLS{
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	// czcionka i formatowanie komorki tabeli
	private static WritableCellFormat getCellFormat(Colour colour, boolean header) throws WriteException {
	  WritableFont cellFont;
	  if (!header) cellFont = new WritableFont(WritableFont.TIMES, 12);
	  else  {
		  cellFont = new WritableFont(WritableFont.COURIER, 12);
		  cellFont.setBoldStyle(WritableFont.BOLD);
	  }
	  WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
	  cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	  cellFormat.setBackground(colour);
	  cellFormat.setAlignment(Alignment.CENTRE);
	  return cellFormat;
	}
	
	// ustaw kolor dla komorki wzgledem wartosci Mean
	private Colour setColorMeanMssPerCluster(double meanValue){
		if (meanValue >= 0.71) return Colour.DARK_GREEN;
		else if (meanValue >= 0.61) return Colour.LIGHT_GREEN;
		else if (meanValue >= 0.50) return Colour.SEA_GREEN;
		else if (meanValue >= 0.34) return Colour.YELLOW;
		else if (meanValue > 0.25) return Colour.LIGHT_ORANGE;
		else if (meanValue <= 0.25) return Colour.RED;
		
		return Colour.WHITE;
	}
	
	// ustaw kolor dla komorki wzgledem ilosci klastrow jedynkowych
	private Colour setColorSingleInvariantPerCluster(int singleInvCluster){
		if (singleInvCluster <= 4) return Colour.DARK_GREEN;
		else if (singleInvCluster <= 9) return Colour.YELLOW;
		else if (singleInvCluster >= 10) return Colour.RED;
		
		return Colour.WHITE;
	}
	
	// naglowek tabeli
	private void setTableHeader(WritableSheet sheet, int column) throws WriteException{
		WritableFont cellFont = new WritableFont(WritableFont.COURIER, 14);
		cellFont.setBoldStyle(WritableFont.BOLD);
		WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
		cellFormat.setWrap(true);
		cellFormat.setAlignment(Alignment.CENTRE);
		sheet.addCell(new Label(0, 2, lang.getText("RCP_entry001"), cellFormat)); //Distance/Cluster alg.
		sheet.setColumnView(0, 30);
		//System.out.println("merge "+3+"-"+(column+1));
		sheet.mergeCells(1, 2, column+1, 2);
		
	    sheet.addCell(new Label(1, 2, lang.getText("RCP_entry002"), cellFormat)); //Silhouette
	}
	
	// legenda do tabeli
	private void setTableLegend(WritableSheet sheet, int column) throws WriteException{
		WritableFont cellFont = new WritableFont(WritableFont.COURIER, 12);
		cellFont.setBoldStyle(WritableFont.BOLD);
		WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
		cellFormat.setWrap(true);
		cellFormat.setAlignment(Alignment.CENTRE);
		sheet.addCell(new Label(column, 2, lang.getText("RCP_entry003"), cellFormat)); //Range
		sheet.setColumnView(column, 30);
		sheet.addCell(new Label(column+1, 2, lang.getText("RCP_entry003"), cellFormat)); //Evaluation
		sheet.setColumnView(column+1, 20);
		
		sheet.addCell(new Label(column, 3, "0.71-1.00", getCellFormat(setColorMeanMssPerCluster(0.9), false)));
		sheet.addCell(new Label(column+1, 3, lang.getText("RCP_entry005"), getCellFormat(setColorMeanMssPerCluster(0.9), false)));
		sheet.addCell(new Label(column, 4, "0.61-0.70", getCellFormat(setColorMeanMssPerCluster(0.7), false)));
		sheet.addCell(new Label(column+1, 4, lang.getText("RCP_entry006"), getCellFormat(setColorMeanMssPerCluster(0.7), false)));
		sheet.addCell(new Label(column, 5, "0.50-0.60", getCellFormat(setColorMeanMssPerCluster(0.6), false)));
		sheet.addCell(new Label(column+1, 5, lang.getText("RCP_entry007"), getCellFormat(setColorMeanMssPerCluster(0.6), false)));
		sheet.addCell(new Label(column, 6, "0.34-0.49", getCellFormat(setColorMeanMssPerCluster(0.4), false)));
		sheet.addCell(new Label(column+1, 6, lang.getText("RCP_entry008"), getCellFormat(setColorMeanMssPerCluster(0.4), false)));
		sheet.addCell(new Label(column, 7, "0.25-0.33", getCellFormat(setColorMeanMssPerCluster(0.3), false)));
		sheet.addCell(new Label(column+1, 7, lang.getText("RCP_entry009"), getCellFormat(setColorMeanMssPerCluster(0.3), false)));
		sheet.addCell(new Label(column, 8, "<= 0.25", getCellFormat(setColorMeanMssPerCluster(0.1), false)));
		sheet.addCell(new Label(column+1, 8, lang.getText("RCP_entry010"), getCellFormat(setColorMeanMssPerCluster(0.1), false)));
		
		sheet.addCell(new Label(column, 10, lang.getText("RCP_entry011"), cellFormat)); //Single clusters
		sheet.addCell(new Label(column+1, 10, lang.getText("RCP_entry012"), cellFormat)); //Evaluation
		sheet.addCell(new Label(column, 11, "1-4", getCellFormat(setColorSingleInvariantPerCluster(1), false)));
		sheet.addCell(new Label(column+1, 11, lang.getText("RCP_entry013"), getCellFormat(setColorSingleInvariantPerCluster(1), false))); //Very good
		sheet.addCell(new Label(column, 12, "5-9", getCellFormat(setColorSingleInvariantPerCluster(6), false)));
		sheet.addCell(new Label(column+1, 12, lang.getText("RCP_entry014"), getCellFormat(setColorSingleInvariantPerCluster(6), false))); //Quite good
		sheet.addCell(new Label(column, 13, "> 10", getCellFormat(setColorSingleInvariantPerCluster(11), false)));
		sheet.addCell(new Label(column+1, 13, lang.getText("RCP_entry015"), getCellFormat(setColorSingleInvariantPerCluster(11), false)));//Bad
		
	}
	
	// pobierz zawartosc pliku z klastrowaniem
	private String getFileContent(String source) throws IOException{
		FileReader we = new FileReader(source);
	    BufferedReader in = new BufferedReader(we);
	    String linia;
	    StringBuilder sb = new StringBuilder();
	    while ((linia = in.readLine()) != null) sb.append(linia+"\n");
	    in.close();
	    return sb.toString();
	}
	
	// parsowanie informacji z pojedynczego klastrowania
	private ClusterRepresentation parseSingleClusterInfo(String source) throws NumberFormatException{
		int nrCurrentReadClustersValues = 0;		
		ClusterRepresentation clusterSingle = new ClusterRepresentation();
		Pattern pattern = Pattern.compile("in (.*?) clusters");
		Matcher matcher = pattern.matcher(source);
		 
	    if (matcher.find())
	    {
	      String[] lines = source.split("Output:"); // podzial na linie
	      int nrClusters = Integer.parseInt(matcher.group(1));
	      clusterSingle.nrClusters = nrClusters;
	      
	      for(int i = 2; i < lines.length; i++){
	    	  if (nrCurrentReadClustersValues < nrClusters) {
		    	  String[] nrInvariantsPerCluster = lines[i++].split("[\\s\\t\\n]+");
		    	  for (int j = 1; j < nrInvariantsPerCluster.length; ++j) {
		    		  clusterSingle.nrInvariantsPerCluster.add(Integer.parseInt(nrInvariantsPerCluster[j]));
		    		  ++nrCurrentReadClustersValues;
		    	  }
		    	  String[] mssPerCluster = lines[i].split("[\\s\\t\\n]+");
				  for (String s : mssPerCluster)
					  clusterSingle.mssPerCluster.add(clusterSingle.ParseDouble(s));//Float.parseFloat(mssPerCluster[j]));
	    	  } else {
	    		  if ( lines[i].indexOf("Mean") > 0 ) {
	    			  String[] meanValue = lines[i+1].trim().split("\\s+");
	    			  clusterSingle.meanValue = clusterSingle.ParseDouble(meanValue[3]);//Float.parseFloat(meanValue[3]);
	    			  break;
	    		  }
	    	  }
	      }
	    }
	    return clusterSingle;
	}
	
	// parsowanie do pliku XLS
	public void extractAllRClusteringToXLS(String pathInput, String outputFile) throws IOException, WriteException{
		int[] tabIndexes = new int[100];
		Arrays.fill(tabIndexes, 3);
		RClusteringParserToXLS r = new RClusteringParserToXLS();
		
		// tworzy arkusz o zadanej nazwie
	    WritableWorkbook workbook = Workbook.createWorkbook(new File(outputFile));
	    WritableSheet sheet = workbook.createSheet("Clusters", 0);
	    
		File fp1 = new File(pathInput);
		FilenameFilter only = new ByExt("_clusters.txt");
		String[] dirList = fp1.list(only); // metoda list z filtrem
		Arrays.sort(dirList);
		
		for(int columnPosition = 0; columnPosition< 100; columnPosition++) { // ustal szerokosc kolumn
			if (columnPosition%2 != 0) sheet.setColumnView(columnPosition, 5); // na ilosc klastrow jedynkowych mniej
			else sheet.setColumnView(columnPosition, 15);
		}
		
		int column = 0, kolumna = column;
		boolean newColumn = true;
		String compare = dirList[0].substring(0, 4);
		for (String s : dirList) { // dla wszystkich plikow z katalogu
			String fileContent = r.getFileContent(pathInput + "//" + s);
			String[] clusters = fileContent.split("Output:Silhouette"); // tablica informacji o klastrach

			if (s.startsWith(compare)) { // ta sama miara, ale inna metoda
				String[] parts = s.split("_");
				parts[0] = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1); //pierwsza litera nazwy z duzej
				parts[1] = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1); //pierwsza litera nazwy z duzej
				if (newColumn) kolumna = column + 1;
				else kolumna = column;

				if (newColumn)
					sheet.addCell(new Label(column, tabIndexes[column], parts[1], getCellFormat(Colour.WHITE, true)));
				sheet.addCell(new Label(kolumna, tabIndexes[column], "1:", getCellFormat(Colour.WHITE, true)));
				sheet.addCell(new Label(kolumna + 1, tabIndexes[column]++, parts[0], getCellFormat(Colour.WHITE, true)));
				if (newColumn) for (int k = 1; k < clusters.length; ++k)
					sheet.addCell(new Number(column, tabIndexes[column] + k - 1, (k + 1), getCellFormat(Colour.WHITE, false))); // numery kolejnych klastrow, od 2
			} else { // nowa miara
				compare = s.substring(0, 4);
				if (newColumn) column += 3;
				else column += 2;
				newColumn = false;
				String[] parts = s.split("_");
				parts[0] = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1); //pierwsza litera nazwy z duzej
				parts[1] = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1); //pierwsza litera nazwy z duzej

				sheet.addCell(new Label(column, tabIndexes[column], "1:", getCellFormat(Colour.WHITE, true)));
				sheet.addCell(new Label(column + 1, tabIndexes[column]++, parts[0], getCellFormat(Colour.WHITE, true)));
			}
			for (int j = 1; j < clusters.length; ++j) { // przetwarzanie dla kazdego klastrowania osobno
				ClusterRepresentation cluster = r.parseSingleClusterInfo(clusters[j]); // dane pojedynczego klastrowania
				int singleInvCluster = 0;
				if (newColumn) kolumna = column + 1;
				else kolumna = column;
				for (int k = 0; k < cluster.nrInvariantsPerCluster.size(); ++k)
					if (cluster.nrInvariantsPerCluster.get(k) == 1)
						++singleInvCluster; // zliczanie klasterow jedynkowych
				sheet.addCell(new Number(kolumna, tabIndexes[column], singleInvCluster, getCellFormat(setColorSingleInvariantPerCluster(singleInvCluster), false))); // ilosc klastrow jedynkowych
				sheet.addCell(new Number(kolumna + 1, tabIndexes[column]++, cluster.meanValue, getCellFormat(setColorMeanMssPerCluster(cluster.meanValue), false))); // srednie mss dla klastra
			}
		}
		// naglowek tabeli
		setTableHeader(sheet, column);
		// legenda tabeli
		setTableLegend(sheet, column+3);
		// zapisuje dane w formacie Excela
	    workbook.write(); 
	    workbook.close(); 
	}
}
