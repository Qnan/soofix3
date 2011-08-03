package soofix3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tree {
	Map<Integer, Node> nodes = new HashMap<Integer, Node>();
	List<int[]> sequences = new ArrayList<int[]>();
	Node root = null;
	
	public Node edgeSplit (int token, Edge edge) {
		Node startNode = edge.startNode;
		Node endNode = edge.endNode;
		Node newNode = new Node(null);
		Edge newEdge = new Edge();
		newEdge.startNode = newNode;
		newEdge.endNode = endNode;		
		newEdge.startPos = edge.startPos + 1;
		newEdge.endPos = edge.endPos;
		newNode.addEdge(token, newEdge);
		edge.endNode = newNode;
		edge.endPos = edge.startPos + 1;		
		return newNode;
	}
	
	public void addSequence (int[] seq) {
		int seqId = sequences.size();
		sequences.add(seq);
		root = new Node(null);
//		Node activePoint = root;
		Node current = root, longestSuffixNode = root, last = null;
		for (int i = 0; i < seq.length; ++i) {
			int token = seq[i];
			if (current.isLeaf()) {
				current = current.next();
				continue;
			}
			
			if (current.hasEdge(token)) {
				Edge edge = current.getEdge(token);
				if (edge.endPos - edge.startPos > 1) {
					Node newNode = edgeSplit(token, edge);
					
				}
					
			}

		}		
	}
	
}
