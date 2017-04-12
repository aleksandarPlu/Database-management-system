package view;

import java.awt.Component;
import java.net.URL;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

import model.file.UIAbstractFile;
import model.file.UISERFile;

public class ListaCellRenderer extends DefaultListCellRenderer{

	private static final long serialVersionUID = 1L;
	
	public ListaCellRenderer() {

	}

	public Component getListCellRendererComponent(
			JList list,
			Object value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {

		super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
		 if (value instanceof UIAbstractFile){
			 if (((UIAbstractFile)value).isDirectory()){
                URL imageURL = getClass().getResource("images/folder.png");
                Icon icon = null;
                if (imageURL != null)                       
                  icon = new ImageIcon(imageURL);
                setIcon(icon);
			 }else{
				 URL imageURL = getClass().getResource("images/file.png");
                Icon icon = null;
                if (imageURL != null)                       
                  icon = new ImageIcon(imageURL);
                setIcon(icon);				 
			 }
		 }
		 return this;
	}
}
