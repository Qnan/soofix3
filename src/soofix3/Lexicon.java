package soofix3;

import java.util.HashMap;
import java.util.Map;

public class Lexicon {

	final Map<String, Integer> lexicon = new HashMap<String, Integer>();
	final Map<Integer, String> inv = new HashMap<Integer, String>();
	final int size;

	Lexicon(Iterable<String> tokens) {
		lexicon.put(null, -1);
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
}
