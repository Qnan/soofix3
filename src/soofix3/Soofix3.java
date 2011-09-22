package soofix3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Soofix3 {

	public static int _lastPos;

	public static void makeRandomCharSeq(int[] seq, char last, int seed) {
		Random rnd = new Random(seed);
		for (int i = 0; i < seq.length; ++i) {
			seq[i] = 'a' + rnd.nextInt(last - 'a' + 1);
		}
	}

	public static void testSearch(int targetLength, int queryLength, int queryNum, char last, int seed, boolean testFindAll) {
		int[] target = new int[targetLength], query = new int[queryLength];
		Random rnd = new Random(seed);
		makeRandomCharSeq(target, last, rnd.nextInt());
		String targetStr = seqToString(target);
		Tree tree = new Tree(null, target);
		int treeTotal = 0, stringTotal = 0;
		for (int i = 0; i < queryNum; ++i) {
			makeRandomCharSeq(query, last, rnd.nextInt());
			String queryStr = seqToString(query);
			int treeRes = -2, stringRes = -2;
			List<Integer> treeResAll = null, stringResAll = null;

			final long startTime1 = System.nanoTime();
			final long endTime1;
			try {
				treeRes = tree.find(query);
			} finally {
				endTime1 = System.nanoTime();
			}
			final long duration1 = endTime1 - startTime1;

			final long startTime2 = System.nanoTime();
			final long endTime2;
			try {
				stringRes = targetStr.indexOf(queryStr);
			} finally {
				endTime2 = System.nanoTime();
			}
			final long duration2 = endTime2 - startTime2;

			treeTotal += duration1;
			stringTotal += duration2;

			if (testFindAll) {
				// tree
				Map<Integer, List<Integer>> map = tree.findAll(query);
				treeResAll = new LinkedList<Integer>();
				if (map.containsKey(-1))
					treeResAll.addAll(map.get(-1));
				Collections.sort(treeResAll);

				// string
				Pattern p = Pattern.compile(queryStr, Pattern.LITERAL);
				Matcher matcher = p.matcher(targetStr);
				List<Integer> list = new LinkedList<Integer>();
				int pos = 0;
				while (matcher.find(pos)) {
					list.add(matcher.start());
					pos = matcher.start() + 1;
				}
				stringResAll = list;
				Collections.sort(stringResAll);
				if (!Arrays.equals(treeResAll.toArray(new Integer[]{}), stringResAll.toArray(new Integer[]{}))) {
					throw new Error("search result mismatch");
				}
			}

			if (treeRes != stringRes) {
				throw new Error("search result mismatch");
			}
		}
		System.out.println("treeTotal: " + treeTotal / 1000000);
		System.out.println("stringTotal: " + stringTotal / 1000000);
	}

	private static void printSeq(int[] seq) {
		for (int i = 0; i < seq.length; ++i) {
			System.out.print((char) seq[i]);
		}
		System.out.println();
	}

	private static List<List<String>> readDataFile(File fileInput) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileInput));
		String document;
		List<List<String>> documents = new LinkedList<List<String>>();
		while ((document = br.readLine()) != null) {
			documents.add(Arrays.asList(document.trim().split("\\s+")));
		}
		return documents;
	}

	private static String seqToString(int[] seq) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < seq.length; ++i) {
			sb.append((char) seq[i]);
		}
		return sb.toString();
	}

	private static void run(String[] args) throws Error, IOException {
		if (args.length < 2) {
			throw new Error("arguments: <corpus file> <query file>");
		}
		String fnameCorpus = args[0];
		String fnameQuery = args[1];
		File fileCorpus = new File(fnameCorpus).getCanonicalFile();
		File fileQuery = new File(fnameQuery).getCanonicalFile();
		if (!fileCorpus.exists()) {
			throw new Error("file not found: " + fileCorpus.getAbsolutePath());
		}
		if (!fileQuery.exists()) {
			throw new Error("file not found: " + fileQuery.getAbsolutePath());
		}
		List<List<String>> documents = readDataFile(fileCorpus);
		List<List<String>> queries = readDataFile(fileQuery);

		StringTree st = new StringTree(documents);
		for (List<String> query : queries) {
			Map<Integer, List<Integer>> matches = st.find(query);
			for (String word : query) {
				System.out.print(word);
				System.out.print(' ');
			}
			System.out.println();
			for (Integer docId : matches.keySet()) {
				System.out.print(docId);
				System.out.print(':');
				for (Integer matchPos : matches.get(docId)) {
					System.out.print(' ');
					System.out.print(matchPos);
				}
				System.out.println();
			}
			System.out.println();
			System.out.println();
		}
	}

	private static void testWord() {
		int seq[] = new int[]{'m','i','s','s','i','s','s','i','p','p','i'};
		printSeq(seq);
		Tree.logBuilding = true;
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < seq.length; ++i) {
			map.put(String.format("%c", (char)seq[i]), seq[i]);
		}
		Lexicon lexicon = new Lexicon(map);
		Tree tree = new Tree(lexicon, seq);
		int[] qseq = new int[]{'s','s','i'};
		Map<Integer, List<Integer>> occur = tree.findAll(qseq);
		for (Integer i : occur.keySet()) {
			System.out.print(i);
			System.out.print(": ");
			for (Integer j : occur.get(i)) {
				System.out.print(" ");
				System.out.print(j);
			}
			System.out.println();
		}
	}

	public static void main(String[] args) throws IOException {
		testWord();
//
//		testSearch(200000, 3, 1000, 'z', 1, true);
//		System.out.println(findShortestFailingSeed(1000, 10000, 'h'));

//		Tree.logBuilding = true;
//		run(args);
	}

}
