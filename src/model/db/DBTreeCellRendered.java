package model.db;

import java.awt.Component;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("serial")
public class DBTreeCellRendered extends DefaultTreeCellRenderer {

	public DBTreeCellRendered() {
		//setOpaque(true);
	
		// TODO Auto-generated constructor stub
	}

	public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel,expanded, leaf, row,hasFocus);
                  
		DBNode node =(DBNode)value;

		if (node.getType()==DBNode.COLUMN){
			URL imageURL = null;
			
			if (node.isPK()){
				imageURL = getClass().getResource("images/pk.jpg");
			} else if(node.isFK()){
				imageURL = getClass().getResource("images/fk.png");
			}else if(node.isLeaf()) {
				imageURL = getClass().getResource("images/file.png");
			} else {
				imageURL = getClass().getResource("images/folder.png");
			}
			
			Icon icon = null;
			if (imageURL != null)                       
				icon = new ImageIcon(imageURL);
			setIcon(icon);
		
		} else if (node.getType()==DBNode.DATABASE){
			URL imageURL = getClass().getResource("images/db.jpg");
			Icon icon = null;
			if (imageURL != null)                       
				icon = new ImageIcon(imageURL);
			setIcon(icon);
			
		} else if (node.getType()==DBNode.FOLDER || node.getType()==DBNode.TABLE){
			URL imageURL = getClass().getResource("images/folder.png");
			Icon icon = null;
			if (imageURL != null)                       
				icon = new ImageIcon(imageURL);
			setIcon(icon);
		} else if (node.getType()==DBNode.PRIMARY_KEY){
			URL imageURL = getClass().getResource("images/pk.jpg");
			Icon icon = null;
			if (imageURL != null)                       
				icon = new ImageIcon(imageURL);
			setIcon(icon);
		} else if (node.getType()==DBNode.FOREIGN_KEY){
			URL imageURL = getClass().getResource("images/fk.png");
			Icon icon = null;
			if (imageURL != null)                       
				icon = new ImageIcon(imageURL);
			setIcon(icon);
			
		}
		
		return this;
	  }
}