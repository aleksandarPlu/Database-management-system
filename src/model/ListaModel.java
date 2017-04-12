package model;

import java.io.File;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

import app.AppCore;
import model.file.UIAbstractFile;
import model.file.UIINDFile;
import model.file.UISEKFile;
import model.file.UISERFile;

public class ListaModel extends DefaultListModel{

	private static final long serialVersionUID = 1L;

	public ListaModel(){
		
	}
	
	public ListaModel(String path){
		File folder = new File(path);
		AppCore.getInstance().setCurrentPath(path);

		File[] listOfFiles = folder.listFiles();
		
		UISERFile back=new UISERFile((folder.getParent() != null) ? folder.getParent() : path, "...", true);
		
		ArrayList<UIAbstractFile> directory = new ArrayList<UIAbstractFile>();
		ArrayList<UIAbstractFile> files = new ArrayList<UIAbstractFile>();
		
		addElement(back);
		for (int i = 0; i < listOfFiles.length; i++) {
						
			if (listOfFiles[i].isDirectory()){
				if(!listOfFiles[i].isHidden() && listOfFiles[i].listFiles() != null && listOfFiles[i].canRead()){
					UISERFile uifile=new UISERFile(listOfFiles[i].getAbsolutePath(),
							listOfFiles[i].getName(),true);
					directory.add(uifile);
				}
			}else if (listOfFiles[i].getName().contains(".ser")){ 	 
				UISERFile uifile=new UISERFile(listOfFiles[i].getParentFile().getAbsolutePath(),
						listOfFiles[i].getName(),false);
				files.add(uifile);
			}else if (listOfFiles[i].getName().contains(".sek")){ 	 
				UISEKFile uifile=new UISEKFile(listOfFiles[i].getParentFile().getAbsolutePath(),
						listOfFiles[i].getName(),false);
				files.add(uifile);
			}else if (listOfFiles[i].getName().contains(".ind")){ 	 
				UIINDFile uifile=new UIINDFile(listOfFiles[i].getParentFile().getAbsolutePath(),
						listOfFiles[i].getName(),false);
				files.add(uifile);
			}
		}
		
		for(UIAbstractFile dir : directory){
			addElement(dir);
		}
		
		for(UIAbstractFile file : files){
			addElement(file);
		}
	}
}
