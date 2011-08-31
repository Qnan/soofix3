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
	public static boolean logBuilding = false;

	public Tree(int[] seq) {
		this.seq = seq;
		build();
	}

	public boolean contains(int[] seq) {
		return find(seq) >= 0;
	}

	public int find(int[] seq) {
		Node node = root;
		int j = -1;
		for (int i = 0; i < seq.length;) {
			int token = seq[i];
			if (!node.hasChild(token)) {
				return -1;
			}			
			node = node.getChild(token);
			for (j = node.startPos(); j < node.endPos() && i < seq.length; ++j, ++i) {
				if (seq[i] != this.seq[j]) {
					return -1;
				}
			}
		}
		return j - seq.length;
	}
	
	private Node edgeSplit(Suffix suffix, int token) {
		// i -> pos
		// p -> pos - 1
		// k -> suffix.from
		// s -> suffix.node
		// s' -> next
		// r -> newNode
		// return explicit state if not end point, otherwise null
		if (suffix.from >= pos - 1) {
			if (suffix.node.hasChild(token)) {
				return null; // end point
			} else {
				return suffix.node; // state already explicit
			}
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

	private void canonize(Suffix suffix, int i) {
		// s -> suffix.node
		// k -> suffix.from
		// p -> pos
		while (true) {
			int suffixLen = i - suffix.from;
			if (suffixLen == 0) {
				return;
			}
			int firstToken = seq[suffix.from];
			if (!suffix.node.hasChild(firstToken)) {
				return;
			}
			Node nextNode = suffix.node.getChild(firstToken);
			int nextNodeWordLen = nextNode.length(i);
			if (suffixLen < nextNodeWordLen) {
				return;
			}
			suffix.from += nextNodeWordLen;
			suffix.node = nextNode;
		}
	}

	private void update(Suffix suffix) {
		// s -> suffix.node
		// k -> suffix.from
		// i -> pos
		// r -> explicitState
		Node oldRoot = root;
		int token = seq[pos - 1];
		Node explicitState = edgeSplit(suffix, token);
		while (explicitState != null) {
			explicitState.setChild(token, new Node(explicitState, pos - 1, INFINITY));
			if (logBuilding) {
				printTree();
			}
			if (oldRoot != root) {
				nn.put(oldRoot, explicitState);
			}
			oldRoot = explicitState;
			suffix.node = nn.get(suffix.node);
			canonize(suffix, pos - 1);
			explicitState = edgeSplit(suffix, token);
			if (logBuilding) {
				System.out.println(".");
			}
		}
		if (oldRoot != root) {
			nn.put(oldRoot, suffix.node);
		}
	}

	private void build() {
		ground = new Node(null, -1, -1);
		root = new Node(ground, -1, 0);
		for (int i = 0; i < seq.length; ++i) {
			ground.setChild(seq[i], root);
		}
		nn.put(root, ground);
		Node current = root;

		Suffix suffix = new Suffix(current, 0);
		for (pos = 1; pos < seq.length; ++pos) {
			if (logBuilding) {
				System.out.println(nodeToString(suffix.node) + " -- " + suffix.from + " - " + Integer.toString(pos));
				System.out.flush();
			}
			update(suffix);
			canonize(suffix, pos);
			if (logBuilding) {
				printTree();
			}
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

	private void printTree() {
		printSubtree(root, 0);
	}

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

	private String nodeToString(Node node) {
		StringBuilder sb = new StringBuilder();
		recPrint(sb, node);
		return sb.toString();
	}
}
