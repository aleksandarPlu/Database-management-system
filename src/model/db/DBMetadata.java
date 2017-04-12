package model.db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import app.AppCore;

public class DBMetadata {
	
	public DBMetadata(){
		
	}
	public void readDatabase() throws SQLException{
		DatabaseMetaData dbMetaData=AppCore.getInstance().getConn().getMetaData();
		
		String[] dbTypes={"TABLE"};
		ResultSet rsTables=dbMetaData.getTables(null, null, null, dbTypes);
		String tableName;
		String columnName;
		String columnDataType;
		boolean isNull;
		
		DBNode dbNode=(DBNode) AppCore.getInstance().getDBTree().getModel().getRoot();
		DBNode tableNode;
		
		while (rsTables.next()){
			tableName=rsTables.getString("TABLE_NAME");
			
			tableNode=new DBNode(tableName,DBNode.TABLE,"",0,0,false);
			
			DBNode columnFolder = new DBNode("Columns",DBNode.FOLDER,"",0,0,false);;
			DBNode columnNode;
			
			ResultSet rsColumns= dbMetaData.getColumns(null, null,tableName,null);
			
			while (rsColumns.next()){
				columnName=rsColumns.getString("COLUMN_NAME");
				columnDataType = getDataTaypeSQL(rsColumns.getString("DATA_TYPE"));
				isNull = isNull(rsColumns.getString("IS_NULLABLE"));
				
				columnNode = new DBNode(columnName, DBNode.COLUMN, columnDataType, 
										Integer.valueOf(rsColumns.getString("COLUMN_SIZE")), 
										Integer.valueOf(rsColumns.getString("DECIMAL_DIGITS")), 
										isNull);
				
				ResultSet pKeys= dbMetaData.getPrimaryKeys(null, null, tableName);
				String pkName;
				while(pKeys.next()){
					pkName = pKeys.getString("COLUMN_NAME");
					if(pkName.equals(columnName)){
						columnNode.setPK(true);
					}
				}
				
				ResultSet fKeys= dbMetaData.getImportedKeys(null, null, tableName);
				String fkName;
				while(fKeys.next()){
					fkName = fKeys.getString("FKCOLUMN_NAME");
					if(fkName.equals(columnName)){
						columnNode.setFK(true);
					}
				}
				
				columnFolder.add(columnNode);

//				columnNode = new DBNode(columnName, DBNode.COLUMN, rs.getColumnType(i),rs.getColumnDisplaySize(i),
//						rs.getPrecision(i), rs.isNullable(i));
			}
			
			tableNode.add(columnFolder);
			
			DBNode pkFolder = new DBNode("Keys",DBNode.FOLDER,"",0,0,false);
			DBNode pkNode;
			DBNode fkNode;
			
			ResultSet pKeys= dbMetaData.getPrimaryKeys(null, null, tableName);
			String pkName;
			if (pKeys.next()){
				pkName=pKeys.getString("PK_NAME");
				pkNode = new DBNode(pkName, DBNode.PRIMARY_KEY, "", 0, 0, false);
				pkFolder.add(pkNode);
			}
			
			ResultSet fKeys= dbMetaData.getImportedKeys(null, null, tableName);
			String fkName;
			while (fKeys.next()){
				fkName=fKeys.getString("FK_NAME");
				fkNode = new DBNode(fkName, DBNode.FOREIGN_KEY, "", 0, 0, false);
				boolean addFK = false;
				for(int i=0;i<pkFolder.getChildCount();i++){
					DBNode child = (DBNode) pkFolder.getChildAt(i);
					if(child.getName().equals(fkName)){
						addFK=false;
						break;
					}
					addFK=true;
				}
				if(addFK)
					pkFolder.add(fkNode);
			}
			
			tableNode.add(pkFolder);
			
			dbNode.add(tableNode);
		}

		rsTables.close();
		AppCore.getInstance().getDBTree().expandRow(0);

	}
	
	public String getDataTaypeSQL(String string){
		if(string.equals(String.valueOf(Types.CHAR))){
			return "char";
		} else if(string.equals(String.valueOf(Types.VARCHAR))){
			return "varchar";
		} else if(string.equals(String.valueOf(Types.NUMERIC))){
			return "numeric";
		} else if(string.equals(String.valueOf(Types.DATE))){
			return "datetime";
		} else if(string.equals(String.valueOf(Types.TIMESTAMP))){
			return "datetime";
		} else 
			return "";
	}
	
	public boolean isNull(String s){
		if(s.equals("YES")){
			return true;
		} else 
			return false;
	}
}
