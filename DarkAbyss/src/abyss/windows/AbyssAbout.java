package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import abyss.utilities.Tools;

/**
 * Klasa tworząca okienko informacji o programie.
 * @author MR
 *
 */
public class AbyssAbout extends JFrame {
	private static final long serialVersionUID = 6034143130559149651L;
	JFrame parentFrame;
	JTextArea textArea;
	
	public AbyssAbout(JFrame parent) {
		parentFrame = parent;
		parentFrame.setEnabled(false);
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		setTitle("About the program.");
		setLayout(null);
		setSize(new Dimension(550, 400));
		
		JPanel logoInfo = upperPanel();
		add(logoInfo);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(
        		textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
        		BorderLayout.CENTER);
        CreationPanel.setBounds(0, 180, 530, 150);
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
		} catch (Exception e) {
			
		}
		
		JLabel nameLabel = new JLabel("Abyss v1.23");
		nameLabel.setFont(new Font("Consolas", Font.PLAIN, 30));
		nameLabel.setBounds(170, 40, 200,40);
		panel.add(nameLabel);
		
		JLabel nameLabel2 = new JLabel("Integrated Petri Net Environment");
		nameLabel2.setFont(new Font("Consolas", Font.PLAIN, 18));
		nameLabel2.setBounds(170, 70, 330, 40);
		panel.add(nameLabel2);
		
		JLabel nameLabel3 = new JLabel("Release: 11-01-2015");
		nameLabel3.setFont(new Font("Consolas", Font.PLAIN, 14));
		nameLabel3.setBounds(170, 110, 330, 40);
		panel.add(nameLabel3);
		
		return panel;
	}
	
	/**
	 * Metoda wypełnia pole tekstowe informacjami o programie, bibliotekach, licencjach, wersjach, etc.
	 */
	private void fillText() {
		textArea.append("Versions and authors:\n");
		textArea.append("Version 1.2+, 2014-2015, 18k+ lines of code total \n");
		textArea.append("  Radom Marcin, Ph.D.\n");
		textArea.append("  Rybarczyk Agnieszka, Ph.D.\n");
		textArea.append("\n");
		textArea.append("Version 1.1, 2013-2014, 10k lines of code total\n");
		textArea.append("  Bartłomiej Szawulak, M.Sc. thesis\n");
		textArea.append("  (supervisor: Radom Marcin, Ph.D.)\n");
		textArea.append("\n");
		textArea.append("Version 1.0, 2012-2013, 7k lines od code total\n");
		textArea.append("  Andrzejewski Hubert, B.Sc. thesis\n");
		textArea.append("  Chabelski Piotr, B.Sc. thesis\n");
		textArea.append("  Szawulak Bartłomiej, B.Sc. thesis\n");
		textArea.append("  (supervisor: Formanowicz Piotr Ph.D. hab.)\n");
		textArea.append("\n");
		textArea.append("Abyss IPNE is a free scientific software distributed under GNU GPL, developed in "
				+ "Poznan University of Technology, Faculty of Computing Science, Poznan, Poland.\n");
		textArea.append("\n");
		textArea.append("Used external libraries and software:\n");
		textArea.append(" Sanaware JavaDocking (GNU GPL, http://www.javadocking.com/)\n");
		textArea.append(" jXLS library (GNU LGPL, http://jxls.sourceforge.net/)\n");
		textArea.append(" XStream (BSD License http://xstream.codehaus.org/license.html )\n");
		textArea.append(" Simple-xml (Apache Licence, http://simple.sourceforge.net/home.php)\n");
		textArea.append(" RCaller (GNU LGPL, https://code.google.com/p/rcaller/)\n");
		textArea.append("\n");
		textArea.append("In addition, R environment with appropriate libraries (cluster, fpc, amap) "
				+ "and INAwin32.exe are used in some of the program functions. The user must acquire "
				+ "them in order to fully use Abyss software.\n");
		textArea.setCaretPosition(0);
	}
}
