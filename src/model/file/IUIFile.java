package model.file;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface IUIFile {

	public void readHeader() throws IOException,SQLException;
	public boolean fetchNextBlock() throws IOException,SQLException;
	public boolean addRecord(List<List<String>> list) throws IOException,SQLException;
	public boolean updateRecord(ArrayList<String> oldRrecord,ArrayList<String> newRecord, int row)throws IOException,SQLException;
	public boolean findRecord(ArrayList<String> searchRec,int[] position, boolean start,boolean viseSlogova, boolean snimiURAM);
	public boolean deleteRecord(ArrayList<String> searchRec) throws IOException,SQLException;
	public void sortMDI()throws IOException;
	public void sortMM()throws IOException;
		
}
