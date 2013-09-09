package org.nlogo.extensions.zscorepa;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

/**
 * This class manages the network analysis stuff - All the measures are
 * implemented from the Guillame...<inserisce articolo>
 *
 * @author Simone Gabbriellini
 */
public class Statistics extends DefaultCommand {

    List<User> users;
    List<Thread> threads;

    /**
     * Input parameters
     */
    @Override
    public Syntax getSyntax() {
        return Syntax.commandSyntax(new int[]{Syntax.StringType()});
    }

    /**
     * This method calculates the main descriptive stats for a bipatite network.
     */
    @Override
    public void perform(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {

        // initialzie network object
        UndirectedSparseGraph<Node, Edge> g = new UndirectedSparseGraph<Node, Edge>();
        // retrieve bipartite file name
        String forum = argmnts[0].getString();
        // read from
        String empiricalPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_bipartite_networks/" + forum + "-empirical.txt";
        String attributesPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_network_attributes/" + forum + "-empirical-attributes.txt";
        String startedbyPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_network_attributes/" + forum + "-empirical-startedby.txt";
        // write for random experiment
        String usersDegreesPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_degrees_empiric_networks/users-degree-" + forum + "-empirical.txt";
        String threadsDegreePath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_degrees_empiric_networks/threads-degree-" + forum + "-empirical.txt";
        // write for zpa experiment
        String attributesForZpaPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_network_attributes/" + forum + "-empirical-zpa-attributes.txt";
        // write for stats
        String usersStatsPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/EXPERIMENTS/" + forum + "/users-stats-" + forum + "-empirical.txt";
        String threadsStatsPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/EXPERIMENTS/" + forum + "/threads-stats-" + forum + "-empirical.txt";
        String activityPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/EXPERIMENTS/" + forum + "/users-activity-" + forum + "-empirical.txt";
        String threadactivityPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/EXPERIMENTS/" + forum + "/thread-activity-" + forum + "-empirical.txt";
        // arraylist to hold users
        users = new ArrayList<User>();
        // arraylist to hold threads
        threads = new ArrayList<Thread>();
        // hold nodes' names
        Set<String> names = new HashSet<String>();
        // read file from _bipartite_networks folder
        BufferedReader netFile;
        BufferedReader attributesFile;
        BufferedReader startedbyFile;
        try {
            netFile = new BufferedReader(new FileReader(empiricalPath));
            attributesFile = new BufferedReader(new FileReader(attributesPath));
            startedbyFile = new BufferedReader(new FileReader(startedbyPath));
            String line;
            while ((line = netFile.readLine()) != null) {
                String[] split = line.split(";");
                String userName = split[0];
                String threadName = split[1];
                if (names.contains(userName) && names.contains(threadName)) {
                    User u = getUser(userName);
                    Thread t = getThread(threadName);
                    // just add a new edge between user and thread
                    g.addEdge(new Edge(u, t), u, t, EdgeType.UNDIRECTED);
                } else if (!names.contains(userName) && !names.contains(threadName)) {
                    // create each of them and then 
                    User u = new User();
                    u.setId(Integer.valueOf(split[0].substring(1)));
                    u.setColor("red");
                    u.setName(split[0]);
                    Thread t = new Thread();
                    t.setId(Integer.valueOf(split[1].substring(1)));
                    t.setColor("blue");
                    t.setName(split[1]);
                    // add to nodes lists
                    users.add(u);
                    threads.add(t);
                    // add nodes
                    g.addVertex(u);
                    g.addVertex(t);
                    // add the edge
                    g.addEdge(new Edge(u, t), u, t, EdgeType.UNDIRECTED);
                    // update names
                    names.add(userName);
                    names.add(threadName);
                } else if(!names.contains(userName) && names.contains(threadName)) {
                    // create each of them and then 
                    User u = new User();
                    u.setId(Integer.valueOf(split[0].substring(1)));
                    u.setColor("red");
                    u.setName(split[0]);
                    Thread t = getThread(threadName);
                    // add to nodes lists
                    users.add(u);
                    // add nodes
                    g.addVertex(u);
                    // add the edge
                    g.addEdge(new Edge(u, t), u, t, EdgeType.UNDIRECTED);
                    // update names
                    names.add(userName);
                } else {
                    User u = getUser(userName);
                    Thread t = new Thread();
                    t.setId(Integer.valueOf(split[1].substring(1)));
                    t.setColor("blue");
                    t.setName(split[1]);
                    // add to nodes lists
                    threads.add(t);
                    // add nodes
                    g.addVertex(t);
                    // add the edge
                    g.addEdge(new Edge(u, t), u, t, EdgeType.UNDIRECTED);
                    // update names
                    names.add(threadName);
                }
            }
            netFile.close();
            // add post, thread, zindex and appeal attributes to the empirical network
            String attr;
            while ((attr = attributesFile.readLine()) != null) {
                final String[] splitUser = attr.split(";");
                for (User u : users) {
                    if (u.getName().equals(splitUser[0])) {
                        u.setPostsDone(Double.valueOf(splitUser[1]));
                        u.setThreadsDone(Double.valueOf(splitUser[2]));
                        double zindex = (u.getPostsDone() - u.getThreadsDone()) / Math.sqrt(u.getPostsDone() + u.getThreadsDone());
                        u.setZindex(Double.valueOf(zindex));
                    }
                }
            }
            attributesFile.close();
            String start;
            while ((start = startedbyFile.readLine()) != null) {
                final String[] splitThread = start.split(";");
                for (Thread t : threads) {
                    if (t.getName().equals(splitThread[1])) {
                        for (User u : users) {
                            if (u.getName().equals(splitThread[0])) {
                                t.setStartedBy(u);
                                t.setZindex(u.getZindex());
                                t.setPosts(Double.valueOf(splitThread[2]));
                                t.setAppeal(1 + (t.getPosts() / g.degree(t)));
                            }
                        }
                    }
                }
            }
            startedbyFile.close();
            System.out.println("g has " + g.getVertexCount() + " vertici");
            System.out.println("g has " + g.getEdgeCount() + " links");
            System.out.println("g has " + users.size() + " users");
            System.out.println("g has " + threads.size() + " threads");
        } catch (FileNotFoundException ex) {
            throw new ExtensionException(ex);
        } catch (IOException e) {
            throw new ExtensionException(e);
        }
        // EXTRACT LARGEST COMPONENT
        WeakComponentClusterer<Node, Edge> clusterer = new WeakComponentClusterer<Node, Edge>();
        Set<Set<Node>> clusterset = clusterer.transform(g);
        Set<Node> largest = Collections.EMPTY_SET;
        for (Set<Node> cluster : clusterset) {
            if (cluster.size() > largest.size()) {
                largest = cluster;
            }
        }
        UndirectedSparseGraph<Node, Edge> lcc = FilterUtils.createInducedSubgraph(largest, g);
        Collection<Node> nodesLcc = lcc.getVertices();
        List<Node> usersLcc = new ArrayList<Node>();
        List<Node> threadsLcc = new ArrayList<Node>();
        Iterator<Node> nodesLccIter = nodesLcc.iterator();
        while (nodesLccIter.hasNext()) {
            Node node = nodesLccIter.next();
            if (node.getColor().matches("red")) {
                usersLcc.add(node);
            } else {
                threadsLcc.add(node);
            }
        }
        // ANALISI STATISTICA SOLO DELLA LCC!!! SVEGLIA PERO'
        System.out.println("lcc has " + lcc.getVertexCount() + " vertici");
        System.out.println("lcc has " + usersLcc.size() + " user");
        System.out.println("lcc has " + threadsLcc.size() + " threads");
        System.out.println("WRITE DEGREE DISTRIBUTION FILE TO FEED RANDOM GENERATOR");
        WriteStats.writeDegreeDistribution(lcc, usersLcc, usersDegreesPath);
        WriteStats.writeDegreeDistribution(lcc, threadsLcc, threadsDegreePath);
        System.out.println("WRITE ATTRIBUTES FILE TO FEED ZPA GENERATOR");
        WriteStats.writeAttributesForZpa(lcc, usersLcc, threadsLcc, attributesForZpaPath);
        // TERMINAL OUTPUT
        System.out.println("CALCULATE STATS FOR EACH NODES");
        // CALCULATE STATS FOR EACH NODES
        WriteStats.findValues(lcc);
        // TERMINAL OUTPUT
        System.out.println("FIND USERS DEGREE DISTRIBUTION");
        // FIND USERS DEGREE DISTRIBUTION
        HashMap<Double, Collection<Node>> usersLccDegreeDistr = WriteStats.findDegreeDistribution(usersLcc);
        // TERMINAL OUTPUT
        System.out.println("WRITE USERS STATS TO FILE");
        // WRITE USERS STATS TO FILE
        WriteStats.writeBipartiteStats(usersLccDegreeDistr, usersLcc.size(), usersStatsPath);
        // TERMINAL OUTPUT
        System.out.println("FIND THREADS DEGREE DISTRIBUTION");
        // FIND THREADS DEGREE DISTRIBUTION
        HashMap<Double, Collection<Node>> threadsLccDegreeDistr = WriteStats.findDegreeDistribution(threadsLcc);
        // TERMINAL OUTPUT
        System.out.println("WRITE THREADS STATS TO FILE");
        // WRITE USERS STATS TO FILE
        WriteStats.writeBipartiteStats(threadsLccDegreeDistr, threadsLcc.size(), threadsStatsPath);
        // TERMINAL OUTPUT
        System.out.println("EMPIRICAL ACTIVITY ANALYSYS");
        // WRITE ACTIVITY TO FILE
        WriteStats.writeActivity(lcc, usersLccDegreeDistr, threadsLccDegreeDistr, activityPath, threadactivityPath);
        // TERMINAL ACTIVITY
        System.out.println("EMPIRICAL ANALYSIS DONE");
    }

    private User getUser(String s) {
        for (User n : users) {
            if (n.getName().equals(s)) {
                return n;
            }
        }
        return null;
    }

    private Thread getThread(String s) {
        for (Thread n : threads) {
            if (n.getName().equals(s)) {
                return n;
            }
        }
        return null;
    }
}
