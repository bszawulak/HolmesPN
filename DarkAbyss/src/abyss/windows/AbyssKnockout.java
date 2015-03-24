package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import abyss.analyse.InvariantsCalculator;
import abyss.utilities.Tools;

public class AbyssKnockout extends JFrame {
	private JTextArea logField;
	
	
	
	public AbyssKnockout() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		setVisible(false);
		this.setTitle("Knockout analysis");
		
		setLayout(new BorderLayout());
		setSize(new Dimension(675, 500));
		setLocation(50, 50);
		//setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setResizable(false);
		
		JPanel mainPanel = createMainPanel();
		add(mainPanel, BorderLayout.CENTER);
		

		
	}

	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		
		//Panel wyboru opcji szukania
		JPanel buttonPanel = createUpperButtonPanel(0, 0, 650, 90);
		JPanel logMainPanel = createLogMainPanel(0, 90, 540, 400);
		JPanel leftButtonPanel = createLeftButtonPanel(540, 90, 100, 400);
		
		panel.add(buttonPanel);
		panel.add(logMainPanel);
		panel.add(leftButtonPanel);
		panel.repaint();
		return panel;
	}
	
	

	private JPanel createUpperButtonPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Search options"));
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 20;
		
		JButton generateButton = new JButton("Generate");
		generateButton.setBounds(posX, posY, 110, 35);
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);
		
		return panel;
	}
	
	private JPanel createLogMainPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Log"));
		panel.setBounds(x, y, width, height);
		
		logField = new JTextArea();
		logField.setLineWrap(true);
		logField.setEditable(false);
		DefaultCaret caret = (DefaultCaret)logField.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, width-20, height-25);
        panel.add(logFieldPanel);
		
		return panel;
	}
	
	private JPanel createLeftButtonPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Left"));
		panel.setBounds(x, y, width, height);
		
		return panel;
	}
}
