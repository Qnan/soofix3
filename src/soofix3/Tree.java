package soofix3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Tree {

	public static String signature = "AIMT-ST";
	Map<Node, Node> nn = new HashMap<Node, Node>();
	int[] seq;
	int pos;
	Node root, ground;
	static int INFINITY = Integer.MAX_VALUE;
	public static boolean logBuilding = false;
	private Lexicon lexicon;
	int nextDocEnd;
	List<Integer> docOffset;

	private Tree() {
	}

	public Tree(Lexicon lexicon, int[] seq) {
		this.lexicon = lexicon != null ? lexicon : new Lexicon(new LinkedList<String>());
		this.seq = seq;
		this.nextDocEnd = -1;
		build();
	}

	public boolean contains(int[] seq) {
		return find(seq) >= 0;
	}

	private void collectLeafOffsets(Map<Integer, List<Integer>> leafOffsets, Node node, int offset) {
		if (node.isLeaf()) {
			int doc = getDoc(node);
			if (doc < 0) {
				doc = -1; // in case there's no document delimiter
			}
			int dOff = doc < 0 ? 0 : docOffset.get(doc);
			if (!leafOffsets.containsKey(doc)) {
				leafOffsets.put(doc, new LinkedList<Integer>());
			}
			leafOffsets.get(doc).add(node.endPos() - offset - dOff);
		} else {
			for (Integer token : node.tokens()) {
				Node child = node.getChild(token);
				collectLeafOffsets(leafOffsets, child, offset + child.length(pos));
			}
		}
	}

	private int getDoc(Node node) {
		int doc = -seq[node.endPos() - 1] - 1;
		return doc;
	}

	public int match(Ref<Node> endNodeRef, int[] seq) {
		Node node = root;
		int j = -1;
		for (int i = 0; i < seq.length;) {
			int token = seq[i];
			if (!node.hasChild(token)) {
				return -1;
			}
			node = node.getChild(token);
			for (j = node.startPos(); j < node.endPos(pos) && i < seq.length; ++j, ++i) {
				if (seq[i] != this.seq[j]) {
					return -1;
				}
			}
		}
		endNodeRef.set(node);
		return j;
	}

	// doesn't support the notion of multiple documents
	public int find(int[] seq) {
		Ref<Node> endNodeRef = new Ref<Node>();
		int j = match(endNodeRef, seq);
		return j - seq.length;
	}

	public Map<Integer, List<Integer>> findAll(int[] seq) {
		Ref<Node> endNodeRef = new Ref<Node>();
		int j = match(endNodeRef, seq);
		Map<Integer, List<Integer>> leafOffsets = new HashMap<Integer, List<Integer>>();
		if (j < 0) {
			return leafOffsets; // return empty list
		}
		Node node = endNodeRef.get();
		int offset = node.endPos(pos) - j + seq.length;
		collectLeafOffsets(leafOffsets, node, offset);
		return leafOffsets;
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
			explicitState.setChild(token, new Node(explicitState, pos - 1, nextDocEnd));
			if (oldRoot != root) {
				nn.put(oldRoot, explicitState);
			}
			oldRoot = explicitState;
			suffix.node = nn.get(suffix.node);
			canonize(suffix, pos - 1);
			explicitState = edgeSplit(suffix, token);
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
		nextDocEnd = 0;
		docOffset = new ArrayList<Integer>();
		docOffset.add(0);

		Suffix suffix = new Suffix(current, 0);
		for (pos = 1; pos <= seq.length; ++pos) {
			// detect the next terminator symbol
			if (pos > nextDocEnd) {
				for (; nextDocEnd < seq.length; ++nextDocEnd) {
					if (seq[nextDocEnd] < 0) {
						nextDocEnd++;
						break;
					}
				}
				docOffset.add(nextDocEnd);
			}
			update(suffix);
			canonize(suffix, pos);
			if (logBuilding) {
				System.out.println("...");
				printTree();
			}
		}
	}

	private Set<Integer> clustersCollect(Map<Node, Set<Integer>> clusters, Node node) {
		Set<Integer> cluster = new HashSet<Integer>();
		if (node.isLeaf()) {
			cluster.add(getDoc(node));
		} else {
			for (Integer token : node.tokens()) {
				Set<Integer> subCluster = clustersCollect(clusters, node.getChild(token));
				cluster.addAll(subCluster);
			}
		}
		clusters.put(node, cluster);
		return cluster;
	}

	public Map<Node, Set<Integer>> getBaseClusters() {
		Map<Node, Set<Integer>> clusters = new HashMap<Node, Set<Integer>>();
		clustersCollect(clusters, root);
		for (Node node : clusters.keySet()) {
			if (clusters.get(node).size() > 1) {
				System.out.println(nodeToString(node));
				for (Integer doc : clusters.get(node)) {
					System.out.format(" %d", doc);
				}
				System.out.println();
			}
		}
		return clusters;
	}

	private void printSubtree(Node node, int depth) {
		for (Integer childToken : node.tokens()) {
			Node child = node.getChild(childToken);
			for (int i = 0; i < depth; ++i) {
				System.out.print("\t");
			}
			System.out.print(getToken(childToken.intValue()));
			System.out.print(':');
			for (int i = child.startPos(); i < child.endPos(pos); ++i) {
				System.out.print(" " + getToken(seq[i]));
			}
			System.out.println();
			printSubtree(child, depth + 1);
		}
	}

	private void printTree() {
		printSubtree(root, 0);
	}

	private String getToken(int id) {
		if (lexicon.hasId(id)) {
			return lexicon.token(id);
		}
		return Integer.toString(id);
	}

	private void recPrint(StringBuilder sb, Node node) {
		if (node.parent() != ground) {
			recPrint(sb, node.parent());
			for (int i = node.startPos(); i < node.endPos(pos); ++i) {
				sb.append(' ');
				sb.append(getToken(seq[i]));
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

	private int enumnodes(Map<Integer, Node> nodes, Map<Node, Integer> ids, int cnt, Node node) {
		nodes.put(cnt, node);
		ids.put(node, cnt);
		cnt++;
		for (Integer token : node.tokens()) {
			cnt = enumnodes(nodes, ids, cnt, node.getChild(token));
		}
		return cnt;
	}

	private void storeNodeData(DataOutputStream dos, Map<Node, Integer> ids, Node node) throws IOException {
		dos.writeInt(node.startPos());
		dos.writeInt(node.endPos());
		dos.writeInt(node.tokens().size());
		for (Integer token : node.tokens()) {
			dos.writeInt(token);
			dos.writeInt(ids.get(node.getChild(token)));
		}
	}

	private void readNodeData(Node node, Map<Integer, Node> nodes, DataInputStream dis) throws IOException {
		node.setStartPos(dis.readInt());
		node.setEndPos(dis.readInt());
		int num = dis.readInt();
		for (int i = 0; i < num; ++i) {
			int token = dis.readInt();
			int nodeid = dis.readInt();
			Node child = nodes.get(nodeid);
			node.setChild(token, child);
			child.setParent(node);
		}
	}

	public void save(OutputStream os) throws IOException {
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeBytes(signature); // item 00
		dos.writeInt(seq.length); // item 01
		for (int i = 0; i < seq.length; ++i) { // item 02
			dos.writeInt(seq[i]);
		}
		Map<Integer, Node> nodes = new HashMap<Integer, Node>();
		Map<Node, Integer> ids = new HashMap<Node, Integer>();
		nodes.put(0, ground);
		ids.put(ground, 0);
		int total = enumnodes(nodes, ids, 1, root);
		dos.writeInt(total); // item 03
		// skip ground
		for (int i = 1; i < total; ++i) // item 04
		{
			storeNodeData(dos, ids, nodes.get(i));
		}

		dos.writeInt(nn.size()); // item 05
		for (Node node : nn.keySet()) { // item 06
			dos.writeInt(ids.get(node));
			dos.writeInt(ids.get(nn.get(node)));
		}
	}

	private void load(InputStream is) throws IOException {
		DataInputStream dis = new DataInputStream(is);
		char[] signatureBuf = new char[signature.length()];
		for (int i = 0; i < signatureBuf.length; ++i) // item 00
		{
			signatureBuf[i] = (char) dis.readByte();
		}
		if (!new String(signatureBuf).equals(signature)) {
			throw new java.io.IOException("Signature is not valid");
		}
		int length = dis.readInt(); // item 01
		seq = new int[length];
		for (int i = 0; i < length; ++i) { // item 02
			seq[i] = dis.readInt();
		}
		pos = seq.length;
		int total = dis.readInt(); // item 03
		Map<Integer, Node> nodes = new HashMap<Integer, Node>();
		for (int i = 0; i < total; ++i) {
			nodes.put(i, new Node(null, -1, -1));
		}
		ground = nodes.get(0);
		for (int i = 0; i < seq.length; ++i) {
			ground.setChild(seq[i], root);
		}
		root = nodes.get(1);
		for (int i = 1; i < total; ++i) {  // item 04
			readNodeData(nodes.get(i), nodes, dis);
		}

		int nSuffixLinks = dis.readInt(); // item 05
		for (int i = 0; i < nSuffixLinks; ++i) { // item 06
			int from = dis.readInt();
			int to = dis.readInt();
			nn.put(nodes.get(from), nodes.get(to));
		}
	}

	public static Tree fromStream(InputStream is) throws IOException {
		Tree tree = new Tree();
		tree.load(is);
		return tree;
	}
}