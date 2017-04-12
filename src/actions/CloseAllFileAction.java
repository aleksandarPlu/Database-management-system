package actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import model.file.UIAbstractFile;
import model.file.UISERFile;
import view.FileView;
import app.AppCore;

public class CloseAllFileAction extends AbstractUIAction{

	private static final long serialVersionUID = 1L;

	public CloseAllFileAction() {		
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_W, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		putValue(MNEMONIC_KEY, KeyEvent.VK_L);
		putValue(SMALL_ICON, loadIcon("images/closeAll.png"));
		putValue(NAME, "Close all");
		putValue(SHORT_DESCRIPTION, "Close all");
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		boolean close = true;
		for(int i=0; i<AppCore.getInstance().getFramework().getTabCount(); i++){
			FileView fileView = (FileView) AppCore.getInstance().getFramework().getComponentAt(i);
			UIAbstractFile uiFile = fileView.getUiFile();
			
			if(uiFile.isChanged()){
				AppCore.getInstance().getFramework().setSelectedIndex(i);
				int jop = JOptionPane.showConfirmDialog(null, "Da li zelite da sacuvate " + uiFile.getFileName() + " ?", 
						"Save?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if(jop == JOptionPane.YES_OPTION){
					AppCore.getInstance().getActionManager().getSaveFileAction().actionPerformed(null);
					uiFile.setChanged(false);
				} else if (jop == JOptionPane.CLOSED_OPTION){
					close = false;
				}
			}
		}
		
		if(close) {
			while (AppCore.getInstance().getFramework().getSelectedIndex() >= 0){
				FileView fileView = (FileView) AppCore.getInstance().getFramework().getComponentAt(AppCore.getInstance().getFramework().getSelectedIndex());
				UIAbstractFile uiFile = fileView.getUiFile();
				uiFile.setChanged(false);
				
				AppCore.getInstance().getFramework().removeTabAt(AppCore.getInstance().getFramework().getSelectedIndex());
			}
			

			AppCore.getInstance().getActionManager().getCloseFileAction().setEnabled(false);
			AppCore.getInstance().getActionManager().getCloseAllFileAction().setEnabled(false);
			AppCore.getInstance().getActionManager().getSaveFileAction().setEnabled(false);
			AppCore.getInstance().getActionManager().getSaveAsFileAction().setEnabled(false);
		}
	}
}
