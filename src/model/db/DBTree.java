package model.db;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import app.AppCore;
import view.FileView;

@SuppressWarnings("serial")
public class DBTree extends JTree implements MouseListener{
	
	public DBTree(){
		super(new DefaultTreeModel(new DBNode("no database",DBNode.DATABASE,"", 0, 0, false)));
		addMouseListener(this);
		setCellRenderer(new DBTreeCellRendered());
		setRowHeight(0);
	}
	
	/*
	 * Prilikom duplog klika na stablo koje predstavlja strukturu otvorene baze podataka,
	 * proverava se da li je korisnik kliknuo na cvor koji predstavlja tabelu baze podataka,
	 * ukoliko jeste pravi se nova instanca klase UIDBFile kojoj se prosledjuju ime
	 * tabele koju je korisnik odabrao. Klasa UIDBFile takodje nasledjuje klasu
	 * UIAbstractFile i u sebi sadrži imlementaciju metoda za ucitavanje podataka,
	 * dodavanje, izmenu, brisanje, pretragu i sortiranje
	 */
	public void mouseClicked(MouseEvent e) {
		if (e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==2){
			DBNode dbNode=(DBNode) this.getLastSelectedPathComponent();
			if (dbNode.getType()==DBNode.TABLE){
				UIDBFile uidbfile=new UIDBFile(dbNode.getName());
				FileView fileView=new FileView(uidbfile);
				AppCore.getInstance().setFileView(fileView);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

}
