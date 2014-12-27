package abyss.files.clusters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import abyss.clusters.ClusteringExtended;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
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
	
	private String outputFilePath;
	private ClusteringExtended dataCore;
	public boolean succeed;
	public String errorMsg;
	
	public ExcelWriter() {
		succeed = false;
		errorMsg = "";
	}
	
	public ExcelWriter(int mode, ClusteringExtended data, String path) {
		this();
		if(mode == 0) {
			dataCore = data;
			outputFilePath = path;
			makeClusteringFile();
		}
	}
	
	/**
	 * G³ówna metoda do tworzenia arkusza w formacie .xls dla danego klastrowania
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
				fillClusterDatasheet(clusterSheet, i);
			}
			
			//createLabel(mctSheet);
			//createContent(mctSheet);

			workbook.write();
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		succeed = true;
	}
	
	
	private void fillClusterDatasheet(WritableSheet clusterSheet, int clusterIndex) throws Exception {
		int rowIndex = 0;
		int colIndex = 0;
		String txt = "";
		double clusterMSS = dataCore.metaData.clusterMSS[clusterIndex];
		int clusterSize = dataCore.clustersInv.get(clusterIndex).size();
		
		//nag³ówek:
		txt = "Cluster #"+(clusterIndex+1)+" ("+dataCore.metaData.algorithmName+"/"
				+dataCore.metaData.metricName+") Size: "+clusterSize+" MSS: "+clusterMSS;
		addTextCell(clusterSheet, 0, 0, txt, defCellFormatBold);
		
		//MCT
		ArrayList<Integer> res = dataCore.getMCTFrequencyInCluster(clusterIndex);
		int[][] res2 = dataCore.getTransitionFrequencyNoMCT(clusterIndex, res);
		ArrayList<String> inv1 = dataCore.getNormalizedInvariant(39);
		ArrayList<String> inv2 = dataCore.getNormalizedInvariant(181);
		ArrayList<String> inv3 = dataCore.getNormalizedInvariant(338);
		ArrayList<String> inv4 = dataCore.getNormalizedInvariant(386);
	}

	/**
	 * Metoda odpowiedzialna za wype³nienie arkusza zbiorów MCT danymi.
	 * @param mctSheet WritableSheet - zak³ada tworzonego arkusza Excel
	 * @throws Exception - coœ siê zepsu³o..
	 */
	private void fillMCTdatasheet(WritableSheet mctSheet) throws Exception {
		int rowIndex = 0;
		addTextCell(mctSheet, 0, rowIndex, "MCT #:", defCellFormatBold);
		addTextCell(mctSheet, 1, rowIndex, "Size:", defCellFormatBold);
		addTextCell(mctSheet, 2, rowIndex, "Meaning:", defCellFormatBold);
		rowIndex++;
		for(int mctIndex=0; mctIndex<dataCore.metaData.MCTnumber; mctIndex++) {
			int mctSize = dataCore.mctSets.get(mctIndex).size();
			addIntCell(mctSheet, 0, rowIndex, mctIndex+1, defCellFormat);
			addIntCell(mctSheet, 1, rowIndex, mctSize, defCellFormat);
			addTextCell(mctSheet, 2, rowIndex, "default meaning for mct no. "+(mctIndex+1), defCellFormatItalic);
			rowIndex++;
		}
		
		//IV kolumna - rozpiska MCT
		rowIndex++;
		addTextCell(mctSheet, 2, rowIndex++, "MCT sets composition:", defCellFormatBold);
		rowIndex++;
		for(int mctIndex=0; mctIndex<dataCore.metaData.MCTnumber; mctIndex++) {
			int mctSize = dataCore.mctSets.get(mctIndex).size();
			addTextCell(mctSheet, 2, rowIndex++, "MCT#"+(mctIndex+1)+ " Size: "+mctSize, defCellFormatBold);
			
			for(int mctElIndex=0; mctElIndex < mctSize; mctElIndex++) {
				String txt = "";
				int elementIndex = dataCore.mctSets.get(mctIndex).get(mctElIndex);
				txt = dataCore.transNames[elementIndex];
				addTextCell(mctSheet, 2, rowIndex++, txt, defCellFormat);
				
			}
			addTextCell(mctSheet, 2, rowIndex++, "", defCellFormat);
		} 
	}

	private void setColumns(WritableSheet sheet, String type) {
		// setColumnView(ind_col, width) width: ka¿da 1 to: 9px / 0.56czegoœ-tam
		if(type.equals("mct")) {
			sheet.setColumnView(0, 12); //nr MCT
			sheet.setColumnView(1, 12); //ile tranz
			sheet.setColumnView(2, 35); //znaczenie
			//sheet.setColumnView(3, 35); //nazwy tranzycji
		} else if(type.equals("clusterStd")) {
			sheet.setColumnView(0, 18); // zarys, nazwa, info, etc
			sheet.setColumnView(1, 10);
			sheet.setColumnView(2, 41); //nazwy tranzycji
			sheet.setColumnView(3, 5); //odstêp
			//reszta standardowo
			//sheet.setColumnView(4, 18);
		}
	}

	/**
	 * Metoda odpowiedzialna za utworzenie stylów wype³niania komórek dla obiektu klasy.
	 * @throws WriteException - jeœli coœ nie zadzia³a
	 */
	private void initiateFonts() throws WriteException {
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10); // Lets create a times font
		defCellFormat = new WritableCellFormat(times10pt); // Define the cell format
		//defCellFormat.setWrap(true); // Lets automatically wrap the cells
		
		WritableFont times10ptBold = new WritableFont(WritableFont.TIMES, 10);
		times10ptBold.setBoldStyle(WritableFont.BOLD);
		defCellFormatBold = new WritableCellFormat(times10ptBold);
		//defCellFormatBold.setWrap(true);
		
		WritableFont times10ptItalic = new WritableFont(WritableFont.TIMES, 10);
		times10ptItalic.setItalic(true);
		defCellFormatItalic = new WritableCellFormat(times10ptBold);
		//defCellFormatItalic.setWrap(true);
		
		// create create a bold font with unterlines
		WritableFont times10ptBoldUnderline = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD, false, UnderlineStyle.SINGLE);
		timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
		// Lets automatically wrap the cells
		//timesBoldUnderline.setWrap(true);
		
		//defCellFormatBold
		
		CellView cv = new CellView();
		cv.setFormat(defCellFormat);
		cv.setFormat(defCellFormatBold);
		cv.setFormat(defCellFormatItalic);
		cv.setFormat(timesBoldUnderline);
		cv.setAutosize(true);
	}
	
	private void createContent(WritableSheet sheet) throws WriteException, RowsExceededException {
		// Write a few number
		for (int i = 1; i < 10; i++) {
			// First column
			//addNumber(sheet, 0, i, i + 10);
			// Second column
			//addNumber(sheet, 1, i, i * i);
		}
		// Lets calculate the sum of it
		StringBuffer buf = new StringBuffer();
		buf.append("SUM(A2:A10)");
		Formula f = new Formula(0, 10, buf.toString());
		sheet.addCell(f);
		buf = new StringBuffer();
		buf.append("SUM(B2:B10)");
		f = new Formula(1, 10, buf.toString());
		sheet.addCell(f);

		// now a bit of text
		for (int i = 12; i < 20; i++) {
			// First column
			//addLabel(sheet, 0, i, "Boring text " + i);
			// Second column
			//addLabel(sheet, 1, i, "Another text");
		}
	}
	
	private void addCaption(WritableSheet sheet, int column, int row, String s) throws RowsExceededException, WriteException {
		Label label;
		label = new Label(column, row, s, timesBoldUnderline);
		sheet.addCell(label);
	}

	private void addIntCell(WritableSheet sheet, int column, int row, Integer value, WritableCellFormat format) throws WriteException, RowsExceededException {
		Number cell = new Number(column, row, value, format);
		sheet.addCell(cell);
	}
	
	private void addDoubleCell(WritableSheet sheet, int column, int row, Double value, WritableCellFormat format) throws WriteException, RowsExceededException {
		Number cell = new Number(column, row, value, format);
		sheet.addCell(cell);
	}

	private void addTextCell(WritableSheet sheet, int column, int row, String s, WritableCellFormat format) throws WriteException, RowsExceededException {
		Label label = new Label(column, row, s, format);
		sheet.addCell(label);
	}
}