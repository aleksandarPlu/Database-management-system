package model.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.tree.TreePath;

import model.FindResult;
import model.tree.Node;
import model.tree.Tree;

public class UIINDFile extends UIAbstractFile {
	
	private Tree tree;
	
	private FindResult findResult;
	private Object[] object;
	private int objectIndex;
	
	private String treeFileName;
	private String overZoneFileName;
	
	public UIINDFile(String path,String headerName, boolean directory) {
		super(path,headerName,directory);
	}

	public UIINDFile() {
		super();
	}

	/*
	 * Zaglavlje kod indeks - sekvencijalne datoteke je 
	 * drugacije u odnosu na serijsku i sekvencijalnu
	 * pa se metoda redefinise 
 	 */
	public void readHeader() throws IOException{
		fields.clear();
		String delimiter = "\\/";
		ArrayList<String> headRec= new ArrayList<String>();
		RandomAccessFile headerFile=null;
		Object data[]=null;
		
		headerFile = new RandomAccessFile(path+File.separator+headerName,"r");
		while (headerFile.getFilePointer()<headerFile.length() )
			headRec.add(headerFile.readLine());
				
		headerFile.close();
			
		int row = 0;
		
		for (String record : headRec) {
			StringTokenizer tokens = new StringTokenizer(record,delimiter);
			 
			int cols = tokens.countTokens();
			data = new String[cols];  
			int col = 0;
			      while (tokens.hasMoreTokens()) {
			         data[col] = tokens.nextToken();
			         if (data[col].equals("field")){
			        	 String fieldName=tokens.nextToken();
			        	 String fieldType=tokens.nextToken();
			        	 int fieldLenght=Integer.parseInt(tokens.nextToken());
			        	 RECORD_SIZE=RECORD_SIZE+fieldLenght;
			        	 boolean fieldPK=new Boolean(tokens.nextToken());
			        	 UIFileField field=new UIFileField(fieldName,fieldType,fieldLenght,fieldPK);
			        	 
			        	 fields.add(field);
			         }else if (data[col].equals("path")){
			        	    fileName=tokens.nextToken();
			        	 
			         }else if (data[col].equals("tree")){
			        	 treeFileName=tokens.nextToken();
			         }else if (data[col].equals("overZone")){
			        	 overZoneFileName=tokens.nextToken();
			         }
			      }
		          row++;
			   }
			   RECORD_SIZE=RECORD_SIZE+2;
			   
			   //postavljanje atributa datoteke
			   RandomAccessFile afile=new RandomAccessFile(path+File.separator+fileName,"r");
			   FILE_SIZE=afile.length();
			   RECORD_NUM=(long) Math.ceil((FILE_SIZE*1.0000)/(RECORD_SIZE*1.0000));
			   BLOCK_NUM=(int) (RECORD_NUM/BLOCK_FACTOR)+1;
			   afile.close();
			   //makeTree();
			   openTree(path+File.separator+treeFileName);
	}
	
	public void setObject(){
		objectIndex=0;
		Node root = tree.getRootElement();
		int z=1;
		while(true){
			if(root.getChildCount()!=0){
				z++; 
			} else {
				break;
			}	
			root = (Node) root.getChildAt(0);
		}
		object = new Object[z];
	}
	
    /**
     *  Prenos bloka iz datoteke u radnu memoriju aplikacije
     *  Velicina bloka određena je atributom BLOCK_SIZE 
     *  Po zavšetku metode blok podataka iz datoteke
     *  nalazi se u radnoj memoriji aplikaciji
     *  u matrici datа[][]
     * @throws IOException 
     * 
     */	
	public boolean fetchNextBlock() throws IOException{
				  
		RandomAccessFile afile=new RandomAccessFile(path+File.separator+fileName,"r");
		FILE_SIZE=afile.length();
		RECORD_NUM=(long) Math.ceil((FILE_SIZE*1.0000)/(RECORD_SIZE*1.0000));
		BLOCK_NUM=(int) (RECORD_NUM/BLOCK_FACTOR)+1;
			  
		//BUFFER_SIZE je uvek jednak broju slogova u jednom bloku * velicina jednog sloga
		//izuzev ako poslednji blok nije pun blok
		if (FILE_POINTER/RECORD_SIZE+BLOCK_FACTOR>RECORD_NUM) 
			BUFFER_SIZE=(int) (RECORD_NUM-FILE_POINTER/RECORD_SIZE)*RECORD_SIZE;
		else 
			BUFFER_SIZE=(int)(RECORD_SIZE*BLOCK_FACTOR);
		
		buffer=new byte[BUFFER_SIZE];
		data = new String[(int) BUFFER_SIZE/RECORD_SIZE][];
		//po otvaranju pozicioniram se na prethodni kraj zahvata
		afile.seek(FILE_POINTER);
		afile.read(buffer);
		String contentS=new String(buffer);
		if (contentS.length()<buffer.length){
			for (int x=contentS.length();x<buffer.length;x++)
				contentS=contentS+" ";
		}	
		
		for (int i=0;i<BUFFER_SIZE/RECORD_SIZE;i++){
			String line=contentS.substring(i*RECORD_SIZE,i*RECORD_SIZE+RECORD_SIZE);
			data[i] = new String[fields.size()]; 
			int k=0;
			for (int j=0;j<fields.size();j++){
				String field=null;
				field=   line.substring(k,k+fields.get(j).getFieldLength());
				data[i][j]=field;
				k=k+fields.get(j).getFieldLength();
			}		
		}
			
		FILE_POINTER=afile.getFilePointer();
		afile.close();
		
		//ucitavanje novog bloka treba da izazove osvezivanje podataka u tabeli
		fireUpdateBlockPerformed();

		return true;
     }
	 
	/**
	 *  Metoda za dodavanje sloga  -  Neće se realizovati
	 *  
	 */
	@Override
	public boolean addRecord(List<List<String>> list) throws IOException {
		return false;
	}


   /**
    *  Izmena podataka  - Neće se realizovati
    *  
    */
   public boolean updateRecord(ArrayList<String> oldRecord,ArrayList<String> newRecord, int row) throws IOException{
	  
	   return false;
   }

   
   /**
    *  Pretraga 
    */
   public boolean findRecord(ArrayList<String> searchRec,int[] position, boolean start, boolean viseSlogova, boolean snimiURAM){
	   return false;
   }   
   
   boolean searchRecIsPK(ArrayList<String> searchRec){
	   for (int i=0;i<fields.size();i++){
		   if (!searchRec.get(i).trim().equals("") && !fields.get(i).isFieldPK()){
			   return false;	       
		   }
	   }
	   return true;
   }
   
   /**
    * Brisanje - Neće se realizovati 
    */
	public boolean deleteRecord(ArrayList<String> searchRec) {
		return false;
	}


	public Tree getTree() {
		return tree;
	}


	public void setTree(Tree tree) {
		this.tree = tree;
	}
	
	public void openTree(String treeFilePath){

		ObjectInputStream os=null;
		try {
			os = new ObjectInputStream(new FileInputStream(treeFilePath));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		  
		try {
			tree = (Tree) os.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean findInIndex(ArrayList<String> searchRec, ArrayList<String> searchPK, Node root, int[] position){
		String[] first = new String[fields.size()];
		String[] second = new String[fields.size()];
		
		for(int i=0;i<fields.size();i++){
			if(fields.get(i).isFieldPK()){
				first[i] = root.getData().get(0).getKeyValue().get(i).getValue().toString();
			} else {
				first[i] = "";
			}
		}
		for(int i=0;i<fields.size();i++){
			if(fields.get(i).isFieldPK()){
				second[i] = root.getData().get(1).getKeyValue().get(i).getValue().toString();
				if(second[i].trim().equals("")){
					second[i]= "~";
				}
			} else {
				second[i] = "";
			}
		}
		
		//Ako je veci od prve a manji od druge, vrati prvi
		if(!isRowGreater(first, searchPK) && isRowGreater(second, searchPK)){
			if(root.getChildCount() != 0){
				object[objectIndex++] = root;
				return findInIndex(searchRec, searchPK, (Node) root.getChildAt(0), position);
			} else {
				//Ako nema dece, ako je manji od druge, else ako je veci od druge
				if(isRowGreater(second, searchPK)){
					FILE_POINTER = root.getData().get(0).getBlockAddress()*RECORD_SIZE;
				} else {
					FILE_POINTER = root.getData().get(1).getBlockAddress()*RECORD_SIZE;
				}
				try {
					fetchNextBlock();
				} catch (IOException e) {
					e.printStackTrace();
					position[0]=-1;
					return false;
				}
				
				for (int row=0;row<data.length;row++){
					   if (isRowEqual(data[row],searchRec)){
						   object[objectIndex++] = root;
						   setFindResult(new FindResult(object, root.getData().get(0).getBlockAddress()));
						   position[0]=row;
						   return true;
					   }
				}
				
				return false;
			}
		} 
		
		//Ako je veci od druge ili ako je jednak sa drugim, vrati drugi
		if(!isRowGreater(second, searchPK) || isRowEqual(second, searchPK)){
			if(root.getChildCount() > 1){
				object[objectIndex++] = root;
				return findInIndex(searchRec, searchPK, (Node) root.getChildAt(1), position);
			} else if(root.getChildCount() == 1){
				object[objectIndex++] = root;
				return findInIndex(searchRec, searchPK, (Node) root.getChildAt(0), position);
			} else {
				//Ako nema dece, ako je manji od druge, else ako je veci od druge
				if(isRowGreater(second, searchPK)){
					FILE_POINTER = root.getData().get(0).getBlockAddress()*RECORD_SIZE;
				} else {
					FILE_POINTER = root.getData().get(1).getBlockAddress()*RECORD_SIZE;
				}
				try {
					fetchNextBlock();
				} catch (IOException e) {
					e.printStackTrace();
					position[0]=-1;
					return false;
				}
				
				for (int row=0;row<data.length;row++){
					   if (isRowEqual(data[row],searchRec)){

						   object[objectIndex++] = root;
						   setFindResult(new FindResult(object, root.getData().get(0).getBlockAddress()));
						   
						   position[0]=row;
						   return true;
					   }
				}

				return false;
			}
		}

		//Ako je jednak kao prvi, ako ima child vrati prvi, ako nema pretrazi taj blok
		if(isRowEqual(first, searchPK)){
			if(root.getChildCount() != 0){
				object[objectIndex++] = root;
				return findInIndex(searchRec, searchPK, (Node) root.getChildAt(0), position);
			} else {
				//Ako nema dece, u svakom slucaju je prvi
				FILE_POINTER = root.getData().get(0).getBlockAddress()*RECORD_SIZE;
				try {
					fetchNextBlock();
				} catch (IOException e) {
					e.printStackTrace();
					position[0]=-1;
					return false;
				}
				
				for (int row=0;row<data.length;row++){
					   if (isRowEqual(data[row],searchRec)){

						   object[objectIndex++] = root;
						   setFindResult(new FindResult(object, root.getData().get(0).getBlockAddress()));
						   
						   position[0]=row;
						   return true;
					   }
				}

				return false;
			}
		}		
		return false;
	}

	private boolean isRowEqual(String [] aData, ArrayList<String> searchRec){
		   for (int col=0;col<fields.size();col++){
			   if (!searchRec.get(col).trim().equals("")){
				   if (!aData[col].trim().equals(searchRec.get(col).trim())){
					   return false;
				   }
			   }
		   }	   
		   return true;
	   }
	
	private boolean isRowGreater(String [] aData, ArrayList<String> searchRec){
		   boolean result=true;
		   int noPK=0;
		   boolean prev=true;
		   
		   for (int i=0;i<fields.size();i++){
			   if (!searchRec.get(i).trim().equals("") && !fields.get(i).isFieldPK()){
			          return false;	       
			   }
	           if (fields.get(i).isFieldPK())noPK++;
		   }
		   
			for (int col=0;col<fields.size();col++){
			    	 if (!searchRec.get(col).trim().equals("")){
			    		 
			    		  if (aData[col].trim().compareToIgnoreCase(searchRec.get(col).trim())>0 && col<noPK-1){
			    			  return true;
			    			  
			    		  }else if (aData[col].trim().compareToIgnoreCase(searchRec.get(col).trim())!=0 && col<noPK-1){
			    			  result=false;
			    			  prev=false;
			    		  }
			    		  else if (aData[col].trim().compareToIgnoreCase(searchRec.get(col).trim())<=0){
			    			  result=false;
			    			  
			    		  }else if (aData[col].trim().compareToIgnoreCase(searchRec.get(col).trim())>0 && prev && col==(noPK-1)){
			    			  result=true;
			    		  }
			    	 }
			     }	   
		   return result;
	   }

	public FindResult getFindResult() {
		return findResult;
	}

	public void setFindResult(FindResult findResult) {
		this.findResult = findResult;
	}
	
}
