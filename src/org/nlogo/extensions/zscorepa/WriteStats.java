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
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

/**
 *
 * @author Simone Gabbriellini
 */
public class WriteStats {

    // holds all the interactions between neighborhoods to speed up computation
    static HashMap<String, Integer> intersections = new HashMap<String, Integer>();
    static HashMap<String, Integer> unions = new HashMap<String, Integer>();
    static HashMap<String, Integer> minimums = new HashMap<String, Integer>();
    static HashMap<String, Integer> maximums = new HashMap<String, Integer>();

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
     * NEW STUFF - GOES FROM HERE...
     */
    /**
     * For each key, or degree value found in the network, there will be a
     * collection of nodes with that degree value - OK
     *
     * @param nodes, the list of nodes to analyze
     * @return HashMap
     */
    static HashMap<Integer, Collection<Node>> findNodesForDegree(List<Node> nodes, Graph<Node, Edge> g) throws ExtensionException {

        // hashmap to hold the result 
        HashMap<Integer, Collection<Node>> result = new HashMap<Integer, Collection<Node>>();
        // for each node in the list
        for (Node n : nodes) {
            // find node's neighbors
            n.setNei(g.getNeighbors(n));
            // find node's degree
            n.setDegree(n.getNei().size());
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
     * Writes to file the number of distance two neighbours for nodes with
     * degree i - OK.
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
                // finds the number of 2-dist neighbors
                Integer size = numberOfTwoDistNei(nodesWithDegree, g);
                // find how many users have this value divided by total users
                writeToFile.append(i + "\t" + size.toString());
                // add a new line
                writeToFile.newLine();
            }
            // flush the writing stream
            writeToFile.flush();
            // flush the buffer
            writeToFile.close();
            // else is not the first experiment
        } else {
            // create a string builder which adds current values
            StringBuilder values = new StringBuilder();
            // read the file
            org.apache.commons.io.LineIterator it = org.apache.commons.io.FileUtils.lineIterator(new File(path));
            try {
                // while there are lines to read
                while (it.hasNext()) {
                    String line = it.nextLine();
                    // find degree of this line
                    String[] id = line.split("\t");
                    // retrieve nodes with degree i
                    Collection<Node> nodesWithDegree = result.get(Integer.valueOf(id[0]));
                    // finds the number of 2-dist neighbors
                    Integer size = numberOfTwoDistNei(nodesWithDegree, g);
                    // build the new string
                    values.append(line).append("\t").append(size.toString()).append("\n");
                }
            } finally {
                // close the line iterator
                it.close();
            }
            // write the new file
            org.apache.commons.io.FileUtils.writeStringToFile(new File(path), values.toString());
        }
    }

    /**
     * Computes average degree of neighbours of nodes as a function of their
     * degree, both for top and bottom nodes, separately - in other words, for
     * each integer i we plot the average degree of all nodes which are
     * neighbours of a node of degree i - OK.
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
                Double averageNeiDegree = averageDegreeOfNei(nodesWithDegree, g);
                // find how many users have this value divided by total users
                writeToFile.append(i + "\t" + averageNeiDegree.toString());
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
                    Double averageNeiDegree = averageDegreeOfNei(nodesWithDegree, g);
                    // build the new string
                    values.append(line).append("\t").append(averageNeiDegree.toString()).append("\n");
                }
            } finally {
                it.close();
            }
            org.apache.commons.io.FileUtils.writeStringToFile(new File(path), values.toString());
        }

    }

    /**
     * Writes degree distributions to file - OK.
     *
     * @param result, nodes for each degree values
     * @param size, how many nodes in this partition
     * @param counter, the experiment number
     * @param path, the file path
     */
    static void writeDegreeDistribution(HashMap<Integer, Collection<Node>> result, int size, int counter, String path, String method) throws IOException {
        // sort degree keys in ascending order
        Set<Integer> keySet = result.keySet();
        ArrayList<Integer> keySetOrdered = new ArrayList<Integer>(keySet);
        Collections.sort(keySetOrdered);
        // if first experiment, write also degree values
        if (counter == 0) {
            //<editor-fold defaultstate="collapsed" desc="WRITE FIRST TIME">
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
            // flust the buffer
            writeToFile.flush();
            // close the buffer
            writeToFile.close();
            //</editor-fold>
        } else {
            // if we do not have a fixed degree list
            if (method.equals("links")) {
                //<editor-fold defaultstate="collapsed" desc="WRITE TO FILE LINKS GENERATED NETWORK">
                // create a string builder which adds current values
                StringBuilder values = new StringBuilder();
                // read again file, this time to find the right place where to write results
                org.apache.commons.io.LineIterator it = org.apache.commons.io.FileUtils.lineIterator(new File(path));
                // retrieve all the degrees already written to the file
                try {
                    // 
                    while (it.hasNext()) {
                        // next line in the file line iterator 
                        String line = it.nextLine();
                        // find degree of this line
                        String[] id = line.split("\t");
                        // 
                        Integer valueOf = Integer.valueOf(id[0]);
                        // finds if this network contains an already written degree
                        if (keySetOrdered.contains(valueOf)) {
                            // retrieve the value
                            float f = (float) result.get(Integer.valueOf(id[0])).size() / size;
                            // build the new string
                            values.append(line).append("\t").append(f).append("\n");
                            // remove this degree from the list 
                            keySetOrdered.remove(valueOf);
                        }
                    }
                    // 
                    for(Integer i:keySetOrdered){
                        // 
                        values.append(i).append("\t");
                        // as many zeroes as the last experiments minus this one, where we have the value
                        for(int c=1;c<counter;c++){
                           // 
                            values.append("0").append("\t");
                        }
                        // find value
                        float f = (float) result.get(i).size() / size;
                        // close the line
                        values.append(f).append("\n");
                    }
                // finally
                } finally {
                    // close the line iterator
                    it.close();
                }
                // write the new file
                org.apache.commons.io.FileUtils.writeStringToFile(new File(path), values.toString());
                //</editor-fold>
            // else if we have a fixed degree list, old approach works fine
            } else {
                //<editor-fold defaultstate="collapsed" desc="WRITE TO FILE FOR FIXED DEGREE DISTRIBUTION">
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
                // write the new file
                org.apache.commons.io.FileUtils.writeStringToFile(new File(path), values.toString());
                //</editor-fold>
            }
        }
    }

    /**
     * Writes redundancy values to file - OK.
     *
     * @param result, nodes for each degree values
     * @param size, how many nodes in this partition
     * @param counter, the experiment number
     * @param path, the file path
     */
    static void writeRedundancyOfNodesWithDegree(HashMap<Integer, Collection<Node>> result, int size, int counter, String path) throws IOException, ExtensionException {
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
                // nodes with this degree
                Collection<Node> nodesWithDegree = result.get(i);
                // holds redundancy for nodes with degree i
                Double redundancy = redundancy(nodesWithDegree);
                // find how many users have this value divided by total users
                writeToFile.append(i + "\t" + redundancy.toString());
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
                    // nodes with this degree
                    Collection<Node> nodesWithDegree = result.get(Integer.valueOf(id[0]));
                    // holds redundancy for nodes with degree i
                    Double redundancy = redundancy(nodesWithDegree);
                    // build the new string
                    values.append(line).append("\t").append(redundancy.toString()).append("\n");
                }
            } finally {
                it.close();
            }
            org.apache.commons.io.FileUtils.writeStringToFile(new File(path), values.toString());
        }
    }

    /**
     * Reports the number of 2-dist neighbors for nodes with degree i - OK.
     *
     * @param nodesWithDegree, a collection of nodes with degree i
     * @param g, the graph
     * @return Integer
     */
    private static Integer numberOfTwoDistNei(Collection<Node> nodesWithDegree, Graph<Node, Edge> g) {
        // holds *unique* 2-dist neighbors of nodes with degree n
        HashSet<Node> twoDistNeiOfNodesWithDegree = new HashSet<Node>();
        // for each node with degree i
        for (Node n : nodesWithDegree) {
            // retrieve 1-dist nei
            Set<Node> nei = n.getNei();
            // holds *unique* 2-dist neighbors of n
            HashSet<Node> twoDistNeiOfN = new HashSet<Node>();
            // for each 1-dist neighbor
            for (Node nn : nei) {
                // find nei of nn
                Collection<Node> neiOfNn = g.getNeighbors(nn);
                // for each nei of nn
                for (Node nnn : neiOfNn) {
                    // try to add it to the list, if it is not present already and if it's not n itself
                    if (!n.equals(nnn)) {
                        boolean add = twoDistNeiOfN.add(nnn);
                        // if 2-dist nei has been added, then
                        if (add) {
                            // increment size of 2-dist neighbors of nodes with degree i
                            twoDistNeiOfNodesWithDegree.add(nnn);
                        }
                    }
                }
            }
            // add all the 2-dist nei to n
            n.setAllTwoDistNei(twoDistNeiOfN);
        }
        // 
        return twoDistNeiOfNodesWithDegree.size();
    }

    /**
     * Finds redundancy for each of the nodes with degree i - OK.
     *
     * @param nodesWithDegree, collection of nodes with degree i
     * @throws ExtensionException
     * @return the redundancy value
     */
    private static Double redundancy(Collection<Node> nodesWithDegree) throws ExtensionException {
        // holds redundancy values for nodes with degree i
        Double redundancyOfNodesWithDegree = 0.0;
        // for each node with degree i
        for (Node n : nodesWithDegree) {
            // overlap between pairs of n's neighbors
            int overlap = 0;
            // Create the initial vector
            ICombinatoricsVector<Node> initialVector = Factory.createVector(n.getNei());
            // Create a simple combination generator to generate 2-combinations of the initial vector
            Generator<Node> gen = Factory.createSimpleCombinationGenerator(initialVector, 2);
            // for each possible combination
            for (ICombinatoricsVector<Node> s : gen) {
                // check if the two nodes are linked even if n is not there
                overlap += alreadyJoined(s.getValue(0), s.getValue(1), n);
            }
            // maximum number of pairs
            double den;
            // if there is at least an overlap
            if (overlap > 0) {
                // then
                int neiOfN = n.getNei().size();
                //
                den = neiOfN * (neiOfN - 1);
                // if no overlap, then redundancy is 0 and if n is not there, the graph would look 
                // much sparse in the one-mode projection
            } else {
                // 
                den = 1;
            }
            // find redundancy
            n.setRedundancy(overlap / den);
            // add value to global count of redundancy for nodes with degree i
            redundancyOfNodesWithDegree += n.getRedundancy();
        }
        // find average redundancy value for nodes with degree i
        redundancyOfNodesWithDegree /= nodesWithDegree.size();
        // return the value
        return redundancyOfNodesWithDegree;
    }

    /**
     * Finds if a pair of neighbors of n remains linked even if n is not there -
     * OK.
     *
     * @param nn, one of n's neighbor
     * @param nnn, another of n's neighbor
     * @param n, the referring node
     * @throws ExtensionException
     * @return int
     */
    private static int alreadyJoined(Node nn, Node nnn, Node n) throws ExtensionException {
        // find the nei of nn, one of n's neighbors
        Set<Node> nei = nn.getNei();
        // find the nei of nnn, another one of n's neighbors
        Set<Node> nei1 = nnn.getNei();
        // if check
        if (!nei.isEmpty() && !nei1.isEmpty()) {
            // find the intersection betwenn neighbors of nn and nnn
            Sets.SetView<Node> intersection = Sets.intersection(nei, nei1);
            // if the intersection is empty
            if (intersection.isEmpty()) {
                // n is the only thing that links nn and nnn
                return 0;
                // else if there is someone
            } else {
                // 
                int size = intersection.size();
                // 
                if (intersection.contains(n)) {
                    size--;
                }
                if (size > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } else {
            // both are empty, so n is the only link between u and w
            return 0;
        }
    }

    /**
     * Finds for every degree i, the average degree of the neighbors of the
     * nodes with degree i - OK.
     *
     * @param nodesWithDegree, the nodes with degree i
     * @param g, the graph
     * @return a Double, the average degree value for the neighbors of this set
     * of nodes.
     */
    static Double averageDegreeOfNei(Collection<Node> nodesWithDegree, Graph<Node, Edge> g) {
        // holds number of neighbors of nodes with degree i
        int averageDegree = 0;
        int size = 0;
        // holds the degree values of neighbors of nodes with degree i.
        // we need an hashmap because we want to avoid adding multiple times the same node just because it is neighbor
        // of more than one node.
        HashMap<Node, Integer> degreeOfNeighbors = new HashMap<Node, Integer>();
        // for each node with degree i
        for (Node n : nodesWithDegree) {
            // retrieve node's neighbors
            Set<Node> nei = n.getNei();
            // for each neighbor, save its degree
            for (Node nn : nei) {
                // neighbor's degree
                Integer degree = nn.getDegree();
                // add to the list of my neighbors
                n.setNeiDegree(degree);
                // add this nei to the list of neighbors of nodes with degree i
                degreeOfNeighbors.put(nn, degree);
            }
        }
        // find
        for (Integer ii : degreeOfNeighbors.values()) {
            // sum values
            averageDegree += ii;
            // update numbers
            size++;
        }
        // return average degree of neighbors of nodes with degree i
        return Double.valueOf((double) averageDegree / size);
    }

    /**
     * Writes to file the CC coefficient for nodes with degree i - OK.
     *
     * @param result
     * @param size
     * @param counter
     * @param path
     * @throws IOException
     */
    static void writeCCsOfNodesWithDegree(HashMap<Integer, Collection<Node>> result, int size, int counter, String pathCC, String pathCCtop, String pathCClow) throws IOException {
        // sort degree keys in ascending order
        Set<Integer> keySet = result.keySet();
        ArrayList<Integer> keySetOrdered = new ArrayList<Integer>(keySet);
        Collections.sort(keySetOrdered);
        // if first experiment, write also degree values
        if (counter == 0) {
            // open writer
            BufferedWriter writeCC = new BufferedWriter(new FileWriter(pathCC, true));
            BufferedWriter writeCCtop = new BufferedWriter(new FileWriter(pathCCtop, true));
            BufferedWriter writeCClow = new BufferedWriter(new FileWriter(pathCClow, true));
            // for each degree in the network
            for (Integer i : keySetOrdered) {
                // nodes with this degree
                Collection<Node> nodesWithDegree = result.get(i);
                // holds redundancy for nodes with degree i
                Double[] CC = findCCs(nodesWithDegree);
                // find how many users have this value divided by total users
                writeCC.append(i + "\t" + CC[0].toString());
                // add a new line
                writeCC.newLine();
                // find how many users have this value divided by total users
                writeCCtop.append(i + "\t" + CC[1].toString());
                // add a new line
                writeCCtop.newLine();
                // find how many users have this value divided by total users
                writeCClow.append(i + "\t" + CC[2].toString());
                // add a new line
                writeCClow.newLine();
            }
            // close the writing process
            writeCC.flush();
            writeCC.close();
            writeCCtop.flush();
            writeCCtop.close();
            writeCClow.flush();
            writeCClow.close();
        } else {
            // create a string builder which adds current values
            StringBuilder valuesCC = new StringBuilder();
            StringBuilder valuesCCtop = new StringBuilder();
            StringBuilder valuesCClow = new StringBuilder();
            // read the file
            org.apache.commons.io.LineIterator itCC = org.apache.commons.io.FileUtils.lineIterator(new File(pathCC));
            org.apache.commons.io.LineIterator itCCtop = org.apache.commons.io.FileUtils.lineIterator(new File(pathCCtop));
            org.apache.commons.io.LineIterator itCClow = org.apache.commons.io.FileUtils.lineIterator(new File(pathCClow));
            try {
                while (itCC.hasNext()) {
                    String line = itCC.nextLine();
                    // find degree of this line
                    String[] id = line.split("\t");
                    // nodes with this degree
                    Collection<Node> nodesWithDegree = result.get(Integer.valueOf(id[0]));
                    // holds redundancy for nodes with degree i
                    Double[] CCs = findCCs(nodesWithDegree);
                    // build the new string
                    valuesCC.append(line).append("\t").append(CCs[0].toString()).append("\n");
                    valuesCCtop.append(line).append("\t").append(CCs[1].toString()).append("\n");
                    valuesCClow.append(line).append("\t").append(CCs[2].toString()).append("\n");
                }
            } finally {
                itCC.close();
                itCCtop.close();
                itCClow.close();
            }
            org.apache.commons.io.FileUtils.writeStringToFile(new File(pathCC), valuesCC.toString());
            org.apache.commons.io.FileUtils.writeStringToFile(new File(pathCCtop), valuesCCtop.toString());
            org.apache.commons.io.FileUtils.writeStringToFile(new File(pathCClow), valuesCClow.toString());
        }
    }

    /**
     * Finds the CCs coefficients for nodes with degree i - OK.
     *
     * @param nodesWithDegree, a collection of nodes with degree i
     * @return Double
     */
    static Double[] findCCs(Collection<Node> nodesWithDegree) {
        // holds value of CC, CCtop and CClow respectively for nodes with degree i
        Double[] CC = new Double[]{0.0, 0.0, 0.0};
        // size of the group of nodes with degree i
        int size = nodesWithDegree.size();
        // for each node with degree i
        for (Node n : nodesWithDegree) {
            // retrieve nei
            Set<Node> nei = n.getNei();
            // retrieve 2-dist nei
            Set<Node> twoDistNei = n.getTwoDistNei();
            // if n does not have 2-dist nei, then CCs are not defined
            if (twoDistNei.isEmpty()) {
                // set value
                n.setCC(Double.NaN);
                n.setCCtop(Double.NaN);
                n.setCClow(Double.NaN);
                // exclude this nodes from the average
                size--;
            } else {
                // holds CC value for node n
                Double[] nCC = new Double[]{0.0, 0.0, 0.0};
                // size of 2-dist nei of n
                int size1 = twoDistNei.size();
                // for each nei
                for (Node nn : twoDistNei) {
                    // find unique key for this couple
                    String key;
                    if (n.getId() < nn.getId()) {
                        key = n.getId() + "-" + nn.getId();
                    } else {
                        key = nn.getId() + "-" + n.getId();
                    }
                    //
                    int union;
                    // 
                    int unionLow;
                    // 
                    int unionTop;
                    //
                    int intersection;
                    // if this intersection has been computed already
                    if (intersections.containsKey(key)) {
                        // then just retrieve the value for this intersection
                        intersection = intersections.get(key);
                        union = unions.get(key);
                        unionLow = minimums.get(key);
                        unionTop = maximums.get(key);
                        // otherwise...
                    } else {
                        // find 2-dist nei neighbors
                        Set<Node> nei1 = nn.getNei();
                        // find intersection size of n's and nn's neighborhoods
                        intersection = Sets.intersection(nei, nei1).size();
                        // add the new value to the hash map
                        intersections.put(key, intersection);
                        // find intersection size of n's and nn's neighborhoods
                        union = Sets.union(nei, nn.getNei()).size();
                        // 
                        unionLow = Math.min(nei.size(), nei1.size());
                        // 
                        unionTop = Math.max(nei.size(), nei1.size());
                        // add the new value to the hash map
                        unions.put(key, union);
                        // add the new value to the hash map
                        minimums.put(key, unionLow);
                        // add the new value to the hash map
                        maximums.put(key, unionTop);
                    }
                    // if there is no overlap
                    if (intersection == 0) {
                        // exclude this pair from the average
                        size1--;
                        // at least one overlap in the neighborhoods
                    } else {
                        // set nCC value
                        nCC[0] += (double) intersection / union;
                        // set nCCtop value
                        nCC[1] += (double) intersection / unionTop;
                        // set nCClow value
                        nCC[2] += (double) intersection / unionLow;
                    }
                }
                // average CC over 2-dist nei of n
                nCC[0] /= size1;
                // average CCtop over 2-dist nei of n
                nCC[1] /= size1;
                // average CClow over 2-dist nei of n
                nCC[2] /= size1;
                // add value
                n.setCC(nCC[0]);
                n.setCCtop(nCC[1]);
                n.setCClow(nCC[2]);
                // increment CC value for nodes with degree i
                CC[0] += nCC[0];
                CC[1] += nCC[1];
                CC[2] += nCC[2];
            }
        }
        CC[0] /= size;
        CC[1] /= size;
        CC[2] /= size;
        return CC;
    }
}
