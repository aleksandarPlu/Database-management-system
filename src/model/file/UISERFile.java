package model.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import model.FindRecordThread;
import model.ListaModel;
import view.FileView;
import app.AppCore;

public class UISERFile extends UIAbstractFile {
	
	public UISERFile(String path,String headerName, boolean directory) {
		super(path,headerName,directory);
	
	}

	public UISERFile() {
		super();
	}

    /**
     *  Prenos bloka iz datoteke u radnu memoriju aplikacije
     *  Velicina bloka odredjena je atributom BLOCK_SIZE 
     *  Po zavšetku metode blok podataka iz datoteke
     *  nalazi se u radnoj memoriji aplikaciji
     *  u matrici data[][]
     * @throws IOException 
     */	
	public boolean fetchNextBlock() throws IOException{
		FileView fileView = (FileView) AppCore.getInstance().getFramework().getSelectedComponent();
		helpDelete = new ArrayList<Integer>();
		
		if(getMODE()!=4){
			if(FILE_POINTER!=0){
				setACCESS_NUM(getACCESS_NUM()+1);
				fileView.getTxtAccessNum().setText(String.valueOf(getACCESS_NUM()));
			} else {
				setACCESS_NUM(1);
				fileView.getTxtAccessNum().setText(String.valueOf(getACCESS_NUM()));
			}
		}
		
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
				String field = null;
				field = line.substring(k,k+fields.get(j).getFieldLength());
				data[i][j] = field;
				k = k+fields.get(j).getFieldLength();
			}	
		}
		
		String[][] helpData = new String[(int) BUFFER_SIZE/RECORD_SIZE][];
		int z=0;
		for (int i=0;i<BUFFER_SIZE/RECORD_SIZE;i++){
			helpData[i] = new String[fields.size()];
			
			if(!data[i][fields.size()-1].equals("1")){
				helpData[z++] = data[i];
				helpDelete.add(i+previousHelpDelete);
			}
		}
		
		String[] helpHelpData;
		int helpDel=(int) BLOCK_FACTOR-1;
		for(int i=0;i<BUFFER_SIZE/RECORD_SIZE;i++){
			if(helpData[i][0] == null){
				while(true){
					helpDel++;
					String line = afile.readLine();
					if(line==null){
						break;
					}
					helpHelpData = new String[fields.size()];
					int k=0;
					for (int j=0;j<fields.size();j++){
						String field = null;
						field = line.substring(k,k+fields.get(j).getFieldLength());
						helpHelpData[j] = field;
						k = k+fields.get(j).getFieldLength();
					}
					if(!helpHelpData[fields.size()-1].equals("1")){
						helpData[i] = helpHelpData;
						helpDelete.add(helpDel+previousHelpDelete);
						break;
					}
				}
			}
		}
				
		data = helpData;
		
//		for(int i=0;i<helpDelete.size();i++){
//			System.out.println((helpDelete.get(i)+1));
//		}

		if(helpDelete.size()>0){
			previousHelpDelete=helpDelete.get(helpDelete.size()-1)+1;
		}
//		long fp=FILE_POINTER;
		
		FILE_POINTER=afile.getFilePointer();
		afile.close();
		
		//ucitavanje novog bloka treba da izazove osvezivanje podataka u tabeli
		fireUpdateBlockPerformed();
		
		return true;
     }
	
	/**
	 *  Metoda za dodavanje sloga u serijskoj datoteci, dodavanje se uvek vrši na kraj
	 * datoteke. 
	 */
   public boolean addRecord(List<List<String>> record)throws IOException{
	   String newRecord = "";
	   for(List<String> str : record){
		   newRecord+="\n";	
		   for(String st : str){
			   newRecord+=st;
		   }
	   }
	   
	   RandomAccessFile afile=new RandomAccessFile(path+File.separator+fileName,"rw");
	   
	   afile.seek(afile.length());
	   afile.writeBytes(newRecord);
	   afile.setLength(afile.length());
	   afile.close();
	   JOptionPane.showMessageDialog(null, "Uspesno ste dodali nove slogove", "UI Project", 1);
	   return true;
   }


   /**
    *  Izmenu podataka u serijskoj datoteci necemo raditi
    *  
    */
   public boolean updateRecord(ArrayList<String> oldRecord,ArrayList<String> record, int row) throws IOException{
	   return false;
   }

   
   /**
    *  Pretraga u serijskoj datoteci koja pocinje od pocetka datoteke
    * Moze da se pretrazuje po bilo kom polju datoteke
    * metoda zaustavlja pretragu na prvom slogu koji zadovoljava zadate kriterijume
    * ili po neuspesnom trazenju sloga a to je na kraju datoteke
    */
   public boolean findRecord(ArrayList<String> searchRec,int[] position, boolean start, boolean viseSlogova, boolean snimiURAM){
	      
	   String str= "";
	   for(String s : searchRec){
		   str+=s.trim();
	   }
	   
	   if(str.length() <= 1){
		   return false;
	   }
	   
	   if(start){
		   FILE_POINTER=0;
	   }
	   
	   FileView fileView = (FileView) AppCore.getInstance().getFramework().getSelectedComponent();
	   
	   FindRecordThread frt = new FindRecordThread(fileView, position, searchRec, viseSlogova, snimiURAM);
	   
	   frt.start();
	   	   
	   return true;
   }
       
   
   /**
    * @param aData - jedan slog iz bloka datoteke
    * @param searchRec - parametri pretrage
    * @return - true ukoliko dati slog iz bloka sadrzi polja koja odgovaraju parametrima pretrage
    */
   public boolean isRowEqual(String [] aData, ArrayList<String> searchRec){
	   boolean result=true;
		     for (int col=0;col<fields.size();col++){
		    	 if (!searchRec.get(col).trim().equals("")){
		    		  if (!aData[col].trim().equals(searchRec.get(col).trim())){
		    			  result=false;
		    			  return result;
		    		  }
		    	 }
	   }	   
	   return result;
   }
   
   
   /**
    * Brisanje u serijskoj datoteci realizovati logicki
    */
   public boolean deleteRecord(ArrayList<String> searchRec) {
	   FILE_POINTER=helpDelete.get(0)*RECORD_SIZE;
	   previousHelpDelete=helpDelete.get(0);
	   
	   while (FILE_POINTER<FILE_SIZE){
		   try {
			   fetchNextBlock();
		   } catch (IOException e) {
			   e.printStackTrace();
			   return false;
		   }
			
		   for (int row=0;row<data.length;row++){
				   
			   if (isRowEqual(data[row],searchRec)){
				   String content="";
				   for (int i=0;i<data[row].length-1;i++){
					   content+=data[row][i];
				   }
				   content+="1";
				   try {
					   RandomAccessFile raf = new RandomAccessFile(path+File.separator+fileName, "rw");
					   
					   long filePointer = helpDelete.get(row)*RECORD_SIZE;

					   raf.seek(filePointer);
					   
					   raf.writeBytes(content);
					   raf.close();
					   
					   FILE_POINTER=helpDelete.get(0)*RECORD_SIZE;
					   previousHelpDelete=helpDelete.get(0);
					   fetchNextBlock();
					   
				   } catch (FileNotFoundException e) {
					   e.printStackTrace();
				   } catch (IOException e) {
					   e.printStackTrace();
				   }
				   return true;
			   }   
		   }	
	   }
	   return false;
   }

	
	
	
	public boolean makeSEKFile() throws IOException{
	    boolean result=true;
	   
	    //prvo pozivamo sortiranje serijske datoteke
	    //pre sortiranja trebamo obezbediti sortiranje po rastucoj vrednosti kljuca 
	    makeSortPK();
	  
	    try {
		  sortMDI();
	    } catch (IOException e) {
		    JOptionPane.showMessageDialog(AppCore.getInstance(),e.getMessage(),"Greška",JOptionPane.ERROR_MESSAGE);
  		    //neuspesno sortiranje
		    result=false;
		    return result;
        }
       
	    
	    //posle sortiranja u baferu data[][] imamo sortirane slogove
	    //trebamo jos da napravimo novu sekvencijalnu datoteke sa novim headerom
	    String headerSekName=headerName.replaceAll(".ser",".sek");
	    File serHeader=new File(path+File.separator+headerSekName);
	    if (!serHeader.exists()){
			serHeader.createNewFile();
	    }	 
	   
	   
		//otvaramo header file serijske datoteke
		RandomAccessFile afile=new RandomAccessFile(path+File.separator+headerName,"r");
		byte [] temp_buffer=new byte[(int) afile.length()];
		//promenicemo putanju do fajla sa podacima
		String tpath=afile.readLine();
		tpath=tpath.replace(".txt",".stxt");
		afile.read(temp_buffer);
		afile.close();

		
		//u header sekvencijalne datoteke upisujemo novu putanju i isti opis
		afile=new RandomAccessFile(serHeader.getAbsoluteFile(),"rw");
		afile.seek(0);
		afile.writeBytes(tpath+"\r\n");
		afile.write(temp_buffer);
		afile.setLength(afile.length());
		afile.close();
			

		//jos ostaje da se sortirani slogovi upisu u novu datoteku
		String fileSekName=fileName.replaceAll(".txt",".stxt");
		File serText=new File(path+File.separator+fileSekName);
		serText.createNewFile();
		afile=new RandomAccessFile(serText.getAbsoluteFile(),"rw");
		afile.seek(0);
		
		for (int i=0;i<data.length;i++){
		     for (int j=0;j<fields.size();j++){
		          afile.writeBytes(data[i][j].toString());
		     }     
		     afile.writeBytes("\r\n");
		}
		
		afile.close();
		
		AppCore.getInstance().getLista().setModel(new ListaModel(path));
		//sekvencijalna datoteka je kreirana, vrsimo njen prikaz:
		UISEKFile uifile=new UISEKFile(path,serHeader.getName(),false);
		FileView fileView=new FileView(uifile);
    	AppCore.getInstance().setFileView(fileView);
    	AppCore.getInstance().getFramework().setSelectedIndex(AppCore.getInstance().getFramework().getSelectedIndex()+1);
	   
	   return result;
	}
	
	public String readFile() {
		String content = "";
		if(this.getPath() == null)
			return content;
		try {		
			RandomAccessFile raf=new RandomAccessFile(this.getPath() + "\\" + this.getFileName(),"r");
			while (raf.getFilePointer() < raf.length()) {
				content=content+raf.readLine()+"\n";
            }
			raf.close(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return content;
	}

	public void SaveFile(String content, int index){
		try {
			RandomAccessFile raf = null;
			FileView fileView = (FileView) AppCore.getInstance().getFramework().getSelectedComponent();
			UISERFile uiFile = (UISERFile) fileView.getUiFile();
//			File fileExists = new File(this.getPath() + "\\" + this.getFileName());
			
			if(this.getPath()!=null){	// && fileExists.exists()){
				raf=new RandomAccessFile(this.getPath() + "\\" + this.getFileName(),"rw");

				content = content.replace("\n","\r\n");
				
				raf.writeBytes(content);
				raf.setLength(content.length());
				raf.close();
				
				AppCore.getInstance().getFramework().setTitleAt(index, this.getFileName());
				uiFile.setChanged(false);				
			} else {
				AppCore.getInstance().getActionManager().getSaveAsFileAction().actionPerformed(null);
				uiFile.setChanged(false);
			}
						
		} catch (Exception e) {
			e.printStackTrace();
		}
		AppCore.getInstance().getActionManager().getSaveFileAction().setEnabled(false);
	}
		
}