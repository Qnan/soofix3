package soofix3;

import java.util.HashMap;
import java.util.Map;

public class Node {

	public Node(Node suffixLink) {
		this.suffixLink = suffixLink;
		edges = new HashMap<Integer, Edge>();
	}
//	public final int id;
//	public final int token;
//	public final int strid, pos; // identifier of the string and position in that string
	private Map<Integer, Edge> edges;
	private Node suffixLink = null, next = null;

	public boolean isLeaf() {
		return edges.isEmpty();
	}
//	public abstract boolean nextIsParent ();
	public Node next() {
		return next;
	}
//	public abstract Iterable<Node> children();

	public boolean hasEdge(int token) {
		return edges.containsKey(token);
	}

	public Edge getEdge(int token) {
		return edges.get(token);
	}

	public void addEdge(int token, Edge edge) {
		edges.put(token, edge);
	}
	
	public Node suffixLink() {
		return suffixLink;
	}
}
