package soofix3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tree {

	Map<Integer, Node> nodes = new HashMap<Integer, Node>();
	List<int[]> sequences = new ArrayList<int[]>();
	Node root = null, start;

	public Node edgeSplit(int[] seq, int token, Node node, Node next) {
		Node parent = node.parent();
		Node newNode = new Node(parent, node.startPos(), node.startPos() + 1, next);
		parent.setChild(token, newNode);
		newNode.setChild(seq[newNode.endPos()], node);
		node.setParent(newNode);
		node.setStartPos(newNode.endPos());
		return newNode;
	}

	private void printSubtree(int[] seq, Node node, int depth) {
		for (Integer childToken : node.tokens()) {
			Node child = node.getChild(childToken);
			for (int i = 0; i < depth; ++i) {
				System.out.print("\t");
			}
			System.out.print((char) childToken.intValue());
			System.out.print(':');
			for (int i = child.startPos(); i < child.endPos(); ++i) {
				System.out.print((char) seq[i]);
			}
			System.out.println();
			printSubtree(seq, child, depth + 1);
		}
	}

	public void printTree(int[] seq) {
		printSubtree(seq, root, 0);
	}

	public void addSequence(int[] seq) {
		int seqId = sequences.size();
		sequences.add(seq);
		root = new Node(null, 0, 0, null);
		root.setNext(root);
		start = new Node(root, 0, seq.length, root);
		root.setChild(seq[0], start);
		boolean stopFlag = false;
		for (int i = 1; i < seq.length; ++i) {
			int token = seq[i];
			Node current = start, last = null;
			do {
				Node next = current.next();
				stopFlag = current == root;
				if (current.isLeaf()) {
//					current.setEndPos(i + 1);
				} else {
					if (current.hasChild(token)) {
						Node child = current.getChild(token);
						if (child.endPos() - child.startPos() > 1) {
							Node newNode = edgeSplit(seq, token, child, next);
							current = newNode;
						} else {
							child.setNext(next);
							current = child;
						}
					} else {
						// create a new leaf node
						Node newNode = new Node(current, i, seq.length, next);
						current.setChild(token, newNode);
						current = newNode;
					}
				}

				if (last != null)
					last.setNext(current);
				last = current;
				current = next;
			} while (!stopFlag);
//			printTree(seq);
//			printSuffixes(start, seq);
		}
	}

	private void recPrint(Node current, int[] seq) {
		if (current.parent() != null) {
			recPrint(current.parent(), seq);
			for (int i = current.startPos(); i < current.endPos(); ++i) {
				System.out.print((char) seq[i]);
			}
		}

	}

	public void printSuffixes(int[] seq) {
		Node current = start, last = null;
		while (last != root) {
			System.out.print('"');
			recPrint(current, seq);
			System.out.print('"');
			System.out.println();
			last = current;
			current = current.next();
		}
	}
}
