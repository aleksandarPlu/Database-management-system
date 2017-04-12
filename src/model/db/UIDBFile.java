package model.db;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;

import db.DatabaseDAO;
import app.AppCore;
import model.FindResult;
import model.file.UIAbstractFile;
import model.file.UIFileField;

public class UIDBFile extends UIAbstractFile{

	//naziv tabele iz baze podtaka koju UIDBFile predstavlja
	protected String TABLE_NAME;
	protected HashMap<String, Object> whereFilter=new HashMap<String, Object>();
	protected ArrayList<String> fkTableName = new ArrayList<String>();
	private ArrayList<UIDBFile> uidbArrayList = new ArrayList<UIDBFile>();
	private ArrayList<JTable> mcTableArrayList = new ArrayList<JTable>();

	
	/*
	 * U konstruktoru se inicijalizuje naziv tabele koju instanca klase
	 * predstavlja
	 */
	public UIDBFile(String tableName){
		super("",tableName+".db",false);
		this.TABLE_NAME=tableName;
	}
		
	/*
	 * Metoda Ä�ita naziv svih kolona iz otvorene tabele, proverava koje  kolone 
	 * ulaze u sastav primarnog kljuÄ�a
	 * u tabeli i na osnovu toga formira listu polja (lista fields) 
	 */
	public void readHeader() throws IOException, SQLException {
		fields.clear();
		DatabaseMetaData dbMetaData=AppCore.getInstance().getConn().getMetaData();
			
		//Ä�itanje svih kolona koje ulaze u sastav tabele
		ResultSet columnNames=dbMetaData.getColumns(null, null, TABLE_NAME, null);
		//Ä�itanje strukture primarnog kljuÄ�a posmatrane tabele
		ResultSet 	pkeys= dbMetaData.getPrimaryKeys(null, null,TABLE_NAME);
			
		//za svaku kolonu koja se nalazi u tabeli, kreira se po jedna instanca klasa UIFieldFild
		while (columnNames.next()){
			UIFileField field=new UIFileField(columnNames.getString("COLUMN_NAME"),"TYPE_"+columnNames.getString("TYPE_NAME").toUpperCase(),columnNames.getInt("COLUMN_SIZE"),false);
			fields.add(field);
				
			while (pkeys.next()){
				if (pkeys.getString("COLUMN_NAME").equals(field.getFieldName())){
					field.setFieldPK(true);
					field.setSort(true);
					break;
				}
			}
			
		}
		pkeys.close();
		columnNames.close();
		
		ResultSet rs=dbMetaData.getExportedKeys(null, null, TABLE_NAME);
		
		Map<String, String> expKeys;
		while (rs.next()){
			expKeys = new HashMap<String, String>();
			String pkName= rs.getString("PKTABLE_NAME");
			expKeys.put("PKTABLE_NAME", pkName);
			String pkColumn= rs.getString("PKCOLUMN_NAME");
			expKeys.put("PKCOLUMN_NAME", pkColumn);
			String fkTableName=rs.getString("FKTABLE_NAME");
			expKeys.put("FKTABLE_NAME", fkTableName);
			String fkColumn= rs.getString("FKCOLUMN_NAME");
			expKeys.put("FKCOLUMN_NAME", fkColumn);
			String fkName=rs.getString("FK_NAME");
			expKeys.put("FK_NAME", fkName);
			exportedKeys.add(expKeys);
		}
		
		for(int i=0;i<exportedKeys.size();i++){
			Map<String, String> ekMap = exportedKeys.get(i);
			if(!fkTableName.contains(ekMap.get("FKTABLE_NAME")))
				fkTableName.add(ekMap.get("FKTABLE_NAME"));
		}
		
		for(int i=0;i<fkTableName.size();i++){
			UIDBFile uidbfile=new UIDBFile(fkTableName.get(i));
			uidbArrayList.add(uidbfile);
			
			try {
				uidbfile.readHeader();
			} catch (IOException | SQLException e1) {
				e1.printStackTrace();
			}
			
		}
		
	}

		
	
	
	/*
	 * Metoda Ä�ita sve podatke iz tabele
	 * SELECT upit se generiÄ�ki formira na osnovu opisa polja u tabeli
	 */
	public boolean fetchNextBlock() throws IOException, SQLException {
		
		
		//broj slogova u tabeli :
		Statement stmt0=AppCore.getInstance().getConn().createStatement();
		ResultSet rs0=stmt0.executeQuery("SELECT COUNT(*)  FROM "+TABLE_NAME);
		if (rs0.next()){
			RECORD_NUM=rs0.getInt(1);
		}
		rs0.close();
		
		
		//formiranje dela upita za SELECT sql nad tabelom
		String columnParams=null;
		for (int i=0;i<fields.size();i++){
			if (columnParams==null){
				columnParams=fields.get(i).getFieldName();
			}else{
				columnParams+=","+fields.get(i).getFieldName();
			}
			
		}
		
		Statement stmt=AppCore.getInstance().getConn().createStatement();
		ResultSet rs=stmt.executeQuery("SELECT "+columnParams+" FROM "+TABLE_NAME);
		data = new String[(int) RECORD_NUM][];
		//u objektu ResultSet-a rs nalaze se svi podaci iz date tabele
		//iz result set-a se podaci Ä�itaju i prebacuju u matricu data[][]
		int i=0;
		while (rs.next()){
			data[i]=new String [fields.size()];
			for (int j=0;j<fields.size();j++){
				data[i][j]=rs.getString(j+1);
			}
			i++;
		}
		fireUpdateBlockPerformed();
		return true;
	}
	
	

	
	/*
	 * Metoda za dodavanje slogova u tabelu. Metoda treba da bude realizovana koriÅ¡Ä‡enjem objekta PreparedStatement.
	 * Sve SQLException-e u sluÄ�aju neuspeÅ¡nog dodavanja sloga prikazivati kroz JOptionPane.
	 * Nakon uspeÅ¡nog dodavanja sloga, sadrÅ¾aj tabele treba da bude osveÅ¾en 
	 * (ponovo proÄ�itan iz baze podataka) i u tabeli treba da bude selektovan dodati slog.
	 */

	@Override
	public boolean addRecord(List<List<String>> record) throws IOException, SQLException {
		boolean savedObject= false;
		
		List<String> addString = new ArrayList<String>();
		for(List<String> str : record){
			for(String s : str){
				if(s.equals("")){
					addString.add(null);
				} else {
					addString.add(s);
				}
			}
		}
		
		savedObject = DatabaseDAO.getInstance().saveObject(addString, this);
		
		return savedObject;
	}
	
	
	/*
	 * Metoda za izmenu vrednosti selektovanog sloga. Metoda treba da bude realizovana koriÅ¡Ä‡enjem objekta PreparedStatement.
	 * Sve SQLException-e u sluÄ�aju neuspeÅ¡nog dodavanja sloga prikazivati kroz JOptionPane.
	 * Nakon uspeÅ¡ne izmene sloga, sadrÅ¾aj tabele treba da bude osveÅ¾en 
	 * (ponovo proÄ�itan iz baze podataka) i u tabeli treba da bude selektovan izmenjeni slog.
	 */
	@Override
	public boolean updateRecord(ArrayList<String> oldRecord,ArrayList<String> newRecord, int row) throws IOException {
		boolean updateObject = false;
		
		ArrayList<String> primaryKeys = new ArrayList<String>();
		for(int i=0;i<getFields().size();i++){
			if(getFields().get(i).isFieldPK()){
				if(getFields().get(i).isStringType())
					primaryKeys.add(getFields().get(i).getFieldName() + "='" + oldRecord.get(i) + "'");
				else 
					primaryKeys.add(getFields().get(i).getFieldName() + "=" + oldRecord.get(i) + "");
			}
		}
		
		updateObject = DatabaseDAO.getInstance().updateObject(primaryKeys,newRecord, this);
		
		return updateObject;
	}
	
	
	
	/*
	 * 
	 * Brisanje sloga iz datoteke, po vrednosti primarnog kljuca:
	 */
	public boolean deleteRecord(ArrayList<String> deleteRec)throws IOException,SQLException {
		boolean delete = DatabaseDAO.getInstance().deleteObject(deleteRec, this);

		fetchNextBlock();

		return delete;		
		/*
		//formirati DELETE iskaz na osnovu naziva tabele, na osnovu naziva PK i vrednosti iz deleteRec:
		String whereStmt=" WHERE ";
		
		
		for (int i=0;i<fields.size();i++){
			//u WHERE uslov ulaze samo polja iz primarnog kljuca
			
			if (fields.get(i).isFieldPK()){
					if (i==0){
						whereStmt+=fields.get(i).getFieldName()+"= "+((fields.get(i).isStringType())?"'"+ deleteRec.get(i) +"'":deleteRec.get(i));
					}else{
						whereStmt+=" AND "+fields.get(i).getFieldName()+"= "+((fields.get(i).isStringType())?"'"+ deleteRec.get(i) +"'":deleteRec.get(i));
					}
			}
			
		}
		
		Statement stmt=AppCore.getInstance().getConn().createStatement();
		stmt.execute("DELETE FROM "+TABLE_NAME+whereStmt);
		fetchNextBlock();
		return true;
		*/
		
		//formirati DELETE iskaz na osnovu naziva tabele, na osnovu naziva PK i vrednosti iz deleteRec:
		
	}



	@Override
	public boolean findRecord(ArrayList<String> searchRec, int[] position, boolean start, boolean viseSlogova, boolean snimiURAM) {
		try {
			fetchNextBlock();
			
			String tableName = getFileName().substring(0, getFileName().length()-3);
			Statement stmt = null;
		    ResultSet rs = null;

		    String pkFields = "";
		    String pkNaziv = "";
		    for (int i=0;i<fields.size();i++){
				if (fields.get(i).isFieldPK()){
					pkNaziv+=fields.get(i).getFieldName() + ",";
					if(fields.get(i).isStringType())
						pkFields += fields.get(i).getFieldName() + " = '" + searchRec.get(i) + "' AND ";
					else 
						pkFields += fields.get(i).getFieldName() + " = " + Integer.parseInt(searchRec.get(i)) + " AND ";
				}
			}
		    
		    pkNaziv = pkNaziv.substring(0, pkNaziv.length()-1);
		    pkFields = pkFields.substring(0, pkFields.length()-5);
		    
		    try {
		      stmt = AppCore.getInstance().getConn().createStatement();
			   
		      rs = stmt.executeQuery("SELECT * FROM (SELECT ROW_NUMBER() OVER(ORDER BY " + pkNaziv + ") AS ROW ,"
		      		+ " * FROM " + tableName + ") " + tableName + " WHERE " + pkFields);
	      
		      
		      while(rs.next()){
		    	  position[0]=Integer.parseInt(rs.getString(1))-1;
		      }
		      		      
		    } finally {
		      rs.close();
		      stmt.close();
		    }
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		
		return false;
	}
	
	public boolean findRecord(ArrayList<String> searchRec, FindResult findResult) {
		boolean result=false;
		for (int i=0;i<RECORD_NUM-1;i++){
			
			//provera za jedan slog u matrici data[][]
			result=true;
			for (int j=0;j<fields.size();j++){
				if (!searchRec.get(j).trim().equals("")){
					if (!data[i][j].trim().equals(searchRec.get(j).trim())){
						
						result=false;
						break;
					}
				}
			}
			
			//provera da li smo pronasli smo odgovarajuÄ‡i slog
			if (result){
				findResult.setPosition(i);
				return true;
			}
			
		}
		return result;
	}

	

	/*
	 * Metoda koja postavlja WHERE upit u bazu, ResultSet
	 * sa slogovima koji odgovaraju parametrima pretrage
	 * se prikazuje u tabeli
	 */
	public boolean filterFind(ArrayList<String> searchRec) throws SQLException{
		
		String[][] filterFindData = DatabaseDAO.getInstance().search(searchRec, this);
		
		data = filterFindData;
		fireUpdateBlockPerformed();
		
		return true;
	}
	
	
	/*
	 * Sortiranje Ä‡e odraditi generisanjem SELECT - ORDER BY iskaza
	 * i izvrÅ¡iti koriÅ¡Ä‡enjem Statement objekta
	 */
	public void sortMDI() throws IOException {
		
		String[][] dataSort = DatabaseDAO.getInstance().sqlSortMDI(this);
		
		data = dataSort;
		fireUpdateBlockPerformed();
	}

	
	
	/*
	 * Ne koristi se!!!
	 */
	public void sortMM() throws IOException {
	}


	public String getTABLE_NAME() {
		return TABLE_NAME;
	}


	public void setTABLE_NAME(String tABLENAME) {
		TABLE_NAME = tABLENAME;
	}


	public HashMap<String, Object> getWhereFilter() {
		return whereFilter;
	}


	public void setWhereFilter(HashMap<String, Object> whereFilter) {
		this.whereFilter = whereFilter;
	}
	
	public ArrayList<String> getFkTableName() {
		return fkTableName;
	}

	public void setFkTableName(ArrayList<String> fkTableName) {
		this.fkTableName = fkTableName;
	}

	public ArrayList<UIDBFile> getUidbArrayList() {
		return uidbArrayList;
	}

	public void setUidbArrayList(ArrayList<UIDBFile> uidbArrayList) {
		this.uidbArrayList = uidbArrayList;
	}

	public ArrayList<JTable> getMcTableArrayList() {
		return mcTableArrayList;
	}

	public void setMcTableArrayList(ArrayList<JTable> mcTableArrayList) {
		this.mcTableArrayList = mcTableArrayList;
	}

}
