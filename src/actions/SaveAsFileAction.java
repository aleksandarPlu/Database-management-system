package actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import view.FileView;
import model.ListaModel;
import model.file.UISERFile;
import app.AppCore;

public class SaveAsFileAction extends AbstractUIAction{

	private static final long serialVersionUID = 1L;

	public SaveAsFileAction() {
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		putValue(MNEMONIC_KEY, KeyEvent.VK_S);
		putValue(SMALL_ICON, loadIcon("images/SaveAs.png"));
		putValue(NAME, "Save As");
		putValue(SHORT_DESCRIPTION, "Save As");
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(AppCore.getInstance().getFramework().getSelectedIndex()!=-1){
			FileView fileV = (FileView) AppCore.getInstance().getFramework().getSelectedComponent();
			UISERFile uiserFile = (UISERFile) fileV.getUiFile();
			
			String content = fileV.getArea().getText();
			
			int index = AppCore.getInstance().getFramework().getSelectedIndex();
			
			JFileChooser chooser = new JFileChooser();

			FileFilter filter = new FileNameExtensionFilter("Normal text file (.txt)","txt");
			chooser.addChoosableFileFilter(filter);
			chooser.setFileFilter(filter);
			chooser.setSelectedFile(new File(uiserFile.getFileName()));
			
			int ch = chooser.showSaveDialog(AppCore.getInstance());
			
			if(ch != JFileChooser.APPROVE_OPTION){
				return;
			}
	        
	        String path = chooser.getCurrentDirectory().toString() + chooser.getSelectedFile().getName();
			
	        File f = new File(chooser.getSelectedFile().getName(), path);
	        
	        content = content.replace("\n","\r\n");
			
	        RandomAccessFile raf;
			try {
				raf = new RandomAccessFile(path,"rw");
				raf.writeBytes(content);
				raf.setLength(content.length());
				raf.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			UISERFile uiFile = new UISERFile(chooser.getCurrentDirectory().toString(), f.getName(), false);
	        FileView fileView = new FileView(uiFile);
	        
			Icon icon = new ImageIcon(getClass().getResource("images/tab.png"));
	        AppCore.getInstance().getFramework().insertTab(f.getName(), icon, fileView, null, index);
	        AppCore.getInstance().getFramework().remove(index+1);
	        AppCore.getInstance().setFileView(fileView);
	        AppCore.getInstance().getFileView().getUiFile().setChanged(false);
			AppCore.getInstance().getActionManager().getSaveFileAction().setEnabled(false);
	      
	        if(AppCore.getInstance().getCurrentPath()!=null){
				ListaModel lm = new ListaModel(AppCore.getInstance().getCurrentPath());
				AppCore.getInstance().getLista().setModel(lm);
			}
		}
	}

}
