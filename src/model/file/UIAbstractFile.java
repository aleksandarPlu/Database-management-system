package model.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.event.EventListenerList;

import event.UpdateBlockEvent;
import event.UpdateBlockListener;

public abstract class UIAbstractFile implements IUIFile{
	protected boolean changed = false;
	protected boolean isDelete = false;
	protected ArrayList<Integer> helpDelete;
	protected int previousHelpDelete = 0;
	
	static public final int BROWSE_MODE=1;
	static public final int ADD_MODE=2;
	static public final int UPDATE_MODE=3;
	static public final int DELETE_MODE=4;
	static public final int FIND_MODE=5;
	
	//broj pristupa datoteci prilikom trazenja odredjenog sloga
	protected int ACCESS_NUM=0;
	
   	//velicina bloka u broju slogova koji se uzimaju u jednom zahvatu iz datoteke, tj. faktor blokiranja
	protected long BLOCK_FACTOR=20;
	
	
	//velicina bafera u bajtima, odnosno broj bajtova koji se uyimaju u jednom bloku
	protected int BUFFER_SIZE=0;
	
	//velicina jednog sloga u datoteci u bajtovima, u datotekama sa kojima cemo mi raditi slogovi su fiksne velicine
	// to znaci da svaki slog u jednoj datoteci ima istu dužinu
	protected int RECORD_SIZE=0;
	
	//broj blokova u datoteci
	protected int BLOCK_NUM=0;
	
	//broj slogova u datoteci
	protected long RECORD_NUM=0;
	
	//pointer u datoteci
	protected long FILE_POINTER=0;
	protected long FILE_POINTER_NEW=0;
	
	//velicina datoteke u bajtima
	protected long FILE_SIZE=0;
	
	
	//rezim rada datoteke, inicijalno je datoteka u režimu pregleda
	protected int MODE=UISERFile.BROWSE_MODE;
	
	
	// putanja do direktorijuma u kome se nalazi datoteka
	protected String path;
	//naziv fajla u kome se nalazi header datoteke
	protected String headerName;
	//naziv fajla u kome se nalazi podaci datoteke
	protected String fileName;
	
	protected boolean directory;
	
	//opis polja koji cine slog dobija se iz header datoteke
	protected ArrayList<UIFileField> fields = new ArrayList<UIFileField>();

	//informacije o ExportedKeys
	protected ArrayList<Map<String, String>> exportedKeys = new ArrayList<Map<String, String>>();
    
	//sadrzaj bloka koji se uzima u jednom zahvatu iz datoteke, ovo je u stvari bafer	
	protected byte[] buffer;
	
	//sadrzaj jednog bloka predstavljen kao matrica
	protected String[][] data=null;
	
    //lista slušaca koja se koristi da se osveži prikaz tabele u klasi FileView
	//prilikom ucitavanja novog bloka iz datoteke 
	
	EventListenerList listenerBlockList = new EventListenerList();
	UpdateBlockEvent updateBlockEvent = null;

	public UIAbstractFile(String path,String headerName, boolean directory) {
		this.path = path;
		this.headerName=headerName;
		this.directory=directory;
		this.fileName=headerName;
	}

	public UIAbstractFile() {
	}
	
	/**
	 * Metoda za formiranje zaglavlja je identicna kod serijske i sekvencijalne
	 * datoteke, zbog toga se nalazi u ovoj apstraktnoj klasi
	 * @throws SQLException 
	 */
	public void readHeader() throws IOException, SQLException{
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
	}	
	
	
	
	//registracija i deregistracija slusaca
	 public void addUpdateBlockListener(UpdateBlockListener l) {
		 listenerBlockList.add(UpdateBlockListener.class, l);
	 }

	 public void removeUpdateBlockListener(UpdateBlockListener l) {
		 listenerBlockList.remove(UpdateBlockListener.class, l);
	 }
	 
	 //kada se izvrsi odgovarajuca akcija, sve observere (slusace) obavestavamo da se dogadjaj desio
	protected void fireUpdateBlockPerformed() {
	     Object[] listeners = listenerBlockList.getListenerList();
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==UpdateBlockListener.class) {
	             if (updateBlockEvent == null)
	            	 updateBlockEvent = new UpdateBlockEvent(this);
	             ((UpdateBlockListener)listeners[i+1]).updateBlockPerformed(updateBlockEvent);
	         }
	     }

	 }	
	
	
	
    public void sortMDI()throws IOException{
		//kreiramo bafer velicine N+1 slogova
		byte [] sort_buffer=new byte[(int) (RECORD_SIZE*(RECORD_NUM+1))];
		String [][]sort_data = new String[(int) RECORD_NUM+1][];
		
		RandomAccessFile afile=new RandomAccessFile(path+File.separator+fileName,"r");
		  
		afile.seek(0);
		afile.read(sort_buffer);
		afile.close();
	    String contentS=new String(sort_buffer);
	    if (contentS.length()<sort_buffer.length){
	      for (int x=contentS.length();x<sort_buffer.length;x++)
	    	  contentS=contentS+" ";
	    }
	
	      
	     for (int i=0;i<RECORD_NUM;i++){
	    	  
	    	 String line=contentS.substring(i*RECORD_SIZE,i*RECORD_SIZE+RECORD_SIZE);
	    	 
	    	 sort_data[i] = new String[RECORD_SIZE]; 
			 int k=0;
			 for (int j=0;j<fields.size();j++){
				String field=null;
			   	field=   line.substring(k,k+fields.get(j).getFieldLength());

			   	sort_data[i][j]=field;
				k=k+fields.get(j).getFieldLength();
			 }		
	    	  
	      }		
	     
//		//sada u sort_data imam matricu svih slogova iz datoteke, krecem u sortiranje
//	     String minValue="";
//	     for (int f=0;f<fields.size();f++){
//	    	  if (fields.get(f).isSort()){
//	                for (int i=0;i<RECORD_NUM-1;i++){
//	    	             minValue=sort_data[i][f];
//		                 int k=i;	 
//		   
//	        	    	 for (int j=i+1;j<RECORD_NUM;j++){
//			                     //prvo provera da li su prethodna obelezja identicna
//			                     boolean comp=true;
//			                     for (int p=0;p<f;p++){
//			                	     if (fields.get(p).isSort()){
//			                		    if (sort_data[i][p].compareToIgnoreCase(sort_data[j][p])!=0){
//			                		    	comp=false;
//			                			 
//			                		    }
//			                	     }
//			                     }
//			                     //----------------------------------------------------
//
//	    		                if ((
//	    		                		(minValue.compareToIgnoreCase(sort_data[j][f])>0 && fields.get(f).isAsc()) || 
//	    		                		(minValue.compareToIgnoreCase(sort_data[j][f])<0 && !fields.get(f).isAsc())
//	    		                	) 
//	    		                	&&
//	    		                	comp){
//                                    minValue=sort_data[j][f];
//                                    k=j;
//	    		                }
//	                     }
//	    	             //  posle ovog u k imam index najmanjeg sloga, izvrsi zamenu
//	    	             sort_data[(int) (RECORD_NUM)]=sort_data[i];
//	    	             sort_data[i]=sort_data[k];
//	    	             sort_data[k]=sort_data[(int) (RECORD_NUM)];
//	               }//for i  
//	        }//if sort
//	     } //of f
//		//zavrseno sortiranje osvezi
		
	     
	     ArrayList<Integer> indexOfSortFields = new ArrayList<Integer>();
	     for(int i=0;i<fields.size();i++){
	    	 if(fields.get(i).isSort())
	    		 indexOfSortFields.add(i);
	     }
	     
	     
	     if(fields.get(indexOfSortFields.get(0)).getFieldType().equals("TYPE_CHAR") || 
	    		 fields.get(indexOfSortFields.get(0)).getFieldType().equals("TYPE_VARCHAR") ||
	    		 fields.get(indexOfSortFields.get(0)).getFieldType().equals("TYPE_DATETIME")){
	    	     	 
	     String minValue = "";
	     int index = indexOfSortFields.get(0);
	     for(int i=0;i<RECORD_NUM-1;i++){
	    	 minValue = sort_data[i][index];
	    	 int k=i;
	    	 for (int j=i+1;j<RECORD_NUM;j++){
	    		 if(minValue.compareToIgnoreCase(sort_data[j][index]) == 0){
	    			 
	    			 if(indexOfSortFields.size() > 1) {
	    				 if(fields.get(indexOfSortFields.get(1)).getFieldType().equals("TYPE_CHAR") || 
	    						 fields.get(indexOfSortFields.get(1)).getFieldType().equals("TYPE_VARCHAR") ||
	    						 fields.get(indexOfSortFields.get(1)).getFieldType().equals("TYPE_DATETIME")) {
	    					 k = helpSortString(indexOfSortFields, k, j, sort_data, 1);
	    				 } else 
	    					 k = helpSortInt(indexOfSortFields, k, j, sort_data, 1);
	    					 
	    				 if(k==j)
	    					 minValue=sort_data[j][indexOfSortFields.get(0)];
	    			 }

	    		 } else if((minValue.compareToIgnoreCase(sort_data[j][index]) > 0 && fields.get(index).isAsc()) ||
	    				   (minValue.compareToIgnoreCase(sort_data[j][index]) < 0 && !fields.get(index).isAsc())){
	    			 
	    			 minValue=sort_data[j][indexOfSortFields.get(0)];
	    			 k=j;
	    		 }
	    	 }
	    	 sort_data[(int) (RECORD_NUM)]=sort_data[i];
	    	 sort_data[i]=sort_data[k];
	    	 sort_data[k]=sort_data[(int) (RECORD_NUM)];
	     }
	     } else {
	     	 
			 int minValue;
			 int index = indexOfSortFields.get(0);
			 for(int i=0;i<RECORD_NUM-1;i++){
				 minValue = Integer.valueOf(sort_data[i][index].trim());
				 int k=i;
				 for (int j=i+1;j<RECORD_NUM;j++){
					 if(minValue == Integer.valueOf(sort_data[j][index].trim())){
						 
						 if(indexOfSortFields.size() > 1)
							 if(fields.get(indexOfSortFields.get(1)).getFieldType().equals("TYPE_CHAR") || 
		    						 fields.get(indexOfSortFields.get(1)).getFieldType().equals("TYPE_VARCHAR") ||
		    						 fields.get(indexOfSortFields.get(1)).getFieldType().equals("TYPE_DATETIME")) {
		    					 k = helpSortString(indexOfSortFields, k, j, sort_data, 1);
		    				 } else 
		    					 k = helpSortInt(indexOfSortFields, k, j, sort_data, 1);
						 
						 if(k==j)
							 minValue=Integer.valueOf(sort_data[j][indexOfSortFields.get(0)].trim());
						 
					 } else if((minValue > Integer.valueOf(sort_data[j][index].trim()) && fields.get(index).isAsc()) ||
							   (minValue < Integer.valueOf(sort_data[j][index].trim()) && !fields.get(index).isAsc())){
						 
						 minValue=Integer.valueOf(sort_data[j][indexOfSortFields.get(0)].trim());
						 k=j;
					 }
				 }
				 sort_data[(int) (RECORD_NUM)]=sort_data[i];
				 sort_data[i]=sort_data[k];
				 sort_data[k]=sort_data[(int) (RECORD_NUM)];
			 }
	     }
	     
	     
	     data=sort_data;
	     fireUpdateBlockPerformed();
	     
	}

    int helpSortString(ArrayList<Integer> sortFields,int i,int j,String[][] sort_data,int currentIndex){
    	String s1 = sort_data[i][sortFields.get(currentIndex)];
    	String s2 = sort_data[j][sortFields.get(currentIndex)];
    	if(s1.compareToIgnoreCase(s2) == 0){
    		if(currentIndex < sortFields.size()-1){
    			if(fields.get(sortFields.get(currentIndex+1)).getFieldType().equals("TYPE_CHAR") || 
				   fields.get(sortFields.get(currentIndex+1)).getFieldType().equals("TYPE_VARCHAR") ||
				   fields.get(sortFields.get(currentIndex+1)).getFieldType().equals("TYPE_DATETIME")) {
					
    				helpSortString(sortFields, i, j, sort_data, currentIndex+1);
				 } else 
					 helpSortInt(sortFields, i, j, sort_data, currentIndex+1);
    		}
    	} else if((s1.compareToIgnoreCase(s2) > 0 && fields.get(sortFields.get(currentIndex)).isAsc()) ||
    			 (s1.compareToIgnoreCase(s2) < 0 && !fields.get(sortFields.get(currentIndex)).isAsc())){
    			 return j;
    	} 
    	
    	return i;
    }
    
    int helpSortInt(ArrayList<Integer> sortFields,int i,int j,String[][] sort_data,int currentIndex){
    	int s1 = Integer.valueOf(sort_data[i][sortFields.get(currentIndex)].trim());
    	int s2 = Integer.valueOf(sort_data[j][sortFields.get(currentIndex)].trim());
    	if(s1 == s2){
    		if(currentIndex < sortFields.size()-1){
    			 if(fields.get(sortFields.get(currentIndex+1)).getFieldType().equals("TYPE_CHAR") || 
    			    fields.get(sortFields.get(currentIndex+1)).getFieldType().equals("TYPE_VARCHAR") ||
					fields.get(sortFields.get(currentIndex+1)).getFieldType().equals("TYPE_DATETIME")) {
					
    				 helpSortString(sortFields, i, j, sort_data, currentIndex+1);
				 } else 
					 helpSortInt(sortFields, i, j, sort_data, currentIndex+1);
    		}
    	} else if((s1 > s2 && fields.get(sortFields.get(currentIndex)).isAsc()) ||
    			  (s1 < s2 && !fields.get(sortFields.get(currentIndex)).isAsc())){
    			 return j;
    	} 
    	
    	return i;
    }
    
    public void sortMM()throws IOException{
    	
   		byte [] sort_buffer=new byte[ (int) (RECORD_SIZE*RECORD_NUM)];
		
		//broj prolaza za sortiranje
		int F=(int) Math.ceil(Math.log(RECORD_NUM)/Math.log(2));
		//ukoliko nije F ceo broj dodajemo fd lažnih slogova u datoteku 
    	int fd=(int) (Math.pow(2,F)-RECORD_NUM);
    	
    	//prvi source bafer u kome držimo nesortiranu datoteku
		String [][]sort_data1 = new String[(int) RECORD_NUM+fd][];
		//drugi bafer u kome smeštamo sortirane slogove
		String [][]sort_data2 = new String[(int) RECORD_NUM+fd][];
		//pomocni bafer koji služi za zamenu sort_data1 i sort_data2
		String [][]temp = new String[(int) RECORD_NUM+fd][];
		
		
		//ucitavamo kompletnu datoteku u bafer----------------------------------------
		RandomAccessFile afile=new RandomAccessFile(path+File.separator+fileName,"r");
        afile.seek(0);
        afile.read(sort_buffer);
		afile.close();
		//----------------------------------------------------------------------------
		
		
		
		//podatke iz bafera pretvaramo u matricu sort_buffer1--------------------------
	    String contentS=new String(sort_buffer);
	    if (contentS.length()<sort_buffer.length){
	      for (int x=contentS.length();x<sort_buffer.length;x++)
	    	  contentS=contentS+" ";
	    }
	
	      
	     for (int i=0;i<RECORD_NUM;i++){
	    	  
	    	 String line=contentS.substring(i*RECORD_SIZE,i*RECORD_SIZE+RECORD_SIZE);
	    	 
	    	 sort_data1[i] = new String[RECORD_SIZE]; 
			 int k=0;
			 for (int j=0;j<fields.size();j++){
				String field=null;
			   	field=line.substring(k,k+fields.get(j).getFieldLength());
			   	sort_data1[i][j]=field;
				k=k+fields.get(j).getFieldLength();
			 }		
 
	      }
	     //---------------------------------------------------------------------------------
	     
	     //dodajemo lažne slogove da bi dobili uslov koji nam treba za metodu mešanja
	     //sa jednakim brojem slogova u redosledu
	     addTempRecords(sort_data1, fd);
	     
//	     //ukoliko je više obeležja u sortnom kriterijumu imamo više prolaza
//	     //ovo treba promeniti, bez ove prve for petlje ubrzacemo sortiranje
//	     for (int f=0;f<fields.size();f++){
//	    	  if (fields.get(f).isSort()){
	    		   
                    //duzina redosleda, na pocetku u redosledu samo jedan slog
	                int d=1; 
	                //broj grupa redosleda u jednom prolazu
	                //u prvom prolazu broj grupa k=N/2
                    int k=(int) Math.floor((RECORD_NUM+fd)/2);
                   
                    ArrayList<Integer> indexOfSortFields = new ArrayList<Integer>();
            		for(int i=0;i<fields.size();i++){
            			if(fields.get(i).isSort())
            				indexOfSortFields.add(i);
            		}
            		
        	        while (k>=1) {
                         //j je redni broj grupe od po 2 redosleda
        		         int j=1; 
        		         while (j<=k){
        		    	    sortGrupe(sort_data1,sort_data2,d,j,indexOfSortFields,0);
        		    	    j++;
      		             }
        		         // u svakom narednom prolazu broj grupa je duplo manji
        		         k=k/2;
                         //u svakom narednom prolazu redosled ima duplo više slogova
        		         d=2*d;  
        		         temp=sort_data1;
        		         sort_data1=sort_data2;
        		         sort_data2=temp;
          	        }
//	    	   }
//	     }
	     
	     //uklanjamo lažne slogove i prikazujemo rezultat sortiranja
	     data=removeTempRecords(sort_data1, fd);
	     fireUpdateBlockPerformed();
    }
    
    
	private void sortGrupe(String [][]source,String [][]dest, int d, int j,ArrayList<Integer> indexOfSortFields,int in){
		
				
		int index = indexOfSortFields.get(in);
		
		if(fields.get(indexOfSortFields.get(0)).getFieldType().equals("TYPE_CHAR") || 
		   fields.get(indexOfSortFields.get(0)).getFieldType().equals("TYPE_VARCHAR") ||
		   fields.get(indexOfSortFields.get(0)).getFieldType().equals("TYPE_DATETIME")){
			
		//algoritam spaje dva redosleda dužine d u jedan redosled dužine 2*d
		int t=2*(j-1)*d+1; //t je indeks tekuceg elementa dest[][]
		int p=2*(j-1)*d+1; //indeks prvog redosleda
		int q=(2*j-1)*d+1; //indeks drugog redosleda
		
		while (t<=2*j*d){
			if (p>(2*j-1)*d){
				dest[t-1]=source[q-1];
				q++;
			}else{ 
				if (q>2*j*d){
				   dest[t-1]=source[p-1];
				   p++;
				}else{
					
					if ((source[p-1][index].compareToIgnoreCase(source[q-1][index])<0 && fields.get(index).isAsc()) || 
						(source[p-1][index].compareToIgnoreCase(source[q-1][index])>0 && !fields.get(index).isAsc())){
							
						dest[t-1]=source[p-1];
						p++;
					}else{
						if(in < indexOfSortFields.size()-1){
							if(source[p-1][index].compareToIgnoreCase(source[q-1][index]) == 0){
								int w = 0;
								if(fields.get(indexOfSortFields.get(1)).getFieldType().equals("TYPE_CHAR") || 
								   fields.get(indexOfSortFields.get(1)).getFieldType().equals("TYPE_VARCHAR") ||
								   fields.get(indexOfSortFields.get(1)).getFieldType().equals("TYPE_DATETIME")) {
									 
									w = helpSortMMString(source, p, q, indexOfSortFields.get(in+1),indexOfSortFields, in+1);
								} else {
									w = helpSortMMInt(source, p, q, indexOfSortFields.get(in+1),indexOfSortFields, in+1);
								}
								 
								if(w==1){
									dest[t-1]=source[p-1];
									p++;
								} else {
									dest[t-1]=source[q-1];
									q++;
								}
							} else {
								dest[t-1]=source[q-1];
								q++;
							}
						} else {
							dest[t-1]=source[q-1];
							q++;
						}
					}	
				}
			}	
			t++;
		}
		// Sortiranje za Integer type
		} else {
		
			//algoritam spaje dva redosleda dužine d u jedan redosled dužine 2*d
			int t=2*(j-1)*d+1; //t je indeks tekuceg elementa dest[][]
			int p=2*(j-1)*d+1; //indeks prvog redosleda
			int q=(2*j-1)*d+1; //indeks drugog redosleda
			
			while (t<=2*j*d){
				if (p>(2*j-1)*d){
					dest[t-1]=source[q-1];
					q++;
				}else{ 
					if (q>2*j*d){
					   dest[t-1]=source[p-1];
					   p++;
					}else{
												
						if ((Integer.valueOf(source[p-1][index].trim()) < Integer.valueOf(source[q-1][index].trim()) && fields.get(index).isAsc()) || 
							(Integer.valueOf(source[p-1][index].trim()) > Integer.valueOf(source[q-1][index].trim()) && !fields.get(index).isAsc())){
								
							dest[t-1]=source[p-1];
							p++;
						}else{
							if(in < indexOfSortFields.size()-1){
								if(Integer.valueOf(source[p-1][index].trim()) == Integer.valueOf(source[q-1][index].trim())){
									int w = 0;
									if(fields.get(indexOfSortFields.get(1)).getFieldType().equals("TYPE_CHAR") || 
									   fields.get(indexOfSortFields.get(1)).getFieldType().equals("TYPE_VARCHAR") ||
									   fields.get(indexOfSortFields.get(1)).getFieldType().equals("TYPE_DATETIME")) {
										 
										w = helpSortMMString(source, p, q, indexOfSortFields.get(in+1),indexOfSortFields, in+1);
									} else {
										w = helpSortMMInt(source, p, q, indexOfSortFields.get(in+1),indexOfSortFields, in+1);
									}
									 
									if(w==1){
										dest[t-1]=source[p-1];
										p++;
									} else {
										dest[t-1]=source[q-1];
										q++;
									}
								} else {
									dest[t-1]=source[q-1];
									q++;
								}
							} else {
								dest[t-1]=source[q-1];
								q++;
							}
						}	
					}
				}	
				t++;
			}	
		}
	}

	int helpSortMMString(String[][] source, int p, int q, int index,ArrayList<Integer> indexOfSortFields, int in){
		if(source[p-1][index].compareToIgnoreCase(source[q-1][index]) == 0){
			if(in < indexOfSortFields.size()-1){
				 if(fields.get(indexOfSortFields.get(in+1)).getFieldType().equals("TYPE_CHAR") || 
					fields.get(indexOfSortFields.get(in+1)).getFieldType().equals("TYPE_VARCHAR") ||
					fields.get(indexOfSortFields.get(in+1)).getFieldType().equals("TYPE_DATETIME")) {
		    				 
					 helpSortMMString(source, p, q, indexOfSortFields.get(in+1), indexOfSortFields, in+1);
				 } else {
					 helpSortMMInt(source, p, q, indexOfSortFields.get(in+1), indexOfSortFields, in+1);
				 }
			} 
		} else if ((source[p-1][index].compareToIgnoreCase(source[q-1][index])<0 && fields.get(index).isAsc()) || 
				  (source[p-1][index].compareToIgnoreCase(source[q-1][index])>0 && !fields.get(index).isAsc())){
				return 1;
		}
		return 0;
	}
	
	int helpSortMMInt(String[][] source, int p, int q, int index,ArrayList<Integer> indexOfSortFields, int in){
		if(Integer.valueOf(source[p-1][index].trim()) == Integer.valueOf(source[q-1][index].trim())){
			if(in < indexOfSortFields.size()-1){
				 if(fields.get(indexOfSortFields.get(in+1)).getFieldType().equals("TYPE_CHAR") || 
					fields.get(indexOfSortFields.get(in+1)).getFieldType().equals("TYPE_VARCHAR") ||
					fields.get(indexOfSortFields.get(in+1)).getFieldType().equals("TYPE_DATETIME")) {
		    				 
					 helpSortMMString(source, p, q, indexOfSortFields.get(in+1), indexOfSortFields, in+1);
				 } else {
					 helpSortMMInt(source, p, q, indexOfSortFields.get(in+1), indexOfSortFields, in+1);
				 }
			}
		} else if ((Integer.valueOf(source[p-1][index].trim()) < Integer.valueOf(source[q-1][index].trim()) && fields.get(index).isAsc()) || 
				  (Integer.valueOf(source[p-1][index].trim()) > Integer.valueOf(source[q-1][index].trim()) && !fields.get(index).isAsc())){
				return 1;
		}
		return 0;
	}
	
	private void addTempRecords(String [][] sort_data1, int fd){
		 for (int i=(int) RECORD_NUM;i<RECORD_NUM+fd;i++){
	         sort_data1[i] = new String[RECORD_SIZE]; 
			 for (int j=0;j<fields.size();j++){
				 if(fields.get(j).getFieldType().equals("TYPE_CHAR") || 
							fields.get(j).getFieldType().equals("TYPE_VARCHAR") ||
							fields.get(j).getFieldType().equals("TYPE_DATETIME")) {
				 
					String field=null;
					for (int x=0;x<fields.get(j).getFieldLength();x++){
				   	   field="~";
					}   
				   	sort_data1[i][j]=field;
				 
				} else {
					Integer field = null;
					for (int x=0;x<fields.get(j).getFieldLength();x++){
				   	   field= Integer.MAX_VALUE;
					}   
				   	sort_data1[i][j]=String.valueOf(field);
				 
				}
			 }		
 	      }			     

	}
	private  String [][] removeTempRecords(String [][] sort_data1, int fd){
		 String [][]temp = new String[(int) RECORD_NUM][];
		 int k=0;
		 for (int i=0;i<RECORD_NUM+fd;i++){
			 
			 if (!sort_data1[i][0].contains("~")){
				 temp[k] = new String[RECORD_SIZE];
				 temp[k]=sort_data1[i];
				 k++;
			 }
	      }	
		 return temp;

	}
	protected void makeSortPK(){
		for (int i=0;i<fields.size();i++){
			fields.get(i).setSort(fields.get(i).isFieldPK());
			fields.get(i).setAsc(fields.get(i).isFieldPK());
		}
		
	}
	
	public void setBLOCK_SIZE(long block_size) {
		BLOCK_FACTOR = block_size;
		BLOCK_NUM=(int) (RECORD_NUM/BLOCK_FACTOR)+1;
	}
	
	
	// get metode za klasu
	public static int getADD_MODE() {
		return ADD_MODE;
	}

	public static int getBROWSE_MODE() {
		return BROWSE_MODE;
	}

	public static int getDELETE_MODE() {
		return DELETE_MODE;
	}

	public static int getFIND_MODE() {
		return FIND_MODE;
	}

	public static int getUPDATE_MODE() {
		return UPDATE_MODE;
	}

	public int getBLOCK_NUM() {
		return BLOCK_NUM;
	}

	public long getBLOCK_FACTOR() {
		return BLOCK_FACTOR;
	}

	public byte[] getBlockContent() {
		return buffer;
	}

	public String[][] getData() {
		return data;
	}

	public boolean isDirectory() {
		return directory;
	}

	public ArrayList<UIFileField> getFields() {
		return fields;
	}

	public long getFILE_POINTER() {
		return FILE_POINTER;
	}

	public long getFILE_SIZE() {
		return FILE_SIZE;
	}

	public String getFileName() {
		return fileName;
	}

	public String getHeaderName() {
		return headerName;
	}

	public int getMODE() {
		return MODE;
	}

	public String getPath() {
		return path;
	}

	public long getRECORD_NUM() {
		return RECORD_NUM;
	}

	public int getRECORD_SIZE() {
		return RECORD_SIZE;
	}

	public void setMODE(int mode) {
		MODE = mode;
	}

	public String toString(){
		return headerName;
	}

	public int getACCESS_NUM() {
		return ACCESS_NUM;
	}

	public void setACCESS_NUM(int aCCESS_NUM) {
		ACCESS_NUM = aCCESS_NUM;
	}

	public void setFILE_POINTER(long fILEPOINTER) {
		FILE_POINTER = fILEPOINTER;
	}



	public void setRECORD_SIZE(int rECORDSIZE) {
		RECORD_SIZE = rECORDSIZE;
	}
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public long getFILE_POINTER_NEW() {
		return FILE_POINTER_NEW;
	}

	public void setFILE_POINTER_NEW(long fILE_POINTER_NEW) {
		FILE_POINTER_NEW = fILE_POINTER_NEW;
	}

	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}

	public ArrayList<Map<String, String>> getExportedKeys() {
		return exportedKeys;
	}

}
