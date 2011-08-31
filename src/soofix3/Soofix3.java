package soofix3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Soofix3 {

	public static int _lastPos;

	public static void makeRandomCharSeq(int[] seq, char last, int seed) {
		Random rnd = new Random(seed);
		for (int i = 0; i < seq.length; ++i) {
			seq[i] = 'a' + rnd.nextInt(last - 'a' + 1);
		}
	}

	public static int getFailPos(int[] seq) {
		Tree tree = null;
		try {
			tree = new Tree(seq);
		} catch (Exception ex) {
		} finally {
			return _lastPos;
		}
	}

	public static int findShortestFailingSeed(int length, int iter, char last) {
		int seq[] = new int[length];
		int bestSeed = -1, minLength = length;
		for (int seed = 0; seed < iter; ++seed) {
			makeRandomCharSeq(seq, last, seed);
			int lastPos = getFailPos(seq);
			if (lastPos < minLength) {
				minLength = lastPos;
				bestSeed = seed;
			}
		}
		System.out.println(minLength);
		return bestSeed;
	}

	public static void testSearch(int targetLength, int queryLength, int queryNum, char last, int seed) {
		int[] target = new int[targetLength], query = new int[queryLength];
		Random rnd = new Random(seed);
		makeRandomCharSeq(target, last, rnd.nextInt());
		String targetStr = seqToString(target);
		Tree tree = new Tree(target);
		int treeTotal = 0, stringTotal = 0;
		for (int i = 0; i < queryNum; ++i) {
			makeRandomCharSeq(query, last, rnd.nextInt());
			String queryStr = seqToString(query);
			int treeRes = -2;
			int stringRes = -2;

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
			
			if (treeRes != stringRes) {
				throw new Error("search result mismatch");
			}
		}
		System.out.println("treeTotal: " + treeTotal/1000000);
		System.out.println("stringTotal: " + stringTotal/1000000);
	}

	public static void main(String[] args) throws IOException {
//		int seq[] = new int[]{'m','i','s','s','i','s','s','i','p','p','i'};
//		printSeq(seq);
//		Tree.logBuilding = true;
//		Tree tree = new Tree(seq);
//
//		testSearch(200000, 3, 1000, 'z', 1);
//		System.out.println(findShortestFailingSeed(1000, 10000, 'h'));

		run(args);
	}

	private static void printSeq(int[] seq) {
		for (int i = 0; i < seq.length; ++i) {
			System.out.print((char) seq[i]);
		}
		System.out.println();
	}

	private static String seqToString(int[] seq) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < seq.length; ++i) {
			sb.append((char) seq[i]);
		}
		return sb.toString();
	}

	private static void run(String[] args) throws Error, IOException {
		if (args.length < 1) {
			throw new Error("mode not specified");
		}
		String mode = args[0];
		System.out.println("mode: " + mode);
		Lexicon lexicon = null;
		if (mode.equals("build")) {
			if (args.length < 2) {
				throw new Error("no input file name");
			}
			String fnameInput = args[1];
			File fileInput = new File(fnameInput);
			if (!fileInput.exists()) {
				throw new Error("file not found: " + fileInput.getCanonicalFile().getAbsolutePath());
			}
			BufferedReader br = new BufferedReader(new FileReader(fileInput));
			String line;
			List<String> text = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				String[] split = line.trim().split("\\s+");
				text.addAll(Arrays.asList(split));
				text.add(null);
			}
//			 for (String token : text) {
//				 System.out.print(token);
//				 System.out.print(' ');
//			 }

			lexicon = new Lexicon(text);
			int[] seq = new int[text.size()];
			for (int i = 0; i < seq.length; ++i) {
				seq[i] = lexicon.id(text.get(i));
			}

			Tree tree = new Tree(seq);
		} else {
			throw new Error("mode unknwon: " + mode);
		}
	}
}
