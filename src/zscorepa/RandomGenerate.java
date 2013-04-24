/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zscorepa;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

/**
 *
 * @author Simone Gabbriellini
 */
public class RandomGenerate extends DefaultCommand {

    @Override
    public Syntax getSyntax() {
        return Syntax.commandSyntax(new int[]{Syntax.StringType(), Syntax.StringType(), Syntax.NumberType()});
    }

    @Override
    public void perform(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {

        // forum to be replicated
        String forumName = argmnts[0].getString();
        // links or degree distribution
        String method = argmnts[1].getString();
        // experiment number
        double expNum = argmnts[2].getDoubleValue();
        // network file path
        String networkPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_bipartite_networks/" + forumName + "-" + method + "-random.txt";
        String usersDegreePath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_degrees_empiric_networks/users-degree-" + forumName + "-empirical.txt";
        String threadsDegreePath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_degrees_empiric_networks/threads-degree-" + forumName + "-empirical.txt";
        // for each experiment
        for (int exp = 0; exp < expNum; exp++) {
            int expp = exp + 1;
            System.out.println("START RANDOM EXPERIMENT " + expp);
            // this experiment paths
            String usersStatsPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/experiments/" + forumName + "/EXP " + expp + "/users-stats-" + forumName + "-" + method + "-random.txt";
            String threadsStatsPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/experiments/" + forumName + "/EXP " + expp + "/threads-stats-" + forumName + "-" + method + "-random.txt";
            // built an empty network first
            UndirectedSparseGraph<Node, Edge> rand = new UndirectedSparseGraph<Node, Edge>();
            // retrieve the right degree files
            BufferedReader inUsers;
            BufferedReader inThreads;
            try {
                inUsers = new BufferedReader(new FileReader(usersDegreePath));
                inThreads = new BufferedReader(new FileReader(threadsDegreePath));
            } catch (FileNotFoundException ex) {
                // throw exception in NetLogo
                throw new ExtensionException(ex);
            }
            // arrays to hold users and threads
            ArrayList<Node> users = new ArrayList<Node>();
            ArrayList<Node> threads = new ArrayList<Node>();
            // total amount of links to build if in 'link' mode
            double totalLinks = 0;
            double checkThreads = 0;
            String readLine;
            try {
                readLine = inUsers.readLine();
                String[] splitUser = readLine.split(" ");
                int id = 0;
                for (int i = 0; i < splitUser.length; i++) {
                    Node user = new Node();
                    user.setId(id);
                    user.setColor("red");
                    user.setName("u" + id);
                    user.setDegree(Double.valueOf(splitUser[i]));
                    totalLinks += user.getDegree();
                    id++;
                    users.add(user);
                    rand.addVertex(user);
                }
                readLine = inThreads.readLine();
                String[] splitThread = readLine.split(" ");
                id = 0;
                for (int i = 0; i < splitThread.length; i++) {
                    Node thread = new Node();
                    thread.setId(id);
                    thread.setColor("blue");
                    thread.setName("t" + id);
                    thread.setDegree(Double.valueOf(splitThread[i]));
                    checkThreads += thread.getDegree();
                    id++;
                    threads.add(thread);
                    rand.addVertex(thread);
                }
                // close readers
                inUsers.close();
                inThreads.close();
            } catch (IOException ex) {
                throw new ExtensionException(ex);
            }
//            System.out.println(forumName + " has " + totalLinks + " and " + checkThreads);
            // decide if links or degree-based generator
            if (method.equals("links")) {
                while (totalLinks > 0) {
                    Node u = users.get(cntxt.getRNG().nextInt(users.size()));
                    Node t = extractRandom(threads, u, cntxt, rand);
                    Edge randomEdge = new Edge();
                    randomEdge.setFromid(u.getName());
                    randomEdge.setToid(t.getName());
                    rand.addEdge(randomEdge, u, t, EdgeType.UNDIRECTED);
                    totalLinks--;
                }
            } else {
                // add edges correspondig to degree distribution
                // first people first... :)
                Collections.sort(users,
                        new Comparator<Node>() {
                    @Override
                    public int compare(Node o1, Node o2) {
                        Double deg1 = o1.getDegree(), deg2 = o2.getDegree();
                        int cmp = deg2.compareTo(deg1);
                        if (cmp == 0) {
                            Integer id1 = o1.getId(), id2 = o2.getId();
                            cmp = id2.compareTo(id1);
                        }
                        return cmp;
                    }
                    // end compare
                });
                for (Node user : users) {
                    if (threads.size() >= 0) {
//                        System.out.println("available threads " + threads.size());
                        ArrayList<Node> someThreads = giveNode(user, threads, rand);
                        Iterator<Node> iterator = someThreads.iterator();
                        while (iterator.hasNext()) {
                            Node thread = iterator.next();
                            Edge randomEdge = new Edge();
                            randomEdge.setFromid(user.getName());
                            randomEdge.setToid(thread.getName());
                            rand.addEdge(randomEdge, user, thread, EdgeType.UNDIRECTED);
                            totalLinks--;
                            //System.out.println("users is " + user.getId() + " has degree " + rand.degree(user) + " and should be " + user.getDegree());
                            // check if reusable
                            if (rand.degree(thread) >= thread.getDegree()) {
                                //System.out.println("OUT thread " + thread.getName() + " has degree " + rand.degree(thread) + " and should be " + thread.getDegree());
                                threads.remove(thread);
                            }
                            //System.out.println("thread is " + thread.getId() + " and has space for " + (thread.getDegree() - rand.degree(thread)) + " more links");
                        }
                    } else {
                        System.out.println("ho finito prima!!!");
                    }
                }
            }
            // RETRIEVE ALL NODES
            Collection<Node> vertices = rand.getVertices();
            // CLEAN USERS LIST
            users = new ArrayList<Node>();
            // CLEAN THREADS LIST
            threads = new ArrayList<Node>();
            // ASK EACH NODE
            for (Node n : vertices) {
                // IF NODE IS A USER
                if (n.getColor().equals("red")) {
                    // ADD TO USERS LIST
                    users.add(n);
                } else {
                    // ADD TO THREADS LIST
                    threads.add(n);
                }
            }
            System.out.println("EXPERIMENT " + expp + " WRITE NETWORK");
            // write random generated network to file
            WriteStats.writeNetwork(rand, networkPath);
            System.out.println("EXPERIMENT " + expp + " STATS");
            // write statistics
            WriteStats.writeBipartiteStats(rand, users, threads, usersStatsPath, threadsStatsPath);
            System.out.println("EXPERIMENT " + expp + " DONE");
        }
        System.out.println("ALL EXPERIMENTS DONE.");
    }

    /**
     * Method to extract random thread which is not already connected to user
     * otherwise the number of link would be less than the empirical one.
     */
    private Node extractRandom(ArrayList<Node> candidates, Node user, Context cntxt, Graph<Node, Edge> rand) {
        // 
        Node candidate = null;
        // 
        boolean stop = false;
        // 
        while (!stop) {
            //
            candidate = candidates.get(cntxt.getRNG().nextInt(candidates.size()));
            // 
            if (!rand.getNeighbors(user).contains(candidate)) {
                // 
                stop = true;
            }
        }
        // 
        return candidate;
    }

    private ArrayList<Node> giveNode(Node user, ArrayList<Node> threads, final Graph<Node, Edge> rand) {

        // list of available threads
        ArrayList<Node> availableThreads = new ArrayList<Node>();
        // find my neighbors
        Collection<Node> neighbors = rand.getNeighbors(user);
        // 
        // add edges correspondig to degree distribution
        // first people first... :)
        Collections.shuffle(threads);
//        Collections.sort(threads,
//                new Comparator<Node>() {
//            @Override
//            public int compare(Node o1, Node o2) {
//                Double deg1 = o1.getDegree() - rand.degree(o1), deg2 = o2.getDegree() - rand.degree(o2);
//                int cmp = deg2.compareTo(deg1);
//                if (cmp == 0) {
//                    Integer id1 = o1.getId(), id2 = o2.getId();
//                    cmp = id2.compareTo(id1);
//                }
//                return cmp;
//            }
//            // end compare
//        });
        // threads iterator
        Iterator<Node> iterator = threads.iterator();
        // stop counters
        int limit = (int) user.getDegree();
        int increase = 0;
        // filter array with threads not already my neighbors
        while (iterator.hasNext() && increase < limit) {
            // take the node
            Node next = iterator.next();
            // make comparison on the basis of id
            if (!neighbors.contains(next)) {
                // add
                availableThreads.add(next);
                // increment
                increase++;
            }
        }
        // return linkable threads
        return availableThreads;
    }
}
