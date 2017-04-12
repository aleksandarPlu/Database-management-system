package gui;

import javax.swing.JToolBar;

import app.AppCore;

public class Toolbar extends JToolBar{

	private static final long serialVersionUID = 1L;

	public Toolbar(){
		
		add(AppCore.getInstance().getActionManager().getNewFileAction());
		addSeparator();
		add(AppCore.getInstance().getActionManager().getCloseFileAction());
		addSeparator();
		add(AppCore.getInstance().getActionManager().getCloseAllFileAction());
		addSeparator();
		add(AppCore.getInstance().getActionManager().getSaveFileAction());
		addSeparator();
		add(AppCore.getInstance().getActionManager().getSaveAsFileAction());
		addSeparator();
		add(AppCore.getInstance().getActionManager().getDeleteAction());
		addSeparator();
		add(AppCore.getInstance().getActionManager().getHelpAboutAction());
		
		setFloatable(false);
	}
	
}
