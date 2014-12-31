/**
 * 
 */
package abyss.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * Klasa u¿ytkowa, odpowiednik klasy statycznej w normalnym jêzyku programowania
 * jak np. C#. testing testing
 * @author Rince
 *
 */
public final class Tools {
	/**
	 * Prywatny konstruktor. To powinno za³atwiæ problem obiektów.
	 */
	private Tools() {

	}
	
	/**
	 * Metoda pomocnicza s³u¿¹ca do kopiowania pliku z pierwszej lokalizacji w drug¹.
	 * @param source String - œcie¿ka do pliku kopiowanego
	 * @param target String - œcie¿ka do miejsca gdzie kopiujemy
	 * @throws IOException - siê zepsu³o siê...
	 */
	public static void copyFileByPath(String source, String target) throws IOException{
    	InputStream inStream = null;
    	OutputStream outStream = null;
   	    File file1 =new File(source);
   	    File file2 =new File(target);
   	    inStream = new FileInputStream(file1);
   	    outStream = new FileOutputStream(file2);
 
   	    byte[] buffer = new byte[1024];
   	    int length;
   	    while ((length = inStream.read(buffer)) > 0){
   	    	outStream.write(buffer, 0, length);
   	    }
   	    
   	    if (inStream != null)inStream.close();
   	    if (outStream != null)outStream.close();
    }
	
	/**
	 * Metoda kopiuj¹ca plik Ÿród³owy do docelowego.
	 * @param source File - plik do kopiowania
	 * @param target File - plik który zastêpujemy kopiowanym
	 */
	public static void copyFileDirectly(File source, File target) {
    	InputStream inStream = null;
    	OutputStream outStream = null;
   	    try {
			inStream = new FileInputStream(source);
			outStream = new FileOutputStream(target);
			byte[] buffer = new byte[1024];
			
	   	    int length;
	   	    while ((length = inStream.read(buffer)) > 0){
	   	    	outStream.write(buffer, 0, length);
	   	    }
	   	    
	   	    if (inStream != null)
	   	    	inStream.close();
	   	    if (outStream != null)
	   	    	outStream.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"I/O operation failed for reason unknown. You can now start panicking.\nHave a nice day!",
					"Critical error", JOptionPane.ERROR_MESSAGE);
			return;
		}
    }
	
	/**
	 * Okno dialogowe do wskazywania pliku.
	 * @param lastPath String - ostatnia otwarta lokalizacja
	 * @param filter FileFilter[] - filtry plików, pierwszy - domyœlny
	 * @param buttonText String - tekst na przycisku akceptacji
	 * @param buttonToolTip String - tekst wyjaœnienia dla przycisku akceptacji
	 * @return String - œcie¿ka do pliku
	 */
	public static String selectFileDialog(String lastPath, FileFilter[] filter, 
			String buttonText, String buttonToolTip) {
		String resultPath = "";
		JFileChooser fc;
		if(lastPath == null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);

		for(int i=0; i<filter.length; i++) {
			fc.addChoosableFileFilter(filter[i]);
		}
		fc.setFileFilter(filter[0]);
		
		if(!buttonText.equals(""))
			fc.setApproveButtonText(buttonText);
		if(!buttonToolTip.equals(""))
			fc.setApproveButtonToolTipText(buttonToolTip);
		
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			resultPath = f.getPath();	
		} else {
			resultPath = "";
		}
		return resultPath;
	}
	
	/**
	 * Metoda wyœwietla okno dialogowe dla wskazania katalogu z plikami klastrów
	 * @param lastPath String - ostatnia otwarta œcie¿ka dostêpu
	 * @param buttonText String - tekst na przycisku akceptacji
	 * @param buttonToolTip String - tekst wyjaœnienia dla przycisku akceptacji
	 * @return boolean - true, jeœli wskazano jakikolwiek katalog, false w przeciwnym
	 * 		wypadku
	 */
	public static String selectDirectoryDialog(String lastPath, String buttonText, String buttonToolTip) {
		String resultDir = "";
		JFileChooser fc = null;
		if(lastPath == null)
			fc = new JFileChooser();
		else if(lastPath == "")
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		
		if(!buttonText.equals(""))
			fc.setApproveButtonText(buttonText);
		if(!buttonToolTip.equals(""))
			fc.setApproveButtonToolTipText(buttonToolTip);
		
		int returnVal = fc.showDialog(fc, fc.getApproveButtonText());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			resultDir = fc.getSelectedFile().getPath();
		} 
		return resultDir;
	}

	/*
	 * Zestaw poni¿szych metod zapewnia bezpieczny sposób pobierania plików z zasobów.
	 * Innymi s³owy program siê nie wy³o¿y tylko dlatego, ¿e ikonki zabrak³o. Ikony i
	 * inne obrazki programu s¹ natomiast w zasobach (a nie w zwyczajnym katalogu)
	 * dlatego, aby zapewniæ np. bezproblemowe otrzymanie wykonywalnego pliku JAR.
	 */
	/**
	 * Pobiera z zasobów ikonê o podanej nazwie. Albo zwraca awaryjn.
	 * @param resPath String - œcie¿ka do zasobów
	 * @return ImageIcon - ikona obrazek z zasobów
	 */
	public static ImageIcon getResIcon16(String resPath) {
		ImageIcon icon=null;
		try {
			icon = new ImageIcon(Tools.class.getResource(resPath));
		} catch (Exception e) {
			try {
				icon = new ImageIcon(Tools.class.getResource("/nullIcon16.png"));
			} catch (Exception e2) {
				icon = new ImageIcon();
			}
		}
		return icon;
	}
	
	/**
	 * Pobiera z zasobów ikonê o podanej nazwie. Albo zwraca awaryjn.
	 * @param resPath String - œcie¿ka do zasobów
	 * @return ImageIcon - ikona obrazek z zasobów
	 */
	public static ImageIcon getResIcon22(String resPath) {
		ImageIcon icon=null;
		try {
			icon = new ImageIcon(Tools.class.getResource(resPath));
		} catch (Exception e) {
			try {
				icon = new ImageIcon(Tools.class.getResource("/nullIcon22.png"));
			} catch (Exception e2) {
				icon = new ImageIcon();
			}
		}
		return icon;
	}
	
	/**
	 * Pobiera z zasobów ikonê o podanej nazwie. Albo zwraca awaryjn.
	 * @param resPath String - œcie¿ka do zasobów
	 * @return ImageIcon - ikona obrazek z zasobów
	 */
	public static ImageIcon getResIcon32(String resPath) {
		ImageIcon icon=null;
		try {
			icon = new ImageIcon(Tools.class.getResource(resPath));
		} catch (Exception e) {
			try {
				icon = new ImageIcon(Tools.class.getResource("/nullIcon32.png"));
			} catch (Exception e2) {
				icon = new ImageIcon();
			}
		}
		return icon;
	}
	
	/**
	 * Pobiera z zasobów ikonê o podanej nazwie. Albo zwraca awaryjn.
	 * @param resPath String - œcie¿ka do zasobów
	 * @return ImageIcon - ikona obrazek z zasobów
	 */
	public static ImageIcon getResIcon48(String resPath) {
		ImageIcon icon=null;
		try {
			icon = new ImageIcon(Tools.class.getResource(resPath));
		} catch (Exception e) {
			try {
				icon = new ImageIcon(Tools.class.getResource("/nullIcon48.png"));
			} catch (Exception e2) {
				icon = new ImageIcon();
			}
		}
		return icon;
	}
}
