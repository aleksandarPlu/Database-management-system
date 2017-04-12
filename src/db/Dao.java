package db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.db.UIDBFile;
import model.file.UIAbstractFile;

public interface Dao {
	
	boolean updateObject(ArrayList<String> primaryKeys, ArrayList<String> diffData, UIAbstractFile uiFile);

	boolean saveObject(List<String> str, UIAbstractFile uiFile);
	
	boolean deleteObject(ArrayList<String> deleteRec, UIAbstractFile uiFile);
	
	String[][] search(ArrayList<String> searchRec, UIAbstractFile uiFile) throws SQLException;
	
	String[][] sqlSortMDI(UIAbstractFile uiFile);
	
	String[][] exportedKeys(Map<String, String> query, String tableName, UIDBFile uidbArrayList);
}
