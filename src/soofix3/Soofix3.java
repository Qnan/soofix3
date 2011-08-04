/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soofix3;

/**
 *
 * @author mikhail
 */
public class Soofix3 {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		Tree tree = new Tree();
//        int seq[] = new int[]{'m','i','s','s','i','s'};
        int seq[] = new int[]{'a','b','a','b','a','b'};
//        int seq[] = new int[]{'m','i','s','s','i','s','s','i','p','p','i'};
//                int seq[] = new int[]{'b','o','o','k','k','e','e','p','e','r'};
//                int seq[] = new int[]{'a','b','b','a','a'};
//		int seq[] = new int[]{'b', 'b', 'b', 'b', 'b'};
//		int seq[] = new int[]{'a', 'b', 'c', 'd', 'e'};
		tree.addSequence(seq);
//		tree.printTree(seq);
	}
}
