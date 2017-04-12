package actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import view.FileView;
import model.ListaModel;
import model.file.UISERFile;
import app.AppCore;

public class SaveFileAction extends AbstractUIAction{

	private static final long serialVersionUID = 1L;

	public SaveFileAction() {
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		putValue(MNEMONIC_KEY, KeyEvent.VK_A);
		putValue(SMALL_ICON, loadIcon("images/saveFile.png"));
		putValue(NAME, "Save file");
		putValue(SHORT_DESCRIPTION, "Save file");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(AppCore.getInstance().getFramework().getSelectedIndex()!=-1){
			FileView fileView = (FileView) AppCore.getInstance().getFramework().getSelectedComponent();
			UISERFile uiFile = (UISERFile) fileView.getUiFile();
			
			uiFile.SaveFile(fileView.getArea().getText(), AppCore.getInstance().getFramework().getSelectedIndex());
			
			if(AppCore.getInstance().getCurrentPath()!=null){
				ListaModel lm = new ListaModel(AppCore.getInstance().getCurrentPath());
				AppCore.getInstance().getLista().setModel(lm);
			}
		}
	}

}