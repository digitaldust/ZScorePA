package org.nlogo.extensions.zscorepa;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
            for(Node n:nodesWithDegree){
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
                out.write(d + "\t" + zindexThread_hash.get((Double)d) + "\n");
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
            out.append("zindex\t" + "nei_zindex\t"+ "appeal\t" +"degree\t" + "nei_degree\n");
            for (Node n : vertices) {
                if(n.getColor().equals("red")){
                    Collection<Node> neighbors = g.getNeighbors(n);
                    for(Node t:neighbors){
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
     * Find mean of values.
     *
     * @param p
     * @return
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
         */
        Collection<Node> nodes = g.getVertices();
        Iterator<Node> nodesListIter = nodes.iterator();
        while (nodesListIter.hasNext()) {
            // ego - the caller
            Node ego = nodesListIter.next();
            // find neighbors of me
            Collection<Node> egoNei = g.getNeighbors(ego);
            // array for my neighbors degree
            ArrayList<Double> egoNeiDeg = new ArrayList<Double>();
            // hashmap for 2-dist-nei
            HashMap<Node, Integer> egoNeiOfNei = new HashMap<Node, Integer>();
            // redundancy hashmap
            HashMap<Node, Collection<Node>> redundancy = new HashMap<Node, Collection<Node>>();
            // find degrees of neighbors of me
            Iterator<Node> egoNeiIter = egoNei.iterator();
            while (egoNeiIter.hasNext()) {
                // alter - the caller now
                Node alter = egoNeiIter.next();
                // find degrees each alter of ego
                egoNeiDeg.add((double) g.degree(alter));
                // find neighbors of alter
                Collection<Node> twoDist = g.getNeighbors(alter);
                //
                redundancy.put(alter, twoDist);
                // find how many unique nodes in the same projection ego is linked with
                // - corresponds to the degree of ego in a bipartite projection
                Iterator<Node> twoDistIter = twoDist.iterator();
                while (twoDistIter.hasNext()) {
                    // add unique nei of nei to the hashmap
                    // save the number for redundancy
                    egoNeiOfNei.put(twoDistIter.next(), 1);
                }
            }
            // redundancy
            double num = 0;
            ArrayList<Node> Blist = new ArrayList<Node>(redundancy.keySet());
            Iterator<Node> redundancyIter = redundancy.keySet().iterator();
            while (redundancyIter.hasNext()) {
                Node A = redundancyIter.next();
                Blist.remove(A);
                Iterator<Node> newListIter = Blist.iterator();
                while (newListIter.hasNext()) {
                    Node B = newListIter.next();
                    Collection<Node> Anei = redundancy.get(A);
                    Collection<Node> Bnei = redundancy.get(B);
                    if ((Anei.size() > 0) && (Bnei.size() > 0)) {
                        if (!(disjoint(Anei, Bnei, ego))) {
                            num += 1;
                        }
                    }
                }
            }
            // set degree of caller
            ego.setDegree((double) g.degree(ego)); // QUESTO CI DOVREBBE GIà ESSERE PER USERS E THREADS
            // set ego's list of degree of neighbors
            ego.setNeiDeg(egoNeiDeg);
            // set ego's list of unique 2-dist-nei - corresponds to degree in the projection.
            egoNeiOfNei.remove(ego);
            ego.setTwoDistNei(egoNeiOfNei.keySet());
            // set ego's redundancy
            if (egoNei.size() > 1) {
                ego.setRedundancy((double) (num / ((egoNei.size() * (egoNei.size() - 1)) / 2)));
            } else {
                ego.setRedundancy((double) 0.05);
            }
            // set ego's clustering dot, lowDot and topDot
            double numclust = 0;
            double numclustLow = 0;
            double numclustTop = 0;
            double emptyDot = 0;
            double emptyLowDot = 0;
            double emptyTopDot = 0;
            Set<Node> clust = ego.getTwoDistNei();

            if (egoNei.size() > 0 && clust.size() > 0) {
                Iterator<Node> clustIter = clust.iterator();
                while (clustIter.hasNext()) {
                    Node clustNei = clustIter.next();
                    double dot = findCommons(egoNei, g.getNeighbors(clustNei));
                    numclust += dot;
                    if (dot == 0) {
                        emptyDot++;
                    }
                    double lowDot = findCommonsLow(egoNei, g.getNeighbors(clustNei));
                    numclustLow += lowDot;
                    if (lowDot == 0) {
                        emptyLowDot++;
                    }
                    double topDot = findCommonsTop(egoNei, g.getNeighbors(clustNei));
                    numclustTop += topDot;
                    if (topDot == 0) {
                        emptyTopDot++;
                    }
                }
            }
            if (clust.size() > 0) {
                ego.setClustDot((double) (numclust / (clust.size() - emptyDot)));
                ego.setClustLowDot((double) (numclustLow / (clust.size() - emptyLowDot)));
                ego.setClustTopDot((double) (numclustTop / (clust.size() - emptyTopDot)));
            } else {
                ego.setClustDot(0.0);
                ego.setClustLowDot(0.0);
                ego.setClustTopDot(0.0);
            }
        }
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
    static HashMap<Double, Collection<Node>> findDegreeDistribution(List<Node> nodes) {
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
}
