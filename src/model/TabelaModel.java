package model;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import view.FileView;
import app.AppCore;
import model.file.UIAbstractFile;
import model.file.UIFileField;

public class TabelaModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;

	public TabelaModel(ArrayList<UIFileField> fields,Object[][] data) {
		  //prvi parametar u konstruktoru nadklase se podaci u obliku matrice
		  //drugi parametar u konstruktoru su nazivi polja u headeru tabele 
		
		try{
			setDataVector(data,fields.toArray());			
		}catch(OutOfMemoryError e){
			JOptionPane.showMessageDialog(AppCore.getInstance(),e.getMessage(),"Greška",JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		FileView fileView = (FileView) AppCore.getInstance().getFramework().getSelectedComponent();
		UIAbstractFile uiFile = fileView.getUiFile();
		
		if(uiFile.getFileName().contains(".db")){
			return false;
		}

		if(getColumnName(getColumnCount()-1).equals("IS_DELETED"))
			if(getValueAt(row, getColumnCount()-1).equals("1"))
				return false;
		
		
		for(UIFileField uiField : fileView.getUiFile().getFields()){
			if(uiField.getFieldName().equals(getColumnName(column))){
				if(uiField.isFieldPK())
					return false;
				else 
					return true;
			}
		}
		return false;
	}
	
}
