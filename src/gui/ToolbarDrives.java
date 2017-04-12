package gui;

import java.io.File;

import javax.swing.JToolBar;

import actions.OpenDirAction;

public class ToolbarDrives extends JToolBar{

	private static final long serialVersionUID = 1L;

	public ToolbarDrives(){
		
		setFloatable(false);
	    File[] roots = File.listRoots();
	        for (int index = 0; index < roots.length; index++){
	        	if(roots[index].canRead()){
	        		add(new OpenDirAction(roots[index].toString()));
	            	addSeparator();
	        	}
	        }
	}
	
}
