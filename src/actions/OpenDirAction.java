package actions;

import java.awt.event.ActionEvent;

import app.AppCore;
import model.ListaModel;

public class OpenDirAction extends AbstractUIAction{

	private static final long serialVersionUID = 1L;

	public OpenDirAction(String name) {
//		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
//	        KeyEvent.VK_O, ActionEvent.CTRL_MASK));
//		putValue(SMALL_ICON, loadIcon("images/openDir.png"));
		putValue(NAME, name);
		putValue(SHORT_DESCRIPTION, "Open project");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		ListaModel lm = new ListaModel(e.getActionCommand());
		AppCore.getInstance().getLista().setModel(lm);
	}

}
