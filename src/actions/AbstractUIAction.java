package actions;

import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public abstract class AbstractUIAction extends AbstractAction{

	private static final long serialVersionUID = 1L;

	public Icon loadIcon(String fileName){
		URL imageURL = getClass().getResource(fileName);
		Icon icon = null;
		
		if (imageURL != null) {                      
	        icon = new ImageIcon(imageURL);
	    } else {                                     
	        System.err.println("Resource not found: " + fileName);
	    }

		return icon;
	}
	
}
