package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Serial;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.utilities.Tools;

/**
 * Klasa tworząca okienko informacji o programie.
 */
public class HolmesAbout extends JFrame {
	@Serial
	private static final long serialVersionUID = 6034143130559149651L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	JFrame parentFrame;
	JTextArea textArea;
	
	public HolmesAbout(JFrame parent) {
		parentFrame = parent;
		parentFrame.setEnabled(false);
		try {
			setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00436exception")+ "\n"+ex.getMessage(), "error", true);
		}
		setTitle("About Holmes");
		setLayout(null);
		setSize(new Dimension(550, 550));
		
		JPanel logoInfo = upperPanel();
		add(logoInfo);
		
		//textArea = new JTextArea();
		textArea = new MyTextArea();
		
		try {
			BufferedImage wPic = ImageIO.read(this.getClass().getResource("/abyssHolmes2.png"));
			((MyTextArea)textArea).setBackgroundImage(wPic);
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00437exception")+ "\n"+ex.getMessage(), "error", true);
		}
		
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(
        		textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
        		BorderLayout.CENTER);
        CreationPanel.setBounds(0, 180, 535, 330);
        add(CreationPanel);
 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	parentFrame.setEnabled(true);
		    }
		});
		
		fillText();
		
		repaint();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * Metoda tworzy górny panel, z logo PP oraz nazwą programu.
	 * @return JPanel - panel
	 */
	private JPanel upperPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBounds(0, 0, 550, 200);
		panel.setSize(550, 180);
		panel.setLocation(0, 0);
		
		try {
			BufferedImage wPic = ImageIO.read(this.getClass().getResource("/PPlogo.png"));
			JLabel wIcon = new JLabel(new ImageIcon(wPic));
			wIcon.setBounds(10, 10, 160, 160);
			panel.add(wIcon);
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00438exception")+ "\n"+ex.getMessage(), "error", true);
		}
		
		JLabel nameLabel = new JLabel("Holmes v2.0.0"); // Kim Kitsuragi
		nameLabel.setFont(new Font("Consolas", Font.PLAIN, 25));
		nameLabel.setBounds(170, 20, 300, 40);
		panel.add(nameLabel);
		
		JLabel nameLabel1b = new JLabel("Petri nets editor and analyzer");
		nameLabel1b.setFont(new Font("Consolas", Font.PLAIN, 18));
		nameLabel1b.setBounds(170, 40, 300, 30);
		panel.add(nameLabel1b);
		
		JLabel nameLabel2 = new JLabel("[build 31-07-2024]");
		nameLabel2.setFont(new Font("Consolas", Font.PLAIN, 12));
		nameLabel2.setBounds(170, 80, 330, 15);
		panel.add(nameLabel2);
		
		JLabel nameLabel3 = new JLabel("Release edition: \"Kim Kitsuragi\"");
		nameLabel3.setFont(new Font("Consolas", Font.BOLD, 14));
		nameLabel3.setBounds(170, 95, 330, 15);
		panel.add(nameLabel3);

		JLabel nameLabel3b = new JLabel("Subnets, MCS, Language (AI translated)");
		nameLabel3b.setFont(new Font("Consolas", Font.PLAIN, 12));
		nameLabel3b.setBounds(170, 110, 330, 15);
		panel.add(nameLabel3b);
		
		JLabel nameLabel4 = new JLabel("(Scroll for authors, versions and libraries");
		nameLabel4.setFont(new Font("Consolas", Font.ITALIC, 12));
		nameLabel4.setBounds(100, 150, 330, 40);
		panel.add(nameLabel4);
		
		return panel;
	}
	
	/**
	 * Metoda wypełnia pole tekstowe informacjami o programie, bibliotekach, licencjach, wersjach, etc.
	 */
	private void fillText() {
		for(int i=0; i<24; i++)
			textArea.append("\n");

		textArea.setForeground(Color.BLACK);
		textArea.setBackground(new Color(238, 238, 238));
		
		textArea.setFont(new Font("Consolas", Font.PLAIN, 15));

		textArea.append("Versions and authors:\n");
		textArea.append("Holmes version 1.7, July 2024, 120k+ LOC \n");
		textArea.append("Holmes version 1.6, February 2024 \n");
		textArea.append("Holmes version 1.5, June 2022-December 2022, even more LOC \n");
		textArea.append("  Marcin Radom, Ph.D.\n");
		textArea.append("Holmes version 1.1, June 2022, 100k+ LOC \n");
		textArea.append("  Bartlomiej Szawulak, Ph.D.\n");
		textArea.append("Holmes version 1.0, January 2017, 57k+ LOC \n");
		textArea.append("  Radom Marcin, Ph.D.\n");
		textArea.append("Holmes version 0.8, October 2015, 50k+ LOC \n");
		textArea.append("  Radom Marcin, Ph.D.\n");
		textArea.append("\n");
		textArea.append("Abyss Version 1.2+, 2014-2015, 30k+ LOC \n");
		textArea.append("  Radom Marcin, Ph.D.\n");
		textArea.append("  Rybarczyk Agnieszka, Ph.D.\n");
		textArea.append("Abyss Version 1.1, 2013-2014, 10k LOC \n");
		textArea.append("  Bartłomiej Szawulak, M.Sc. thesis\n");
		textArea.append("  (supervisor: Radom Marcin, Ph.D.)\n");
		textArea.append("Abyss Version 1.0, 2012-2013, 7k LOC \n");
		textArea.append("  Andrzejewski Hubert, B.Sc. thesis\n");
		textArea.append("  Chabelski Piotr, B.Sc. thesis\n");
		textArea.append("  Szawulak Bartłomiej, B.Sc. thesis\n");
		textArea.append("  (supervisor: Formanowicz Piotr Ph.D. Dr.Hab.)\n");
		textArea.append("\n");
		textArea.append("Holmes is a free scientific software distributed under GNU GPL, developed in "
				+ "Poznan University of Technology, Faculty of Computing Science, Poznan, Poland.\n");
		textArea.append("\n");
		textArea.append("Used external libraries and software:\n");
		textArea.append(" jXLS library (GNU LGPL, http://jxls.sourceforge.net/)\n");
		textArea.append(" XStream (BSD License http://xstream.codehaus.org/license.html )\n");
		textArea.append(" Simple-xml (Apache Licence, http://simple.sourceforge.net/home.php)\n");
		textArea.append(" RCaller (GNU LGPL, https://code.google.com/p/rcaller/)\n");
		textArea.append(" exp4J (Apache Licence, http://www.objecthunter.net/exp4j/license.html)\n");
		textArea.append("\n");
		textArea.setCaretPosition(0);
	}
	
	static class MyTextArea extends JTextArea {
		@Serial
		private static final long serialVersionUID = -6987188635486059891L;
		private Image backgroundImage;

	    public MyTextArea() {
	        super();
	        setOpaque(false);
	    }

	    public void setBackgroundImage(Image image) {
	        this.backgroundImage = image;
	        this.repaint();
	    }

	    @Override
	    protected void paintComponent(Graphics g) {
	        g.setColor(getBackground());
	        g.fillRect(0, 0, getWidth(), getHeight());

	        if (backgroundImage != null) {
	            g.drawImage(backgroundImage, 0, 0, 520, 440, this);
	            
	        }

	        super.paintComponent(g);
	    }
	}
}
