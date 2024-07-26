package holmes.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.*;
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
import holmes.darkgui.LanguageManager;
import holmes.utilities.Tools;
import holmes.workspace.ExtensionFileFilter;

/**
 * Okno wewnętrznego notatnika programu.
 */
public class HolmesNotepad extends JFrame {
	@Serial
	private static final long serialVersionUID = 1694133455242675169L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private String newline = "\r\n";
	private StyledDocument doc; //
	private JTextPane textPane; //panel z tekstem -> paneScrollPane

	private JTextArea textArea;
	
	private boolean simpleMode = false;
	/** regular, italic, bold, small, large, warning, error */
	private String[] initStyles = { "regular", "italic", "bold", "small", "large", "warning", "error", "time", "node" };
	
	/**
	 * Główny konstruktor domyślny okna notatnika.
	 */
	private HolmesNotepad() {
		setTitle(lang.getText("HNwin_entry001title"));
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00485exception")+" "+ex.getMessage(), "error", true);
		}
    	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	/**
	 * Konstruktor tworzący nowe okno o zadanych rozmiarach.
	 * @param width int - szerokość okna
	 * @param height int - wysokość okna
	 */
	public HolmesNotepad(int width, int height) {
		this();
		
		try {
			if(overlord.getSettingsManager().getValue("programUseSimpleEditor").equals("1"))
				simpleMode = true;
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00486exception")+ " "+ex.getMessage(), "error", true);
		}
		setPreferredSize(new Dimension(width, height));
		setLocation(50,50);
		
		JPanel mainPanel = createEditor(width, height);
        setContentPane(mainPanel);
        this.pack();
        setVisible(false); 
	}
	
	public HolmesNotepad(int width, int height, boolean simpleToken) {
		this();
		
		simpleMode = simpleToken;
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

		//panel scrollbar -> editPanel
		JScrollPane paneScrollPane;
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
	@SuppressWarnings("unused")
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
		savedButton.setToolTipText(lang.getText("HNwin_entry002")); //Saves the content of notepad.
		savedButton.setFocusPainted(false);
		savedButton.addActionListener(actionEvent -> saveContent());
		buttonPanel.add(savedButton);
		
		JButton loadButton = new JButton();
		loadButton.setBounds(50, 7, 35, 35);
		loadButton.setMargin(new Insets(0, 0, 0, 0));
		loadButton.setIcon(Tools.getResIcon16("/icons/notepad/loadFile.png"));
		loadButton.setToolTipText(lang.getText("HNwin_entry003")); //Load txt files.
		loadButton.setFocusPainted(false);
		loadButton.addActionListener(actionEvent -> loadContent());
		buttonPanel.add(loadButton);
		
		JButton clearButton = new JButton();
		clearButton.setBounds(90, 7, 35, 35);
		clearButton.setMargin(new Insets(0, 0, 0, 0));
		clearButton.setIcon(Tools.getResIcon16("/icons/notepad/eraserIcon.png"));
		clearButton.setToolTipText(lang.getText("HNwin_entry004")); //Clear content
		clearButton.setFocusPainted(false);
		clearButton.addActionListener(actionEvent -> clearContent());
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
			} catch (BadLocationException ex) {
				overlord.log(lang.getText("LOGentry00487exception")+" "+ex.getMessage(), "error", true);
			}
		}
	}

	/**
	 * Obsługa wczytywania pliku tekstowego
	 */
	protected void loadContent() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Holmes notepad text file (.txt)",  new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("load"), lang.getText("HNwin_entry005"), "");
		
		if(!selectedFile.isEmpty()) {
			try {
				clearContent();
				DataInputStream dis = new DataInputStream(new FileInputStream(selectedFile));
				BufferedReader buffer = new BufferedReader(new InputStreamReader(dis));
				
				String line;
				//addTextLineNL(line,  "text");
				while((line = buffer.readLine()) != null) {
					addTextLineNL(line,  "text");
				}
				buffer.close();
				setCaretFirstLine();
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentry00488exception")+ " "+e.getMessage(), "error", true);
			}
		}
	}

	/**
	 * Zapisuje zawartość notatnika do pliku.
	 */
	protected void saveContent() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Holmes notepad text file (.txt)",  new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("load"), lang.getText("HNwin_entry006"), "");
		
		if(!selectedFile.isEmpty()) {
			String extension = ".txt";
			if(!selectedFile.contains(extension))
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
				overlord.log(lang.getText("LOGentry00489exception")+" "+e.getMessage(), "error", true);
			}
		}
	}

	/**
	 * Metoda pomocnicza konstruktora, tworzy obiekt edytora.
	 * @return (<b>JTextPane</b>) - panel edytora.
	 */
	private JTextPane createTextPane() {
	    JTextPane txtPane = new JTextPane();
	    doc = txtPane.getStyledDocument();
	    addStylesToDocument(doc);
	    return txtPane;
	}
	
	/**
	 * Metoda wpisuje nową linię do okna logów.
	 * @param text (<b>String</b>) text do wpisania.
	 * @param mode (<b>String</b>) tryb pisania.
	 * @param time (<b>boolean</b>) true, jeśli ma być wyświetlony czas wpisu.
	 * @param enter (<b>boolean</b>) true jeśli kończymy enterem.
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
		    } catch (Exception e) {
				overlord.log(lang.getText("LOGentry00490exception")+" "+e.getMessage(), "error", true);
		    }
			
			int len = textPane.getDocument().getLength();
			textPane.setCaretPosition(len);
		}
	}
	
	/**
	 * Dodaj nową linię do notatnika (bez entera).
	 * @param text (<b>String</b>) tekst.
	 * @param mode (<b>String</b>) tryb wstawiania.
	 */
	public void addTextLine(String text, String mode) {
		if(simpleMode) {
			textArea.append(text);
		} else {
			int style = setWritingStyle(mode);
			try {
		        doc.insertString(doc.getLength(), text, doc.getStyle(initStyles[style]));
		    } catch (Exception e) {
				overlord.log(lang.getText("LOGentry00491exception")+" "+e.getMessage(), "error", true);
		    }
			int len = textPane.getDocument().getLength();
			textPane.setCaretPosition(len);
		}
	}
	
	/**
	 * Dodaj nową linię do notatnika (z enterem).
	 * @param text (<b>String</b>) tekst.
	 * @param mode (<b>String</b>) tryb wstawiania.
	 */
	public void addTextLineNL(String text, String mode) {
		if(simpleMode) {
			textArea.append(text+newline);
		} else {
			int style = setWritingStyle(mode);
			try {
		        doc.insertString(doc.getLength(), text+newline, doc.getStyle(initStyles[style]));
		    } catch (Exception e) {
				overlord.log(lang.getText("LOGentry00492exception")+" "+e.getMessage(), "error", true);
		    }
			int len = textPane.getDocument().getLength();
			textPane.setCaretPosition(len);
		}
	}
	
	/**
	 * Dodaje pojedyńczą linię tekstu.
	 * @param text (<b>String</b>) tekst do dodania.
	 * @param mode (<b>String</b>) tryb pisania.
	 */
	public void addLine(String text, String mode) {
		if(simpleMode) {
			textArea.append(text);
		} else {
			int style = setWritingStyle(mode);
			try {
		        doc.insertString(doc.getLength(), text, doc.getStyle(initStyles[style]));
		    } catch (Exception e) {
				overlord.log(lang.getText("LOGentry00493exception")+" "+e.getMessage(), "error", true);
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
	 * @param mode (<b>String</b>) nazwa stylu pisania tekstu.
	 * @return (<b>int</b>) - numer stylu.
	 */
	private int setWritingStyle(String mode) {
		return switch (mode) {
			case "text", "t" -> 0;
			case "italic", "i" -> 1;
			case "bold", "b" -> 2;
			case "small" -> 3;
			case "large" -> 4;
			case "warning" -> 5;
			case "error" -> 6;
			case "time" -> 7;
			case "nodeName" -> 8;
			default -> 0;
		};
	}
	
	/**
	 * Metoda pomocnicza konstruktora klasy, tworzy style dla wypisywanych komunikatów.
	 * @param styledDoc (<b>StyledDocument</b>) obiekt dokumentu przechowującego style.
	 */
	private void addStylesToDocument(StyledDocument styledDoc) {
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(def, "Consolas"); //Monospaced
        StyleConstants.setFontSize(def, 14);
        
        Style regular = styledDoc.addStyle("regular", def); //0
        StyleConstants.setFontFamily(regular, "Consolas");
        StyleConstants.setFontSize(regular, 14);

        Style s = styledDoc.addStyle("italic", regular); //1
        StyleConstants.setItalic(s, true);

        s = styledDoc.addStyle("bold", regular); //2
        StyleConstants.setBold(s, true);

        s = styledDoc.addStyle("small", regular); //3
        StyleConstants.setFontSize(s, 10);

        s = styledDoc.addStyle("large", regular); //4
        StyleConstants.setFontSize(s, 18);
        
        s = styledDoc.addStyle("warning", regular); //5
        StyleConstants.setForeground(s, Color.orange);
        
        s = styledDoc.addStyle("error", regular); //6
        StyleConstants.setForeground(s, Color.red);
        
        s = styledDoc.addStyle("time", regular); //7
        StyleConstants.setForeground(s, Color.darkGray);
        StyleConstants.setBold(s, true);
        
        s = styledDoc.addStyle("node", regular); //8
        StyleConstants.setForeground(s, Color.darkGray);
        StyleConstants.setBold(s, true);
	}
}
