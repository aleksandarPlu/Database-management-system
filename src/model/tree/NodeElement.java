package model.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class NodeElement implements Serializable{

	private int blockAddress;
	
	private List<KeyElement> keyValue;
    
    public NodeElement(int blockAddress,List<KeyElement> keyValue) {
		super();
		this.blockAddress = blockAddress;
		this.keyValue = keyValue;
	}

	public List<KeyElement> getKeyValue() {
		return keyValue;
	}
	public void setKeyValue(List<KeyElement> keyValue) {
		this.keyValue = keyValue;
	}
	public int getBlockAddress() {
		return blockAddress;
	}
	public void setBlockAddress(int blockAddress) {
		this.blockAddress = blockAddress;
	}
	
	public String toString(){
		String value="AE: "+blockAddress+" Key: ";
		for (int i=0;i<keyValue.size();i++){
			value+=keyValue.get(i).getValue()+"|";
		}
		return value;

	}
	
	public NodeElement clone(){
		List<KeyElement> keyValueCopy=new ArrayList<KeyElement>();
		keyValueCopy.addAll(keyValue);
		return new NodeElement(blockAddress,keyValueCopy);
	}
	
}
