/**
 * 
 */
package holmes.utilities;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;

/**
 * Klasa narzędziowa, odpowiednik klasy statycznej w <b>NORMALNYM</b> języku programowania.
 */
public final class Tools {
	public static String lastExtension = "";
	private static LanguageManager lang = GUIManager.getLanguageManager();
	private static GUIManager overlord = GUIManager.getDefaultGUIManager();
	
	/**
	 * Prywatny konstruktor. To powinno załatwić problem obiektów.
	 */
	private Tools() {
	}
	
	/**
	 * Metoda pomocnicza służąca do kopiowania pliku z pierwszej lokalizacji w drugą.
	 * @param source String - ścieżka do pliku kopiowanego
	 * @param target String - ścieżka do miejsca gdzie kopiujemy
	 * @throws IOException - się zepsuło się...
	 */
	public static void copyFileByPath(String source, String target) throws IOException{
    	InputStream inStream;
    	OutputStream outStream;
   	    File file1 =new File(source);
   	    File file2 =new File(target);
   	    inStream = new FileInputStream(file1);
   	    outStream = new FileOutputStream(file2);
 
   	    byte[] buffer = new byte[1024];
   	    int length;
   	    while ((length = inStream.read(buffer)) > 0){
   	    	outStream.write(buffer, 0, length);
   	    }

		inStream.close();
		outStream.close();
    }
	
	/**
	 * Metoda kopiująca plik źródłowy do docelowego.
	 * @param source File - plik do kopiowania
	 * @param target File - plik który zastępujemy kopiowanym
	 */
	public static void copyFileDirectly(File source, File target) {
    	InputStream inStream;
    	OutputStream outStream;
   	    try {
			inStream = new FileInputStream(source);
			outStream = new FileOutputStream(target);
			byte[] buffer = new byte[1024];
			
	   	    int length;
	   	    while ((length = inStream.read(buffer)) > 0){
	   	    	outStream.write(buffer, 0, length);
	   	    }

			inStream.close();
			outStream.close();
		} catch (IOException e) {
			overlord.log(lang.getText("LOGentry00434exception"), "error", true);
		}
    }
	
	/**
	 * Okno dialogowe do wskazywania pliku.
	 * @param lastPath String - ostatnia otwarta lokalizacja
	 * @param filter FileFilter[] - filtry plików, pierwszy - domyślny
	 * @param buttonText String - tekst na przycisku akceptacji
	 * @param buttonToolTip String - tekst wyjaśnienia dla przycisku akceptacji
	 * @return String - ścieżka do pliku
	 */
	public static String selectFileDialog(String lastPath, FileFilter[] filter, 
			String buttonText, String buttonToolTip, String suggestedFileName) {
		String resultPath;
		JFileChooser fc;
		if(lastPath == null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		fc.setFileView(new HolmesFileView());

		for (FileFilter fileFilter : filter) {
			fc.addChoosableFileFilter(fileFilter);
		}
		fc.setFileFilter(filter[0]);

		if(!buttonText.isEmpty())
			fc.setApproveButtonText(buttonText);
		if(!buttonToolTip.isEmpty())
			fc.setApproveButtonToolTipText(buttonToolTip);
		//TODO:
		if(!suggestedFileName.isEmpty()) { //sugerowana nazwa pliku
			fc.setSelectedFile(new File(suggestedFileName));
		}
		
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
	
	public static String selectNetSaveFileDialog(String lastPath, FileFilter[] filter, 
			String buttonText, String buttonToolTip, String suggestedFileName) {
		String resultPath;
		JFileChooser fc;
		if(lastPath == null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		fc.setFileView(new HolmesFileView());

		for (FileFilter fileFilter : filter) {
			fc.addChoosableFileFilter(fileFilter);
		}
		//TODO: detekcja domyślnego filtra
		fc.setFileFilter(filter[0]);

		if(!buttonText.isEmpty())
			fc.setApproveButtonText(buttonText);
		if(!buttonToolTip.isEmpty())
			fc.setApproveButtonToolTipText(buttonToolTip);
		
		if(!suggestedFileName.isEmpty()) { //sugerowana nazwa pliku
			fc.setSelectedFile(new File(suggestedFileName));
		}
		
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
	 * Metoda wyświetla okno dialogowe dla wskazania katalogu.
	 * @param lastPath String - ostatnia otwarta ścieżka dostępu
	 * @param buttonText String - tekst na przycisku akceptacji
	 * @param buttonToolTip String - tekst wyjaśnienia dla przycisku akceptacji
	 * @return boolean - true, jeśli wskazano jakikolwiek katalog, false w przeciwnym
	 * 		wypadku
	 */
	public static String selectDirectoryDialog(String lastPath, String buttonText, String buttonToolTip) {
		String resultDir = "";
		JFileChooser fc;
		if(lastPath == null)
			fc = new JFileChooser();
		else if(lastPath.isEmpty())
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		fc.setFileView(new HolmesFileView());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		
		if(!buttonText.isEmpty())
			fc.setApproveButtonText(buttonText);
		if(!buttonToolTip.isEmpty())
			fc.setApproveButtonToolTipText(buttonToolTip);
		
		int returnVal = fc.showDialog(fc, fc.getApproveButtonText());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			resultDir = fc.getSelectedFile().getPath();
		} 
		return resultDir;
	}
	
	/**
	 * Zwraca tylko ścieżkę dostępu do pliku zakończoną "\\".
	 * @param x File - plik
	 * @return String - ścieżka do katalogu
	 */
	public static String getFilePath(File x) {
		if(x == null)
			return null;
		
		String absolutePath = x.getAbsolutePath();
		String filePath = absolutePath. substring(0, absolutePath.lastIndexOf(File.separator));
		return filePath + "\\";
	}
	
	public static String getFileName(File x) {
		if(x == null)
			return null;
		
		String absolutePath = x.getAbsolutePath();
		String filePath = absolutePath. substring(absolutePath.lastIndexOf(File.separator));
		return filePath + "\\";
	}
	
	public static boolean ifExist(String path) {
		File tmp = new File(path);
		return tmp.exists();
	}

	/*
	 * Zestaw poniższych metod zapewnia bezpieczny sposób pobierania plików z zasobów.
	 * Innymi słowy program się nie wyłoży tylko dlatego, że ikonki zabrakło. Ikony i
	 * inne obrazki programu są natomiast w zasobach (a nie w zwyczajnym katalogu)
	 * dlatego, aby zapewnią np. bezproblemowe otrzymanie wykonywalnego pliku JAR.
	 */
	/**
	 * Pobiera z zasobów ikonę o podanej nazwie. Albo zwraca awaryjną.
	 * @param resPath String - ścieżka do zasobów
	 * @return ImageIcon - ikona obrazek z zasobów
	 */
	@SuppressWarnings("ConstantConditions")
	public static ImageIcon getResIcon16(String resPath) {
		ImageIcon icon;
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
	 * Pobiera z zasobów ikonę o podanej nazwie. Albo zwraca awaryjną.
	 * @param resPath String - ścieżka do zasobów
	 * @return ImageIcon - ikona obrazek z zasobów
	 */
	@SuppressWarnings("ConstantConditions")
	public static ImageIcon getResIcon22(String resPath) {
		ImageIcon icon;
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
	 * Pobiera z zasobów ikonę o podanej nazwie. Albo zwraca awaryjną.
	 * @param resPath String - ścieżka do zasobów
	 * @return ImageIcon - ikona obrazek z zasobów
	 */
	@SuppressWarnings("ConstantConditions")
	public static ImageIcon getResIcon32(String resPath) {
		ImageIcon icon;
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
	 * Pobiera z zasobów ikonę o podanej nazwie. Albo zwraca awaryjną.
	 * @param resPath String - ścieżka do zasobów
	 * @return ImageIcon - ikona obrazek z zasobów
	 */
	@SuppressWarnings("ConstantConditions")
	public static ImageIcon getResIcon48(String resPath) {
		ImageIcon icon;
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
	 * TO JEST KURWA CHORE. ImageIcon może być spokojnie przechowywany w Jar i getResource
	 * bez problemu znajduje do niego dostęp, ale już Image - ZA CHOLERĘ. W Eclipse zadziała,
	 * w exporcie do Jara - wywali cały program nieobsługiwalnym wyjątkiem (catch (Exception e)
	 * można sobie wsadzić gdzie Słońce nie dochodzi). Metoda obchodzi ten problem, ale
	 * nazwanie tego partyzantką to niedopowiedzenie.
	 * Oto metoda. Klękajcie narody, kosztowała 2 godziny pracy, jak zawsze wynik to kilka linii.
	 * TYCH linii.
	 * @param resPath String - ścieżka do source katalogu zasobów
	 * @return Image - obiekt kurwa klasy Image, niegodny Jar'a jak się okazuje
	 */
	@SuppressWarnings("ConstantConditions")
	public static Image getImageFromIcon(String resPath) {
		resPath = resPath.toLowerCase();
		ImageIcon icon;
		Image result;
		try {
			icon = new ImageIcon(Tools.class.getResource(resPath));
			result = icon.getImage();
		} catch (Exception e) {
			try {
				//DWIE-TRZY PIERD... LINIJKI KOSZTOWAŁY MNIE PRAWIE 2 GODZINY SZUKANIA PO NECIE
				//Po Eclipsem oczywiście getClass().getResource działa jak marzenie
				//przy eksporcie do Jar, okazuje się że nic nie działa a wyjątek który
				//wyskakuje jest NIEMOŻLIWE DO OBSŁUŻENIA przez nawet catch (Exception e )
				//po prostu k... super, kochamy Jave
				//linijki, zastąpione dalej podobną formą:
				//ImageIcon x = Tools.getResIcon16("/icons/blackhole.png"); 
				//Image y = x.getImage();
				//frame.setIconImage(y);
				
				icon = new ImageIcon(Tools.class.getResource("/nullIcon16.png"));
				result = icon.getImage(); //geniusz, zaiste geniusz to wymyślił w Javie...
			} catch (Exception e2) {
				overlord.log(lang.getText("LOGentry00435exception")+"\n"+e2.getMessage(), "error", true);
				return null;
			}
		}
		return result;
	}
	
	/**
	 * Metoda wstawia spacje przed napis, aby rozszerzyć go do odpowiedniej długości.
	 * @param source String - łańcuch do rozszerzenia
	 * @param size int - rozmiar do rozszerzenia
	 * @param left boolean - true jeśli dodajemy space od lewej strony napisu
	 * @return String - łańcuch wynikowy
	 */
	public static String setToSize(String source, int size, boolean left) {
		if(size <= source.length())
			return source;
		
		int oldSize = source.length();
		StringBuilder sourceBuilder = new StringBuilder(source);
		for(int i = 0; i<size-oldSize; i++) {
			if(left)
				sourceBuilder.insert(0, " ");
			else
				sourceBuilder.append(" ");
		}
		source = sourceBuilder.toString();

		return source;
	}
	
	/**
	 * Metoda formatuje liczbę typu double do wyznaczonej liczby miejsc po przecinku, a następnie
	 * zwraca ją jako String.
	 * @param value - double; liczba do przycięcia
	 * @return String - reprezentacja liczby
	 */
	public static String cutValue(double value) {
    	DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(GUIManager.getDefaultGUIManager().getLocale());
    	otherSymbols.setDecimalSeparator('.');
    	DecimalFormat df = new DecimalFormat("#.##", otherSymbols);
		return df.format(value);
	}
	
	/**
	 * Metoda formatuje liczbę typu double do wyznaczonej liczby miejsc po przecinku, a następnie
	 * zwraca ją jako String.
	 * @param value double - liczba
	 * @param howMany int - ile miejsc po przecinku (0 - 8)
	 * @return String - liczba
	 */
	public static String cutValueExt(double value, int howMany) {
		NumberFormat formatter = DecimalFormat.getInstance();
		formatter.setMinimumFractionDigits(2);
		formatter.setMaximumFractionDigits(howMany);
		formatter.setRoundingMode(RoundingMode.HALF_UP);

		//String result = formatter.format(value);
		//result = result.replace(",", ".");
		return formatter.format(value).replace(",", ".");
		/*
		String format = "#.";
		for(int i=0; i<howMany; i++)
			format += "#";
		
		if(howMany < 1)
			format = "#";
		if(howMany > 8)
			format = "#.########";
		
    	DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(GUIManager.getDefaultGUIManager().getLocale());
    	otherSymbols.setDecimalSeparator('.');
    	DecimalFormat df = new DecimalFormat(format, otherSymbols);
		return df.format(value);
		 */
	}
	
	public static int absolute(int i) {
		if (i < 0)
			return -i;
		else
			return i;
	}
	
	/**
	 * Metoda sprawdza, czy można kontynuować operację jeśli plik istnieje.
	 * @param selectedFilePath String - ścieżka do pliku
	 * @return boolean - true, jeśli można kontynuować
	 */
	public static boolean overwriteDecision(String selectedFilePath) {
		File file = new File(selectedFilePath);
		if(file.exists()) {
			String name = selectedFilePath;
			int ind = name.lastIndexOf("\\");
			if(ind > 1) {
				name = name.substring(ind+1);
				Object[] options = {"Yes", "No",};
				int n = JOptionPane.showOptionDialog(null,
								"File "+name+" already exists.\nDo you want to overwrite it?",
								"File exists", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				if (n == 1) // NO
					return false;
				else //YES, overwrite
					return true;
			}
		} 
		
		return true; //no file, continue
	}
	
	/**
	 * Metoda koduje znaki specjalne (np. spacja, enter)
	 * @param line String - oryginalna linia
	 * @return String - zakodowana linia
	 */
	public static String convertToCode(String line) {
		line = line.replace("\n", "#5475");
		line = line.replace(" ", "&nbsp;");
		return line;
	}
	
	/**
	 * Metoda dekoduje znaki specjalne (np. spacja, enter)
	 * @param line String - przeczytana linia
	 * @return String - odkodowana linia
	 */
	public static String decodeString(String line) {
		line = line.replace("#5475", "\n");
		line = line.replace("&nbsp;", " ");
		return line;
	}
	
	@SuppressWarnings("unused")
	private static long binomial(int n, int k) {
        if (k>n-k)
            k=n-k;
 
        long b=1;
        for (int i=1, m=n; i<=k; i++, m--)
            b=b*m/i;
        return b;
    }

	/**
	 * Zwraca czas w formie łańcucha znaków HH:MM:SS dla wartości w milisekundach.
	 * @param milisecond (<b>long</b>) czas w milisekundach.
	 * @return (<b>String</b>) czas w formacie HH:MM:SS
	 */
	public static String getTime(long milisecond) {
		long seconds = milisecond /= 1000;
		long hours = seconds / 3600;
		String h = hours+"";
		if(h.length() == 1)
			h = "0" + h;

		seconds = seconds - (hours * 3600);
		long minutes = seconds / 60;
		String m = minutes+"";
		if(m.length() == 1)
			m = "0" + m;

		seconds = seconds - (minutes * 60);
		String s = seconds+"";
		if(s.length() == 1)
			s = "0" + s;

		return h + ":" + m + ":" + s;
	}
}
