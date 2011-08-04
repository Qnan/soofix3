package soofix3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tree {

	Map<Integer, Node> nodes = new HashMap<Integer, Node>();
	List<int[]> sequences = new ArrayList<int[]>();
	Node root = null;

	public Node edgeSplit(int[] seq, int token, Edge edge) {
		Node startNode = edge.startNode;
		Node endNode = edge.endNode;
		Node newNode = new Node(null);
		Edge newEdge = new Edge();
		endNode.setParentEdge(newEdge);
		newEdge.startNode = newNode;
		newEdge.endNode = endNode;
		newEdge.startPos = edge.startPos + 1;
		newEdge.endPos = edge.endPos;
		newNode.addEdge(seq[newEdge.startPos], newEdge);
		newNode.setParentEdge(edge);
		edge.endNode = newNode;
		edge.endPos = newEdge.startPos;
		return newNode;
	}

	private void printSubtree(int[] seq, Node node, int depth) {
		for (Integer childToken : node.tokens()) {
			Edge edge = node.getEdge(childToken);
			for (int i = 0; i < depth; ++i) {
				System.out.print("\t");
			}
			System.out.print((char)childToken.intValue());
			System.out.print(':');
			for (int i = edge.startPos; i < edge.endPos; ++i) {
				System.out.print((char) seq[i]);
			}
			System.out.println();
			printSubtree(seq, edge.endNode, depth + 1);
		}
	}

	public void printTree(int[] seq) {
		System.out.println();
		System.out.println();
		System.out.println("========");
		printSubtree(seq, root, 0);
	}

	public void addSequence(int[] seq) {
		int seqId = sequences.size();
		sequences.add(seq);
		root = new Node(null);
		root.setNext(null);
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
//		printSuffixes(longestSuffixNode, seq);
		printTree(seq);
		for (int i = 1; i < seq.length; ++i) {
			int token = seq[i];
			current = longestSuffixNode;
			System.out.println();
			System.out.println();
			System.out.println("###############################");
			do {
				Node next = current.next();
				if (current.isLeaf()) {
					current.parentEdge().endPos++;
				} else {
					if (current.hasEdge(token)) {
						Edge edge = current.getEdge(token);
						if (edge.endPos - edge.startPos > 1) {
							Node newNode = edgeSplit(seq, token, edge);
							current = newNode;
						} else {
							current = edge.endNode;
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
						current = newNode;
					}
				}

				current.setNext(next);
				if (last != null)
					last.setNext(current);
				printSuffixes(longestSuffixNode, seq);
				last = current;
				current = next;
			} while (current != null);
			printTree(seq);
		}
	}

	private void printSuffixes(Node longestSuffixNode, int[] seq) {
		System.out.println("********");
		Node current = longestSuffixNode;
		while (current != null) {
			Node cc = current;
			System.out.print('"');
			while (cc.parentEdge() != null) {
				Edge edge = cc.parentEdge();
				for (int j = edge.endPos - 1; j >= edge.startPos; --j) {
					System.out.print((char) seq[j]);
				}
				cc = edge.startNode;
			}
			System.out.print('"');
			System.out.println();
//			if (current.next() == current)
//				break;
			current = current.next();
		}
	}
}
