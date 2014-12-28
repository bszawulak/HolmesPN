package abyss.files.clusters;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JOptionPane;

import abyss.clusters.ClusteringExtended;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


public class ExcelWriter {
	private WritableCellFormat defCellFormat; //standardowe formatowanie
	private WritableCellFormat defCellFormatBold;
	private WritableCellFormat defCellFormatItalic;
	private WritableCellFormat timesBoldUnderline;
	//inne kolory:
	private WritableCellFormat defCellFormatBoldRed;
	
	private String outputFilePath;
	private ClusteringExtended dataCore;
	public boolean succeed;
	public String errorMsg;
	
	private boolean success = false;
	
	public ExcelWriter() {
		succeed = false;
		errorMsg = "";
	}
	
	/**
	 * Kontruktor obiektu klasy ExcelWriter.
	 * @param mode int - jaki tym dokumentu Excela tworzymy
	 * @param data ClusteringExtended - dane klastrowania
	 * @param path String - �cie�ka do pliku
	 */
	public ExcelWriter(int mode, ClusteringExtended data, String path) {
		this();
		if(mode == 0) {
			dataCore = data;
			outputFilePath = path;
			makeClusteringFile();
			success = true;
		}
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	/**
	 * G��wna metoda do tworzenia arkusza w formacie .xls dla danego klastrowania
	 */
	public void makeClusteringFile() {
		try {
			File excelFile = new File(outputFilePath);
			WorkbookSettings wbSettings = new WorkbookSettings(); //black magic here
			wbSettings.setLocale(new Locale("en", "EN")); //hmmmm
			
			initiateFonts();
			WritableWorkbook workbook = Workbook.createWorkbook(excelFile, wbSettings);
			
			
			workbook.createSheet("MCT", 0);
			for(int i=0; i<dataCore.metaData.clusterNumber; i++) {
				workbook.createSheet("Cluster"+(i+1), (i+1));
			}
			
			//MCT:
			WritableSheet mctSheet = workbook.getSheet(0); //pierwszy arkusz to zawsze to MCT
			setColumns(mctSheet, "mct");
			fillMCTdatasheet(mctSheet);
			
			for(int i=0; i<dataCore.metaData.clusterNumber; i++) {
				WritableSheet clusterSheet = workbook.getSheet(i+1);
				setColumns(clusterSheet, "clusterStd");
				fillClusterDatasheet(clusterSheet, i, true);
			}
			
			//createLabel(mctSheet);
			//createContent(mctSheet);

			workbook.write();
			workbook.close();
			succeed = true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Unable to access file: "+ outputFilePath 
					+"\nPlease make sure it is not open in any other application!", "Error", JOptionPane.ERROR_MESSAGE);
			succeed = false;
		}
	}
	
	/**
	 * Metoda wype�nia kolejne arkusze klastr�w danymi.
	 * @param clusterSheet WritableSheet - kt�ry arkusz
	 * @param clusterIndex int - nr klastra
	 * @throws Exception - p�acze i j�ki jxl'a
	 */
	private void fillClusterDatasheet(WritableSheet clusterSheet, int clusterIndex, boolean extended) throws Exception {
		int rowIndex = 0;
		int transStartRow = 0;
		//int colIndex = 0;
		String txt = "";
		double clusterMSS = dataCore.metaData.clusterMSS[clusterIndex];
		int clusterSize = dataCore.clustersInv.get(clusterIndex).size();
		
		//nag��wek:
		txt = "Cluster #"+(clusterIndex+1)+" ("+dataCore.metaData.algorithmName+"/"
				+dataCore.metaData.metricName+") Size: "+clusterSize; //+" MSS: "+clusterMSS;
		addTextCell(clusterSheet, 0, 0, txt, defCellFormatBold, null);
		
		addTextCell(clusterSheet, 4, 0, getMSSEval(clusterMSS), defCellFormatBold, setMSSEval(clusterMSS));
		addTextCell(clusterSheet, 3, 1, "CLUSTER "+(clusterIndex+1), defCellFormatBoldRed, null);
		addTextCell(clusterSheet, 1, 2, "MCT#:", defCellFormatBold, null);
		addTextCell(clusterSheet, 2, 2, "HowMany?", defCellFormatBold, null);
		addTextCell(clusterSheet, 3, 2, "Size:", defCellFormatBold, null);
		addTextCell(clusterSheet, 4, 2, "Meaning", defCellFormatBold, null);
		
		ArrayList<Integer> mctInCluster = dataCore.getMCTFrequencyInCluster(clusterIndex);
		rowIndex = 3;
		for(int i=0; i<mctInCluster.size(); i++) { //tabelka zbior�w MCT
			if(mctInCluster.get(i) > 0) {
				addTextCell(clusterSheet, 1, rowIndex, "MCT "+(i+1), defCellFormat, null);
				addIntCell(clusterSheet, 2, rowIndex, mctInCluster.get(i), defCellFormat,
						setColour(mctInCluster.get(i), clusterSize));
				addIntCell(clusterSheet, 3, rowIndex, dataCore.mctSets.get(i).size(), defCellFormat,
						null);
				Formula form = new Formula(4, rowIndex, "MCT!C"+(i+2));
				clusterSheet.addCell(form);
				rowIndex++;
			}
		}
		
		rowIndex++;
		addTextCell(clusterSheet, 1, rowIndex, "Trans.#:", defCellFormatBold, null);
		addTextCell(clusterSheet, 2, rowIndex, "Freq.:", defCellFormatBold, null);
		addTextCell(clusterSheet, 3, rowIndex, "Fired:", defCellFormatBold, null);
		addTextCell(clusterSheet, 4, rowIndex, "Transition name:", defCellFormatBold, null);
		transStartRow = rowIndex;
		rowIndex++;
		int[][] transInCluster = dataCore.getTransitionFrequencyNoMCT(clusterIndex, mctInCluster);
		
		for(int trans=0; trans<dataCore.transNames.length; trans++) { //tabelka tranzycji
			if(transInCluster[0][trans]>0) {
				addTextCell(clusterSheet, 1, rowIndex, "Trans. "+trans, defCellFormat, null);
				addIntCell(clusterSheet, 2, rowIndex, transInCluster[0][trans], defCellFormat,
						setColour(transInCluster[0][trans], clusterSize));
				addIntCell(clusterSheet, 3, rowIndex, transInCluster[1][trans], defCellFormat,
						null);
				addTextCell(clusterSheet, 4, rowIndex, dataCore.transNames[trans], defCellFormat, null);
				rowIndex++;
			}
		}
		if(extended) {
			addTextCell(clusterSheet, 6, transStartRow-1, "Real firing of MCT transitions", defCellFormatBold, null);
			addTextCell(clusterSheet, 6, transStartRow, "Trans.#:", defCellFormatBold, null);
			addTextCell(clusterSheet, 7, transStartRow, "from MCT:", defCellFormatBold, null);
			addTextCell(clusterSheet, 8, transStartRow, "Freq:", defCellFormatBold, null);
			addTextCell(clusterSheet, 9, transStartRow, "Fired:", defCellFormatBold, null);
			addTextCell(clusterSheet, 10, transStartRow, "Transition name:", defCellFormatBold, null);
			transStartRow++;
			for(int trans=0; trans<dataCore.transNames.length; trans++) { //tabelka tranzycji
				if(transInCluster[2][trans]>0) {
					addTextCell(clusterSheet, 6, transStartRow, "Trans. "+trans, defCellFormat, null);
					addTextCell(clusterSheet, 7, transStartRow, "MCT "+transInCluster[3][trans], defCellFormat, null);
					addIntCell(clusterSheet, 8, transStartRow, mctInCluster.get(transInCluster[3][trans]-1)
							, defCellFormat
							, setColour( mctInCluster.get(transInCluster[3][trans]-1), clusterSize));
					addIntCell(clusterSheet, 9, transStartRow, transInCluster[2][trans], defCellFormat,
							null);
					addTextCell(clusterSheet, 10, transStartRow, dataCore.transNames[trans], defCellFormat, null);
					transStartRow++;
				}
			}
		}
		if(transStartRow > rowIndex)
			rowIndex = transStartRow;
		rowIndex++;
		
		addTextCell(clusterSheet, 1, rowIndex, "Inv.#:", defCellFormatBold, null);
		addTextCell(clusterSheet, 2, rowIndex, "MCT:", defCellFormatBold, null);
		addTextCell(clusterSheet, 4, rowIndex, "Transitions:", defCellFormatBold, null);
		rowIndex++;
		
		for(int inv=0; inv<dataCore.clustersInv.get(clusterIndex).size(); inv++) { //tabelka inwariant�w
			int invNo = dataCore.clustersInv.get(clusterIndex).get(inv);
			ArrayList<String> invArray = dataCore.getNormalizedInvariant(invNo);
			String nr = invArray.get(0);
			String mct = invArray.get(1);
			String transitions = "";
			for(int i=2; i<invArray.size(); i++)
			{
				transitions += invArray.get(i) + "  ;  ";
			}
			
			addTextCell(clusterSheet, 1, rowIndex, ""+nr, defCellFormat, null);
			addTextCell(clusterSheet, 2, rowIndex, mct, defCellFormat, null);
			addTextCell(clusterSheet, 4, rowIndex, transitions, defCellFormat, null);
			rowIndex++;
		}
		
		
	}
	
	private String getMSSEval(double clusterMSS) {
        DecimalFormat df = new DecimalFormat("#.###");
        String txt = "MSS: ";
        txt += df.format(clusterMSS);
        
        if(clusterMSS > 0.80)
        	txt += " (very strong structure)";
        else if(clusterMSS > 0.60)
        	txt += " (solid structure)";
        else if(clusterMSS > 0.45)
        	txt += " (regular structure)";
        else if(clusterMSS > 0.25)
        	txt += " (weak structure)";
        else if(clusterMSS == 0.0)
        	txt += " (single element structure?)";
        else if(clusterMSS <= 0.25)
        	txt += " (no significant structure)";
        
        
		return txt;
	}

	private Colour setMSSEval(double clusterMSS) {
		if (clusterMSS > 0.8) return Colour.GREEN;
		else if (clusterMSS > 0.60) return Colour.SEA_GREEN;
		else if (clusterMSS > 0.45) return Colour.YELLOW;
		else if (clusterMSS > 0.25) return Colour.LIGHT_ORANGE;
		else if (clusterMSS <= 0.25) return Colour.RED;
		return Colour.WHITE;
	}

	/**
	 * Metoda zwraca kolor dla t�a kom�rki w zale�no�ci od warto�ci I wzgl�dem II
	 * @param meanValue int - warto�� w kom�rce
	 * @param maxValue int - warto�� referencyjna, maksymalna
	 * @return Colour - obiekt z klasy kolekcji kolor�w (Excel2003)
	 */
	private Colour setColour(int meanValue, int maxValue){
		/*
		double pie = ((double)meanValue/(double)maxValue)*100;
		double R = (255 * pie) / 100;
		double G = (255 * (100 - pie)) / 100; 
		double B = 0;
		Colour customColor = new Colour(10000, "1", (int)R, (int)G, (int)B){ };
		return customColor;
		*/
		double pie = (double)meanValue/(double)maxValue;
		
		if (pie == 1) return Colour.DARK_GREEN;
		else if (pie >= 0.80) return Colour.GREEN;
		else if (pie >= 0.60) return Colour.SEA_GREEN ; //bright_green
		else if (pie >= 0.45) return Colour.YELLOW;
		else if (pie > 0.25) return Colour.LIGHT_ORANGE;
		else if (pie <= 0.25) return Colour.RED;
		
		return Colour.WHITE;

		
	}
	
	/**
	 * Metoda odpowiedzialna za wype�nienie arkusza zbior�w MCT danymi.
	 * @param mctSheet WritableSheet - zak�ada tworzonego arkusza Excel
	 * @throws Exception - co� si� zepsu�o, p�acze i j�ki jxl'a
	 */
	private void fillMCTdatasheet(WritableSheet mctSheet) throws Exception {
		int rowIndex = 0;
		addTextCell(mctSheet, 0, rowIndex, "MCT #:", defCellFormatBold, null);
		addTextCell(mctSheet, 1, rowIndex, "Size:", defCellFormatBold, null);
		addTextCell(mctSheet, 2, rowIndex, "Meaning:", defCellFormatBold, null);
		rowIndex++;
		for(int mctIndex=0; mctIndex<dataCore.metaData.MCTnumber; mctIndex++) {
			int mctSize = dataCore.mctSets.get(mctIndex).size();
			addIntCell(mctSheet, 0, rowIndex, mctIndex+1, defCellFormat, null);
			addIntCell(mctSheet, 1, rowIndex, mctSize, defCellFormat, null);
			addTextCell(mctSheet, 2, rowIndex, "default meaning for mct no. "+(mctIndex+1), defCellFormatItalic, null);
			rowIndex++;
		}
		
		//IV kolumna - rozpiska MCT
		rowIndex++;
		addTextCell(mctSheet, 2, rowIndex++, "MCT sets composition:", defCellFormatBold, null);
		rowIndex++;
		for(int mctIndex=0; mctIndex<dataCore.metaData.MCTnumber; mctIndex++) {
			int mctSize = dataCore.mctSets.get(mctIndex).size();
			addTextCell(mctSheet, 2, rowIndex++, "MCT#"+(mctIndex+1)+ " Size: "+mctSize, defCellFormatBold, null);
			
			for(int mctElIndex=0; mctElIndex < mctSize; mctElIndex++) {
				String txt = "";
				int elementIndex = dataCore.mctSets.get(mctIndex).get(mctElIndex);
				txt = dataCore.transNames[elementIndex];
				addTextCell(mctSheet, 2, rowIndex++, txt, defCellFormat, null);
				
			}
			addTextCell(mctSheet, 2, rowIndex++, "", defCellFormat, null);
		} 
	}

	/**
	 * Metoda ustawia szeroko�ci kolumn dla odpowiednich klas arkuszy.
	 * @param sheet WritableSheet - obiekt arkusza
	 * @param type String - jaki typ arkusza
	 */
	private void setColumns(WritableSheet sheet, String type) {
		// setColumnView(ind_col, width) width: ka�da 1 to: 9px / 0.56czego�-tam
		if(type.equals("mct")) {
			sheet.setColumnView(0, 12); //nr MCT
			sheet.setColumnView(1, 12); //ile tranz
			sheet.setColumnView(2, 35); //znaczenie
			//sheet.setColumnView(3, 35); //nazwy tranzycji
		} else if(type.equals("clusterStd")) {
			sheet.setColumnView(0, 18); // zarys, nazwa, info, etc
			sheet.setColumnView(1, 12); // numery mct, tranzycji, inw.
			sheet.setColumnView(2, 12); // liczno��
			sheet.setColumnView(3, 10); // liczno�� (real)
			sheet.setColumnView(4, 41); //nazwy tranzycji
			sheet.setColumnView(7, 12); //mct extended

		}
	}

	/**
	 * Metoda odpowiedzialna za utworzenie styl�w wype�niania kom�rek dla obiektu klasy.
	 * @throws WriteException - je�li co� nie zadzia�a
	 */
	private void initiateFonts() throws WriteException {
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		defCellFormat = new WritableCellFormat(times10pt);
		//defCellFormat.setWrap(true);
		
		WritableFont times10ptBold = new WritableFont(WritableFont.TIMES, 10);
		times10ptBold.setBoldStyle(WritableFont.BOLD);
		defCellFormatBold = new WritableCellFormat(times10ptBold);
		//defCellFormatBold.setWrap(true);
		
		WritableFont times10ptItalic = new WritableFont(WritableFont.TIMES, 10);
		times10ptItalic.setItalic(true);
		defCellFormatItalic = new WritableCellFormat(times10ptBold);
		//defCellFormatItalic.setWrap(true);
		
		WritableFont times10ptBoldUnderline = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD, false, UnderlineStyle.SINGLE);
		timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
		//timesBoldUnderline.setWrap(true);
		
		//Inne kolory:
		WritableFont times10ptBoldRed = new WritableFont(WritableFont.TIMES, 10);
		times10ptBoldRed.setBoldStyle(WritableFont.BOLD);
		times10ptBoldRed.setColour(Colour.RED);
		defCellFormatBoldRed = new WritableCellFormat(times10ptBoldRed);
		
		//Aktywacja czcionek:
		CellView cv = new CellView();
		cv.setFormat(defCellFormat);
		cv.setFormat(defCellFormatBold);
		cv.setFormat(defCellFormatBoldRed);
		cv.setFormat(defCellFormatItalic);
		cv.setFormat(timesBoldUnderline);
		cv.setAutosize(true);
	}

	private void addIntCell(WritableSheet sheet, int column, int row, Integer value, WritableCellFormat format, Colour col) throws WriteException, RowsExceededException {
		if(col != null) {
			WritableFont cellFont = (WritableFont) format.getFont();
			WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
			cellFormat.setBackground(col);

			Number cell = new Number(column, row, value, cellFormat);
			sheet.addCell(cell);
		} else {
			Number cell = new Number(column, row, value, format);
			sheet.addCell(cell);
		}
	}
	
	@SuppressWarnings("unused")
	private void addDoubleCell(WritableSheet sheet, int column, int row, Double value, WritableCellFormat format, Colour col) throws WriteException, RowsExceededException {
		if(col != null) {
			WritableFont cellFont = (WritableFont) format.getFont();
			WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
			cellFormat.setBackground(col);

			Number cell = new Number(column, row, value, cellFormat);
			sheet.addCell(cell);
		} else {
			Number cell = new Number(column, row, value, format);
			sheet.addCell(cell);
		}
		//Number cell = new Number(column, row, value, format);
		//sheet.addCell(cell);
	}

	private void addTextCell(WritableSheet sheet, int column, int row, String s, WritableCellFormat format, Colour col) throws WriteException, RowsExceededException {
		if(col != null) {
			WritableFont cellFont = (WritableFont) format.getFont();
			WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
			cellFormat.setBackground(col);

			Label label = new Label(column, row, s, cellFormat);
			sheet.addCell(label);
		} else {
			Label label = new Label(column, row, s, format);
			sheet.addCell(label);
		}
		//Label label = new Label(column, row, s, format);
		//sheet.addCell(label);
	}
}