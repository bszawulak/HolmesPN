package holmes.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import holmes.darkgui.GUIManager;
import holmes.utilities.Tools;
import holmes.workspace.ExtensionFileFilter;

/**
 * Okno wewnętrznego notatnika programu.
 * 
 * @author MR
 */
public class HolmesNotepad extends JFrame {
	private static final long serialVersionUID = 1694133455242675169L;

	private String newline = "\n";
	private StyledDocument doc; //
	private JTextPane textPane; //panel z tekstem -> paneScrollPane
	private JScrollPane paneScrollPane; //panel scrollbar -> editPanel
	
	private JTextArea textArea;
	
	private boolean simpleMode = false;
	/** regular, italic, bold, small, large, warning, error */
	private String[] initStyles = { "regular", "italic", "bold", "small", "large", "warning", "error", "time", "node" };
	
	/**
	 * Główny konstruktor domyślny okna notatnika.
	 */
	private HolmesNotepad() {
		setTitle("Holmes Notepad");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}
	
	/**
	 * Konstruktor tworzący nowe okno o zadanych rozmiarach.
	 * @param width int - szerokość okna
	 * @param height int - wysokość okna
	 */
	public HolmesNotepad(int width, int height) {
		this();
		
		try {
			if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("programUseSimpleEditor").equals("1"))
				simpleMode = true;
		} catch (Exception e) {
			
		}
		setPreferredSize(new Dimension(width, height));
		setLocation(50,50);
		
		JPanel mainPanel = createEditor(width, height);
        setContentPane(mainPanel);
        this.pack();
        setVisible(false); 
	}

	/**
	 * Metoda pomocnica tworząca główne elementy okna notatnika.
	 * @param width int - szerokość okna
	 * @param height int - wysokość okna
	 * @return JPanel - panel główny notatnika
	 */
	private JPanel createEditor(int width, int height) {
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

		main.add(createButtonsPanel(width, height));
		
		if(simpleMode) {
			textArea = new JTextArea();
			textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
			paneScrollPane = new JScrollPane(textArea);
			paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		} else {
			textPane = createTextPane();
	        paneScrollPane = new JScrollPane(textPane);
	        paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		}
		
		main.add(paneScrollPane);
		
		return main;
	}
	
	/**
	 * Tworzy panel przycisków notatnika.
	 * @param width int - szerokość
	 * @param height int - wysokość
	 * @return JPanel - panel, okrętu się pan spodziewałeś?
	 */
	private Component createButtonsPanel(int width, int height) {
		JPanel buttonPanel = new JPanel(null);
		buttonPanel.setMinimumSize(new Dimension(width, 50));
		buttonPanel.setPreferredSize(new Dimension(width,50));
		buttonPanel.setMaximumSize(new Dimension(3000, 50));
		buttonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		//TODO:
		JButton savedButton = new JButton();
		savedButton.setBounds(10, 7, 35, 35);
		savedButton.setMargin(new Insets(0, 0, 0, 0));
		savedButton.setIcon(Tools.getResIcon16("/icons/notepad/saveFile.png"));
		savedButton.setToolTipText("Saves the content of notepad.");
		savedButton.setFocusPainted(false);
		savedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				saveContent();
			}
		});
		buttonPanel.add(savedButton);
		
		JButton loadButton = new JButton();
		loadButton.setBounds(50, 7, 35, 35);
		loadButton.setMargin(new Insets(0, 0, 0, 0));
		loadButton.setIcon(Tools.getResIcon16("/icons/notepad/loadFile.png"));
		loadButton.setToolTipText("Load txt files.");
		loadButton.setFocusPainted(false);
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				loadContent();
			}
		});
		buttonPanel.add(loadButton);
		
		JButton clearButton = new JButton();
		clearButton.setBounds(90, 7, 35, 35);
		clearButton.setMargin(new Insets(0, 0, 0, 0));
		clearButton.setIcon(Tools.getResIcon16("/icons/notepad/eraserIcon.png"));
		clearButton.setToolTipText("Clear content");
		clearButton.setFocusPainted(false);
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				clearContent();
			}
		});
		buttonPanel.add(clearButton);
		
		return buttonPanel;
	}

	protected void clearContent() {
		if(simpleMode) {
			textArea.setText("");
		} else {
			
			int len = textPane.getDocument().getLength();
			try {
				doc.remove(0, len);
			} catch (BadLocationException e) {
				;
			}
		}
	}

	/**
	 * Obsługa wczytywania pliku tekstowego
	 */
	protected void loadContent() {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Holmes notepad text file (.txt)",  new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Load", "Select text file", "");
		
		if(selectedFile.equals("")) {
			return;
			//JOptionPane.showMessageDialog(null, "Incorrect filename or location.", "Operation failed.", JOptionPane.ERROR_MESSAGE);
		} else {
			try {
				clearContent();
				DataInputStream dis = new DataInputStream(new FileInputStream(selectedFile));
				BufferedReader buffer = new BufferedReader(new InputStreamReader(dis));
				
				String line = "";
				//addTextLineNL(line,  "text");
				while((line = buffer.readLine()) != null) {
					addTextLineNL(line,  "text");
				}
				buffer.close();
				setCaretFirstLine();
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log("Reading text file into the notepad failed.", "error", true);
			}
		}
	}

	/**
	 * Zapisuje zawartość notatnika do pliku.
	 */
	protected void saveContent() {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Holmes notepad text file (.txt)",  new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "Select new filename", "");
		
		if(selectedFile.equals("")) {
			return;
		} else {
			String extension = ".txt";
			if(selectedFile.contains(extension) == false)
				selectedFile += extension;
			
			try {
				PrintWriter pw = new PrintWriter(selectedFile);
				if(simpleMode) {
					pw.write(textArea.getText());
				} else {
					int len = textPane.getDocument().getLength();
					pw.write(doc.getText(0, len));
				}
				pw.close();
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log("Notepad saving operation for file "+selectedFile.toString()+" failed.", "error", true);
			}
		}
	}

	/**
	 * Metoda pomocnicza konstruktora, tworzy obiekt edytora.
	 * @return JTextPane - panel edytora
	 */
	private JTextPane createTextPane() {
	    JTextPane txtPane = new JTextPane();
	    doc = txtPane.getStyledDocument();
	    addStylesToDocument(doc);
	    try {
	        //doc.insertString(doc.getLength(), initString, doc.getStyle("regular"));
	    } catch (Exception e) {
	        GUIManager.getDefaultGUIManager().log("Couldn't insert initial text into text pane.","error", true);
	    }
	    return txtPane;
	}
	
	/**
	 * Metoda wpisuje nową linię do okna logów.
	 * @param text String - text do wpisania
	 * @param mode String - tryb pisania
	 * @param time boolean - true, jeśli ma być wyświetlony czas wpisu
	 * @param enter boolean - trye jeśli kończymy enterem
	 */
	public void addText(String text, String mode, boolean time, boolean enter) {
		if(simpleMode) {
			String nL = "";
			if(enter)
				nL = newline;
			
			if(time) {
				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
				textArea.append("["+timeStamp+"]   ");
			}
			textArea.append(text+nL);
		} else {
			int style = setWritingStyle(mode);
			String nL = "";
			if(enter)
				nL = newline;
			try {
				if(time) {
					String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
					doc.insertString(doc.getLength(), "["+timeStamp+"]   ", doc.getStyle("time"));
				}
		        doc.insertString(doc.getLength(), text+nL, doc.getStyle(initStyles[style]));
		    } catch (Exception ble) {
		        GUIManager.getDefaultGUIManager().log("Couldn't insert initial text into text pane.", "error", true);
		    }
			
			int len = textPane.getDocument().getLength();
			textPane.setCaretPosition(len);
		}
	}
	
	/**
	 * Dodaj nową linię do notatnika (bez entera).
	 * @param text String - tekst
	 * @param mode mode - tryb wstawiania
	 */
	public void addTextLine(String text, String mode) {
		if(simpleMode) {
			textArea.append(text);
		} else {
			int style = setWritingStyle(mode);
			try {
		        doc.insertString(doc.getLength(), text, doc.getStyle(initStyles[style]));
		    } catch (Exception ble) {
		        GUIManager.getDefaultGUIManager().log("Couldn't insert initial text into text pane.", "error", true);
		    }
			int len = textPane.getDocument().getLength();
			textPane.setCaretPosition(len);
		}
	}
	
	/**
	 * Dodaj nową linię do notatnika (z enterem).
	 * @param text String - tekst
	 * @param mode mode - tryb wstawiania
	 */
	public void addTextLineNL(String text, String mode) {
		if(simpleMode) {
			textArea.append(text+newline);
		} else {
			int style = setWritingStyle(mode);
			try {
		        doc.insertString(doc.getLength(), text+newline, doc.getStyle(initStyles[style]));
		    } catch (Exception ble) {
		        GUIManager.getDefaultGUIManager().log("Couldn't insert initial text into text pane.", "error", true);
		    }
			int len = textPane.getDocument().getLength();
			textPane.setCaretPosition(len);
		}
	}
	
	/**
	 * Dodaje pojedyńczą linię tekstu.
	 * @param text String - tekst do dodania
	 * @param mode String - tryb pisania
	 */
	public void addLine(String text, String mode) {
		if(simpleMode) {
			textArea.append(text);
		} else {
			int style = setWritingStyle(mode);
			try {
		        doc.insertString(doc.getLength(), text, doc.getStyle(initStyles[style]));
		    } catch (Exception ble) {
		    	GUIManager.getDefaultGUIManager().log("Couldn't insert initial text into text pane.", "error", true);
		    }
			int len = textPane.getDocument().getLength();
			textPane.setCaretPosition(len);
		}
	}
	
	/**
	 * Metoda ustawia kursor na pierszą linię.
	 */
	public void setCaretFirstLine() {
		if(simpleMode)
			textArea.setCaretPosition(0);
		else
			textPane.setCaretPosition(0);
	}
	
	/**
	 * Metoda wewnętrzna definiująca styl po jego nazwie. Zwraca numer ID stylu.
	 * @param mode String - nazwa stylu pisania tekstu
	 * @return int - numer stylu
	 */
	private int setWritingStyle(String mode) {
		int style = 0;
		if(mode.equals("text") || mode.equals("t")) {
			style = 0;
		} else if(mode.equals("italic") || mode.equals("i")) {
			style = 1;
		} else if(mode.equals("bold") || mode.equals("b")) {
			style = 2;
		} else if(mode.equals("small")) {
			style = 3;
		} else if(mode.equals("large")) {
			style = 4;
		} else if(mode.equals("warning")) {
			style = 5;
		} else if(mode.equals("error")) {
			style = 6;
		} else if(mode.equals("time")) {
			style = 7;
		} else if(mode.equals("nodeName")) {
			style = 8;
		} else {
			style = 1;
		}
		return style;
	}
	
	/**
	 * Metoda pomocnicza konstruktora klasy, tworzy style dla wypisywanych komunikatów.
	 * @param doc StyledDocument - obiekt dokumentu przechowującego style
	 */
	private void addStylesToDocument(StyledDocument doc) {
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(def, "Consolas"); //Monospaced
        StyleConstants.setFontSize(def, 14);
        
        Style regular = doc.addStyle("regular", def); //0
        StyleConstants.setFontFamily(regular, "Consolas");
        StyleConstants.setFontSize(regular, 14);

        Style s = doc.addStyle("italic", regular); //1
        StyleConstants.setItalic(s, true);

        s = doc.addStyle("bold", regular); //2
        StyleConstants.setBold(s, true);

        s = doc.addStyle("small", regular); //3
        StyleConstants.setFontSize(s, 10);

        s = doc.addStyle("large", regular); //4
        StyleConstants.setFontSize(s, 18);
        
        s = doc.addStyle("warning", regular); //5
        StyleConstants.setForeground(s, Color.orange);
        
        s = doc.addStyle("error", regular); //6
        StyleConstants.setForeground(s, Color.red);
        
        s = doc.addStyle("time", regular); //7
        StyleConstants.setForeground(s, Color.darkGray);
        StyleConstants.setBold(s, true);
        
        s = doc.addStyle("node", regular); //8
        StyleConstants.setForeground(s, Color.darkGray);
        StyleConstants.setBold(s, true);
	}
}
