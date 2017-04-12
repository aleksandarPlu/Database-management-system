package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import model.db.UIDBFile;
import model.file.UIAbstractFile;
import model.file.UIFileField;
import model.file.UISERFile;


/**
 * Klasa koja sluzi za dijalog korisnika sa aplikacijom
 * Ona sluzi za unos parametara za unos sloga, izmenu sloga, brisanje sloga i pretragu datoteke
 * Na osnovu liste opisa polja datoteke automatski se formira dijalog 
 */
@SuppressWarnings("serial")
public class AUFRow extends JDialog{

	// ulazna polja dijaloga, služe da korisnik unese vrednosti polja 
	private HashMap<String,JTextField> inputFields=new HashMap<String,JTextField>();
	
	//rezultati unosa polja u formi kolekcije
	private ArrayList<String> resultRecord = null;
	private List<List<String>> addRecord = null;
	
	private JRadioButton vrstaOdTekucegSloga;
	private JRadioButton slogJedan;
	private JRadioButton rezultatiRAM;
	
	/**
	 * Konstruktor na osnovu opisa polja datoteke priprema dijalog
	 * za unos podataka od strane korisnika na osnovu opisa polja
	 * datoteke. Opis polja se prosleduje u parametrima konstruktora
	 */
	public AUFRow(String title,final ArrayList<UIFileField> fields, final UIAbstractFile ui) {
		super();
		super.setTitle(title);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setLayout(new BorderLayout());
		
		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();
		
		leftPanel.setLayout(new GridLayout(fields.size()+1,1));
		rightPanel.setLayout(new GridLayout(fields.size()+1,1));
		
		ArrayList<JPanel> namePanel = new ArrayList<JPanel>(); 
		ArrayList<JPanel> inputFieldsPanel = new ArrayList<JPanel>();
		
		for (int i=0;i<fields.size();i++){
			namePanel.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
			String name = Character.toUpperCase(fields.get(i).getFieldName().charAt(0)) 
					+ fields.get(i).getFieldName().toLowerCase().substring(1);
			
			namePanel.get(i).add(new JLabel(" " + name + ": "));
			
			if(!fields.get(i).getFieldName().equals("IS_DELETED")){
				leftPanel.add(namePanel.get(i));
			}
		}
		
		if(ui.getMODE()!=5 || !ui.getHeaderName().contains(".ser")) {
			leftPanel.add(new JPanel());
		} else {
			//VRSTA
			JPanel vrstaPanel = new JPanel();
			
			TitledBorder titlVrsta = BorderFactory.createTitledBorder("Vrsta pretrage");
			vrstaPanel.setBorder(titlVrsta);
			vrstaPanel.setLayout(new GridLayout(2,1));
			
			ButtonGroup vrstaGroup = new ButtonGroup();
			vrstaOdTekucegSloga = new JRadioButton("Od tekuceg sloga");
			JRadioButton vrstaOdPocekta = new JRadioButton("Od pocetka");
			vrstaGroup.add(vrstaOdTekucegSloga);
			vrstaGroup.add(vrstaOdPocekta);

			vrstaPanel.add(vrstaOdTekucegSloga);
			vrstaPanel.add(vrstaOdPocekta);

			vrstaOdTekucegSloga.setSelected(true);
						
			//BROJ SLOGOVA
			JPanel slogPanel = new JPanel();
			
			TitledBorder titlSlog = BorderFactory.createTitledBorder("Broj slogova");
			slogPanel.setBorder(titlSlog);
			slogPanel.setLayout(new GridLayout(2,1));
			
			ButtonGroup slogGroup = new ButtonGroup();
			slogJedan = new JRadioButton("Prvi slog");
			JRadioButton slogSvi = new JRadioButton("Svi slogovi");
			slogGroup.add(slogJedan);
			slogGroup.add(slogSvi);
			
			slogPanel.add(slogJedan);
			slogPanel.add(slogSvi);
			
			slogJedan.setSelected(true);
			
			//REZULTATI
			JPanel rezultatiPanel = new JPanel();
			
			TitledBorder titlRezultati = BorderFactory.createTitledBorder("Rezultate smestiti");
			rezultatiPanel.setBorder(titlRezultati);
			rezultatiPanel.setLayout(new GridLayout(2,1));
			
			ButtonGroup rezultatiGroup = new ButtonGroup();
			rezultatiRAM = new JRadioButton("U radnu memoriju aplikacije");
			JRadioButton rezultatiDatoteka = new JRadioButton("U novu datoteku");
			rezultatiGroup.add(rezultatiRAM);
			rezultatiGroup.add(rezultatiDatoteka);
			
			rezultatiPanel.add(rezultatiRAM);
			rezultatiPanel.add(rezultatiDatoteka);
			
			rezultatiRAM.setSelected(true);
			
			//VRSTA -- SLOG -- REZULTATI
			JPanel detailPanel = new JPanel();
			detailPanel.setLayout(new BorderLayout());
			detailPanel.add(vrstaPanel, BorderLayout.WEST);
			detailPanel.add(slogPanel, BorderLayout.CENTER);
			detailPanel.add(rezultatiPanel, BorderLayout.EAST);
			
			add(detailPanel, BorderLayout.SOUTH);
		}
		
		for (int i=0;i<fields.size();i++){
			
	        inputFields.put(fields.get(i).getFieldName(), new JTextField(fields.get(i).getFieldLength()));
	        
			inputFieldsPanel.add(new JPanel(new FlowLayout(FlowLayout.LEFT)));
			inputFieldsPanel.get(i).add(inputFields.get(fields.get(i).getFieldName()));
			
//			int lenght = fields.get(i).getFieldLength();
//			if(lenght > 40) lenght = 40;
//			
//			inputFields.get(fields.get(i).getFieldName()).setColumns(lenght);
			
			//polja koja su primarni kljucevu posebno obeleyavan
			if (fields.get(i).isFieldPK()){
		     	inputFields.get(fields.get(i).getFieldName()).setBackground(Color.GRAY);
		     	inputFields.get(fields.get(i).getFieldName()).setForeground(Color.WHITE);
			}
			
			if(!fields.get(i).getFieldName().equals("IS_DELETED")){
				rightPanel.add(inputFieldsPanel.get(i));
			}
		}
		
		JPanel okPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		if(ui.getMODE()!=UIAbstractFile.ADD_MODE) {
			JButton btnOK=new JButton("OK");
			btnOK.setSize(70,70);
			btnOK.addActionListener(new ActionListener(){
	
				public void actionPerformed(ActionEvent arg0) {
					resultRecord=new ArrayList<String>();
					for (int i=0;i<fields.size();i++){
						if(ui instanceof UIDBFile){
							if(ui.getMODE()!=UIAbstractFile.FIND_MODE)
								resultRecord.add(getFieldValueForDB(fields.get(i).getFieldName()));
							else 
								resultRecord.add(inputFields.get(fields.get(i).getFieldName()).getText());
						} else
							resultRecord.add(getFieldValue(fields.get(i).getFieldName()));
					}
					setVisible(false);
				}
				
			});
			okPanel.add(btnOK);
		} else {
			JButton btnCancel = new JButton("Cancel");
			btnCancel.setSize(100,70);
			btnCancel.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			
			JButton btnAdd = new JButton("Add");
			addRecord = new ArrayList<List<String>>();
			btnAdd.setSize(100,70);
			btnAdd.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					resultRecord=new ArrayList<String>();
					for (int i=0;i<fields.size();i++){
						if(ui instanceof UIDBFile)
							resultRecord.add(getFieldValueForDB(fields.get(i).getFieldName()));
						else
							resultRecord.add(getFieldValue(fields.get(i).getFieldName()));
					}
					
					String s = "";
					for(String str : resultRecord){
						s+=str.trim();
					}
					
					if((ui instanceof UISERFile)){
						if(s.length() > 1)
							addRecord.add(resultRecord);
					} else {
						if(s.length() > 0)
							addRecord.add(resultRecord);
					}
					
					if(!(ui instanceof UISERFile))
						setVisible(false);
				}
			});

			okPanel.add(btnAdd);
			okPanel.add(btnCancel);
		}

		rightPanel.add(okPanel);
	
		
		add(leftPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
	 
	}

	/**
	 *  Za zadatu polje vraca unetu vrednost dopunjenu praznim karekterima
	 *  do pune dužine polja. Ovim je omoguceno rad sa slogovima
	 *  fiksne dužine 
	 */
    private String getFieldValue(String fieldName){
    	String fieldValue=inputFields.get(fieldName).getText();
    	
    	if(fieldName.equals("IS_DELETED")){
    		fieldValue = "0 ";
    		return fieldValue;
    	}
    	
    	//ako je korisnik uneo dužinu vecu od dozvoljene odsecamo:
    	if (fieldValue.length()>inputFields.get(fieldName).getColumns()) {
    		fieldValue = fieldValue.substring(0,inputFields.get(fieldName).getColumns());
        	inputFields.get(fieldName).setText("");
    	    return fieldValue;	
    	}
    	
    	for (int i=inputFields.get(fieldName).getText().length();i<inputFields.get(fieldName).getColumns();i++)
    		fieldValue=fieldValue+' ';
    	
    	inputFields.get(fieldName).setText("");
    	return fieldValue;
    }
    
    private String getFieldValueForDB(String fieldName){
    	String fieldValue=inputFields.get(fieldName).getText();
    	
    	//ako je korisnik uneo dužinu vecu od dozvoljene odsecamo:
    	if (fieldValue.length()>inputFields.get(fieldName).getColumns()) {
    		fieldValue = fieldValue.substring(0,inputFields.get(fieldName).getColumns());
        	inputFields.get(fieldName).setText("");
    	    return fieldValue;	
    	}
    	
    	inputFields.get(fieldName).setText("");
    	return fieldValue;
    }

	public ArrayList<String> getResultRecord() {
		return resultRecord;
	}

	public JRadioButton getVrstaOdTekucegSloga() {
		return vrstaOdTekucegSloga;
	}

	public void setVrstaOdTekucegSloga(JRadioButton vrstaOdTekucegSloga) {
		this.vrstaOdTekucegSloga = vrstaOdTekucegSloga;
	}

	public JRadioButton getSlogJedan() {
		return slogJedan;
	}

	public void setSlogJedan(JRadioButton slogJedan) {
		this.slogJedan = slogJedan;
	}

	public JRadioButton getRezultatiRAM() {
		return rezultatiRAM;
	}

	public void setRezultatiRAM(JRadioButton rezultatiRAM) {
		this.rezultatiRAM = rezultatiRAM;
	}

	public List<List<String>> getAddRecord() {
		return addRecord;
	}

	public HashMap<String, JTextField> getInputFields() {
		return inputFields;
	}
	
}
