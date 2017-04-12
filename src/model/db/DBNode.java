package model.db;

import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class DBNode extends DefaultMutableTreeNode{
	
	public final static int DATABASE=0;
	public final static int TABLE=1;
	public final static int COLUMN=2;
	public final static int PRIMARY_KEY=3;
	public final static int FOLDER=4;
	public final static int FOREIGN_KEY=5;
	
	private String name;
	private int type;
	private String dataType;
	private int columnSize;
	private int decimalDigits;
	private boolean isNull;
	private boolean isPK;
	private boolean isFK;
	
	
	public DBNode(String name, int type,String dataType,int columnSize, int decimalDigits,	boolean isNull ) {
		super();
		this.columnSize = columnSize;
		this.dataType = dataType;
		this.decimalDigits = decimalDigits;
		this.isNull = isNull;
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	public String toString(){
		if (type==DBNode.COLUMN){
			return createNodeName(name);
		}else{
			return name;
		}
		
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public int getColumnSize() {
		return columnSize;
	}
	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}
	public int getDecimalDigits() {
		return decimalDigits;
	}
	public void setDecimalDigits(int decimalDigits) {
		this.decimalDigits = decimalDigits;
	}
	public boolean isNull() {
		return isNull;
	}
	public void setNull(boolean isNull) {
		this.isNull = isNull;
	}

	public boolean isPK() {
		return isPK;
	}

	public void setPK(boolean isPK) {
		this.isPK = isPK;
	}

	public boolean isFK() {
		return isFK;
	}

	public void setFK(boolean isFK) {
		this.isFK = isFK;
	}
	
	private String createNodeName(String name){
		String realName = name + " (";
		
		if(isPK)
			realName += "PK, ";
		if(isFK)
			realName += "FK, ";
		
		realName += dataType;

		if(!dataType.equals("datetime")){
			realName += "(" + columnSize;
			
			if(dataType.equals("numeric"))
				realName += "," + decimalDigits;
			
			realName += "), ";
		} else 
			realName += ", ";

		if(isNull)
			realName += "NULL";
		else 
			realName += "NOT NULL";
		
		realName += ") ";
		return realName;
	}
}
