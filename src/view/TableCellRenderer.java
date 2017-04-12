package view;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TableCellRenderer extends DefaultTableCellRenderer{

	private static final long serialVersionUID = 1L;
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,	 
			boolean hasFocus, int row, int col){
		
		Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		
//		String s =  table.getModel().getValueAt(row, table.getColumnCount()-1).toString();
		
//		if(s.equalsIgnoreCase("1")){
//			comp.setEnabled(false);
//		} else {
//			comp.setEnabled(true);
//		}
		
		return( comp );
	}

}
