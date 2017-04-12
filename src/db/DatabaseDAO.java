package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import model.db.UIDBFile;
import model.file.UIAbstractFile;
import app.AppCore;

public class DatabaseDAO implements Dao {
	
	private static DatabaseDAO instance = null;
	
	public DatabaseDAO(){
	}
	
	public static DatabaseDAO getInstance(){
		if(instance == null)
			instance = new DatabaseDAO();
		
		return instance;
	}
	
	@Override
	public String[][] sqlSortMDI(UIAbstractFile uiFile) {
		String query = QueryGenerator.generateSortQuery(uiFile);
		return executeSqlSortQuery(query, uiFile);
	}
	
	@Override
	public String[][] search(ArrayList<String> searchRec, UIAbstractFile uiFile) throws SQLException {
		String query = QueryGenerator.generateSearchQuery(searchRec, uiFile);
		return executeSQLSearchQuery(query, searchRec, uiFile);
	}

	@Override
	public boolean updateObject(ArrayList<String> primaryKeys, ArrayList<String> diffData, UIAbstractFile uiFile) {
		String query = QueryGenerator.generateDiffUpdateQuery(primaryKeys, diffData, uiFile);
		return executeUpdateQuery(query, diffData, uiFile);
	}

	@Override
	public boolean saveObject(List<String> str, UIAbstractFile uiFile) {
		String query = QueryGenerator.generateUpdateQuery(str, uiFile);
		return executeUpdateQuery(query, str, uiFile);
	}
	
	@Override
	public String[][] exportedKeys(Map<String, String> query, String tableName, UIDBFile uidbArrayList) {
		return executeExportedKeysSQLQuery(query, tableName, uidbArrayList);
	}
	
	@Override
	public boolean deleteObject(ArrayList<String> deleteRec, UIAbstractFile uiFile) {
		String query = QueryGenerator.generateDeleteQuery(deleteRec, uiFile);
		return executeDeleteQuery(deleteRec,query,uiFile);
	}

	protected boolean executeUpdateQuery(String query, List<String> queryStr, UIAbstractFile uiFile) {
		boolean success = false;
		try {
			PreparedStatement pstmt=AppCore.getInstance().getConn().prepareStatement(query);
			
			for (int i=0;i<uiFile.getFields().size();i++){
				if(uiFile.getFields().get(i).isStringType()){
					if(queryStr.get(i)!=null) {
						pstmt.setString(i+1, queryStr.get(i));
					} else {
						pstmt.setObject(i+1, null);
					}
				} else {
					if(queryStr.get(i)!=null){
						try { 
					        Integer.parseInt(queryStr.get(i));
					        pstmt.setInt(i+1, Integer.parseInt(queryStr.get(i)));
					    } catch(NumberFormatException e) { 
					    	pstmt.setObject(i+1, queryStr.get(i));
					    }
					} else {
						pstmt.setObject(i+1, null);
					}
				}
			}
			
			pstmt.execute();
			success = true;
		} catch (SQLException e) {
			printException(e.getErrorCode());
		}
		return success;
	}
	
	protected String[][] executeSQLSearchQuery(String query,ArrayList<String> searchRec,UIAbstractFile uiFile){
		
		String[][] realData = null;
		
		try {

			PreparedStatement pstmt = AppCore.getInstance().getConn().prepareStatement(query);
		
		int w = 1;
		for(int i=0;i<uiFile.getFields().size();i++){
			if(uiFile.getFields().get(i).isStringType()){
				if(searchRec.get(i)!=null && !searchRec.get(i).trim().equals("")){
					String q = "";
					String rec = searchRec.get(i).trim();
					
					if(rec.startsWith(">=")){
						rec = rec.substring(2);
					} else if (rec.startsWith(">")){
						rec = rec.substring(1);
					} else if(rec.startsWith("<=")){
						rec = rec.substring(2);
					} else if(rec.startsWith("<")){
						rec = rec.substring(1);
					} 
					
					if(rec.contains("*")){
						q += "" + rec.replace("*", "%") + "";
					} else {
						q += "" + rec + "";
					}
					
					
					pstmt.setObject(w++, q);
				}
			} else {
				if(searchRec.get(i)!=null && !searchRec.get(i).trim().equals("")){
					String q = "";
					String rec = searchRec.get(i).trim();
					
					if(rec.startsWith(">=")){
						rec = rec.substring(2);
					} else if (rec.startsWith(">")){
						rec = rec.substring(1);
					} else if(rec.startsWith("<=")){
						rec = rec.substring(2);
					} else if(rec.startsWith("<")){
						rec = rec.substring(1);
					} 
					
					q += rec;
					
					pstmt.setObject(w++, q);				
				}
			}
		}
		ResultSet rs = pstmt.executeQuery();

		String[] data = null;
		ArrayList<String[]> listaData = new ArrayList<String[]>();
		while (rs.next()){
			data=new String [uiFile.getFields().size()];
			for (int j=0;j<uiFile.getFields().size();j++){
				data[j]=rs.getString(j+1);
			}
			listaData.add(data);
		}
		
		rs.close();
		
		realData = new String[listaData.size()][];
		for(int z=0;z<listaData.size();z++){
			realData[z] = new String [uiFile.getFields().size()];
			realData[z]=listaData.get(z);
		}
		
		} catch (SQLException e) {
			printException(e.getErrorCode());
		}
		
		return realData;
	}
	
	protected String[][] executeSqlSortQuery(String query, UIAbstractFile uiFile) {
		String tableName = uiFile.getFileName().substring(0, uiFile.getFileName().length()-3);
		String[][] data = null;
		
		//broj slogova u tabeli :
		try {
			Statement stmt0=AppCore.getInstance().getConn().createStatement();
			ResultSet rs0;
			
			rs0 = stmt0.executeQuery("SELECT COUNT(*)  FROM " + tableName);
			
			if (rs0.next()){
				long recordNumber =rs0.getInt(1);
				data = new String[(int) recordNumber][];
			}
			rs0.close();
			
			
		} catch (SQLException e1) {
			printException(e1.getErrorCode());
		}
		
		Connection connection = null;
		try {
			connection = AppCore.getInstance().getConn();
			
			Statement stmt = connection.createStatement();

			ResultSet rs = stmt.executeQuery(query);
			
			int i=0;
			while (rs.next()){
				data[i]=new String [uiFile.getFields().size()];
				for (int j=0;j<uiFile.getFields().size();j++){
					data[i][j]=rs.getString(j+1);
				}
				i++;
			}
			
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			printException(e.getErrorCode());
		}
		
		return data;
	}	
		
	protected String[][] executeExportedKeysSQLQuery(Map<String, String> query, String tableName, UIDBFile uidbArrayList){
		String[][] realData = null;
		
		Statement stmt;
		ResultSet rs;
		
		try {
			stmt = AppCore.getInstance().getConn().createStatement();
			rs = stmt.executeQuery(query.get(tableName));
		
			String[] data = null;
			ArrayList<String[]> listaData = new ArrayList<String[]>();
			while (rs.next()){
				data=new String [uidbArrayList.getFields().size()];
				for (int j=0;j<uidbArrayList.getFields().size();j++){
					data[j]=rs.getString(j+1);
				}
				listaData.add(data);
			}
			
			rs.close();
			
			realData = new String[listaData.size()][];
			for(int z=0;z<listaData.size();z++){
				realData[z] = new String [uidbArrayList.getFields().size()];
				realData[z]=listaData.get(z);
			}
		
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		return realData;
	}
	
	protected boolean executeDeleteQuery(ArrayList<String> deleteRec, String query, UIAbstractFile uiFile){
		boolean succes= false;
		try {
			PreparedStatement pstmt=AppCore.getInstance().getConn().prepareStatement(query);		
			for (int i=0;i<uiFile.getFields().size();i++){
				//u WHERE uslov ulaze samo polja iz primarnog kljuca
				if (uiFile.getFields().get(i).isFieldPK()){
					pstmt.setObject(i+1, deleteRec.get(i));
				}
			}
			pstmt.execute();
			succes=true;
		} catch (SQLException e) {
			printException(e.getErrorCode());
		}
		return succes;
	}
	
	public void printException(int error) {

		String text = "";

		switch (error) {
		case 2627: {
			//Message: Duplicate entry '%s' for key %d
			text = "Vec postoji slog sa istim primarnim kljucem";
			break;
		}
		case 102: {
			//Message: %s near '%s' at line %d
			text = "Upit neuspesan, proverite unete podatke";
			break;
		}
		case 128: {
			//The name "%.*s" is not permitted in this context. Valid expressions are constants, 
			//constant expressions, and (in some contexts) variables. Column names are not permitted.
			text = "Upit neuspesan, pogresan tip polja";
			break;
		}
		case 515: {
			//Attempt to insert NULL value into column '%.*s', table '%.*s'; column does not allow nulls.
			text = "Upit neuspesan, NOT NULL polja ne smeju ostati prazna";
			break;
		}
		case 8114: {
			//Error converting data type %ls to %ls.
			text = "Upit neuspesan, pogresan tip polja";
			break;
		}
		case 547: {
			//The UPDATE statement conflicted with the REFERENCE constraint
			text = "Upit neuspesan, dolazi do konflikta sa REFERENCE ogranicenjem";
			break;
		}
		default: {
			text = "code: " + error;
		}
		}
		Object[] options = { "OK" };
		JOptionPane.showOptionDialog(null, text, "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
	}

}
