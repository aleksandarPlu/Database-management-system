package model.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

@SuppressWarnings("serial")
public class Node implements TreeNode, Serializable{
	 
    public List<NodeElement>  data;
    public List<Node> children;
    
    public Node() {
        data=new ArrayList<NodeElement>();
        children=new ArrayList<Node>();
    }

    public Node(List<NodeElement>  data) {
        this();
        setData(data);
    } 
    
    public void addChild(Node child) {
        if (children == null) {
            children = new ArrayList<Node>();
        }
        children.add(child);
    }
    
	public Enumeration children() {
		return null;
	}

	public boolean getAllowsChildren() {
		// TODO Auto-generated method stub
		return true;
	}

	public TreeNode getChildAt(int childIndex) {
		// TODO Auto-generated method stub
		 return this.children.get(childIndex);
	}

	public int getChildCount() {
		// TODO Auto-generated method stub
        if (children == null) {
            return 0;
        }
        return children.size();
	}

	public int getIndex(TreeNode node) {
		// TODO Auto-generated method stub
		return children.indexOf(node);
	}

	public TreeNode getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return false;
	}
	
    public List<NodeElement> getData() {
        return this.data;
    }
 
    public void setData(List<NodeElement> data) {
        this.data = data;
    }
    
    public List<Node> getChildren() {
        if (this.children == null) {
            return new ArrayList<Node>();
        }
        
        return this.children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }
    
	public Node clone(){
	    List<NodeElement>  dataCopy=new ArrayList<NodeElement>();
	    for (int i=0;i<data.size();i++){
	    	NodeElement nodeElement=data.get(i).clone();
	    	dataCopy.add(nodeElement);
	    }
	    List<Node> childrenCopy=new ArrayList<Node>();		
	    for (int i=0;i<children.size();i++){
	    	Node node=children.get(i).clone();
	    	childrenCopy.add(node);
	    }	
	    
	    Node nodeCopy=new Node();
	    nodeCopy.setChildren(childrenCopy);
	    nodeCopy.setData(dataCopy);
	    return nodeCopy;
	}
}
