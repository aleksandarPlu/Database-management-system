package db;

import java.util.ArrayList;
import java.util.List;

import model.file.UIAbstractFile;
import view.FileView;
import app.AppCore;

public class QueryGenerator {
	
	public static String generateSortQuery(UIAbstractFile uiFile){
		String tableName = uiFile.getFileName().substring(0, uiFile.getFileName().length()-3);
		
		String query = "SELECT * FROM " + tableName + " ORDER BY";
		
		for(int i=0;i<uiFile.getFields().size();i++){
	    	 if(uiFile.getFields().get(i).isSort()){
	    		 query += " " + uiFile.getFields().get(i).getFieldName();
	    		 
	    		 if(uiFile.getFields().get(i).isAsc())
	    			 query += ",";
	    		 else 
	    			 query += " DESC,";
	    	 }
		}
		
		query = query.substring(0, query.length() - 1);

		return query;
	}
	
	public static String generateUpdateQuery(List<String> str, UIAbstractFile uiFile) {
		String tableName = uiFile.getFileName().substring(0, uiFile.getFileName().length()-3);
		
		String columns = "", values = "";
		
		for(int i=0;i<uiFile.getFields().size();i++){
			columns += uiFile.getFields().get(i) + ",";
			values += "?,";
		}

		String query = "INSERT INTO " + tableName + "(" + columns.substring(0, columns.length() - 1) + ") VALUES(" + values.substring(0, values.length() - 1)
				+ ");";
		return query;
	}

	public static String generateDiffUpdateQuery(ArrayList<String> primaryKeys, ArrayList<String> diffData, UIAbstractFile uiFile){
		String tableName = uiFile.getFileName().substring(0, uiFile.getFileName().length()-3);
		
		String pkString = "";
		for(String s : primaryKeys){
			pkString+=s + " AND ";
		}
		pkString = pkString.substring(0, pkString.length()-5);
		
		String updString = "";
		for(int i=0; i<uiFile.getFields().size();i++){
			updString += uiFile.getFields().get(i).getFieldName() + "=?,";
		}
		
		updString = updString.substring(0, updString.length()-1);
		
		
		String query = "UPDATE " + tableName + " SET " + updString  + " WHERE " + pkString;
		return query;
	}
	
	
	public static String generateSearchQuery(ArrayList<String> searchRec, UIAbstractFile uiFile){
		String tableName = uiFile.getFileName().substring(0, uiFile.getFileName().length()-3);
		
		String query = "SELECT * FROM " + tableName + " WHERE ";
		
		for(int i=0;i<uiFile.getFields().size();i++){
			if(searchRec.get(i)!=null && !searchRec.get(i).trim().equals("")){
				query += uiFile.getFields().get(i).getFieldName() + " " +
							znak(searchRec.get(i), (searchRec.get(i).contains("*") || searchRec.get(i).contains("%"))) + " ? AND ";
			}
		}
		
		query = query.substring(0, query.length()-4);
		
		return query;
	}
	
	public static String generateDeleteQuery(ArrayList<String> deleteRec, UIAbstractFile uiFile){
		String tableName = uiFile.getFileName().substring(0, uiFile.getFileName().length()-3);
		
		String query = " DELETE FROM " + tableName + " WHERE ";
		
		for (int i=0;i<uiFile.getFields().size();i++){
			//u WHERE uslov ulaze samo polja iz primarnog kljuca
			
			if (uiFile.getFields().get(i).isFieldPK()){
					if (i==0){
						query+=uiFile.getFields().get(i).getFieldName()+ "  = ? ";
					}else{
						query+=" AND "+ uiFile.getFields().get(i).getFieldName()+  "  =  ?";
					}
			}
			
		}
		
		return query;
	}
	
	public static String znak(String znak, boolean zvezdica){
		String tacanZnak= "";
		if(zvezdica)
			tacanZnak = "LIKE";
		else 
			tacanZnak = "=";
		
		if(znak.trim().startsWith("<=")){
			tacanZnak = "<=";
		} else if(znak.trim().startsWith(">=")){
			tacanZnak=">=";
		} else if(znak.trim().startsWith("<")){
			tacanZnak="<";
		} else if(znak.trim().startsWith(">")){
			tacanZnak=">";
		}
		
		return tacanZnak;
	}
	
}
