package actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import app.AppCore;

public class DBLoginAction extends AbstractUIAction{

	private static final long serialVersionUID = 1L;

	public DBLoginAction() {
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		putValue(MNEMONIC_KEY, KeyEvent.VK_N);
		putValue(SMALL_ICON, loadIcon("images/dbconnect.png"));
		putValue(NAME, "Connect Database");
		putValue(SHORT_DESCRIPTION, "Connect Database");
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		AppCore.getInstance().DBLogin();
	}

}
