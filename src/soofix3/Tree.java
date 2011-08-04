package soofix3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tree {

	Map<Integer, Node> nodes = new HashMap<Integer, Node>();
	List<int[]> sequences = new ArrayList<int[]>();
	Node root = null;

	public Node edgeSplit(int token, Edge edge) {
		Node startNode = edge.startNode;
		Node endNode = edge.endNode;
		Node newNode = new Node(null);
		Edge newEdge = new Edge();
		endNode.setParentEdge(newEdge);
		newEdge.startNode = newNode;
		newEdge.endNode = endNode;
		newEdge.startPos = edge.startPos + 1;
		newEdge.endPos = edge.endPos;
		newNode.addEdge(token, newEdge);
		edge.endNode = newNode;
		edge.endPos = edge.startPos + 1;
		return newNode;
	}

	private void printSubtree(int[] seq, Node node, int token, int depth) {
		for (Integer childToken : node.tokens()) {
			Edge edge = node.getEdge(childToken);
			for (int i = 0; i < depth; ++i) {
				System.out.print("\t");
			}
			for (int i = edge.startPos; i < edge.endPos; ++i) {
				System.out.print((char) seq[i]);
			}
			System.out.println();
			printSubtree(seq, edge.endNode, childToken, depth + 1);
		}
	}

	public void printTree(int[] seq) {
		printSubtree(seq, root, -1, 0);
	}

	public void addSequence(int[] seq) {
		int seqId = sequences.size();
		sequences.add(seq);
		root = new Node(null);
		root.setNext(root);
		Node current = root, longestSuffixNode = null, last = null;
		longestSuffixNode = new Node(null);
		Edge firstEdge = new Edge();
		firstEdge.startNode = root;
		firstEdge.endNode = longestSuffixNode;
		firstEdge.startPos = 0;
		firstEdge.endPos = 1;
		root.addEdge(seq[0], firstEdge);
		longestSuffixNode.setNext(root);
		longestSuffixNode.setParentEdge(firstEdge);
		for (int i = 1; i < seq.length; ++i) {
			int token = seq[i];
			current = longestSuffixNode;
			do {
				if (current.isLeaf()) {
					current.parentEdge().endPos++;
//                    continue;
				} else {

					if (current.hasEdge(token)) {
						Edge edge = current.getEdge(token);
						if (edge.endPos - edge.startPos > 1) {
							Node newNode = edgeSplit(token, edge);
							newNode.setNext(current);
							last.setNext(newNode);
							current = newNode;
						}
					} else {
						// create a new leaf node
						Node newNode = new Node(null);
						Edge newEdge = new Edge();
						newEdge.startNode = current;
						newEdge.endNode = newNode;
						newEdge.startPos = i;
						newEdge.endPos = i + 1;
						newNode.setParentEdge(newEdge);
						current.addEdge(token, newEdge);
						newNode.setNext(current.next());
						last.setNext(newNode);
						current = newNode;
					}
				}

				last = current;
				if (current == current.next()) {
					break;
				}
				current = current.next();
			} while (true);
		}
	}
}
