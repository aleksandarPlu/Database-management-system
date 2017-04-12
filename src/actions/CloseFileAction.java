package actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import model.file.UIAbstractFile;
import model.file.UISERFile;
import view.FileView;
import app.AppCore;

public class CloseFileAction extends AbstractUIAction{

	private static final long serialVersionUID = 1L;
	
	public CloseFileAction() {
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		putValue(MNEMONIC_KEY, KeyEvent.VK_C);
		putValue(SMALL_ICON, loadIcon("images/closeFile.png"));
		putValue(NAME, "Close file");
		putValue(SHORT_DESCRIPTION, "Close file");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(AppCore.getInstance().getFramework().getSelectedIndex() >= 0){
			FileView fileView = (FileView) AppCore.getInstance().getFramework().getSelectedComponent();
			UIAbstractFile uiFile = fileView.getUiFile();
			
			if(!uiFile.isChanged()){
				AppCore.getInstance().getFramework().removeTabAt(AppCore.getInstance().getFramework().getSelectedIndex());		
			} else {
				int jop = JOptionPane.showConfirmDialog(null, "Da li zelite da sacuvate " + uiFile.getFileName() + " ?", 
						"Save?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if(jop == JOptionPane.YES_OPTION){
					AppCore.getInstance().getActionManager().getSaveFileAction().actionPerformed(null);
					AppCore.getInstance().getFramework().removeTabAt(AppCore.getInstance().getFramework().getSelectedIndex());
					uiFile.setChanged(false);
				} else if (jop == JOptionPane.NO_OPTION){
					AppCore.getInstance().getFramework().removeTabAt(AppCore.getInstance().getFramework().getSelectedIndex());
					uiFile.setChanged(false);
				}
			}
		}
		
		if(AppCore.getInstance().getFramework().getTabCount()==0){
			AppCore.getInstance().getActionManager().getCloseFileAction().setEnabled(false);
			AppCore.getInstance().getActionManager().getCloseAllFileAction().setEnabled(false);
			AppCore.getInstance().getActionManager().getSaveFileAction().setEnabled(false);
			AppCore.getInstance().getActionManager().getSaveAsFileAction().setEnabled(false);
		}
	}

}
