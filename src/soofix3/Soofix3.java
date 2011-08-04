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
//                int seq[] = new int[]{'b','o','o','k','k','e','e','p','e','r',};
		int seq[] = new int[]{'a', 'b', 'b', 'b', 'b'};
		tree.addSequence(seq);
		tree.printTree(seq);
	}
}
