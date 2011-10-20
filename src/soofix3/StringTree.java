package soofix3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StringTree {

	List<List<String>> documents;
	Lexicon lexicon;
	List<Integer> seq;
	public Tree tree;

	public StringTree(List<List<String>> documents) {
		this.documents = documents;
		System.out.println("Total " + documents.size() + " documents");
		Set<String> words = new HashSet<String>();
		int totalSz = 0;
		for (List<String> doc : documents) {
			words.addAll(doc);
			totalSz += doc.size() + 1;
		}
		lexicon = new Lexicon(words);
		tree = new Tree(lexicon, totalSz);
		List<Integer> seq1;
		for (int d = 0; d < documents.size(); ++d) {
			List<String> doc = documents.get(d);
			seq1 = new ArrayList<Integer>(doc.size());
			for (int k = 0; k < doc.size(); ++k) {
				seq1.add(lexicon.id(doc.get(k)));
			}
			tree.add(seq1);
		}
	}

	public StringTree(List<List<String>> documents, List<String> stopWords) {
		this(documents);
		lexicon.setStopWordList(stopWords);
	}

	public Map<Integer, List<Integer>> find(List<String> query) {
		List<Integer> qseq = new ArrayList<Integer>(query.size());
		Map<Integer, List<Integer>> ret = new HashMap<Integer, List<Integer>>();
		for (int i = 0; i < query.size(); ++i) {
			if (!lexicon.hasToken(query.get(i))) {
				return ret;
			}
			qseq.add(lexicon.id(query.get(i)));
		}
		return tree.findAll(qseq);
	}

	public Map<Node, List<Integer>> clusters(Map<Node, List<List<Integer>>> clusterSummaries, Map<Node, Double> clusterScores) {
		Map<Node, List<Integer>> clusters = tree.getClusters(clusterSummaries, clusterScores);
		return clusters;
	}
}
