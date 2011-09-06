package soofix3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Node {

	public Node(Node parent, int startPos, int endPos) {
		this.parent = parent;
		this.startPos = startPos;
		this.endPos = endPos;
		children = new HashMap<Integer, Node>();
	}
	private Map<Integer, Node> children;
	private int endPos;
	private int startPos;
	private Node parent;

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public boolean hasChild(int token) {
		return children.containsKey(token);
	}

	public Node getChild(int token) {
		return children.get(token);
	}

	public Collection<Integer> tokens() {
		return children.keySet();
	}

	public void setChild(int token, Node child) {
		children.put(token, child);
	}

	public void removeChild(int token) {
		children.remove(token);
	}

	public Node parent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public int endPos() {
		return endPos;
	}

	public int endPos(int pos) {
		return Math.min(endPos, pos);
	}

	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

	public int startPos() {
		return startPos;
	}

	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	public int length(int pos) {
		return endPos(pos) - startPos;
	}
}
