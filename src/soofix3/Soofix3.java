package soofix3;

public class Soofix3 {

	public static void main(String[] args) {
//        int seq[] = new int[]{'m','i','s','s','i','s'};
//        int seq[] = new int[]{'a','b','a','b','a'};
//        int seq[] = new int[]{'a','b','b','b','a','b'};
//		  int seq[] = new int[]{'a','b','b','b','b'};
		int seq[] = new int[]{'m','i','s','s','i','s','s','i','p','p','i'};
//		int seq[] = new int[]{'c','a','c','a','o'};
//                int seq[] = new int[]{'b','o','o','k','k','e','e','p','e','r'};
//                int seq[] = new int[]{'a','b','b','a','a'};
//		int seq[] = new int[]{'b', 'b', 'b', 'b', 'b'};
//		int seq[] = new int[]{'a', 'b', 'c', 'd', 'e'};
		Tree tree = new Tree(seq);
		System.out.println(tree.find(new int[]{'s','s','i'}));
		System.out.println(tree.find(new int[]{'m','i','s','s','i','s','s','i','p','p','i'}));
//		tree.printTree();
//		System.out.println();
//		System.out.println();
//		tree.printSuffixes();
	}
}
