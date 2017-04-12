package actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import view.FileView;
import model.ListaModel;
import model.file.UISERFile;
import app.AppCore;

public class DeleteAction extends AbstractUIAction {

	private static final long serialVersionUID = 1L;

	public DeleteAction() {
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_DELETE, 0));
		putValue(MNEMONIC_KEY, KeyEvent.VK_D);
		putValue(SMALL_ICON, loadIcon("images/delete.png"));
		putValue(NAME, "Delete");
		putValue(SHORT_DESCRIPTION, "Delete");
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(AppCore.getInstance().getLista().getSelectedIndices().length > 0){
			int selectedIndex[] = AppCore.getInstance().getLista().getSelectedIndices();
			
			int jop = JOptionPane.showConfirmDialog(null, "Da li zelite da izbrisete selektovane stvari?", 
					"Delete", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			
			if(jop == JOptionPane.YES_OPTION){
				for(int i=0;i<selectedIndex.length;i++){
					Object o = AppCore.getInstance().getLista().getModel().getElementAt(selectedIndex[i]);
			
					if(o instanceof UISERFile){
						UISERFile uifile = (UISERFile)o;
									
						File file = new File(AppCore.getInstance().getCurrentPath() + "\\" + uifile.getFileName());
						
						if(file.isDirectory()){
							deleteFolder(file);
						} else { 
							if(file.exists()){
								file.delete();
								closeFileView(uifile);
							}
						}
					}
				}
				
				ListaModel lm = new ListaModel(AppCore.getInstance().getCurrentPath());
				AppCore.getInstance().getLista().setModel(lm);
			}
		}
	}
	
	public void deleteFolder(File directory) {
	    if(!directory.isHidden() && directory.listFiles() != null){
		    for (File file : directory.listFiles()) {
				if (file.isFile())
					file.delete();
		        else {
		        	deleteFolder(file);
		        	file.delete();
		        }
		    }
	    }
	    directory.delete();
	}

	public void closeFileView(UISERFile uifile){
		for(int i=0;i<AppCore.getInstance().getFramework().getTabCount();i++){
    		FileView file = (FileView) AppCore.getInstance().getFramework().getComponentAt(i);
    		
    		String p1 = file.getUiFile().getPath()+file.getUiFile().getFileName();
    		String p2 = uifile.getPath()+uifile.getFileName();
    		
    		if(p1.equals(p2)){
    			int jop = JOptionPane.showConfirmDialog(null, uifile.getFileName() + " je izbrisan.\n" +
    					"Zadrzi ovaj fajl otvoren?", 
    					"Close", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    			
    			if(jop == JOptionPane.YES_OPTION)
    				file.changeDocument(true, i);
    			else 
    				AppCore.getInstance().getFramework().removeTabAt(i);
    		}
		}
	}
	
}
