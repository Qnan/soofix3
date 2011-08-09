package soofix3;

import java.util.HashMap;
import java.util.Map;

public class Node {

	public Node(Node parent, int startPos, int endPos, Node next) {
		this.parent = parent;
		this.startPos = startPos;
		this.endPos = endPos;
		this.next = next;
		children = new HashMap<Integer, Node>();
	}
	private Map<Integer, Node> children;
	private Node next = null;
	private int endPos;
	private int startPos;
	private Node parent;

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public Node next() {
		return next;
	}

	public boolean hasChild(int token) {
		return children.containsKey(token);
	}

	public Node getChild(int token) {
		return children.get(token);
	}

	public Iterable<Integer> tokens() {
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

	public void setNext(Node node) {
		this.next = node;
	}

	public int endPos() {
		return endPos;
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
}
