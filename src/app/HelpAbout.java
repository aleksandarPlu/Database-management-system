package app;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class HelpAbout extends JDialog{

	private static final long serialVersionUID = 1L;

	public HelpAbout(){
		setTitle("About");
		
		JLabel logo = new JLabel(new ImageIcon("bin/actions/images/DSCN6545.jpg"));
		String name = "Aleksandar Pluskoski RN-28/13\n\n" +
					  "apluskoski12@raf.edu.rs\n";
		
		JPanel p = new JPanel();
		JPanel p2 = new JPanel();
		
		JTextArea txt  = new JTextArea(name);
		txt.setEditable(false);
		txt.setFont(new Font(null, Font.BOLD, 16));
		txt.setBackground(getBackground());
		txt.setBorder(new EmptyBorder(50, 0, 0, 0));
		
		p.add(logo);
		p2.add(txt);
		
		getContentPane().add(p, BorderLayout.WEST);
		getContentPane().add(p2, BorderLayout.CENTER);
		
		pack();
		setModal(true);
		setResizable(false);
		setLocationRelativeTo(null);
	}
	
}
