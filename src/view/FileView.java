package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import db.DatabaseDAO;
import model.TabelaModel;
import model.db.UIDBFile;
import model.file.UIAbstractFile;
import model.file.UIINDFile;
import model.file.UISEKFile;
import model.file.UISERFile;
import model.tree.Node;
import model.tree.NodeElement;
import model.tree.TreeCellRendered;
import app.AppCore;
import event.UpdateBlockEvent;
import event.UpdateBlockListener;

public class FileView extends JPanel implements UpdateBlockListener, DocumentListener, TreeSelectionListener, KeyListener{

	private static final long serialVersionUID = 1L;
	
	private UIAbstractFile uiFile;
	private JTextArea area;
	private String content;
	
	private JTable table;
	private JTable overZoneTable;

	private JPanel panTop;
	private JScrollPane scr;

	private JTextField txtBlockSize;
	private JTextField txtFileSize;
	private JTextField txtRecordSize;
	private JTextField txtRecordNum;
	private JTextField txtBlockNum;
	private JTextField txtAccessNum;

	private JTree indexTree;
	
	public FileView(final UIAbstractFile uiFile){
		this.uiFile = uiFile;
		uiFile.setFILE_POINTER_NEW(0);
		
		setLayout(new BorderLayout());
		
		try {
			this.uiFile.readHeader();	
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.uiFile.addUpdateBlockListener(this);
		
		panTop = new JPanel(new BorderLayout());
		
		if (!uiFile.getHeaderName().contains(".db")){
			initPanParams();
		}
		
		initPanToolbar();
		
		add(panTop,BorderLayout.NORTH);
		
		table=new JTable();
		setTableModel(table);
//		table.setModel(new TabelaModel(uiFile.getFields(),uiFile.getData()));
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		if(uiFile.getHeaderName().contains(".db")){
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(table.getSelectedRow()!=-1){
					if(uiFile.getExportedKeys().size()!=0){
						Map<String, String> query = new HashMap<String, String>();
						for(int i=0;i<uiFile.getExportedKeys().size();i++){
							Map<String, String> ekMap = uiFile.getExportedKeys().get(i);
							String fkTable = ekMap.get("FKTABLE_NAME");
							
							if(query.get(fkTable)==null){
								String s = "SELECT * FROM " + fkTable + " WHERE " + ekMap.get("FKCOLUMN_NAME") + "='" + 
									table.getValueAt(table.getSelectedRow(), getColumnByName(table, ekMap.get("PKCOLUMN_NAME"))) + "'";
								
								query.put(fkTable, s);
							} else {
								String s = query.get(fkTable);
								s+= " AND " + ekMap.get("FKCOLUMN_NAME") + "='" + 
										table.getValueAt(table.getSelectedRow(), getColumnByName(table, ekMap.get("PKCOLUMN_NAME"))) + "'";
								
								query.put(fkTable, s);
							}
						}
						
						for(int i=0;i<((UIDBFile) uiFile).getFkTableName().size();i++){
							
							String[][] realData = DatabaseDAO.getInstance().exportedKeys(query, ((UIDBFile) uiFile).getFkTableName().get(i), ((UIDBFile) uiFile).getUidbArrayList().get(i));
							
							((UIDBFile) uiFile).getMcTableArrayList().get(i).setModel(new TabelaModel(((UIDBFile) uiFile).getUidbArrayList().get(i).getFields(),realData));
						}
					}
				} else {
					for(int i=0;i<((UIDBFile) uiFile).getFkTableName().size();i++){
						((UIDBFile) uiFile).getMcTableArrayList().get(i).setModel(new TabelaModel(((UIDBFile) uiFile).getUidbArrayList().get(i).getFields(),null));	
					}
				}
			}
		});
		
		}
		
		
		table.addMouseListener(new MouseAdapter() {
	
			@Override
			public void mousePressed(MouseEvent e) {
				if(table.getEditingRow()!=-1){
					setTableModel(table);
				}
			}
				
		});		
		
		if(!(uiFile instanceof UIDBFile)){
			table.addKeyListener(this);
		}
		
		scr=new JScrollPane(table);
		
		if (uiFile.getHeaderName().contains(".ind")){
			//zona prekoracenja u novoj tabeli, ista struktura kao i osnovni fajl
			
			overZoneTable=new JTable();
			setTableModel(overZoneTable);
//			overZoneTable.setModel( new TabelaModel(uiFile.getFields(),uiFile.getData()));
			JScrollPane scrOZT=new JScrollPane(overZoneTable);
			
		    JSplitPane splitVer=new JSplitPane(JSplitPane.VERTICAL_SPLIT,scr,scrOZT);
		    splitVer.setDividerLocation(400);
		    DefaultTreeModel treeModel = null;
		    
		    treeModel = new DefaultTreeModel(((UIINDFile)uiFile).getTree().getRootElement());
		    
		    indexTree=new JTree(treeModel);
		    TreeCellRendered rendered=new TreeCellRendered();
		    indexTree.setCellRenderer(rendered);
		    indexTree.addTreeSelectionListener(this);
		    indexTree.setRowHeight(0);
		    indexTree.addMouseListener(ml);
		    
			JScrollPane scTree=new JScrollPane(indexTree);
			JSplitPane splitHor=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,scTree,splitVer);
			splitHor.setDividerLocation(300);
			add(splitHor,BorderLayout.CENTER);
			
		} else if(uiFile.getHeaderName().contains(".db")){
			if(((UIDBFile)uiFile).getFkTableName().size()==0){
				add(scr,BorderLayout.CENTER);
			} else {
				JTabbedPane mcTabbedPane = new JTabbedPane();
				JSplitPane splitVer = null;
				for(int i=0;i<((UIDBFile)uiFile).getFkTableName().size();i++){
					UIDBFile uidbfile = ((UIDBFile)uiFile).getUidbArrayList().get(i);
					FileView fileView=new FileView(uidbfile);
					
					mcTabbedPane.setBackground(Color.GRAY);
					mcTabbedPane.addTab(fileView.getUiFile().getFileName(), null, fileView, null);
					mcTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
					((UIDBFile) uiFile).getMcTableArrayList().add(fileView.getTable());
				}

				splitVer = new JSplitPane(JSplitPane.VERTICAL_SPLIT,scr,mcTabbedPane);
			    splitVer.setDividerLocation(150);
				
				add(splitVer,BorderLayout.CENTER);
			}
			
		} else{
			add(scr,BorderLayout.CENTER);
		} 
		
//		area=new JTextArea();
//		
//		area.setText(uiFile.readFile());
//		content = area.getText();
//		
//		area.getDocument().addDocumentListener(this);
//
//		JScrollPane scroll=new JScrollPane(area);
//		add(scroll);
	}

	private void initPanParams(){
		JPanel panParams=new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		//velicina bloka - moze da se menja
		panParams.add(new JLabel("f (block factor):"));
		txtBlockSize=new JTextField();
		txtBlockSize.setColumns(5);
		txtBlockSize.setText(String.valueOf(uiFile.getBLOCK_FACTOR()));
		panParams.add(txtBlockSize);
		JButton btnChangeBS=new JButton("Change f");
		btnChangeBS.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				 
				uiFile.setBLOCK_SIZE(Integer.valueOf(txtBlockSize.getText()).longValue());
				txtBlockNum.setText(String.valueOf(uiFile.getBLOCK_NUM()));
				
			}
			
		});
		panParams.add(btnChangeBS);
		
		//velicina datoteke
		panParams.add(new JLabel("File size:"));
		txtFileSize=new JTextField();
		txtFileSize.setColumns(7);
		txtFileSize.setEnabled(false);
		
		txtFileSize.setText(String.valueOf(Math.ceil(uiFile.getFILE_SIZE()/1024.0000))+"KB");
		panParams.add(txtFileSize);
		
		//velicina linije u datoteci  
		panParams.add(new JLabel("Record size(B):"));
		txtRecordSize=new JTextField();
		txtRecordSize.setColumns(7);
		txtRecordSize.setEnabled(false);
		txtRecordSize.setText(String.valueOf(uiFile.getRECORD_SIZE()));
		panParams.add(txtRecordSize);
		
		//broj recorda u datoteci  
		panParams.add(new JLabel("Record num:"));
		txtRecordNum=new JTextField();
		txtRecordNum.setColumns(7);
		txtRecordNum.setEnabled(false);
		txtRecordNum.setText(String.valueOf(uiFile.getRECORD_NUM()));
		panParams.add(txtRecordNum);
		
		//broj blokova u datoteci  
		panParams.add(new JLabel("Block num:"));
		txtBlockNum=new JTextField();
		txtBlockNum.setColumns(7);
		txtBlockNum.setEnabled(false);
		txtBlockNum.setText(String.valueOf(uiFile.getBLOCK_NUM()));
		panParams.add(txtBlockNum);

		if (uiFile.getHeaderName().contains(".ser")){
			//broj pristupa datoteci prilikom trazenja odredjenog sloga
			panParams.add(new JLabel("Access num:"));
			txtAccessNum=new JTextField();
			txtAccessNum.setColumns(7);
			txtAccessNum.setEnabled(false);
			txtAccessNum.setText(String.valueOf(uiFile.getACCESS_NUM()));
			panParams.add(txtAccessNum);		
		}
		
		panParams.setBackground(new Color(153,204,255));
		panTop.add(panParams, BorderLayout.NORTH);
    }

     
    private void initPanToolbar(){
    	JPanel panToolbar=new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	
		JButton btnFetch=new JButton("Fetch next block");
		
		if (uiFile.getHeaderName().contains(".db")){
			btnFetch.setText("Refresh");
		}
		
		btnFetch.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				uiFile.setMODE(UIAbstractFile.BROWSE_MODE);
				try {
					uiFile.fetchNextBlock();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		});
		panToolbar.add(btnFetch);	
		
		
		JButton btnAdd=new JButton("Add Record");
		btnAdd.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				uiFile.setMODE(UIAbstractFile.ADD_MODE);
				
				AUFRow addRow=new AUFRow(arg0.getActionCommand(),uiFile.getFields(), uiFile);
				
				addRow.setModal(true);
				addRow.setVisible(true);
				
				if(!addRow.isDisplayable()){
					return;
				}
				
				if(!addRow.getAddRecord().isEmpty()){
					try {
						boolean save = uiFile.addRecord(addRow.getAddRecord());
						
						if(save){
							int[] position = new int[1];
							position[0]=-1;
							
							ArrayList<String> searchRec = new ArrayList<String>();
							List<List<String>> list = addRow.getAddRecord();
							List<String> li = list.get(list.size()-1);
							
							for(int i=0;i<li.size();i++){
								searchRec.add(li.get(i));
							}
							
							if (!uiFile.getHeaderName().contains(".ser")){
								uiFile.findRecord(searchRec, position, false, false, false);
								table.getSelectionModel().setSelectionInterval(position[0],position[0]);
								table.scrollRectToVisible(table.getCellRect(table.getSelectedRow(), 0, false));
							} else {
//								uiFile.fetchNextBlock();
//								uiFile.setFILE_POINTER((uiFile.getFILE_SIZE())-
//										((uiFile.getBLOCK_FACTOR()-1)*uiFile.getRECORD_SIZE()));
//								uiFile.fetchNextBlock();
//								table.getSelectionModel().setSelectionInterval(table.getRowCount()-1, table.getRowCount()-1);
							}
							
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			
		});
		panToolbar.add(btnAdd);		

		JButton btnUpdate=new JButton("Update Record");
		btnUpdate.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				uiFile.setMODE(UIAbstractFile.UPDATE_MODE);
				
				if(table.getSelectedRow()==-1){
					JOptionPane.showMessageDialog(null, "Niste selektovali nijedan slog u tabeli.", "UI Project", 1);
					return;
				}
				
				AUFRow addRow=new AUFRow(arg0.getActionCommand(),uiFile.getFields(), uiFile);
				
				ArrayList<String> oldRecord = new ArrayList<String>();
				for (int col=0;col<uiFile.getFields().size();col++){
					addRow.getInputFields().get(uiFile.getFields().get(col).getFieldName()).setText((String) table.getModel().getValueAt(table.getSelectedRow(), col));
					oldRecord.add((String) table.getModel().getValueAt(table.getSelectedRow(), col));
				}
				
				addRow.setModal(true);
				addRow.setVisible(true);
				
				if(!addRow.isDisplayable()){
					return;
				}
				
				try {
					ArrayList<String> newRecord = new ArrayList<String>();
					for(String s: addRow.getResultRecord()){
						if(!s.trim().equals("")){
							newRecord.add(s.trim());
						} else {
							newRecord.add(null);
						}
					}
					
					boolean compare = false;
					for(int i=0;i<oldRecord.size();i++){
						if(oldRecord.get(i)==null && newRecord.get(i)!=null){
							compare = true;
						} else if(oldRecord.get(i)!=null && !oldRecord.get(i).equals(newRecord.get(i))){
							compare = true;
						}
					}
					
					if(compare){
						boolean save = uiFile.updateRecord(oldRecord, newRecord, table.getSelectedRow());
						
						if(save){	
						int[] position = new int[1];
							position[0]=-1;
							uiFile.findRecord(addRow.getResultRecord(), position, false, false, false);
							table.getSelectionModel().setSelectionInterval(position[0],position[0]);
							table.scrollRectToVisible(table.getCellRect(table.getSelectedRow(), 0, false));
					}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
			
		});
		
		if(uiFile.getHeaderName().contains(".db")){
			panToolbar.add(btnUpdate);	
		}
				
		JButton btnDelete=new JButton("Delete Record");
		btnDelete.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				uiFile.setMODE(UIAbstractFile.DELETE_MODE);

				int row = table.getSelectedRow();
				if (row==-1){
					JOptionPane.showMessageDialog(null, "Niste selektovali nijedan slog u tabeli.", "UI Project", 1);
					return;
				}
				
				ArrayList<String> resultRecord=new ArrayList<String>();
				
				for (int col=0;col<uiFile.getFields().size();col++){
					resultRecord.add((String) table.getModel().getValueAt(row, col));
				}  	

				try {
					uiFile.deleteRecord(resultRecord);
					setTableModel(table);
//					table.setModel(new TabelaModel(uiFile.getFields(),uiFile.getData()));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
			
		});		
		panToolbar.add(btnDelete);		
		
		JButton btnFind=new JButton("Find Record");
		
		btnFind.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				uiFile.setMODE(UIAbstractFile.FIND_MODE);

				AUFRow findRow=new AUFRow(arg0.getActionCommand(),uiFile.getFields(), uiFile);
				findRow.setModal(true);
				findRow.setVisible(true);
				
				if(!findRow.isDisplayable()){
					return;
				}
				
				
				int[] position = new int[1];
				position[0]=-1;
				
				if(findRow.getResultRecord() == null){
					return;
				}
				
				if(uiFile instanceof UIDBFile){
					try {
						((UIDBFile) uiFile).filterFind(findRow.getResultRecord());
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return;
				}
				
				boolean start = false;
				boolean viseSlogova = false;
				boolean snimiURAM = false;
				if (findRow.getVrstaOdTekucegSloga()!=null){
					start = !findRow.getVrstaOdTekucegSloga().isSelected();
					viseSlogova = !findRow.getSlogJedan().isSelected();
					snimiURAM = findRow.getRezultatiRAM().isSelected();
				}
				
				if (!uiFile.findRecord(findRow.getResultRecord(),position,start,viseSlogova,snimiURAM)){
					JOptionPane.showMessageDialog(null, "Traženi slog nije pronadjen.", "UI Project", 1);
					table.getSelectionModel().setSelectionInterval(position[0],position[0]);
				}
		}
			
		});
		panToolbar.add(btnFind);
				
		JButton btnFindInIndex=new JButton("Find in Index");
		btnFindInIndex.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				uiFile.setMODE(UIAbstractFile.FIND_MODE);

				AUFRow findRow=new AUFRow(arg0.getActionCommand(),uiFile.getFields(), uiFile);
				findRow.setModal(true);
				findRow.setVisible(true);
				int[] position = new int[1];
				position[0]=-1;
				
				if(findRow.getResultRecord() == null){
					return;
				}
				
				ArrayList<String> searchPK = new ArrayList<String>();
				for(int i=0;i<uiFile.getFields().size();i++){
					if(uiFile.getFields().get(i).isFieldPK()){
						if(findRow.getResultRecord().get(i).trim().equals("")){
							JOptionPane.showMessageDialog(null, "Za pretragu u index morate uneti sva polja koja su oznacena "
									+ "crnom bojom.", "UI Project", 1);
							return;
						} else {
							searchPK.add(findRow.getResultRecord().get(i));
						}
					} else {
						searchPK.add("");
					}
				}
				
				((UIINDFile)uiFile).setObject();
				boolean postoji = ((UIINDFile)uiFile).findInIndex(findRow.getResultRecord(), searchPK, ((UIINDFile)uiFile).getTree().getRootElement(),position);
					
				if(postoji){
					indexTree.setSelectionPath(new TreePath(((UIINDFile)uiFile).getFindResult().getPath()));
					table.getSelectionModel().setSelectionInterval(position[0],position[0]);
				} else {
					JOptionPane.showMessageDialog(null, "Traženi slog nije pronadjen.", "UI Project", 1);
				}
				
			}
			
		});
		
		if(uiFile.getHeaderName().contains(".ind")){
			panToolbar.add(btnFindInIndex);
		}
		
		JButton btnSortMDI=new JButton("Sort MDI");
		btnSortMDI.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				PrepareSortFile pSort=new PrepareSortFile("Select sort parametars for MDI",uiFile.getFields());
				pSort.setModal(true);
				pSort.setVisible(true);
				
				if(!pSort.isDisplayable()){
					return;
				}
				
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));
					if(uiFile instanceof UIDBFile)
						((UIDBFile)uiFile).sortMDI();
					else
						uiFile.sortMDI();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				
			}
			
		});
       panToolbar.add(btnSortMDI);

		
		JButton btnSortMM=new JButton("Sort MM");
		btnSortMM.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				PrepareSortFile pSort=new PrepareSortFile("Select sort parametars for MM",uiFile.getFields());
				pSort.setModal(true);
				pSort.setVisible(true);
				
				try {
					
					setCursor(new Cursor(Cursor.WAIT_CURSOR));
					uiFile.sortMM();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				
			}
			
		});
		if(!uiFile.getHeaderName().contains(".db")){
			panToolbar.add(btnSortMM);
		}
		
		JButton btnMakeSek=new JButton("Make .sek");
		btnMakeSek.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));
					((UISERFile)uiFile).makeSEKFile();
				} catch (IOException e) {
					
					e.printStackTrace();
				}finally{
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				
			}
			
		});
		
		
		if (uiFile.getHeaderName().contains(".ser")){
			   //samo za serijske datoteke je omogucena ova akcija
			panToolbar.add(btnMakeSek);
		}
		
		//samo za sekvencijalne fajlove, pravljenje indeks sekvencijalne datoteke
		JButton btnMakeIND=new JButton("Make .ind");
		btnMakeIND.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));
					((UISEKFile)uiFile).makeINDFile();
				} catch (IOException e) {
					
					e.printStackTrace();
				}finally{
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				
			}
			
		});
		
		
		if (uiFile.getHeaderName().contains(".sek")){
			//samo za sekvencijalne datoteke je omogucena ova akcija
			panToolbar.add(btnMakeIND);
		}

        panTop.add(panToolbar,BorderLayout.CENTER);		
    }
	
	public UIAbstractFile getUiFile() {
		return uiFile;
	}
	
	@Override
	public void updateBlockPerformed(UpdateBlockEvent e) {
		setTableModel(table);
//		table.setModel(new TabelaModel(uiFile.getFields(),uiFile.getData()));
	}
	
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// implementirati otvaranje bloka koji sadrži slog sa vrednošcu kljuca
		// prvog elementa u cvoru
		Node node= (Node) e.getPath().getLastPathComponent();
		if (node.getChildCount()==0){
			NodeElement nodeElement = node.getData().get(0);
			int newFilePointer =  nodeElement.getBlockAddress()*uiFile.getRECORD_SIZE();

			uiFile.setFILE_POINTER(newFilePointer);
			try {
				uiFile.fetchNextBlock();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void changedUpdate(DocumentEvent arg0) {
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		int index = AppCore.getInstance().getFramework().getSelectedIndex();
		
		if(!content.equals(area.getText()))
			changeDocument(true, index);
		else
			changeDocument(false, index);
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		int index = AppCore.getInstance().getFramework().getSelectedIndex();
		
		if(content.equals(area.getText()))
			changeDocument(false, index);
		else
			changeDocument(true, index);
	}

	public JTextArea getArea() {
		return area;
	}
	
	public void changeDocument(boolean change, int index){
		if(change){
			if(!getUiFile().isChanged()){
				AppCore.getInstance().getFramework().setTitleAt(index, 
						AppCore.getInstance().getFramework().getTitleAt(index) + "*");
				getUiFile().setChanged(true);
				
				AppCore.getInstance().getActionManager().getSaveFileAction().setEnabled(true);
			}
		} else {
			AppCore.getInstance().getFramework().setTitleAt(index, uiFile.getFileName());
			getUiFile().setChanged(false);

			AppCore.getInstance().getActionManager().getSaveFileAction().setEnabled(false);
		}
	}

	public JTable getTable() {
		return table;
	}

	/**
	 *  Za zadatu polje vraca unetu vrednost dopunjenu praznim karekterima
	 *  do pune dužine polja. Ovim je omoguceno rad sa slogovima
	 *  fiksne dužine 
	 */
    private String getFieldValue(String fieldValue, int lenght){
    	//ako je korisnik uneo dužinu vecu od dozvoljene odsecamo:
    	if (fieldValue.length() > lenght) {
    	    return fieldValue.substring(0,lenght);	
    	}
    	
    	for (int i=fieldValue.length();i<lenght;i++)
    		fieldValue=fieldValue+' ';
    	
    	return fieldValue;
    }
    
    public void newFileView(){
    	FileView file = (FileView) AppCore.getInstance().getFramework().getSelectedComponent();
		file.remove(scr);
		
    	table=new JTable();
    	setTableModel(table);
//		table.setModel(new TabelaModel(uiFile.getFields(),uiFile.getData()));
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if(table.getEditingRow()!=-1){
					setTableModel(table);
//					table.setModel(new TabelaModel(uiFile.getFields(),uiFile.getData()));
				}
			}
			
		});		
		
		table.addKeyListener(this);

		scr=new JScrollPane(table);
    	
		overZoneTable=new JTable();
		setTableModel(overZoneTable);
//		overZoneTable.setModel( new TabelaModel(uiFile.getFields(),uiFile.getData()));
		JScrollPane scrOZT=new JScrollPane(overZoneTable);
		
	    JSplitPane splitVer=new JSplitPane(JSplitPane.VERTICAL_SPLIT,scr,scrOZT);
	    splitVer.setDividerLocation(400);
	    DefaultTreeModel treeModel = null;
	    
	    treeModel = new DefaultTreeModel(((UISEKFile)uiFile).getTree().getRootElement());
	    
	    indexTree=new JTree(treeModel);
	    TreeCellRendered rendered=new TreeCellRendered();
	    indexTree.setCellRenderer(rendered);
	    indexTree.addTreeSelectionListener(this);
	    indexTree.setRowHeight(45);
		JScrollPane scTree=new JScrollPane(indexTree);
		JSplitPane splitHor=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,scTree,splitVer);
		splitHor.setDividerLocation(300);
		
		file.add(splitHor,BorderLayout.CENTER);
		
		file.repaint();
		file.revalidate();
    }

	@Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_TAB){
            ArrayList<String> resul = new ArrayList<String>();
            
            int row=0;
        	if(table.getSelectedColumn() != 0 || e.getKeyCode()==KeyEvent.VK_ENTER){
            	row = table.getSelectedRow();
        	} else {
        		row = table.getSelectedRow()-1;
        	}
            
            for(int i=0;i<table.getColumnCount();i++){
            	resul.add(getFieldValue(
            			table.getValueAt(row, i).toString(), 
            			uiFile.getFields().get(i).getFieldLength()));

            	table.setValueAt(resul.get(i), row, i);
            }
            
            try {
				uiFile.updateRecord(null, resul, row);
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}
	
	public void setColumnModel(JTable table){
		for(int i=0;i<table.getColumnCount();i++){
			if(table.getModel().getColumnName(i).equals("IS_DELETED")){
				table.getColumnModel().getColumn(i).setMinWidth(0);
				table.getColumnModel().getColumn(i).setMaxWidth(0);
				table.getColumnModel().getColumn(i).setPreferredWidth(0);
				
				TableCellRenderer tcr = new TableCellRenderer();
				table.setDefaultRenderer(Object.class, tcr);
				
			}
		}
	}
	
	public void setTableModel(JTable table){
		table.setModel(new TabelaModel(uiFile.getFields(),uiFile.getData()));
		setColumnModel(table);
	}

	public JTextField getTxtAccessNum() {
		return txtAccessNum;
	}	
	
	 MouseListener ml = new MouseAdapter() {
	     public void mousePressed(MouseEvent e) {
	         int selRow = indexTree.getRowForLocation(e.getX(), e.getY());
	         TreePath selPath = indexTree.getPathForLocation(e.getX(), e.getY());
	         if(selRow != -1) {
	        	 Node node= (Node) selPath.getLastPathComponent();
	        	 NodeElement nodeElement = null;
	        	 if(e.getClickCount() == 1) {
	        		 nodeElement = node.getData().get(0);
	        	 } else if(e.getClickCount() == 2){
	        		 nodeElement = node.getData().get(1);
	        	 }
	        	 
	        	 if (node.getChildCount()==0 && nodeElement!=null){
	            	 	
	        		 int newFilePointer =  nodeElement.getBlockAddress()*uiFile.getRECORD_SIZE();
	     				
	        		 uiFile.setFILE_POINTER(newFilePointer);
	        		 try {
	        			 uiFile.fetchNextBlock();
	        		 } catch (IOException e1) {
	        			 e1.printStackTrace();
	        		 } catch (SQLException e1) {
	        			 e1.printStackTrace();
	        		 }
	        	 }
	         }
	     }
	 };
	 
	 private int getColumnByName(JTable table, String name) {
		 for (int i = 0; i < table.getColumnCount(); i++)
			 if (table.getColumnName(i).equals(name))
				 return i;
		 return -1;
	 }
	 
}