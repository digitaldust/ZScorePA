package org.nlogo.extensions.zscorepa;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
        // forum to be replicated
        String forumName = argmnts[0].getString();
        // empirical network file name - this is the source file for the experiment
        String empiricalSource = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/empirical networks/" + forumName + "-empirical.txt";
        // users
        List<Node> users = new ArrayList<Node>();
        // threads
        List<Node> threads = new ArrayList<Node>();
        // read that file
        try {
            // DEBUG
            System.out.print("Building the empirical network...");
            // buffer to read in the empirical file
            BufferedReader netFile = new BufferedReader(new FileReader(empiricalSource));
            // empirical network
            UndirectedSparseGraph<Node, Edge> empirical = new UndirectedSparseGraph<Node, Edge>();
            // names
            Set<String> userNames = new HashSet<String>();
            Set<String> threadNames = new HashSet<String>();
            // create 
            String line;
            // while there are lines in the file, i.e. there are links to add...
            //<editor-fold defaultstate="collapsed" desc="...BUILD EMPIRICAL NETWORK">
            while ((line = netFile.readLine()) != null) {
                String[] split = line.split(" ");
                String userName = split[0];
                String threadName = split[1];
                String linkWeight = split[2];
                boolean userIn;
                boolean threadIn;
                userIn = userNames.contains(userName);
                threadIn = threadNames.contains(threadName);
                User u;
                Thread t;
                // just add the link
                Edge e = new Edge(Integer.valueOf(linkWeight));
                // if thread and user are both already present
                if (userIn && threadIn) {
                    // retrieve the user
                    u = (User)findUser(users, userName);
                    // retrieve the thread
                    t = (Thread)findThread(threads, threadName);
                    // if thread is present but user is not present
                } else if (!userIn && threadIn) {
                    // retrieve the thread
                    t = (Thread)findThread(threads, threadName);
                    // add new user
                    u = new User();
                    // add name
                    u.setName(userName);
                    // add name to user names list
                    userNames.add(userName);
                    // add user to objects list
                    users.add(u);
                    // if user is present but thread is not present
                } else if (userIn && !threadIn) {
                    // retrieve the thread
                    u = (User)findUser(users, userName);
                    // add new user
                    t = new Thread();
                    // add name
                    t.setName(threadName);
                    // add name to thread names list
                    threadNames.add(threadName);
                    // add thread to objects list
                    threads.add(t);
                    // if user and thread are both not present
                } else {
                    // add new user
                    u = new User();
                    // add name
                    u.setName(userName);
                    // add name to user names list
                    userNames.add(userName);
                    // add user to objects list
                    users.add(u);
                    // add new user
                    t = new Thread();
                    // add name
                    t.setName(threadName);
                    // add name to thread names list
                    threadNames.add(threadName);
                    // add thread to objects list
                    threads.add(t);
                }
                // add the link between them
                empirical.addEdge(e, u, t, EdgeType.UNDIRECTED);
            }
            // DEBUG
            System.out.println(" done!");
            //</editor-fold>
            // find current date
            String experimentFolder = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/generated/empiric/" + forumName + "/";
            // make files for statistics for this experiments set, so must create them 
            // before the experiments for loop starts
            // file to hold users degree analysis
            String usersDegreeDistribution = experimentFolder + "/users-degree-distribution.txt";
            // file to hold threads degree analysis
            String threadsDegreeDistribution = experimentFolder + "/threads-degree-distribution.txt";
            // file to hold users average nei degree
            String usersAverageDegreeOfNei = experimentFolder + "/users-average-degree-nei.txt";
            // file to hold threads average nei degree
            String threadsAverageDegreeOfNei = experimentFolder + "/threads-average-degree-nei.txt";
            // file to hold users number of two distance neighbors
            String usersNumberOfTwoDistNei = experimentFolder + "/users-number-2-dist-nei.txt";
            // file to hold threads number of two distance neighbors
            String threadsNumberOfTwoDistNei = experimentFolder + "/threads-number-2-dist-nei.txt";
            // file to hold users redundancy
            String usersRedundancy = experimentFolder + "/users-redundancy.txt";
            // file to hold threads redundancy
            String threadsRedundancy = experimentFolder + "/threads-redundancy.txt";
            /**
             * Find degree distribution.
             */
            // DEBUG
            System.out.print("Find users degree distribution...");
            // find users degree distribution
            HashMap<Integer, Collection<Node>> usersWithDegree = WriteStats.findNodesForDegree(users, empirical);
            // write degree distribution to file - use 0 to indicate there is no experiment
            WriteStats.writeDegreeDistribution(usersWithDegree, users.size(), 0, usersDegreeDistribution);
            // DEBUG
            System.out.println(" done.");
            // DEBUG
            System.out.print("Find threads degree distribution...");
            // find threads degree distribution
            HashMap<Integer, Collection<Node>> threadsWithDegree = WriteStats.findNodesForDegree(threads, empirical);
            // write degree distribution to file
            WriteStats.writeDegreeDistribution(threadsWithDegree, threads.size(), 0, threadsDegreeDistribution);
            // DEBUG
            System.out.println(" done.");
            /**
             * Find average degree of nei of nodes with degree.
             */
            // DEBUG
            System.out.print("Find average degree of neighbors of users with degree...");
            // find users redundancy
            WriteStats.writeAverageDegreeOfNeiOfNodesWithDegree(usersWithDegree, 0, usersAverageDegreeOfNei, empirical);
            // DEBUG
            System.out.println(" done.");
            // DEBUG
            System.out.print("Find average degree of neighbors of threads with degree...");
            // find users redundancy
            WriteStats.writeAverageDegreeOfNeiOfNodesWithDegree(threadsWithDegree, 0, threadsAverageDegreeOfNei, empirical);
            // DEBUG
            System.out.println(" done.");
            /**
             * Find number of 2-distance neighbours of nodes with degree.
             */
            // DEBUG
            System.out.print("Find number of distance 2 neighbors of users with degree....");
            // find users redundancy
            WriteStats.writeNumberOfDistanceTwoNeighborsOfNodesWithDegree(usersWithDegree, 0, usersNumberOfTwoDistNei, empirical);
            // DEBUG
            System.out.println(" done.");
            // DEBUG
            System.out.print("Find number of distance 2 neighbors of threads with degree...");
            // find users redundancy
            WriteStats.writeNumberOfDistanceTwoNeighborsOfNodesWithDegree(threadsWithDegree, 0, threadsNumberOfTwoDistNei, empirical);
            // DEBUG
            System.out.println(" done.");
            /**
             * Find redundancy of nodes with degree.
             */
            // DEBUG
            System.out.print("Find redundancy of users with degree...");
            // write users redundancy to file
            WriteStats.writeRedundancyOfNodesWithDegree(usersWithDegree, users.size(), 0, usersRedundancy);
            // DEBUG
            System.out.println(" done.");
            // DEBUG
            System.out.print("Find redundancy of threads with degree...");
            // write threads redundancy to file
            WriteStats.writeRedundancyOfNodesWithDegree(threadsWithDegree, threads.size(), 0, threadsRedundancy);
            // DEBUG
            System.out.println(" done.");
        } catch (Exception ex) {
            // a problem occurred reading the file, see stacktrace
            throw new ExtensionException(ex);
        }
    }

    /**
     * Finds a specific user.
     *
     * @param users
     * @param pickMe
     * @return User
     */
    private Node findUser(List<Node> users, String pickMe) {
        // for each user
        for (Node n : users) {
            // if user name equals pickme
            if (n.getName().equals(pickMe)) {
                // then return the node
                return n;
            }
        }
        // in case an error occurs, return null
        return null;
    }

    /**
     * Finds a specific thread.
     *
     * @param threads
     * @param pickMe
     * @return Thread
     */
    private Node findThread(List<Node> threads, String pickMe) {
        // for each user
        for (Node n : threads) {
            // if user name equals pickme
            if (n.getName().equals(pickMe)) {
                // then return the node
                return n;
            }
        }
        // in case an error occurs, return null
        return null;
    }
}
