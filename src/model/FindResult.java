package model;

public class FindResult {
	
	private int position;
	private Object[] path;
	
	public FindResult() {
		super();
		this.position = -1;
	}
	
	public FindResult(Object[] path, int position) {
		super();
		this.path = path;
		this.position = position;
	}
	
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public Object[] getPath() {
		return path;
	}
	public void setPath(Object[] path) {
		this.path = path;
	}
}
