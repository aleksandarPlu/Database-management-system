package view;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.ListaModel;
import model.file.UIAbstractFile;
import model.file.UISERFile;
import app.AppCore;

public class ListaView extends JList implements ListSelectionListener,MouseListener{

	private static final long serialVersionUID = 1L;

	public ListaView(){
		addListSelectionListener(this);
		addMouseListener(this);
		setCellRenderer(new ListaCellRenderer());
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		long size = 0;
		
		if(getSelectedIndices().length > 0){
			int selectedIndex[] = getSelectedIndices();
			
			for(int i=0;i<selectedIndex.length;i++){
				Object o = getModel().getElementAt(selectedIndex[i]);
				
				if(o instanceof UISERFile){
					UISERFile uifile=(UISERFile)o;
					
					File file = new File(AppCore.getInstance().getCurrentPath() + "\\" + uifile.getFileName());
					SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy");
					
					if(file.isDirectory()){
						if(file.listFiles() != null)
							size = size + folderSize(file);
					} else {
						size = size + file.length();
					}
					
					AppCore.getInstance().setFileInfo(formatFileSize(size), 
										selectedIndex.length>1 ? " " : sdf.format(file.lastModified()));
					
					if(getSelectedIndex()==0 && selectedIndex.length == 1) 
						AppCore.getInstance().setFileInfo(" ", " ");
				}
			}
			
		} else {
			AppCore.getInstance().setFileInfo(" ", " ");
		}
		
		if(getSelectedIndex()>0)
			AppCore.getInstance().getActionManager().getDeleteAction().setEnabled(true);
		else 
			AppCore.getInstance().getActionManager().getDeleteAction().setEnabled(false);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount()==2){
			Object o=getModel().getElementAt(getSelectedIndex());
			if (o instanceof UIAbstractFile){
				UIAbstractFile uifile = (UIAbstractFile)o;
			    
				if (uifile.isDirectory()){
				    setModel(new ListaModel(uifile.getPath()));
			    }else{
			    	
			    	FileView fileView = new FileView(uifile);
			    	int tabCount = AppCore.getInstance().getFramework().getTabCount();
			    	
			    	if(tabCount == 0){
			    		AppCore.getInstance().setFileView(fileView);
			    		return;
			    	}
			    	
			    	for(int i=0;i<tabCount;i++){
			    		FileView file = (FileView) AppCore.getInstance().getFramework().getComponentAt(i);

			    		String p1 = file.getUiFile().getPath()+file.getUiFile().getFileName();
			    		String p2 = uifile.getPath()+uifile.getFileName();
			    		
			    		if(p1.equals(p2)){
			    			AppCore.getInstance().getFramework().setSelectedIndex(i);
			    			return;
			    		}
			    	}
			    	
			    	AppCore.getInstance().setFileView(fileView);
			    }
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public static long folderSize(File directory) {
	    long length = 0;
//	    if(!directory.isHidden() && directory.listFiles() != null){
		    for (File file : directory.listFiles()) {
				if (file.isFile())
		        	length += file.length();
		        else
		        	length += folderSize(file);
		    }
//	    }
	    return length;
	}

	public static String formatFileSize(long size) {
	    String hrSize = null;

	    double b = size;
	    double k = size/1024.0;
	    double m = ((size/1024.0)/1024.0);
	    double g = (((size/1024.0)/1024.0)/1024.0);
	    double t = ((((size/1024.0)/1024.0)/1024.0)/1024.0);

	    DecimalFormat dec = new DecimalFormat("0.00");

	    if ( t>1 ) {
	        hrSize = dec.format(t).concat(" TB");
	    } else if ( g>1 ) {
	        hrSize = dec.format(g).concat(" GB");
	    } else if ( m>1 ) {
	        hrSize = dec.format(m).concat(" MB");
	    } else if ( k>1 ) {
	        hrSize = dec.format(k).concat(" KB");
	    } else {
	        hrSize = dec.format(b).concat(" Bytes");
	    }

	    return hrSize;
	}
	
}
