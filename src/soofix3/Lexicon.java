package soofix3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Lexicon {
	static boolean useTopics = false;

	final Map<String, Integer> lexicon = new HashMap<String, Integer>();
	final Map<Integer, String> inv = new HashMap<Integer, String>();
	final int size;

	Lexicon(Iterable<String> tokens) {
		int cnt = 0;
		for (String token : tokens) {
			if (!lexicon.containsKey(token)) {
				lexicon.put(token, cnt++);
			}
		}
		for (String token : lexicon.keySet()) {
			inv.put(lexicon.get(token), token);
		}
		size = cnt;
	}

	Lexicon(Map<String, Integer> map) {
		for (String token : map.keySet()) {
			if (!lexicon.containsKey(token)) {
				lexicon.put(token, map.get(token));
			}
		}
		for (String token : lexicon.keySet()) {
			inv.put(lexicon.get(token), token);
		}
		size = lexicon.size();
	}
	
	void setStopWordList (List<String> list) {
		stopWords.addAll(list);
	}

	int id(String token) {
		return lexicon.get(token);
	}

	String token(int id) {
		return inv.get(id);
	}

	boolean hasToken(String token) {
		return lexicon.containsKey(token);
	}

	boolean hasId(int id) {
		return inv.containsKey(id);
	}
	
	int size () {
		return lexicon.size();
	}
	
	Set<String> stopWords = new HashSet<String>();
	boolean isStopWord (String token) {
		return stopWords.contains(token);
	}
}
