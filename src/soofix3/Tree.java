package soofix3;

import java.util.HashMap;
import java.util.Map;

public final class Tree {

	Map<Integer, Node> nodes = new HashMap<Integer, Node>();
	Map<Node, Node> nn = new HashMap<Node, Node>();
	int[] seq;
	int pos;
	Node root, ground;
	static int INFINITY = Integer.MAX_VALUE;

	public Tree(int[] seq) {
		this.seq = seq;
		build();
	}

	public Node edgeSplit(Suffix suffix, int token) {
		// i -> pos
		// p -> pos - 1
		// k -> suffix.from
		// s -> suffix.node
		// s' -> next
		// r -> newNode
		// return explicit state if not end point, otherwise null
//		int firstToken = seq[suffix.from];
		if (suffix.from >= pos - 1) {
			if (suffix.node.hasChild(token))
				return null; // end point
			else
				return suffix.node; // state already explicit
		} else {
			Node next = suffix.node.getChild(seq[suffix.from]);
			int nextPos = next.startPos() + pos - suffix.from - 1;
			if (token == seq[nextPos]) {
				return null; // end point
			} else {
				// do split
				Node newNode = new Node(suffix.node, next.startPos(), nextPos);
				suffix.node.setChild(seq[next.startPos()], newNode);
				next.setStartPos(nextPos);
				newNode.setChild(seq[nextPos], next);
				return newNode;
			}			
		}
	}
	
	public void canonize (Suffix suffix) {
		// s -> suffix.node
		// k -> suffix.from
		// p -> pos
		while (true) {
			int suffixLen = pos - suffix.from;
			if (suffixLen == 0)
				return;
			int firstToken = seq[suffix.from];
			if (!suffix.node.hasChild(firstToken))
				return;
			Node nextNode = suffix.node.getChild(firstToken);
			int nextNodeWordLen = nextNode.length(pos);
			if (suffixLen < nextNodeWordLen) {
				return;
			}
			suffix.from += nextNodeWordLen;
			suffix.node = nextNode;
		}
	}

	public void update(Suffix suffix) {
		// s -> suffix.node
		// k -> suffix.from
		// i -> pos
		// r -> explicitState
		Node oldRoot = root;
		int token = seq[pos - 1];
		Node explicitState = edgeSplit(suffix, token);
		while (explicitState != null) {
			explicitState.setChild(token, new Node(explicitState, pos - 1, INFINITY));
			printTree();
			if (oldRoot != root)
				nn.put(oldRoot, explicitState);
			oldRoot = explicitState;
			suffix.node = nn.get(suffix.node);// suffix.from);
			canonize(suffix);
			explicitState = edgeSplit(suffix, token);
			System.out.println(".");
		}
		if (oldRoot != root)
			nn.put(oldRoot, suffix.node);
	}
	
	public void build () {
		ground = new Node(null, -1, -1);
		root = new Node(ground, -1, 0);
		for (int i = 0; i < seq.length; ++i) {
			ground.setChild(seq[i], root);
		}
		nn.put(root, ground);
		Node current = root;
		
		Suffix suffix = new Suffix(current, 0);
		for (pos = 1; pos < seq.length; ++pos) {			
			System.out.println(nodeToString(suffix.node) + " -- " + suffix.from);
			System.out.flush();
			update(suffix);
			canonize(suffix);
			printTree();
		}
		pos = seq.length; // TODO: unhack
	}

	private void printSubtree(Node node, int depth) {
		for (Integer childToken : node.tokens()) {
			Node child = node.getChild(childToken);
			for (int i = 0; i < depth; ++i) {
				System.out.print("\t");
			}
			System.out.print((char) childToken.intValue());
			System.out.print(':');
			for (int i = child.startPos(); i < child.endPos(pos); ++i) {
				System.out.print((char) seq[i]);
			}
			System.out.println();
			printSubtree(child, depth + 1);
		}
	}

	public void printTree() {
		printSubtree(root, 0);
	}

//	public void build() {
//		root = new Node(null, 0, 0);
//		nn.put(root, root);
//		start = new Node(root, 0, seq.length);
//		nn.put(start, root);
//		root.setChild(seq[0], start);
//		boolean stopFlag = false;
//		Node activePoint = start;
//		for (pos = 1; pos < seq.length; ++pos) {
//			int token = seq[pos];
//			Node current = activePoint, last = null;
//			Node activePointNew = null;
//			do {
////				Node next = nn.get(current);
////				stopFlag = current == root;
////				if (!current.isLeaf()) {
////					if (current.hasChild(token)) {
////						Node child = current.getChild(token);
////						if (child.endPos() - child.startPos() > 1) {
////							Node newNode = edgeSplit(token, child, next);
////							current = newNode;
////						} else {
////							nn.put(child, next);
////							current = child;
////						}
////					} else {
////						// create a new leaf node
////						Node newNode = new Node(current, i, seq.length);
////						nn.put(newNode, next);
////						current.setChild(token, newNode);
////						current = newNode;
////					}
////					if (activePointNew == null) {
////						activePointNew = current;
////					}
////				}
////
////				if (last != null) {
////					nn.put(last, current);
////				}
////				last = current;
////				current = next;
//			} while (!stopFlag);
//			activePoint = activePointNew;
//		}
//	}

	private void recPrint(StringBuilder sb, Node node) {
		if (node.parent() != ground) {
			recPrint(sb, node.parent());
			for (int i = node.startPos(); i < node.endPos(pos); ++i) {
				sb.append((char) seq[i]);
			}
		} else {
			sb.append("@");
		}
	}
//
//	private void recPrintAll(StringBuilder sb, Node node, int len) {
//		for (int token : node.tokens()) {
//			recPrintAll(sb, node, len);
//		}
//		if (node.parent() != null) {
//			recPrint(sb, node.parent(), len);
//			for (int i = node.startPos(); i < node.endPos(); ++i) {
//				sb.append((char) seq[i]);
//			}
//		}
//	}

	private String nodeToString(Node node) {
		StringBuilder sb = new StringBuilder();
		recPrint(sb, node);
		return sb.toString();
	}

//	public void printSuffixes() {
//		Node current = start, last = null;
//		while (last != root) {
//			System.out.println("\"" + nodeToString(current) + "\"");
//			last = current;
//			current = nn.get(current);
//		}
//	}
}
