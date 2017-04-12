package actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import app.AppCore;
import model.file.UISERFile;
import view.FileView;

public class NewFileAction extends AbstractUIAction{

	private static final long serialVersionUID = 1L;
	
	public NewFileAction() {
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		putValue(MNEMONIC_KEY, KeyEvent.VK_N);
		putValue(SMALL_ICON, loadIcon("images/newFile.png"));
		putValue(NAME, "Open drive");
		putValue(SHORT_DESCRIPTION, "Open drive");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		UISERFile uiFile = new UISERFile(null, "New File", false);
		FileView fileView = new FileView(uiFile);
		Icon icon = new ImageIcon(getClass().getResource("images/tab.png"));
		AppCore.getInstance().getFramework().addTab(uiFile.getFileName(), icon, fileView, null);
		AppCore.getInstance().getFramework().setSelectedComponent(fileView);
		AppCore.getInstance().setFileView(fileView);
	}

}
