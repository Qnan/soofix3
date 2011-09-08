package soofix3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class StringTree {

	List<List<String>> documents;
	Lexicon lexicon;
	int[] docId, seq, docStart;
	Tree tree;

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
		seq = new int[totalSz];
		docId = new int[totalSz];
		docStart = new int[documents.size()];
		int j = 0, d = 0;
		for (List<String> doc : documents) {
			docStart[d] = j;
			for (int k = 0; k < doc.size(); ++k) {
				docId[j] = d;
				seq[j++] = lexicon.id(doc.get(k));
			}
			docId[j] = -1;
			seq[j++] = -d - 1; // i-th document boundry marker
			d++;
		}
		
		tree = new Tree(seq);
	}

	public Map<Integer, List<Integer>> find(List<String> query) {
		int[] qseq = new int[query.size()];
		Map<Integer, List<Integer>> ret = new HashMap<Integer, List<Integer>>();
		for (int i = 0; i < query.size(); ++i) {
			if (!lexicon.hasToken(query.get(i))) {
				return ret;
			}
			qseq[i] = lexicon.id(query.get(i));
		}
		List<Integer> positions = tree.findAll(qseq);
		for (Integer pos : positions) {
			int d = docId[pos];
			int p = pos - docStart[d];
			if (!ret.containsKey(d)) {
				ret.put(d, new LinkedList<Integer>());
			}
			ret.get(d).add(p);
		}
		return ret;
	}
}
