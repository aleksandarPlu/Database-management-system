package model;

import java.awt.Cursor;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import javax.crypto.spec.OAEPParameterSpec;
import javax.swing.JOptionPane;

import app.AppCore;
import model.file.UIAbstractFile;
import model.file.UISERFile;
import view.FileView;

public class FindRecordThread extends Thread {

	private FileView fileView;
	private int[] position;
	private ArrayList<String> searchRec;
	private boolean viseSlogova;
	private boolean snimiURAM;
	
	public FindRecordThread(FileView fileView, int[] position,ArrayList<String> searchRec, boolean viseSlogova, boolean snimiURAM){
		this.fileView = fileView;
		this.position = position;
		this.searchRec= searchRec;
		this.viseSlogova = viseSlogova;
		this.snimiURAM = snimiURAM;
	}
	
	@Override
	public void run() {
		UIAbstractFile uiFile = fileView.getUiFile();
		
		ProgressBarThread pbt = new ProgressBarThread();
		pbt.start();
		
		fileView.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		ArrayList<String[]> viseSlogovaAL = new ArrayList<String[]>();
		
		while (uiFile.getFILE_POINTER()<uiFile.getFILE_SIZE() && position[0]==-1){
		
			if(pbt.isClosed())
				break;
			
			try {
				((UISERFile)uiFile).fetchNextBlock();
			} catch (IOException e) {
				e.printStackTrace();
				position[0]=-1;
				break;
			}
			
			for (int row=0;row<uiFile.getData().length;row++){
				   
				try {
					fileView.getTable().getSelectionModel().setSelectionInterval(row, row);
					sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				   
				if (((UISERFile)uiFile).isRowEqual(uiFile.getData()[row],searchRec)){
					String[] viseSlogovaString = new String[fileView.getTable().getColumnCount()];
					for(int i=0;i<fileView.getTable().getColumnCount();i++){
						viseSlogovaString[i] = (String) fileView.getTable().getModel().getValueAt(row, i);
					}
					viseSlogovaAL.add(viseSlogovaString);
					
					
					if(!viseSlogova){
						position[0]=row;
						break;
					}
				}   
			}
		}
		
		
		String[] str = new String[fileView.getTable().getColumnCount()];
		String[][] findData = new String[viseSlogovaAL.size()][];
		for(int i=0;i<viseSlogovaAL.size();i++){
			str = viseSlogovaAL.get(i);
			findData[i] = str;
		}
		String noviSRTPutanja = "";
		String noviSRTName = "";
		if(snimiURAM){
			if(viseSlogova){
				fileView.getTable().setModel(new TabelaModel(uiFile.getFields(),findData));
				fileView.setColumnModel(fileView.getTable());
				position[0]=0;
			}
		} else {
			try {
				String path = uiFile.getPath()+File.separator+uiFile.getFileName(); 
				noviSRTPutanja = uiFile.getPath();
				noviSRTName = uiFile.getFileName().replace(".txt", "(1).ser");
				RandomAccessFile raf = new RandomAccessFile(path.substring(0, path.length()-4)+"(1).txt","rw");
				
				String content = "";
				for(int i=0;i<findData.length;i++){
					for(int j=0;j<findData[i].length;j++){
						content+=findData[i][j];
					}
					content+="\r\n";
				}
				
				raf.writeBytes(content);
				raf.setLength(content.length());
				raf.close();
				RandomAccessFile serFile = new RandomAccessFile(path.substring(0, path.length()-4)+".ser","r");
				
				String line = serFile.readLine();				
				String con = "";
				while(line!=null){
					con+=line+"\r\n";
					line = serFile.readLine();
				}
				
				serFile.close();
				con = con.replace(".txt", "(1).txt");
				
				RandomAccessFile newSerFile = new RandomAccessFile(path.substring(0, path.length()-4)+"(1).ser", "rw");
				
				newSerFile.writeBytes(con);
				newSerFile.setLength(con.length());
				newSerFile.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		pbt.getDialog().dispatchEvent(new WindowEvent(pbt.getDialog(), WindowEvent.WINDOW_CLOSING));
		fileView.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		if(!snimiURAM){
			AppCore.getInstance().getLista().setModel(new ListaModel(uiFile.getPath()));

			UISERFile serFile =new UISERFile(noviSRTPutanja,noviSRTName,false);
			UIAbstractFile uf = (UIAbstractFile)serFile;
			FileView fileView = new FileView(uf);
			AppCore.getInstance().setFileView(fileView);
			try {
				serFile.fetchNextBlock();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		if(position[0]==-1){
		    fileView.getTable().getSelectionModel().setSelectionInterval(position[0],position[0]);
			JOptionPane.showMessageDialog(null, "Traženi slog nije pronadjen.", "UI Project", 1);
		}
	}

	public int[] getPosition() {
		return position;
	}
	
}
