package soofix3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Soofix3 {

	public static int _lastPos;

	public static void makeRandomCharSeq(List<Integer> seq, int length, char last, int seed) {
		Random rnd = new Random(seed);
		seq.clear();
		for (int i = 0; i < length; ++i) {
			seq.add('a' + rnd.nextInt(last - 'a' + 1));
		}
	}

	public static void testSearch(int targetLength, int queryLength, int queryNum, char last, int seed, boolean testFindAll) {
		List<Integer> target = new ArrayList<Integer>(targetLength);
		List<Integer> query = new ArrayList<Integer>(queryLength);
		Random rnd = new Random(seed);
		makeRandomCharSeq(target, targetLength, last, rnd.nextInt());
		String targetStr = seqToString(target);
		Tree tree = new Tree(null);
		tree.add(target);
		int treeTotal = 0, stringTotal = 0;
		for (int i = 0; i < queryNum; ++i) {
			makeRandomCharSeq(query, queryLength, last, rnd.nextInt());
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
				if (map.containsKey(0)) {
					treeResAll.addAll(map.get(0));
				}
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

	private static void printSeq(Lexicon lexicon, List<Integer> seq) {
		for (int i = 0; i < seq.size(); ++i) {
			System.out.print(lexicon.token(seq.get(i)));
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

	private static List<String> readLines(File fileInput) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileInput));
		String line;
		List<String> lines = new LinkedList<String>();
		while ((line = br.readLine()) != null) {
			lines.add(line.trim());
		}
		return lines;
	}

	private static String seqToString(List<Integer> seq) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < seq.size(); ++i) {
			sb.append((char) seq.get(i).intValue());
		}
		return sb.toString();
	}

	private static void run(String[] args) throws Error, IOException {
		if (args.length < 1) {
			throw new Error("arguments: <corpus file> [<query file>]");
		}
		String fnameCorpus = args[0];
		File fileCorpus = new File(fnameCorpus).getCanonicalFile();
		if (!fileCorpus.exists()) {
			throw new Error("file not found: " + fileCorpus.getAbsolutePath());
		}

		String fnameOut = args[1];
		File fileOut = new File(fnameOut).getCanonicalFile();
//		if (!fileOut.exists()) {
//			throw new Error("file not found: " + fileOut.getAbsolutePath());
//		}

		List<String> stopWords = new LinkedList<String>();
		if (args.length > 2) {
			String fnameStop = args[2];
			File fileStopWords = new File(fnameStop);
			stopWords = readLines(fileStopWords);
		}

		List<List<String>> documents = readDataFile(fileCorpus);

		long t0 = System.currentTimeMillis();
		StringTree st = new StringTree(documents, stopWords);
		Lexicon lexicon = st.lexicon;
		long t1 = System.currentTimeMillis();
		System.out.format("Tree built: %f\n", (t1 - t0) / 1000.0);

		Map<Node, List<List<Integer>>> clusterSummaries = new HashMap<Node, List<List<Integer>>>();
		Map<Node, Double> clusterScores = new HashMap<Node, Double>();
		Map<Node, List<Integer>> clusters = st.clusters(clusterSummaries, clusterScores);
		long t2 = System.currentTimeMillis();
		System.out.format("Clustering done: %f\n", (t2 - t1) / 1000.0);

		BufferedWriter writer = new BufferedWriter(new FileWriter(fileOut));
		for (Node node : clusters.keySet()) {
			List<Integer> cluster = clusters.get(node);
			for (int i = 0; i < cluster.size(); ++i) {
				if (i > 0) {
					writer.append(" ");
				}
				writer.append(cluster.get(i).toString());
			}
			writer.append("\n");

			// print score
			writer.append("#");
			writer.append(String.format("%.2f", clusterScores.get(node)));
			writer.append("\n");

			// print phrases
			for (List<Integer> phrase : clusterSummaries.get(node)) {
				for (int i = 0; i < phrase.size(); ++i) {
					if (i == 0) {
						writer.append("#");
					}
					writer.append(" ");
					writer.append(lexicon.token(phrase.get(i)));
				}
				writer.append("\n");
			}
		}
		long t3 = System.currentTimeMillis();
		System.out.format("Clusters stored: %f\n", (t3 - t2) / 1000.0);

//		if (queries != null) {
//			for (List<String> query : queries) {
//				Map<Integer, List<Integer>> matches = st.find(query);
//				for (String word : query) {
//					System.out.print(word);
//					System.out.print(' ');
//				}
//				System.out.println();
//				for (Integer docId : matches.keySet()) {
//					System.out.print(docId);
//					System.out.print(':');
//					for (Integer matchPos : matches.get(docId)) {
//						System.out.print(' ');
//						System.out.print(matchPos);
//					}
//					System.out.println();
//				}
//				System.out.println();
//				System.out.println();
//			}
//		}
	}

	private static void testWord() {
		String word = "mississippi";
		List<Integer> seq = new ArrayList<Integer>(word.length());
		List<String> chars = new ArrayList<String>(word.length());
		for (int i = 0; i < word.length(); ++i) {
			chars.add(String.format("%c", word.charAt(i)));
		}
		Lexicon lexicon = new Lexicon(new HashSet<String>(chars));
		for (int i = 0; i < chars.size(); ++i) {
			seq.add(lexicon.id(chars.get(i)));
		}
		printSeq(lexicon, seq);

		Tree tree = new Tree(lexicon);
		tree.add(seq);
		String query = "ssi";
		List<Integer> qseq = new ArrayList<Integer>(query.length());
		for (int i = 0; i < query.length(); ++i) {
			qseq.add(lexicon.id(String.format("%c", query.charAt(i))));
		}

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
//		testWord();
//
//		testSearch(200000, 3, 1000, 'z', 1, true);
//		System.out.println(findShortestFailingSeed(1000, 10000, 'h'));

//		Tree.logBuilding = true;
		run(args);
	}
}
