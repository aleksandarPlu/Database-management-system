package view;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.file.UIFileField;


/**
 * Klasa koja služi za dijalog korisnika sa aplikacijom
 * Ona služi za unos parametara za sortiranje datoteke
 *  
 * 
 * @author Igor Z.
 *
 */
@SuppressWarnings("serial")
public class PrepareSortFile extends JDialog{


	// ulazna polja dijaloga, služe da korisnik unese vrednosti polja 
	private HashMap<String,JCheckBox> sortFields=new HashMap<String,JCheckBox>();
	private HashMap<String,JComboBox> typeSort=new HashMap<String,JComboBox>();
	
	
	/**
	 * Konstruktor na osnovu opisa polja datoteke priprema dijalog
	 * za unos podataka od strane korisnika na osnovu opisa polja
	 * datoteke. Opis polja se prosleđuje u parametrima konstruktora
	 */
	public PrepareSortFile(String title,final ArrayList<UIFileField> fields) {
		super();
		super.setTitle(title);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		setLayout(new GridLayout(fields.size()+1,1));
		ArrayList<JPanel> boxes=new ArrayList<JPanel>(); 
		
		for (int i=0;i<fields.size();i++){
			String []tipovi={"Ascending","Descending"};
			
			sortFields.put(fields.get(i).getFieldName(), new JCheckBox());
			sortFields.get(fields.get(i).getFieldName()).setActionCommand(fields.get(i).getFieldName());
			sortFields.get(fields.get(i).getFieldName()).setSelected(fields.get(i).isSort());
			sortFields.get(fields.get(i).getFieldName()).addActionListener(new ActionListener(){
				

				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					typeSort.get(arg0.getActionCommand()).setVisible(sortFields.get(arg0.getActionCommand()).isSelected());
					
				}
				
			});
			
			
			typeSort.put(fields.get(i).getFieldName(), new JComboBox(tipovi));
			typeSort.get(fields.get(i).getFieldName()).setVisible(fields.get(i).isSort());
			typeSort.get(fields.get(i).getFieldName()).setSelectedItem(fields.get(i).isAsc()?"Ascending":"Descending");
			
			
			boxes.add(new JPanel(new FlowLayout(FlowLayout.LEFT)));
			boxes.get(i).add(new JLabel(" "+fields.get(i).getFieldName()));
			boxes.get(i).add(sortFields.get(fields.get(i).getFieldName()));
			boxes.get(i).add(typeSort.get(fields.get(i).getFieldName()));
			
	
 	 	    add(boxes.get(i));
			
		}
		
		Box boxC=new Box(BoxLayout.X_AXIS);
		JButton btnOK=new JButton("ok");
		btnOK.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				for (int i=0;i<fields.size();i++){
					fields.get(i).setSort(sortFields.get(fields.get(i).getFieldName()).isSelected());
					fields.get(i).setAsc(typeSort.get(fields.get(i).getFieldName()).getSelectedIndex()==0);
				}
				setVisible(false);
			}
			
		});
		boxC.add(Box.createHorizontalGlue());
		boxC.add(btnOK);
		add(boxC);
		pack();
		setLocationRelativeTo(null);
	}

}