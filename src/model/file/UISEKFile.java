package model.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import app.AppCore;
import model.ListaModel;
import model.tree.KeyElement;
import model.tree.Node;
import model.tree.NodeElement;
import model.tree.Tree;
import view.FileView;

public class UISEKFile extends UIAbstractFile {
	
	private Tree tree;
	private long helpFilePointer=0;
	
	public UISEKFile(String path,String headerName, boolean directory) {
		super(path,headerName,directory);
	}

	public UISEKFile() {
		super();
	}

    /**
     *  Prenos bloka iz datoteke u radnu memoriju aplikacije
     *  Velicina bloka odredjena je atributom BLOCK_SIZE 
     *  Po zavšetku metode blok podataka iz datoteke
     *  nalazi se u radnoj memoriji aplikaciji
     *  u matrici data[][]
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
		//TODO kod trazenja ovo treba iskljuciti (ili naci neko resenje)
		helpFilePointer=afile.getFilePointer();
//		FILE_POINTER=afile.getFilePointer();
		afile.close();
		
		//ucitavanje novog bloka treba da izazove osvezivanje podataka u tabeli
		fireUpdateBlockPerformed();
		
		return true;
     }
	 
	/**
	 *  Metoda za dodavanje sloga u sekvencijalnoj datoteci
	 * datoteke. 
	 *  
	 */
	@Override
	public boolean addRecord(List<List<String>> list) throws IOException {	
		ArrayList<String> record = new ArrayList<String>();
		List<String> li = list.get(0);
		
		for(int i=0;i<li.size();i++){
			record.add(li.get(i));
		}
		
		boolean result=true;
		//dodavanje slogova treba poceti neuspensom pretragom po kljucu
		//argument trazenja su vrednosti obelezja koje se zele dodati
		ArrayList<String> temp=new ArrayList<String>();
		for (int i=0;i<fields.size();i++){
			if (fields.get(i).isFieldPK()){
				if (record.get(i).trim().equals("")){
					//nije uneta vrednost kljuca, ne mozemo nastaviti unos:
						
					JOptionPane.showMessageDialog(null, "Niste uneli vrednost obeležja "+fields.get(i).getFieldName(), "UI Project", 1);
					return false;
				}else{
					  
					temp.add(record.get(i));
				}
			}else{
				temp.add(" ");
			}
			 
		 }
		 int[] position=new int [1];
		 position[0]=-1;
		 if (findRecord(temp,position,false, false, false)){
			 JOptionPane.showMessageDialog(null, "Slog sa istom vrednošcu PK vec postoji", "UI Project", 1);
			 return false;
			 
		 }
		 //slog sa istom vrednoscu kljuca ne postoji a u position se nalazi
		 //relativne  adresa lokacija na kojoj treba smestiti novi slog
		 long oldFilePointer=(FILE_POINTER/RECORD_SIZE-BLOCK_FACTOR)*RECORD_SIZE;
		 long newPosition=FILE_POINTER/RECORD_SIZE-BLOCK_FACTOR+position[0];
		 
		 RandomAccessFile afile=new RandomAccessFile(path+File.separator+fileName,"rw");
		 byte [] record_buffer=new byte[RECORD_SIZE];
		 for (int i=(int) RECORD_NUM-1;i>=newPosition;i--){
		              	afile.seek(i*RECORD_SIZE);
		              	afile.read(record_buffer);
		              	afile.write(record_buffer);
		 }
		 //ostalo je još da dodamo novi slog
		 String newRecord="";
		 for (int i=0;i<record.size();i++){
			   newRecord=newRecord+record.get(i); 
		 }
		   
		 newRecord=newRecord+"\r\n";
		 afile.seek(newPosition*RECORD_SIZE);
		 afile.writeBytes(newRecord);
		 afile.close();
		 FILE_POINTER=oldFilePointer;
		 fetchNextBlock();
		 return result;		 
   }


   /**
    *  
    *  
    */
   public boolean updateRecord(ArrayList<String> oldRecord,ArrayList<String> record, int row) throws IOException{
	   FileView fileView = (FileView) AppCore.getInstance().getFramework().getSelectedComponent();
	   int filePointerForWrite = (int) (helpFilePointer - (RECORD_SIZE*
			   							(fileView.getTable().getRowCount()-row)));

	   RandomAccessFile afile=new RandomAccessFile(path+File.separator+fileName,"rw");
		
	   String newRecord = "";
	   for(String s : record){
		   newRecord+=s;
	   }
	   
	   afile.seek(filePointerForWrite);
	   afile.writeBytes(newRecord+"\r\n");
	   afile.close();
	   
	   return false;
   }

   
   /**
    *  Pretraga u sekvencijalnoj datoteci koja pocinje od pocetka datoteke
    * Moze da se pretrazuje po bilo kom polju datoteke
    * metoda zaustavlja pretragu na prvom slogu koji zadovoljava zadate kriterijume
    * ili po neuspesnom trazenju sloga a to je na kraju datoteke
    * Ukoliko se pretraga vrsi po kljucu pretraga se moze zaustaviti pri prvoj 
    * vecoj vrednosti od kljuca
    */
   public boolean findRecord(ArrayList<String> searchRec,int[] position, boolean start, boolean viseSlogova, boolean snimiURAM){
	   FILE_POINTER=0;
	   
	   if((RECORD_NUM % 2) == 0){
		   FILE_POINTER = (RECORD_NUM/2)*RECORD_SIZE;
	   } else {
		   FILE_POINTER = ((RECORD_NUM/2)*RECORD_SIZE)+RECORD_SIZE;
	   }
	   
	   long helpRecordNum = RECORD_NUM/2;
	   
	   BLOCK_FACTOR = 1;
	   boolean recIsPK = searchRecIsPK(searchRec);

	   while (FILE_POINTER<=FILE_SIZE && position[0]==-1 && FILE_POINTER>=0){

		   try {
			   fetchNextBlock();
		   } catch (IOException e) {
			   e.printStackTrace();
			   position[0]=-1;
			   return false;
		   }
		   		   
		   if(!recIsPK){
			   for (int row=0;row<data.length;row++){
				   if (isRowEqual(data[row],searchRec)){
					   position[0]=row;
					   FILE_POINTER = helpFilePointer;
					   break;
				   } else  if (isRowGreater(data[row],searchRec)){
					   position[0]=row;
					   FILE_POINTER = helpFilePointer;
					   break;
				   }
				   FILE_POINTER = helpFilePointer;
			   }
		   } else {
			   for (int row=0;row<data.length;row++){
				   if (isRowEqual(data[row],searchRec)){
					   position[0]=row;
					   return true;
				   } else  if (isRowGreater(data[row],searchRec)){
					   if(helpRecordNum==0){
						   FILE_POINTER -= RECORD_SIZE;
					   }
					   if((helpRecordNum % 2) == 0){
						   FILE_POINTER -= ((helpRecordNum / 2)*RECORD_SIZE);
					   } else {
						   FILE_POINTER -= ((helpRecordNum / 2)*RECORD_SIZE)+RECORD_SIZE;
					   }
					   helpRecordNum /= 2;
					   break;
				   } else {
					   if(helpRecordNum==0){
						   FILE_POINTER += RECORD_SIZE;
					   }
					   if((helpRecordNum % 2) == 0){
						   FILE_POINTER += ((helpRecordNum / 2)*RECORD_SIZE);
					   } else {
						   FILE_POINTER += ((helpRecordNum / 2)*RECORD_SIZE)+RECORD_SIZE;
					   }
					   helpRecordNum /= 2;
					   break;
				   }
			   }
		   }
	   }
	   if(position[0]==-1){
		   return false;
	   }else {
		   return true;
	   }
   }
   
   /**
    * 
    * @param aData - jedan slog iz bloka datoteke
    * @param searchRec - parametri pretrage
    * @return - true ukoliko dati slog iz bloka sadrzi polja koja odgovaraju parametrima pretrage
    */
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
   
   boolean searchRecIsPK(ArrayList<String> searchRec){
	   for (int i=0;i<fields.size();i++){
		   if (!searchRec.get(i).trim().equals("") && !fields.get(i).isFieldPK()){
		          return false;	       
		   }
	   }
	   return true;
   }
   
   /**
    * metoda koja proverava da li je tekuci slog pretrage veci od zadatog parametra traženja
    * @param aData -slog iz datoteke u baferu koji se poredi
    * @param searchRec - parametri pretrage
    * @return
    */
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
   
   /**
    * 
    */
	public boolean deleteRecord(ArrayList<String> searchRec) {
		int jop = JOptionPane.showConfirmDialog(null, "Da li zelite da izbrisete selektovan red?", 
				"Delete", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
		
		if(jop == JOptionPane.CLOSED_OPTION || jop == JOptionPane.NO_OPTION)
			return false;
		
		FILE_POINTER = helpFilePointer;
		
		FileView fileView = (FileView) AppCore.getInstance().getFramework().getSelectedComponent();
		int filePointerForWrite = (int) (FILE_POINTER - (RECORD_SIZE*
				   							(fileView.getTable().getRowCount()-fileView.getTable().getSelectedRow())));
		int filePointerToRead = filePointerForWrite + RECORD_SIZE;
		
		try {
			RandomAccessFile afile=new RandomAccessFile(path+File.separator+fileName,"rw");
			
			while(filePointerForWrite<FILE_SIZE){
				if (FILE_POINTER/RECORD_SIZE+BLOCK_FACTOR>RECORD_NUM) 
					BUFFER_SIZE=(int) (RECORD_NUM-FILE_POINTER/RECORD_SIZE)*RECORD_SIZE;
				else 
					BUFFER_SIZE=(int)(RECORD_SIZE*(BLOCK_FACTOR-1));
				
				buffer=new byte[BUFFER_SIZE];
				
				afile.seek(filePointerToRead);
				
				afile.read(buffer);
				
				afile.seek(filePointerForWrite);
				
				afile.write(buffer);
				
				filePointerForWrite+=buffer.length;
				filePointerToRead+=buffer.length;
				
			}
			afile.setLength(FILE_SIZE-RECORD_SIZE);
			afile.close();
			
			FILE_POINTER = FILE_POINTER - (RECORD_SIZE*fileView.getTable().getRowCount());
			fetchNextBlock();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	
	
	public void makeINDFile()throws IOException{
		 makeTree();
		 String headerINDName=makeINDHeader();
		 
		AppCore.getInstance().getLista().setModel(new ListaModel(path));
		//indeks - sekvencijalna datoteka je kreirana, vrsimo njen prikaz:
		UIINDFile uifile=new UIINDFile(path,headerINDName,false);
		uifile.FILE_POINTER=0;
		FileView fileView=new FileView(uifile);
	    AppCore.getInstance().setFileView(fileView);
	    AppCore.getInstance().getFramework().setSelectedIndex(AppCore.getInstance().getFramework().getSelectedIndex()+1);
	}
	
	
	public String makeINDHeader()throws IOException{
		
	    String headerINDName=headerName.replaceAll(".sek",".ind");
	    File indHeader=new File(path+File.separator+headerINDName);
	    if (!indHeader.exists()){
	    	indHeader.createNewFile();
	    }	 
	   
		//otvaramo header file indeks-sekvencijalne datoteke
		RandomAccessFile afile=new RandomAccessFile(path+File.separator+headerName,"r");
		byte [] temp_buffer=new byte[(int) afile.length()];
		//promenicemo putanju do fajla sa podacima
		afile.read(temp_buffer);
		afile.close();

		
		//u header indeks-sekvencijalne datoteke upisujemo novu putanju i isti opis
		afile=new RandomAccessFile(indHeader.getAbsoluteFile(),"rw");
		afile.seek(0);
		//dodajemo jos i putanju do stable
		afile.writeBytes("tree/"+headerName.replaceAll(".sek",".tree")+"\r\n");
		afile.writeBytes("overZone/"+headerName.replaceAll(".sek",".over")+"\r\n");
		afile.write(temp_buffer);
		afile.setLength(afile.length());
		afile.close();
		return headerINDName;
	}
	
	
	
	public void makeTree()throws IOException{
		FILE_POINTER=0;
		List<Node> listNodes=new ArrayList<Node>();
		
//		Tree tree=null;
		long tFilePointer=0;

		//citanje bloka po bloka i formiranje za svaki blok po jedan NodeElement
		//sva NodeElementa cine jedan Node
	    while (FILE_POINTER<FILE_SIZE ){
	    	tFilePointer=FILE_POINTER;
			fetchNextBlock();
			List <KeyElement> listKeyElements=new ArrayList<KeyElement>();
			
			List <NodeElement> listNodeElements=new ArrayList<NodeElement>();
		    for (int i=0;i<fields.size();i++){
		    	if (fields.get(i).isFieldPK()){
		    		KeyElement keyElement=new KeyElement(fields.get(i).getFieldType(),data[0][i]);
		    		listKeyElements.add(keyElement);
		    	}
			
	    	}
		    //posle ovoga moze se kreirati jedan NodeElement
		    NodeElement nodeElement=new NodeElement((int) (tFilePointer/RECORD_SIZE),listKeyElements);

		    
		    listNodeElements.add(nodeElement);
		    Node node=new Node(listNodeElements);
		    tFilePointer=FILE_POINTER;
			fetchNextBlock();
		    listKeyElements=new ArrayList<KeyElement>();
			
		    for (int i=0;i<fields.size();i++){
		    	if (fields.get(i).isFieldPK()){
		    		KeyElement keyElement=new KeyElement(fields.get(i).getFieldType(),data[0][i]);
		    		listKeyElements.add(keyElement);
		    	}
			
	    	}
		    //posle ovoga moze se kreirati jod jedan NodeElement
		    nodeElement=new NodeElement((int) (tFilePointer/RECORD_SIZE),listKeyElements);

		    
		    listNodeElements.add(nodeElement);
		    //dva NodeElement-a cine jedan Node
		    node=new Node(listNodeElements);
		    listNodes.add(node);		
		}
	    
	    //posle ovoga u listNodes imamo za svaka 2  bloka po jedan Node

	    
	    //prolazak kroz Nodov-e prvog nivoa i formiranje ostalih nivoa:
	    //od dva susedne Node-a iz liste listNodes uzimaju se podaci prvog NodeElement-a
	    // i pravi novi Node na visem nivou. Njegovi NodeElementi imaju kao adresu
	    //ne adresu bloka u datoteci vec poziciju u listi childova neposrednog NodeElement-a
	  
	    
	    //ovaj Node treba da bude koren stabla, odnosno poslednji Node koji cete kreirati
	    
	    Node root= makeTreeNode(listNodes);
	    	    
	    tree=new Tree();
	    tree.setRootElement(root);
	    
		 FILE_POINTER=0;
		//imamo stablo potrebno je serijalizovati ga
		ObjectOutputStream os;
		String treeFileName=headerName.replaceAll(".sek",".tree");
		try{
			os = new ObjectOutputStream(new FileOutputStream(path+File.separator+treeFileName));
			os.writeObject(tree);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
			
	}

	public Node makeTreeNode(List<Node> listNodes){
		List<Node> newNodeList = new ArrayList<Node>();
		
		Node node = null;
		
		for(int i=0;i<listNodes.size();i++){
			if(i%2 == 0){
				node = new Node();
				newNodeList.add(node);
			}
			node.addChild(listNodes.get(i));
			node.setData(listNodes.get(i).getData());
		}
		
		List<NodeElement> ne;
		
		for(int i=0;i<newNodeList.size();i++){
			if(newNodeList.get(i).getChildCount()>1){
				
				ne = new ArrayList<NodeElement>();
				
				ne.add(((Node)newNodeList.get(i).getChildAt(0)).getData().get(0));
				ne.add(((Node)newNodeList.get(i).getChildAt(1)).getData().get(0));
				
				newNodeList.get(i).setData(ne);
			}
		}
		
		if(newNodeList.size() > 1){
			return makeTreeNode(newNodeList);
		} 

		return node;
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
	
}