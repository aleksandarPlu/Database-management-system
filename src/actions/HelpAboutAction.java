package actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import app.HelpAbout;

public class HelpAboutAction extends AbstractUIAction{

	private static final long serialVersionUID = 1L;

	public HelpAboutAction() {
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_H, ActionEvent.CTRL_MASK));
		putValue(MNEMONIC_KEY, KeyEvent.VK_H);
		putValue(SMALL_ICON, loadIcon("images/help.png"));
		putValue(NAME, "About UI project");
		putValue(SHORT_DESCRIPTION, "About UI project");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		HelpAbout help = new HelpAbout();
		help.setVisible(true);
	}

}
