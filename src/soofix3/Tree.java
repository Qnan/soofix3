package soofix3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

public final class Tree {

	public static String signature = "AIMT-ST";
	Map<Node, Node> nn = new HashMap<Node, Node>();
	ArrayList<Integer> seq;
	int pos;
	Node root, ground;
	static int INFINITY = Integer.MAX_VALUE;
	public static boolean logBuilding = false;
	private Lexicon lexicon;
	int nextDocEnd;
	List<Integer> docOffset;
	Map<Integer, Integer> wordFrequency;

	private void getAggregateClusterScores(Map<Node, Double> clusterScores, Map<Node, Node> connectedComponents, final Map<Node, Double> baseClusterScores, final Map<Node, List<Integer>> baseClusterPhrases, final Map<Node, Set<Integer>> clusters) {
		for (Node node : connectedComponents.keySet()) {
			Node ref = connectedComponents.get(node);
			if (!clusterScores.containsKey(ref)) {
				clusterScores.put(ref, 0.0);
			}

			clusterScores.put(ref, clusterScores.get(ref) + phraseScore(baseClusterPhrases.get(node)));
		}

		for (Node ref : clusters.keySet()) {
			clusterScores.put(ref, clusterScores.get(ref) * clusters.get(ref).size());
		}
	}

	private Map<Node, List<Integer>> getBaseClusterPhrases(final Map<Node, Set<Integer>> baseClusters) {
		final Map<Node, List<Integer>> baseClusterPhrases = new HashMap<Node, List<Integer>>();
		for (Node node : baseClusters.keySet()) {
			List<Integer> phrase = nodeToWordIds(node);
			baseClusterPhrases.put(node, phrase);
		}
		return baseClusterPhrases;
	}

	private Map<Node, Double> getBaseClusterScores(Map<Node, Set<Integer>> clusters, Map<Node, List<Integer>> baseClusterPhrases) {
		final Map<Node, Double> baseClusterScores = new HashMap<Node, Double>(clusters.size());
		for (Node node : clusters.keySet()) {
			List<Integer> phrase = baseClusterPhrases.get(node);
			double phraseScore = phraseScore(phrase);
			baseClusterScores.put(node, clusters.get(node).size() * phraseScore);
		}
		return baseClusterScores;
	}

	private int getWordCost(Integer word) {
		if (word < 0) {
			return 0;
		}
		String token = lexicon.token(word);
		if (!Pattern.matches(".*[a-zA-Z].*", token)) {
			return 0;
		}
		if (lexicon.isStopWord(token)) {
			return 0;
		}
		int freq = wordFrequency.get(word);
		if (freq < 0.00 * docOffset.size() || freq > 0.40 * docOffset.size()) {
			return 0;
		}
		return 1;
	}

	public Tree(Lexicon lexicon) {
		this(lexicon, 0);
	}

	public Tree(Lexicon lexicon, int totalSz) {
		this.lexicon = lexicon != null ? lexicon : new Lexicon(new LinkedList<String>());
		init(totalSz);
	}

	public boolean contains(List<Integer> seq) {
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

	private Map<Node, Node> findConnectedComponents(Map<Node, Set<Integer>> clusters, Map<Node, List<Node>> graph) {
		Map<Node, Node> connectedComponents = new HashMap<Node, Node>();
		for (Node ref : graph.keySet()) {
			if (!connectedComponents.containsKey(ref)) {
				findConnectedComponent(ref, connectedComponents, graph);
			}
		}
		return connectedComponents;
	}

	private void findConnectedComponent(Node ref, Map<Node, Node> connectedComponents, Map<Node, List<Node>> graph) {
		Queue<Node> queue = new LinkedList<Node>();
		queue.add(ref);
		while (!queue.isEmpty()) {
			Node node = queue.poll();
			if (connectedComponents.get(node) == null) { // mark as processed
				for (Node neighbor : graph.get(node)) {
					if (!connectedComponents.containsKey(neighbor)) { // node is neither processed, nor in the queue
						queue.add(neighbor);
						connectedComponents.put(neighbor, null); // mark as added to the queue
					}
				}
				connectedComponents.put(node, ref); // mark as processed
			}
		}
	}

	private int getDoc(Node node) {
		int doc = -seq.get(node.endPos() - 1) - 1;
		return doc;
	}

	public int match(Ref<Node> endNodeRef, List<Integer> seq) {
		Node node = root;
		int j = -1;
		for (int i = 0; i < seq.size();) {
			int token = seq.get(i);
			if (!node.hasChild(token)) {
				return -1;
			}
			node = node.getChild(token);
			for (j = node.startPos(); j < node.endPos(pos) && i < seq.size(); ++j, ++i) {
				if (seq.get(i) < 0 || !seq.get(i).equals(this.seq.get(j))) {
					return -1;
				}
			}
		}
		endNodeRef.set(node);
		return j;
	}

	// doesn't support the notion of multiple documents
	public int find(List<Integer> seq) {
		Ref<Node> endNodeRef = new Ref<Node>();
		int j = match(endNodeRef, seq);
		return j - seq.size();
	}

	public Map<Integer, List<Integer>> findAll(List<Integer> seq) {
		Ref<Node> endNodeRef = new Ref<Node>();
		int j = match(endNodeRef, seq);
		Map<Integer, List<Integer>> leafOffsets = new HashMap<Integer, List<Integer>>();
		if (j < 0) {
			return leafOffsets; // return empty list
		}
		Node node = endNodeRef.get();
		int offset = node.endPos(pos) - j + seq.size();
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
			Node next = suffix.node.getChild(seq.get(suffix.from));
			int nextPos = next.startPos() + pos - suffix.from - 1;
			if (seq.get(nextPos).equals(token)) {
				return null; // end point
			} else {
				// do split
				Node newNode = new Node(suffix.node, next.startPos(), nextPos);
				suffix.node.setChild(seq.get(next.startPos()), newNode);
				next.setStartPos(nextPos);
				newNode.setChild(seq.get(nextPos), next);
				next.setParent(newNode);
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
			int firstToken = seq.get(suffix.from);
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

	public static int listCmp(List<Integer> a, List<Integer> b) {
		int ldiff = a.size() - b.size();
		if (ldiff != 0) {
			return ldiff;
		}
		for (int i = 0; i < a.size(); ++i) {
			int diff = a.get(i) - b.get(i);
			if (diff != 0) {
				return diff;
			}
		}
		return 0;
	}

	private Map<Node, List<Node>> makeGraph(Map<Node, Set<Integer>> clusters, final Map<Node, Double> baseClusterScores, final Map<Node, List<Integer>> baseClusterPhrases) {
		Map<Node, List<Node>> graph = new HashMap<Node, List<Node>>();
		for (Node node : clusters.keySet()) {
			if (baseClusterScores.get(node) > 0.5) {
				graph.put(node, new LinkedList<Node>(Arrays.asList(new Node[]{node})));
			}
		}
		List<Node> baseClustersList = new ArrayList<Node>(graph.keySet());

		int max = 1000;
		final Comparator<Node> comparator = new Comparator<Node>() {

			@Override
			public int compare(Node t, Node t1) {
				double v = baseClusterScores.get(t), v1 = baseClusterScores.get(t1);
				if (v > v1) {
					return 1;
				}
				if (v < v1) {
					return -1;
				}
				if (t1 == t) {
					return 0;
				}
				return 1;
			}
		};
		PriorityQueue<Node> beam = new PriorityQueue<Node>(max, comparator);

		// create a connectivity table for the graph
		for (int i = 0; i < baseClustersList.size(); ++i) {
			Node node1 = baseClustersList.get(i);
			for (Node node2 : beam) {
				Set<Integer> cluster1 = clusters.get(node1);
				Set<Integer> cluster2 = clusters.get(node2);
				if (cluster1.size() > cluster2.size()) { // swap
					cluster1 = clusters.get(node2);
					cluster2 = clusters.get(node1);
				}
				if (2 * Math.min(cluster1.size(), cluster2.size()) <= Math.max(cluster1.size(), cluster2.size())) {
					continue;
				}
				int intersectionSz = intersection(cluster1, cluster2);
				if (intersectionSz > 0.5 * Math.max(cluster1.size(), cluster2.size())) {
					graph.get(node1).add(node2);
					graph.get(node2).add(node1);
				}
			}
			beam.add(node1);
			if (beam.size() > max) {
				beam.poll();
			}
		}
		return graph;
	}

	private double phraseScore(List<Integer> phrase) {
		double phraseScore = 0;
		for (int i = 0; i < phrase.size(); ++i) {
			phraseScore += getWordCost(phrase.get(i));
		}
		phraseScore = Math.min(phraseScore, 6);
		return phraseScore;
	}

	private int intersection(Set<Integer> cluster1, Set<Integer> cluster2) {
		int intersectionSz = 0;
		for (Integer doc : cluster1) {
			if (cluster2.contains(doc)) {
				intersectionSz++;
			}
		}
		return intersectionSz;
	}

	private void update(Suffix suffix) {
		// s -> suffix.node
		// k -> suffix.from
		// i -> pos
		// r -> explicitState
		Node oldRoot = root;
		int token = seq.get(pos - 1);
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
	Node current;
	Suffix suffix;

	private void init(int totalSz) {
		ground = new Node(null, -1, -1);
		root = new Node(ground, -1, 0);
		nn.put(root, ground);
		current = root;
		nextDocEnd = 0;
		docOffset = new ArrayList<Integer>();
		seq = new ArrayList<Integer>(totalSz);

		wordFrequency = new HashMap<Integer, Integer>(lexicon != null ? lexicon.size() : 0);
		suffix = new Suffix(current, 0);
		pos = 1;
	}

	public void add(List<Integer> doc) {
		docOffset.add(seq.size());
		seq.addAll(doc);
		seq.add(-docOffset.size()); // append document end marker
		this.nextDocEnd = seq.size();
		for (int i = 0; i < doc.size(); ++i) {
			ground.setChild(doc.get(i), root);
		}
		for (Integer id : new HashSet<Integer>(doc)) {
			if (!wordFrequency.containsKey(id)) {
				wordFrequency.put(id, 0);
			}
			wordFrequency.put(id, wordFrequency.get(id) + 1);
		}
		ground.setChild(-docOffset.size(), root);
		build();
	}

	private boolean verifyParents(Node node) {
		for (Integer token : node.tokens()) {
			Node child = node.getChild(token);
			if (child.parent() != node || !verifyParents(child))
				return false;
		}
		return true;
	}
	
	private void build() {
		for (; pos <= seq.size(); ++pos) {
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
		if (cluster.size() > 1) {
			clusters.put(node, cluster);
		}
		return cluster;
	}

	public Map<Node, List<Integer>> getClusters(Map<Node, List<List<Integer>>> clusterSummaries, Map<Node, Double> clusterScores) {
		Map<Node, Set<Integer>> baseClusters = getBaseClusters();
		Map<Node, List<Integer>> baseClusterPhrases = getBaseClusterPhrases(baseClusters);
		Map<Node, Double> baseClusterScores = getBaseClusterScores(baseClusters, baseClusterPhrases);

		long t0 = System.currentTimeMillis();
		Map<Node, List<Node>> graph = makeGraph(baseClusters, baseClusterScores, baseClusterPhrases); // O(n^2)
		long t1 = System.currentTimeMillis();
		System.out.format("\tClustering, makeGraph: %f\n", (t1 - t0) / 1000.0);

		Map<Node, Node> connectedComponents = findConnectedComponents(baseClusters, graph);
		long t2 = System.currentTimeMillis();
		System.out.format("\tClustering, findConnectedComponents: %f\n", (t2 - t1) / 1000.0);

//		for (Node node : clusterRepresentatives)
//			System.out.println(clusterScores.get(node));

//		Comparator<Node> cmp = getNodeComparator(clusterScores);
		Map<Node, Set<Integer>> mergedClusters = new HashMap<Node, Set<Integer>>();
		for (Node node : connectedComponents.keySet()) {
			Node ref = connectedComponents.get(node);
			if (!mergedClusters.containsKey(ref)) {
				mergedClusters.put(ref, new HashSet<Integer>());
			}
			mergedClusters.get(ref).addAll(baseClusters.get(node));
		}
		getAggregateClusterScores(clusterScores, connectedComponents, baseClusterScores, baseClusterPhrases, mergedClusters);

		List<Node> clusterRepresentatives = getSortedClustersRepresentative(clusterScores);

		Map<Node, List<Integer>> ret = new HashMap<Node, List<Integer>>(mergedClusters.size());
		for (Node node : clusterRepresentatives) {
			ret.put(node, new ArrayList<Integer>(mergedClusters.get(node)));
		}

//		Map<Node, List<List<Integer>>> clusterSummaries = new HashMap<Node, List<List<Integer>>>();
		if (clusterSummaries != null) {
			for (Node node : connectedComponents.keySet()) {
				Node ref = connectedComponents.get(node);
				if (!clusterSummaries.containsKey(ref)) {
					clusterSummaries.put(ref, new LinkedList<List<Integer>>());
				}
				clusterSummaries.get(ref).add(baseClusterPhrases.get(node));
			}
		}
//		for (Node node : clusterRepresentatives) {
//			List<List<Integer>> phrases = clusterSummaries.get(node);
//			if (phrases.size() < 2)
//				continue;
//			System.out.println(clusterScores.get(node));
//			StringBuilder str = new StringBuilder();
//			for (List<Integer> phrase : phrases) {
//				for (Integer id : phrase) {
//					str.append(" ");
//					if (!lexicon.hasId(id)) {
//						str.append(String.format("$%d", id));
//					} else {
//						str.append(lexicon.token(id));
//					}
//				}
//				str.append("\n");
//			}
//			System.out.println(str.toString());
//		}
		long t3 = System.currentTimeMillis();
		System.out.format("\tClustering, merging: %f\n", (t3 - t2) / 1000.0);

		return ret;
	}

	public List<Node> getSortedClustersRepresentative(final Map<Node, Double> clusterScores) {
		List<Node> clusterRepresentatives = new ArrayList<Node>(clusterScores.keySet());
		Collections.sort(clusterRepresentatives, getNodeComparator(clusterScores));
		return clusterRepresentatives;
	}

	public Comparator<Node> getNodeComparator(final Map<Node, Double> clusterScores) {
		return new Comparator<Node>() {

			@Override
			public int compare(Node t, Node t1) {
				double diff = clusterScores.get(t1) - clusterScores.get(t);
				if (diff > 0) {
					return 1;
				}
				if (diff < 0) {
					return -1;
				}
				return 0;
			}
		};
	}

	public Map<Node, Set<Integer>> getBaseClusters() {
		Map<Node, Set<Integer>> clusters = new HashMap<Node, Set<Integer>>();
		clustersCollect(clusters, root);
		clusters.remove(root); // remove the root cluster?..
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
				System.out.print(" " + getToken(seq.get(i)));
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
				sb.append(getToken(seq.get(i)));
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

	private void recCollect(List<Integer> list, Node node) {
		if (node.parent() != ground) {
			recCollect(list, node.parent());
			for (int i = node.startPos(); i < node.endPos(pos); ++i) {
				list.add(seq.get(i));
			}
		}
	}

	private List<Integer> nodeToWordIds(Node node) {
		List<Integer> ret = new LinkedList<Integer>();
		recCollect(ret, node);
		return ret;
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
}