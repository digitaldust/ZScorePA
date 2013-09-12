package org.nlogo.extensions.zscorepa;

import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.nlogo.api.ExtensionException;

/**
 *
 * @author Simone Gabbriellini
 */
public class WriteStats {

    //ArrayList<Integer> keySetOrdered;
    static void writeActivity(UndirectedSparseGraph<Node, Edge> g, HashMap<Double, Collection<Node>> degree, HashMap<Double, Collection<Node>> threaDeg, String activityPath, String threadactivityPath) throws ExtensionException {

        // calculate average posts, thread and zindex activity for each degree
        HashMap<Double, Double> posts_hash = new HashMap<Double, Double>();
        HashMap<Double, Double> threads_hash = new HashMap<Double, Double>();
        HashMap<Double, Double> zindex_hash = new HashMap<Double, Double>();
        HashMap<Double, Double> zindexNei_hash = new HashMap<Double, Double>();
        Set<Double> degrees = degree.keySet();
        Iterator<Double> degIter = degrees.iterator();
        while (degIter.hasNext()) {
            // degree value
            double key = degIter.next();
            ArrayList<Double> postsDeg = new ArrayList<Double>();
            ArrayList<Double> threadsDeg = new ArrayList<Double>();
            ArrayList<Double> zindex = new ArrayList<Double>();
            HashSet<Node> zindexNei = new HashSet<Node>();
            Collection<Node> nodesWithDegree = degree.get(key);
            Iterator<Node> nodesWithDegreeIter = nodesWithDegree.iterator();
            while (nodesWithDegreeIter.hasNext()) {
                User caller = (User) nodesWithDegreeIter.next();
                postsDeg.add(caller.getPostsDone());
                threadsDeg.add(caller.getThreadsDone());
                zindex.add(caller.getZindex());
                Collection<Node> neighbors = g.getNeighbors(caller);
                zindexNei.addAll(neighbors);
            }
            double meanNeiZindex = 0;
            for (Node n : zindexNei) {
                meanNeiZindex += n.getZindex();
            }
            meanNeiZindex /= zindexNei.size();
            posts_hash.put(key, mean(postsDeg));
            threads_hash.put(key, mean(threadsDeg));
            zindex_hash.put(key, mean(zindex));
            zindexNei_hash.put(key, meanNeiZindex);
        }
        // write results to file
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(activityPath, false));
            out.append("degree\t" + "ave_posts\t" + "ave_threads\t" + "ave_zindex\t" + "ave_nei_zindex\n");
            Set<Double> keySet = degree.keySet();
            // order results
            Object[] toArray = keySet.toArray();
            Arrays.sort(toArray);
            for (Object d : toArray) {
                out.write(d + "\t" + posts_hash.get((Double) d) + "\t" + threads_hash.get((Double) d) + "\t" + zindex_hash.get((Double) d) + "\t" + zindexNei_hash.get((Double) d) + "\n");
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new ExtensionException(e);
        }

        HashMap<Double, Double> zindexThread_hash = new HashMap<Double, Double>();
        Set<Double> threaDegrees = threaDeg.keySet();
        Iterator<Double> threaDegIter = threaDegrees.iterator();
        while (threaDegIter.hasNext()) {
            // degree value
            double key = threaDegIter.next();
            ArrayList<Double> zindexDeg = new ArrayList<Double>();
            Collection<Node> nodesWithDegree = threaDeg.get(key);
            for (Node n : nodesWithDegree) {
                zindexDeg.add(n.getZindex());
            }
            zindexThread_hash.put(key, mean(zindexDeg));
        }
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(threadactivityPath, false));
            out.append("degree\t" + "ave_zindex\n");
            Set<Double> keySet = threaDeg.keySet();
            // order results
            Object[] toArray = keySet.toArray();
            Arrays.sort(toArray);
            for (Object d : toArray) {
                out.write(d + "\t" + zindexThread_hash.get((Double) d) + "\n");
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new ExtensionException(e);
        }

//        // TODO : FIXME
        String nuovoFile = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/EXPERIMENTS/mmorpg/nuovofile-empirical.txt";
        Collection<Node> vertices = g.getVertices();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(nuovoFile, false));
            out.append("zindex\t" + "nei_zindex\t" + "appeal\t" + "degree\t" + "nei_degree\n");
            for (Node n : vertices) {
                if (n.getColor().equals("red")) {
                    Collection<Node> neighbors = g.getNeighbors(n);
                    for (Node t : neighbors) {
                        Thread tt = (Thread) t;
                        out.write(n.getZindex() + "\t" + tt.getZindex() + "\t" + tt.getAppeal() + "\t" + n.getDegree() + "\t" + t.getDegree() + "\n");
                    }
                }
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new ExtensionException(e);
        }
    }

    static void writeNetwork(UndirectedSparseGraph<Node, Edge> g, String networkPath) throws ExtensionException {
        // write network.
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(networkPath, false));
            for (Node n : g.getVertices()) {
                if (n.getColor().equals("red")) {
                    Collection<Node> nei = g.getNeighbors(n);
                    for (Node nn : nei) {
                        out.write(n.getName() + " " + nn.getName() + "\n");
                    }
                }
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new ExtensionException(e);
        }
    }

    /**
     * store results
     */
    static void writeBipartiteStats(HashMap<Double, Collection<Node>> degree, int size, String statsPath) throws ExtensionException {
        //
        HashMap<Double, Double> dd_hash = new HashMap<Double, Double>();
        HashMap<Double, Double> red_hash = new HashMap<Double, Double>();
        HashMap<Double, Double> two_dist_hash = new HashMap<Double, Double>();
        HashMap<Double, Double> nei_deg_hash = new HashMap<Double, Double>();
        HashMap<Double, Double> clustDot_hash = new HashMap<Double, Double>();
        HashMap<Double, Double> clustLowDot_hash = new HashMap<Double, Double>();
        HashMap<Double, Double> clustTopDot_hash = new HashMap<Double, Double>();
        // dovrei trovare il mean zindex di chi ha quel grado
        Set<Double> degrees = degree.keySet();
        Iterator<Double> degIter = degrees.iterator();
        while (degIter.hasNext()) {
            // degree value
            double key = degIter.next();
            ArrayList<Double> neiDeg = new ArrayList<Double>();
            ArrayList<Double> twoDistNei = new ArrayList<Double>();
            ArrayList<Double> redundancy = new ArrayList<Double>();
            ArrayList<Double> clustDot = new ArrayList<Double>();
            ArrayList<Double> clustLowDot = new ArrayList<Double>();
            ArrayList<Double> clustTopDot = new ArrayList<Double>();
            Collection<Node> nodesWithDegree = degree.get(key);
            Iterator<Node> nodesWithDegreeIter = nodesWithDegree.iterator();
            while (nodesWithDegreeIter.hasNext()) {
                Node caller = nodesWithDegreeIter.next();
                neiDeg.addAll(caller.getNeiDeg());
                twoDistNei.add((double) caller.getTwoDistNei().size());
                redundancy.add(caller.getRedundancy());
                clustDot.add(caller.getClustDot());
                clustLowDot.add(caller.getClustLowDot());
                clustTopDot.add(caller.getClustTopDot());
            }
            dd_hash.put(key, ((double) nodesWithDegree.size() / (double) size));
            nei_deg_hash.put(key, mean(neiDeg));
            two_dist_hash.put(key, mean(twoDistNei));
            red_hash.put(key, mean(redundancy));
            clustDot_hash.put(key, mean(clustDot));
            clustLowDot_hash.put(key, mean(clustLowDot));
            clustTopDot_hash.put(key, mean(clustTopDot));
        }
        // write results to file
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(statsPath, false));
            out.append("degree\t" + "degree distr\t" + "nei_deg\t" + "2-dist-nei\t" + "redundancy\t" + "clustDot\t" + "clustLowDot\t" + "clustTopDot\n");
            Set<Double> keySet = dd_hash.keySet();
            Object[] toArray = keySet.toArray();
            Arrays.sort(toArray);
            for (Object it : toArray) {
                Double deg = (Double) it;
                out.write(deg + "\t" + dd_hash.get(deg) + "\t" + nei_deg_hash.get(deg) + "\t" + two_dist_hash.get(deg) + "\t"
                        + red_hash.get(deg) + "\t" + clustDot_hash.get(deg) + "\t" + clustLowDot_hash.get(deg) + "\t"
                        + clustTopDot_hash.get(deg) + "\n");
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new ExtensionException(e);
        }
    }

    // find how many commons nei two nodes in the same partition have
    private static double findCommons(Collection<Node> myNei, Collection<Node> myNeiOfNei) {
        double sum = 0;
        double den = (double) myNei.size() + myNeiOfNei.size();
        if (myNei.size() > myNeiOfNei.size()) {
            Collection<Node> c = myNei;
            myNei = myNeiOfNei;
            myNeiOfNei = c;
        }
        for (Iterator<Node> iterator = myNeiOfNei.iterator(); iterator.hasNext();) {
            Node o = iterator.next();
            if (myNei.contains(o)) {
                sum += 1.0;
            }
        }
        return sum / den;//(den - sum);
    }

    // find
    private static double findCommonsLow(Collection<Node> myNei, Collection<Node> myNeiOfNei) {
        double sum = 0;
        double den;
        if (myNei.size() < myNeiOfNei.size()) {
            den = (double) myNei.size();
        } else {
            den = (double) myNeiOfNei.size();
        }
        if (myNei.size() > myNeiOfNei.size()) {
            Collection<Node> c = myNei;
            myNei = myNeiOfNei;
            myNeiOfNei = c;
        }
        for (Iterator<Node> iterator = myNeiOfNei.iterator(); iterator.hasNext();) {
            Node o = iterator.next();
            if (myNei.contains(o)) {
                sum += 1.0;
            }
        }
        return sum / den;//(den - sum);
    }

    // find
    private static double findCommonsTop(Collection<Node> myNei, Collection<Node> myNeiOfNei) {
        double sum = 0;
        double den;
        if (myNei.size() > myNeiOfNei.size()) {
            den = (double) myNei.size();
        } else {
            den = (double) myNeiOfNei.size();
        }
        if (myNei.size() > myNeiOfNei.size()) {
            Collection<Node> c = myNei;
            myNei = myNeiOfNei;
            myNeiOfNei = c;
        }
        for (Iterator<Node> iterator = myNeiOfNei.iterator(); iterator.hasNext();) {
            Node o = iterator.next();
            if (myNei.contains(o)) {
                sum += 1.0;
            }
        }
        return sum / den;//(den - sum);
    }

    // from http://www.java2s.com/Code/Java/Collections-Data-Structure/Disjointtwocollections.htm
    private static boolean disjoint(Collection c1, Collection c2, Node ego) {
        if (c1.size() > c2.size()) {
            Collection c = c1;
            c1 = c2;
            c2 = c;
        }
        for (Iterator iterator = c2.iterator(); iterator.hasNext();) {
            Object o = (Object) iterator.next();
            if (c1.contains(o) && o != ego) {
                return false;
            }
        }
        return true;
    }

    // find
    static void findValues(UndirectedSparseGraph<Node, Edge> g) {
        /**
         * find bipartite sna measures
         *
         * Collection<Node> nodes = g.getVertices(); Iterator<Node>
         * nodesListIter = nodes.iterator(); while (nodesListIter.hasNext()) {
         * // ego - the caller Node ego = nodesListIter.next(); // find
         * Collection<Node> egoNei = g.getNeighbors(ego); //array for my
         * neighbors degree ArrayList< Double> egoNeiDeg = new
         * ArrayList<Double>(); // hashmap for 2-dist-nei HashMap<Node, Integer>
         * egoNeiOfNei = new HashMap<Node, Integer>(); // redundancy hashmap
         * HashMap<Node, Collection<Node>> redundancy = new HashMap<Node,
         * Collection<Node>>(); // find degrees of neighbors of me
         * Iterator<Node> egoNeiIter = egoNei.iterator(); while
         * (egoNeiIter.hasNext()) { // alter - the caller now Node alter =
         * egoNeiIter.next(); // find degrees each alter of ego
         * egoNeiDeg.add((double) g.degree(alter)); // find neighbors of alter
         * Collection<Node> twoDist = g.getNeighbors(alter); //
         * redundancy.put(alter, twoDist); // find how many unique nodes in the
         * same projection ego is linked with // - corresponds to the degree of
         * ego in a bipartite projection Iterator<Node> twoDistIter =
         * twoDist.iterator(); while (twoDistIter.hasNext()) { // add unique
         * neiof nei to the hashmap // save the number for redundancy
         * egoNeiOfNei.put(twoDistIter.next(), 1); } } // redundancy double num
         * = 0; ArrayList<Node> Blist = new
         * ArrayList<Node>(redundancy.keySet()); Iterator<Node> redundancyIter =
         * redundancy.keySet().iterator(); while (redundancyIter.hasNext()) {
         * Node A = redundancyIter.next(); Blist.remove(A); Iterator<Node>
         * newListIter = Blist.iterator(); while (newListIter.hasNext()) { Node
         * B = newListIter.next(); Collection<Node> Anei = redundancy.get(A);
         * Collection<Node> Bnei = redundancy.get(B); if ((Anei.size() > 0) &&
         * (Bnei.size() > 0)) { if (!(disjoint(Anei, Bnei, ego))) { num += 1; }
         * } } } // set degree of caller Integer degree = g.degree(ego);
         * ego.setDegree(degree); // QUESTO CI DOVREBBE GIà ESSERE PER USERS E
         * THREADS // set ego's list of degree of neighbors
         * ego.setNeiDeg(egoNeiDeg); // set ego's list of unique 2-dist-nei -
         * corresponds to degree in the projection egoNeiOfNei.remove(ego);
         * ego.setTwoDistNei(egoNeiOfNei.keySet()); // set ego's redundancy if
         * (egoNei.size() > 1) { ego.setRedundancy((double) (num /
         * ((egoNei.size() * (egoNei.size() - 1)) / 2))); } else {
         * ego.setRedundancy((double) 0.01); } // set ego's clustering
         * dot,lowDot and topDot double numclust = 0; double numclustLow = 0;
         * double numclustTop = 0; double emptyDot = 0; double emptyLowDot = 0;
         * double emptyTopDot = 0; Set<Node> clust = ego.getTwoDistNei();
         *
         * if (egoNei.size() > 0 && clust.size() > 0) { Iterator<Node> clustIter
         * = clust.iterator(); while (clustIter.hasNext()) { Node clustNei =
         * clustIter.next(); double dot = findCommons(egoNei,
         * g.getNeighbors(clustNei)); numclust += dot; if (dot == 0) {
         * emptyDot++; } double lowDot = findCommonsLow(egoNei,
         * g.getNeighbors(clustNei)); numclustLow += lowDot; if (lowDot == 0) {
         * emptyLowDot++; } double topDot = findCommonsTop(egoNei,
         * g.getNeighbors(clustNei)); numclustTop += topDot; if (topDot == 0) {
         * emptyTopDot++; } } }
         *
         * if (clust.size() > 0) { ego.setClustDot((double) (numclust /
         * (clust.size() - emptyDot))); ego.setClustLowDot((double) (numclustLow
         * / (clust.size() - emptyLowDot))); ego.setClustTopDot((double)
         * (numclustTop / (clust.size() - emptyTopDot))); } else {
         * ego.setClustDot(0.0); ego.setClustLowDot(0.0);
         * ego.setClustTopDot(0.0); } }
         */
    }

    static void writeDegreeDistribution(UndirectedSparseGraph<Node, Edge> g, List<Node> nodes, String degreeDistributionPath) throws ExtensionException {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(degreeDistributionPath, false));
            for (Node t : nodes) {
                out.write(String.valueOf(g.degree(t) + " "));
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new ExtensionException(e);
        }
    }

    // RESTITUISCE UN 
    static HashMap<Double, Collection<Node>> findNodesWithDegree(List<Node> nodes) {
        HashMap<Double, Collection<Node>> degree = new HashMap<Double, Collection<Node>>();
        // store an array of nodes for each degree found in the network
        Iterator<Node> degreeIter = nodes.iterator();
        while (degreeIter.hasNext()) {
            Node ego = degreeIter.next();
            double deg = ego.getDegree();
            if (degree.containsKey(deg)) {
                Collection<Node> nodeWithDegree = degree.get(deg);
                nodeWithDegree.add(ego);
                degree.put(deg, nodeWithDegree);
            } else {
                ArrayList<Node> nodeWithDegree = new ArrayList<Node>();
                nodeWithDegree.add(ego);
                degree.put(deg, nodeWithDegree);
            }
        }
        return degree;
    }

    static void writeAttributesForZpa(UndirectedSparseGraph<Node, Edge> g, List<Node> usersLcc, List<Node> threadsLcc, String attributesForZpaPath) throws ExtensionException {

        // write results to file
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(attributesForZpaPath, false));
            for (Node n : usersLcc) {
                User u = (User) n;
                // THREAD
                out.write(u.getName() + ";" + u.getPostsDone() + ";" + u.getThreadsDone() + ";" + u.getZindex() + "\n");
            }
            for (Node n : threadsLcc) {
                Thread t = (Thread) n;
                // THREAD
                out.write(t.getName() + ";" + t.getStartedBy().getName() + ";" + t.getZindex() + ";" + t.getAppeal() + "\n");
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new ExtensionException(e);
        }
    }

    /**
     * NEW STUFF - GOES FROM HERE...
     */
    /**
     * For each key, or degree value found in the network, there will be a
     * collection of nodes with that degree value.
     *
     * @param nodes, the list of nodes to analyze
     * @return HashMap
     */
    static HashMap<Integer, Collection<Node>> findNodesForDegree(List<Node> nodes, Graph<Node, Edge> g) throws ExtensionException {

        // hashmap to hold the result 
        HashMap<Integer, Collection<Node>> result = new HashMap<Integer, Collection<Node>>();
        // for each node in the list
        for (Node n : nodes) {
            // find node's degree
            n.setDegree(g.degree(n));
            // placeholder
            Integer degree = n.getDegree();
            // if that degree is already present as a key in result
            if (result.containsKey(degree)) {
                // add n to the list of nodes that has that degree value
                boolean add = result.get(degree).add(n);
                // check
                if (!add) {
                    // raise exception
                    throw new ExtensionException("ERROR: failed to add node to list of nodes with degree " + degree);
                }
                // if that degree is not already present in result
            } else {
                // create a new empty collection of nodes
                List<Node> newList = new ArrayList<Node>();
                // add n as the first element in the new collection
                boolean add = newList.add(n);
                // check
                if (add) {
                    // add degree to the key and the collection of nodes with such degree
                    result.put(degree, newList);
                } else {
                    // raise exception
                    throw new ExtensionException("ERROR: failed to add node to list of nodes with degree " + degree);
                }
            }
        }
        // return result
        return result;
    }

    /**
     * Computers the number of distance 2 neighbours, |N (N (v))| These
     * statistics offer a way to study how node degrees in the projection
     * appear, and to distinguish between different behaviours.
     *
     * @param result, nodes for each degree values
     * @param counter, experiment number
     * @param path, file to write
     * @param g, the graph
     */
    static void writeNumberOfDistanceTwoNeighborsOfNodesWithDegree(HashMap<Integer, Collection<Node>> result, int counter, String path, Graph<Node, Edge> g) throws IOException {
        // sort degree keys in ascending order
        Set<Integer> keySet = result.keySet();
        ArrayList<Integer> keySetOrdered = new ArrayList<Integer>(keySet);
        Collections.sort(keySetOrdered);
        // if first experiment
        if (counter == 0) {
            // open writer
            BufferedWriter writeToFile = new BufferedWriter(new FileWriter(path, true));
            // for each degree in a partition
            for (Integer i : keySetOrdered) {
                // retrieve nodes with degree i
                Collection<Node> nodesWithDegree = result.get(i);
                // 
                HashSet<Node> twoDistNeiOfNodesWithDegree = numberTwoDistNei(nodesWithDegree, g);
                // find how many users have this value divided by total users
                writeToFile.append(i + "\t" + twoDistNeiOfNodesWithDegree.size());
                // add a new line
                writeToFile.newLine();
            }
            // close the writing process
            writeToFile.flush();
            writeToFile.close();
            // else is not the first experiment
        } else {
            // create a string builder which adds current values
            StringBuilder values = new StringBuilder();
            // read the file
            org.apache.commons.io.LineIterator it = org.apache.commons.io.FileUtils.lineIterator(new File(path));
            try {
                while (it.hasNext()) {
                    String line = it.nextLine();
                    // find degree of this line
                    String[] id = line.split("\t");
                    // retrieve nodes with degree i
                    Collection<Node> nodesWithDegree = result.get(Integer.valueOf(id[0]));
                    // 
                    HashSet<Node> twoDistNeiOfNodesWithDegree = numberTwoDistNei(nodesWithDegree, g);
                    // build the new string
                    values.append(line).append("\t").append(twoDistNeiOfNodesWithDegree.size()).append("\n");
                }
            } finally {
                it.close();
            }
            org.apache.commons.io.FileUtils.writeStringToFile(new File(path), values.toString());
        }
    }

    /**
     * Computes average degree of neighbours of nodes as a function of their
     * degree, both for top and bottom nodes, separately - in other words, for
     * each integer i we plot the average degree of all nodes which are
     * neighbours of a node of degree i.
     *
     * @param result, nodes for each degree values
     * @param counter, experiment number
     * @param path, file to write
     * @param g, the graph
     */
    static void writeAverageDegreeOfNeiOfNodesWithDegree(HashMap<Integer, Collection<Node>> result, int counter, String path, Graph<Node, Edge> g) throws IOException {
        // sort degree keys in ascending order
        Set<Integer> keySet = result.keySet();
        ArrayList<Integer> keySetOrdered = new ArrayList<Integer>(keySet);
        Collections.sort(keySetOrdered);
        // if first experiment
        if (counter == 0) {
            // open writer
            BufferedWriter writeToFile = new BufferedWriter(new FileWriter(path, true));
            // for each degree in a partition
            for (Integer i : keySetOrdered) {
                // retrieve nodes with degree i
                Collection<Node> nodesWithDegree = result.get(i);
                // average degree of neighbors of nodes with degree i
                double averageNeiDegree = 0;
                //
                HashMap<Node, Integer> neighborsOfNodesWithDegree = new HashMap<Node, Integer>();
                // for each node with degree i
                for (Node n : nodesWithDegree) {
                    // find neighbors of node
                    n.setNei(g.getNeighbors(n));
                    //
                    for (Node nn : n.getNei()) {
                        // 
                        neighborsOfNodesWithDegree.put(nn, nn.getDegree());
                    }
                }
                // 
                for (Integer ii : neighborsOfNodesWithDegree.values()) {
                    // 
                    averageNeiDegree += ii;
                }
                // 
                averageNeiDegree /= neighborsOfNodesWithDegree.size();
                // find how many users have this value divided by total users
                writeToFile.append(i + "\t" + averageNeiDegree);
                // add a new line
                writeToFile.newLine();
            }
            // close the writing process
            writeToFile.flush();
            writeToFile.close();
            // else if not the first experiment, then add just a column
        } else {
            // create a string builder which adds current values
            StringBuilder values = new StringBuilder();
            // read the file
            org.apache.commons.io.LineIterator it = org.apache.commons.io.FileUtils.lineIterator(new File(path));
            try {
                while (it.hasNext()) {
                    String line = it.nextLine();
                    // find degree of this line
                    String[] id = line.split("\t");
                    // retrieve nodes with degree i
                    Collection<Node> nodesWithDegree = result.get(Integer.valueOf(id[0]));
                    // average degree of neighbors of nodes with degree i
                    double averageNeiDegree = 0;
                    //
                    HashMap<Node, Integer> neighborsOfNodesWithDegree = new HashMap<Node, Integer>();
                    // for each node with degree i
                    for (Node n : nodesWithDegree) {
                        // find neighbors of node
                        n.setNei(g.getNeighbors(n));
                        //
                        for (Node nn : n.getNei()) {
                            // 
                            neighborsOfNodesWithDegree.put(nn, nn.getDegree());
                        }
                    }
                    // 
                    for (Integer ii : neighborsOfNodesWithDegree.values()) {
                        // 
                        averageNeiDegree += ii;
                    }
                    // 
                    averageNeiDegree /= neighborsOfNodesWithDegree.size();
                    // build the new string
                    values.append(line).append("\t").append(averageNeiDegree).append("\n");
                }
            } finally {
                it.close();
            }
            org.apache.commons.io.FileUtils.writeStringToFile(new File(path), values.toString());
        }

    }

    /**
     * Writes degree distributions to file.
     *
     * @param result, nodes for each degree values
     * @param size, how many nodes in this partition
     * @param counter, the experiment number
     * @param path, the file path
     */
    static void writeDegreeDistribution(HashMap<Integer, Collection<Node>> result, int size, int counter, String path) throws IOException {
        // sort degree keys in ascending order
        Set<Integer> keySet = result.keySet();
        ArrayList<Integer> keySetOrdered = new ArrayList<Integer>(keySet);
        Collections.sort(keySetOrdered);
        // if first experiment, write also degree values
        if (counter == 0) {
            // open writer
            BufferedWriter writeToFile = new BufferedWriter(new FileWriter(path, true));
            // for each degree in the network
            for (Integer i : keySetOrdered) {
                // find value
                float f = (float) result.get(i).size() / size;
                // find how many users have this value divided by total users
                writeToFile.append(i + "\t" + f);
                // add a new line
                writeToFile.newLine();
            }
            // close the writing process
            writeToFile.flush();
            writeToFile.close();
        } else {
            // create a string builder which adds current values
            StringBuilder values = new StringBuilder();
            // read the file
            org.apache.commons.io.LineIterator it = org.apache.commons.io.FileUtils.lineIterator(new File(path));
            try {
                while (it.hasNext()) {
                    String line = it.nextLine();
                    // find degree of this line
                    String[] id = line.split("\t");
                    // retrieve the value
                    float f = (float) result.get(Integer.valueOf(id[0])).size() / size;
                    // build the new string
                    values.append(line).append("\t").append(f).append("\n");
                }
            } finally {
                it.close();
            }
            org.apache.commons.io.FileUtils.writeStringToFile(new File(path), values.toString());
        }
    }

    /**
     * Writes redundancy values to file.
     *
     * @param result, nodes for each degree values
     * @param size, how many nodes in this partition
     * @param counter, the experiment number
     * @param path, the file path
     */
    static void writeRedundancyOfNodesWithDegree(HashMap<Integer, Collection<Node>> result, int size, int counter, String path) throws IOException {
        // sort degree keys in ascending order
        Set<Integer> keySet = result.keySet();
        ArrayList<Integer> keySetOrdered = new ArrayList<Integer>(keySet);
        Collections.sort(keySetOrdered);
        // if first experiment, write also degree values
        if (counter == 0) {
            // open writer
            BufferedWriter writeToFile = new BufferedWriter(new FileWriter(path, true));
            // for each degree in the network
            for (Integer i : keySetOrdered) {
                // holds redundancy for nodes with degree i
                double redundancyOfNodesWithDegree = 0;
                // nodes with this degree
                Collection<Node> nodesWithDegree = result.get(i);
                // for each node with degree i
                for (Node n : nodesWithDegree) {
                    // find redundancy
                    n.setRedundancy(redundancy(n));
                    // add value to global count of redundancy for nodes with degree i
                    redundancyOfNodesWithDegree += n.getRedundancy();
                }
                // find redundancy value
                redundancyOfNodesWithDegree /= nodesWithDegree.size();
                // find how many users have this value divided by total users
                writeToFile.append(i + "\t" + redundancyOfNodesWithDegree);
                // add a new line
                writeToFile.newLine();
            }
            // close the writing process
            writeToFile.flush();
            writeToFile.close();
        } else {
            // create a string builder which adds current values
            StringBuilder values = new StringBuilder();
            // read the file
            org.apache.commons.io.LineIterator it = org.apache.commons.io.FileUtils.lineIterator(new File(path));
            try {
                while (it.hasNext()) {
                    String line = it.nextLine();
                    // find degree of this line
                    String[] id = line.split("\t");
                    // redundancy for nodes with degree i
                    double redundancyOfNodesWithDegree = 0;
                    // nodes with this degree
                    Collection<Node> nodesWithDegree = result.get(Integer.valueOf(id[0]));
                    // for each of such nodes
                    for (Node n : nodesWithDegree) {
                        //
                        n.setRedundancy(redundancy(n));
                        //
                        redundancyOfNodesWithDegree += n.getRedundancy();
                    }
                    // find redundancy value
                    redundancyOfNodesWithDegree /= nodesWithDegree.size();
                    // build the new string
                    values.append(line).append("\t").append(redundancyOfNodesWithDegree).append("\n");
                }
            } finally {
                it.close();
            }
            org.apache.commons.io.FileUtils.writeStringToFile(new File(path), values.toString());
        }
    }

    /**
     * Find mean of values.
     *
     * @param p
     * @return double
     */
    private static double mean(ArrayList<Double> p) {
        double sum = 0;  // sum of all the elements
        Iterator<Double> pIter = p.iterator();
        while (pIter.hasNext()) {
            sum += pIter.next();
        }
        return (sum / (double) p.size());
    }

    /**
     * Finds if a pair of neighbors of n remains linked even if n is not there.
     *
     * @param nn, one of n's neighbor
     * @param nnn, another of n's neighbor
     * @param n, the referring node
     * @return int
     */
    private static int alreadyJoined(Node nn, Node nnn, Node n) {
        // find the nei of nn - which is one of n's neighbors
        Set<Node> nei = nn.getNei();
        // remove n from nei
        nei.remove(n);
        // find the nei of nnn - which is another one of my neighbors
        Set<Node> nei1 = nnn.getNei();
        // remove n from nei1
        nei1.remove(n);
        // find the intersection betwenn neighbors of nn and nnn
        Sets.SetView<Node> intersection = Sets.intersection(nei, nei1);
        // if the intersection is empty
        if (intersection.isEmpty()) {
            // n is the only thing that links nn and nnn
            return 0;
            // else if there is someone
        } else {
            // then n is not the only thing that links nn and nnn
            return 1;
        }
    }

    /**
     *
     */
    private static HashSet<Node> numberTwoDistNei(Collection<Node> nodesWithDegree, Graph<Node, Edge> g) {
        // 
        HashSet<Node> twoDistNeiOfNodesWithDegree = new HashSet<Node>();
        // for each node with degree i
        for (Node n : nodesWithDegree) {
            // for each neighbor of node with degree i
            for (Node nn : n.getNei()) {
                // find nei of nn
                Collection<Node> neiOfNn = g.getNeighbors(nn);
                // find 2-dist nei of n
                for (Node nnn : neiOfNn) {
                    // 
                    if (!nnn.equals(n)) {
                        // add 2-dist nei to n
                        n.setTwoDistNei(nnn);
                        // add nnn
                        twoDistNeiOfNodesWithDegree.add(nnn);
                    }
                }
            }
        }
        // 
        return twoDistNeiOfNodesWithDegree;
    }

    /**
     * 
     */
    private static double redundancy(Node n) {
        // results
        double overlap = 0;
        // retrieve neighbors of n
        Set<Node> nei = n.getNei();
        // for each pair of neighbors
        for (Node nn : nei) {
            for (Node nnn : nei) {
                // exclude a node asking to itself
                if (!nn.equals(nnn)) {
                    // 
                    overlap += alreadyJoined(nn, nnn, n);
                }
            }
        }
        double den;
        if (overlap > 0) {
            int neiOfN = n.getNei().size();
            den = neiOfN * (neiOfN - 1);

        } else {
            den = 1;
        }
        return overlap / den;
    }
}
