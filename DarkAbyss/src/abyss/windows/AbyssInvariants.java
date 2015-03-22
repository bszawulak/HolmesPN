package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import abyss.utilities.Tools;

public class AbyssInvariants extends JFrame {
	private boolean tInv = true;

	public AbyssInvariants() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		this.setTitle("Invariants generator and tools");
		
		setLayout(new BorderLayout());
		setSize(new Dimension(670, 550));
		setLocation(50, 50);
		//setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setResizable(false);
		
		JPanel mainPanel = createMainPanel();
		add(mainPanel, BorderLayout.CENTER);
		

		setVisible(true);
	}

	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);  /** (ノಠ益ಠ)ノ彡┻━━━┻ |   */
		
		//Panel wyboru opcji szukania
		JPanel buttonPanel = createUpperButtonPanel(0, 0, 650, 90);
		JPanel logMainPanel = createLogMainPanel(0, 90, 500, 400);
		JPanel leftButtonPanel = createRightButtonPanel(500, 90, 150, 400);
		
		panel.add(buttonPanel);
		panel.add(logMainPanel);
		panel.add(leftButtonPanel);
		panel.repaint();
		return panel;
	}

	private JPanel createUpperButtonPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 10;
		
		// przycisk generatora inwariantów
		JButton generateButton = new JButton("Generate");
		generateButton.setBounds(posX, posY, 110, 60);
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);

		ButtonGroup group = new ButtonGroup();
		JRadioButton tInvariantsMode = new JRadioButton("T-invariants (EM)");
		tInvariantsMode.setBounds(posX+120, posY-3, 120, 20);
		tInvariantsMode.setActionCommand("0");
		ActionListener tInvariantsModeActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				tInv = true;
			}
		};
		tInvariantsMode.addActionListener(tInvariantsModeActionListener);
		panel.add(tInvariantsMode);
		group.add(tInvariantsMode);
		group.setSelected(tInvariantsMode.getModel(), true);
		
		JRadioButton pInvariantsMode = new JRadioButton("P-invariants");
		pInvariantsMode.setBounds(posX+120, posY+15, 120, 20);
		pInvariantsMode.setActionCommand("0");
		ActionListener pInvariantsModeActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				tInv = false;
			}
		};
		pInvariantsMode.addActionListener(pInvariantsModeActionListener);
		panel.add(pInvariantsMode);
		group.add(pInvariantsMode);
		
		
		JButton INAgenerateButton = new JButton();
		INAgenerateButton.setText("<html>&nbsp;&nbsp;&nbsp;INA <br />generator</html>");
		INAgenerateButton.setBounds(posX+250, posY, 120, 32);
		INAgenerateButton.setMargin(new Insets(0, 0, 0, 0));
		INAgenerateButton.setIcon(Tools.getResIcon22("/icons/stateSim/aaa.png"));
		INAgenerateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		INAgenerateButton.setFocusPainted(false);
		panel.add(INAgenerateButton);
		
		JButton showInvariantsButton = new JButton();
		showInvariantsButton.setText("<html> Show <br />invariants</html>");
		showInvariantsButton.setBounds(posX+250, posY+36, 120, 32);
		showInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		showInvariantsButton.setIcon(Tools.getResIcon22("/icons/stateSim/aaa.png"));
		showInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		showInvariantsButton.setFocusPainted(false);
		panel.add(showInvariantsButton);
		
		
		JButton loadInvariantsButton = new JButton();
		loadInvariantsButton.setText("<html> Load <br />invariants</html>");
		loadInvariantsButton.setBounds(posX+380, posY, 120, 32);
		loadInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		loadInvariantsButton.setIcon(Tools.getResIcon22("/icons/stateSim/aaa.png"));
		loadInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		loadInvariantsButton.setFocusPainted(false);
		panel.add(loadInvariantsButton);
		
		JButton saveInvariantsButton = new JButton();
		saveInvariantsButton.setText("<html> Export <br />invariants</html>");
		saveInvariantsButton.setBounds(posX+510, posY, 120, 32);
		saveInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		saveInvariantsButton.setIcon(Tools.getResIcon22("/icons/stateSim/aaa.png"));
		saveInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		saveInvariantsButton.setFocusPainted(false);
		panel.add(saveInvariantsButton);
		

		return panel;
	}
	
	private JPanel createLogMainPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Log"));
		panel.setBounds(x, y, width, height);
		
		JTextArea logField = new JTextArea();
		logField.setLineWrap(true);
		
        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField),BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, width-20, height-25);
        panel.add(logFieldPanel);
        
		return panel;
	}
	
	private JPanel createRightButtonPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Tools"));
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 18;
		
		JButton cardinalityButton = new JButton();
		cardinalityButton.setText("<html>Check<br />cardinality</html>");
		cardinalityButton.setBounds(posX, posY, 110, 32);
		cardinalityButton.setMargin(new Insets(0, 0, 0, 0));
		cardinalityButton.setIcon(Tools.getResIcon22("/icons/stateSim/aa.png"));
		cardinalityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		cardinalityButton.setFocusPainted(false);
		panel.add(cardinalityButton);
		
		JButton minSuppButton = new JButton();
		minSuppButton.setText("<html>Check sup.<br />minimality</html>");
		minSuppButton.setBounds(posX, posY+38, 110, 32);
		minSuppButton.setMargin(new Insets(0, 0, 0, 0));
		minSuppButton.setIcon(Tools.getResIcon22("/icons/stateSim/aa.png"));
		minSuppButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		minSuppButton.setFocusPainted(false);
		panel.add(minSuppButton);
		
		JButton checkMatrixZeroButton = new JButton();
		checkMatrixZeroButton.setText("<html>Check<br />C&nbsp;&middot;&nbsp;x = 0</html>");
		checkMatrixZeroButton.setBounds(posX, posY+76, 110, 32);
		checkMatrixZeroButton.setMargin(new Insets(0, 0, 0, 0));
		checkMatrixZeroButton.setIcon(Tools.getResIcon22("/icons/stateSim/aa.png"));
		checkMatrixZeroButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		checkMatrixZeroButton.setFocusPainted(false);
		panel.add(checkMatrixZeroButton);
		
		JButton loadRefButton = new JButton();
		loadRefButton.setText("<html>Load ref. set<br />and compare</html>");
		loadRefButton.setBounds(posX, posY+114, 110, 32);
		loadRefButton.setMargin(new Insets(0, 0, 0, 0));
		loadRefButton.setIcon(Tools.getResIcon22("/icons/stateSim/aa.png"));
		loadRefButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		loadRefButton.setFocusPainted(false);
		panel.add(loadRefButton);
		
		
		return panel;
	}
}
