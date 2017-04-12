package model;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

public class ProgressBarThread extends Thread{

	private boolean closed = false;
	private JDialog dialog;
	
	
	@Override
	public void run() {
		JProgressBar pb = new JProgressBar();
		pb.setPreferredSize(new Dimension(175, 40));
		pb.setString("Working...");
		pb.setStringPainted(true);
		pb.setIndeterminate(true);
		
		JLabel label = new JLabel("Progress: ");
		JPanel center_panel = new JPanel();
		center_panel.add(label);
		center_panel.add(pb);	
		
		dialog = new JDialog((JFrame) null, "Working");
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.getContentPane().add(center_panel, BorderLayout.CENTER);
		dialog.pack();
		dialog.setLocationRelativeTo(null); 
		dialog.toFront(); 
		dialog.setModal(true);

		dialog.setVisible(true);

		dialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				closed=true;
			}
		});
		
	}


	public boolean isClosed() {
		return closed;
	}


	public JDialog getDialog() {
		return dialog;
	}

}
