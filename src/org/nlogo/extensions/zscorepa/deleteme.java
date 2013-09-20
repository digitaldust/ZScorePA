/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.zscorepa;

import java.util.HashSet;
import java.util.Set;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

/**
 *
 * @author digitaldust
 */
public class deleteme {

    public static void main(String[] args) {
        Set<Node> nei = new HashSet<Node>();
        for(int i=0;i<4;i++){
            Node n = new Node();
            n.setName("node-"+i);
            n.setDegree(3);
            nei.add(n);
            System.out.println(n.getName());
        }
        // Create the initial vector
        ICombinatoricsVector<Node> initialVector = Factory.createVector(nei);

        // Create a simple combination generator to generate 2-combinations of the initial vector
        Generator<Node> gen = Factory.createSimpleCombinationGenerator(initialVector, 2);

        // Print all possible combinations
        for (ICombinatoricsVector<Node> s : gen) {
            System.out.println(s);
        }
    }
//    static int francescosantinipeoro;
//    
//    public static void main(String[] args) {
//        Set<Node> nei = new HashSet<Node>();
//        for(int i=0;i<4;i++){
//            Node n = new Node();
//            n.setName("node-"+i);
//            n.setDegree(3);
//            nei.add(n);
//            System.out.println(n.getName());
//        }
//        Node[] tmp = new Node[2];
//        Arrays.fill(tmp, null);
//        generateCombinations(nei.toArray(), 0, 0, tmp);
//        System.out.println(francescosantinipeoro);
//    }
// 
//    private static void generateCombinations(Object[] array, int start, int depth, Node[] tmp) {
//        // fires only if condition is reached
//        if (depth == tmp.length) {
//            francescosantinipeoro += tmp[0].getDegree() + tmp[1].getDegree();
//            // 
//            for (int j = 0; j < depth; j++) {
//                System.out.print(tmp[j].getName() + " ");
//            }
//            System.out.println();
//            return;
//        }
//        for (int i = start; i < array.length; i++) {
//            tmp[depth] = (Node)array[i];
//            generateCombinations(array, i + 1, depth + 1, tmp);
//        }
//    }
}