package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import abyss.graphpanel.MauritiusMapPanel;
import abyss.math.MauritiusMapBT;
import abyss.utilities.Tools;

public class AbyssKnockout extends JFrame {
	
	private MauritiusMapPanel mmp;
	
	
	private int intX=0;
	private int intY=0;
	
	public AbyssKnockout() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		setVisible(false);
		this.setTitle("Knockout analysis");
		
		setLayout(new BorderLayout());
		setSize(new Dimension(900, 700));
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
		JPanel buttonPanel = createUpperButtonPanel(0, 0, 900, 90);
		JPanel logMainPanel = createLogMainPanel(0, 90, 900, 600);
		
		panel.add(buttonPanel);
		panel.add(logMainPanel);
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
		generateButton.setBounds(posX, posY, 110, 30);
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				MauritiusMapBT kc = new MauritiusMapBT();
				
				//mmp = new MauritiusMapPanel(kc);
				mmp.addMMBT(kc);
				mmp.repaint();
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);

		JButton generate2Button = new JButton("CLEAN");
		generate2Button.setBounds(posX+200, posY, 110, 30);
		generate2Button.setMargin(new Insets(0, 0, 0, 0));
		generate2Button.setIcon(Tools.getResIcon32("/icons/stateSim/aaa.png"));
		generate2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//MauritiusMapBT kc = new MauritiusMapBT();
				
				//mmp = new MauritiusMapPanel(kc);
				mmp.addMMBT(null);
				mmp.repaint();
			}
		});
		generate2Button.setFocusPainted(false);
		panel.add(generate2Button);
		
		return panel;
	}
	
	
	private JPanel createLogMainPanel(int x, int y, int width, int height) {
		JPanel gr_panel = new JPanel();
		gr_panel.setLayout(null);
		gr_panel.setBounds(x, y, width, height);
		
		mmp = new MauritiusMapPanel();
		
		intX = width-10;
		intY = height-25;
		
		JScrollPane scroller = new JScrollPane(mmp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller.setBounds(0, 0, intX, intY);
		gr_panel.add(scroller);
		
		@SuppressWarnings("unused")
		Dimension size = mmp.getSize();

		return gr_panel;
	}
}
