/**
 * 
 */
package abyss.utilities;

import java.awt.Image;
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
 * Klasa u�ytkowa, odpowiednik klasy statycznej w normalnym j�zyku programowania
 * jak np. C#. testing testing
 * @author Rince
 *
 */
public final class Tools {
	public static String lastExtension = "";
	
	/**
	 * Prywatny konstruktor. To powinno za�atwi� problem obiekt�w.
	 */
	private Tools() {

	}
	
	/**
	 * Metoda pomocnicza s�u��ca do kopiowania pliku z pierwszej lokalizacji w drug�.
	 * @param source String - �cie�ka do pliku kopiowanego
	 * @param target String - �cie�ka do miejsca gdzie kopiujemy
	 * @throws IOException - si� zepsu�o si�...
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
	 * Metoda kopiuj�ca plik �r�d�owy do docelowego.
	 * @param source File - plik do kopiowania
	 * @param target File - plik kt�ry zast�pujemy kopiowanym
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
	 * @param filter FileFilter[] - filtry plik�w, pierwszy - domy�lny
	 * @param buttonText String - tekst na przycisku akceptacji
	 * @param buttonToolTip String - tekst wyja�nienia dla przycisku akceptacji
	 * @return String - �cie�ka do pliku
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
		int returnVal = fc.showDialog(fc, fc.getApproveButtonText());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			resultPath = f.getPath();	
		} else {
			resultPath = "";
		}
		
		lastExtension = fc.getFileFilter().getDescription().toLowerCase();
		return resultPath;
	}
	
	/**
	 * Metoda wy�wietla okno dialogowe dla wskazania katalogu z plikami klastr�w
	 * @param lastPath String - ostatnia otwarta �cie�ka dost�pu
	 * @param buttonText String - tekst na przycisku akceptacji
	 * @param buttonToolTip String - tekst wyja�nienia dla przycisku akceptacji
	 * @return boolean - true, je�li wskazano jakikolwiek katalog, false w przeciwnym
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
	
	/**
	 * Zwraca tylko �cie�k� dost�pu do pliku.
	 * @param x File - plik
	 * @return String - �cie�ka do katalogu
	 */
	public static String getFilePath(File x) {
		if(x == null)
			return null;
		
		String absolutePath = x.getAbsolutePath();
		String filePath = absolutePath. substring(0, absolutePath.lastIndexOf(File.separator));
		return filePath + "\\";
	}

	/*
	 * Zestaw poni�szych metod zapewnia bezpieczny spos�b pobierania plik�w z zasob�w.
	 * Innymi s�owy program si� nie wy�o�y tylko dlatego, �e ikonki zabrak�o. Ikony i
	 * inne obrazki programu s� natomiast w zasobach (a nie w zwyczajnym katalogu)
	 * dlatego, aby zapewni� np. bezproblemowe otrzymanie wykonywalnego pliku JAR.
	 */
	/**
	 * Pobiera z zasob�w ikon� o podanej nazwie. Albo zwraca awaryjn.
	 * @param resPath String - �cie�ka do zasob�w
	 * @return ImageIcon - ikona obrazek z zasob�w
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
	 * Pobiera z zasob�w ikon� o podanej nazwie. Albo zwraca awaryjn.
	 * @param resPath String - �cie�ka do zasob�w
	 * @return ImageIcon - ikona obrazek z zasob�w
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
	 * Pobiera z zasob�w ikon� o podanej nazwie. Albo zwraca awaryjn.
	 * @param resPath String - �cie�ka do zasob�w
	 * @return ImageIcon - ikona obrazek z zasob�w
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
	 * Pobiera z zasob�w ikon� o podanej nazwie. Albo zwraca awaryjn.
	 * @param resPath String - �cie�ka do zasob�w
	 * @return ImageIcon - ikona obrazek z zasob�w
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
	
	/**
	 * TO JEST KURWA CHORE. ImageIcon mo�e by� spokojnie przechowywany w Jar i getResource
	 * bez problemu znajduje do niego dost�p, ale ju� Image - ZA CHOLER�. W Eclipse zadzia�a,
	 * w exporcie do Jara - wywali ca�y program nieobs�ugiwalnym wyj�tkiem (catch (Exception e))
	 * mo�na sobie wsadzi� gdzie S�o�ce nie dochodzi. Metoda obchodzi ten problem, ale
	 * nazwanie tego partyzantk� do niedopowiedzenie.
	 * Oto metoda. Kl�kajcie narody, kosztowa�a 2 godziny pracy, jak zawsze wynik to kilka linii.
	 * TYCH linii.
	 * @param resPath String - �cie�ka do source katalogu zasob�w
	 * @return Image - obiekt klasy Image, niegodny Jara jak si� okazuje
	 */
	public static Image getImageFromIcon(String resPath) {
		resPath = resPath.toLowerCase();
		ImageIcon icon=null;
		Image result = null;
		try {
			icon = new ImageIcon(Tools.class.getResource(resPath));
			result = icon.getImage();
		} catch (Exception e) {
			try {
				icon = new ImageIcon(Tools.class.getResource("/nullIcon16.png"));
				result = icon.getImage();
			} catch (Exception e2) {
				System.out.println("CRITICAL EXCEPTION IN getImageFromIcon. "
						+ "No FAILSAFE IMAGE: /nullIcon16.png IN JAR");
				return null;
			}
		}
		return result;
	}
	
	/**
	 * Metoda wstawia spacje przez napis, aby rozszerzy� go do odpowiedniej d�ugo�ci.
	 * @param source String - �a�cuch do rozszerzenia
	 * @param size int - rozmiar do rozszerzenia
	 * @param left boolean - true je�li dodajemy space od lewej strony napisu
	 * @return String - �a�cuch wynikowy
	 */
	public static String setToSize(String source, int size, boolean left) {
		if(size <= source.length())
			return source;
		
		int oldSize = source.length();
		for(int i=0; i<size-oldSize; i++) {
			if(left)
				source = " "+source;
			else
				source += " ";
		}
		
		return source;
	}
}
