package gui;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import app.AppCore;

public class Menu extends JMenuBar{

	private static final long serialVersionUID = 1L;

	public Menu(){
		
		JMenu file = new JMenu("File");
		JMenu db   = new JMenu("Database");
		JMenu help = new JMenu("Help");
		
		file.setMnemonic(KeyEvent.VK_F);
		help.setMnemonic(KeyEvent.VK_H);
		
		file.add(AppCore.getInstance().getActionManager().getNewFileAction());
		file.addSeparator();
		file.add(AppCore.getInstance().getActionManager().getCloseFileAction());
		file.add(AppCore.getInstance().getActionManager().getCloseAllFileAction());
		file.addSeparator();
		file.add(AppCore.getInstance().getActionManager().getSaveFileAction());
		file.add(AppCore.getInstance().getActionManager().getSaveAsFileAction());
		file.addSeparator();
		file.add(AppCore.getInstance().getActionManager().getDeleteAction());
		
		db.add(AppCore.getInstance().getActionManager().getDbLoginAction());
		
		help.add(AppCore.getInstance().getActionManager().getHelpAboutAction());
		
		
		add(file);
		add(db);
		add(help);
	}
	
}
