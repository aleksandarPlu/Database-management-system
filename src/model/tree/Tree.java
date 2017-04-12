package model.tree;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Tree implements Serializable{
	 
    private Node rootElement;
     
    public Tree() {
        super();
    }
 
    public Node getRootElement() {
        return this.rootElement;
    }
 
    public void setRootElement(Node rootElement) {
        this.rootElement = rootElement;
    }
     


}
