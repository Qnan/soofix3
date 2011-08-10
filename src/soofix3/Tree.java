package soofix3;

import java.util.HashMap;
import java.util.Map;

public final class Tree {

	Map<Integer, Node> nodes = new HashMap<Integer, Node>();
	Map<Node, Node> nn = new HashMap<Node, Node>();
	int[] seq;
	Node root, start;

	public Tree(int[] seq) {
		this.seq = seq;
		build();
	}

	public Node edgeSplit(int token, Node node, Node next) {
		Node parent = node.parent();
		Node newNode = new Node(parent, node.startPos(), node.startPos() + 1);
		nn.put(newNode, next);
		parent.setChild(token, newNode);
		newNode.setChild(seq[newNode.endPos()], node);
		node.setParent(newNode);
		node.setStartPos(newNode.endPos());
		return newNode;
	}

	private void printSubtree(Node node, int depth) {
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
			printSubtree(child, depth + 1);
		}
	}

	public void printTree() {
		printSubtree(root, 0);
	}

	public void build() {
		root = new Node(null, 0, 0);
		nn.put(root, root);
		start = new Node(root, 0, seq.length);
		nn.put(start, root);
		root.setChild(seq[0], start);
		boolean stopFlag = false;
		Node activePoint = start;
		for (int i = 1; i < seq.length; ++i) {
			int token = seq[i];
			Node current = activePoint, last = null;
			Node activePointNew = null;
			do {
				Node next = nn.get(current);
				stopFlag = current == root;
				if (!current.isLeaf()) {
					if (current.hasChild(token)) {
						Node child = current.getChild(token);
						if (child.endPos() - child.startPos() > 1) {
							Node newNode = edgeSplit(token, child, next);
							current = newNode;
						} else {
							nn.put(child, next);
							current = child;
						}
					} else {
						// create a new leaf node
						Node newNode = new Node(current, i, seq.length);
						nn.put(newNode, next);
						current.setChild(token, newNode);
						current = newNode;
					}
					if (activePointNew == null) {
						activePointNew = current;
					}
				}

				if (last != null) {
					nn.put(last, current);
				}
				last = current;
				current = next;
			} while (!stopFlag);
			activePoint = activePointNew;
		}
	}

	private void recPrint(StringBuilder sb, Node node, int len) {
		if (node.parent() != null) {
			recPrint(sb, node.parent(), len);
			for (int i = node.startPos(); i < node.endPos(); ++i) {
				sb.append((char) seq[i]);
			}
		}
	}

	private String nodeToString(Node node) {
		return nodeToString(node, seq.length);
	}

	private String nodeToString(Node node, int len) {
		StringBuilder sb = new StringBuilder();
		recPrint(sb, node, len);
		return sb.toString();
	}

	public void printSuffixes() {
		Node current = start, last = null;
		while (last != root) {
			System.out.println("\"" + nodeToString(current) + "\"");
			last = current;
			current = nn.get(current);
		}
	}
}
