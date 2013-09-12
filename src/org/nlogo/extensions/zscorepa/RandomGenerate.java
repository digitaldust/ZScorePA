package org.nlogo.extensions.zscorepa;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

/**
 * Generate a random network with same degree distribution than the empirical
 * network.
 *
 * @author Simone Gabbriellini
 */
public class RandomGenerate extends DefaultCommand {

    /**
     * Get parameters from NetLogo.
     *
     * @param forum, what forum to replicate, either mmorpg, energeticambiente,
     * hipforum
     * @param method, either link or degree distribution based
     * @param experiment, the number of experiments
     * @return
     */
    @Override
    public Syntax getSyntax() {
        return Syntax.commandSyntax(new int[]{Syntax.StringType(), Syntax.StringType(), Syntax.NumberType()});
    }

    /**
     * Actually executes the command task and generates a random network.
     *
     * @param argmnts
     * @param cntxt
     * @throws ExtensionException
     * @throws LogoException
     */
    @Override
    public void perform(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {

        // forum to be replicated
        String forumName = argmnts[0].getString();
        // links or degree distribution
        String method = argmnts[1].getString();
        // experiment number
        double experiments = argmnts[2].getDoubleValue();
        // empirical network file name - this is the source file for the experiment
        String empiricalSource = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/empirical networks/" + forumName + "-empirical.txt";
        // users
        ArrayList<User> users = new ArrayList<User>();
        // threads
        ArrayList<Thread> threads = new ArrayList<Thread>();
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
                    u = findUser(users, userName);
                    // retrieve the thread
                    t = findThread(threads, threadName);
                    // if thread is present but user is not present
                } else if (!userIn && threadIn) {
                    // retrieve the thread
                    t = findThread(threads, threadName);
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
                    u = findUser(users, userName);
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
            //</editor-fold>
            // DEBUG
            System.out.println(" done!");
            // how many links are there
            int L = empirical.getEdgeCount();
            // DEBUG
            System.out.print("Finding method...");
            // find what is the replication method
            if (method.equals("degree")) {
                // DEBUG
                System.out.print(" degree distribution, finding degree vectors...");
                // for each node, find its degree
                for (Node n : empirical.getVertices()) {
                    // find degree of user n
                    Integer degree = empirical.degree(n);
                    // store information in the node's attribute
                    n.setDegree(degree);
                }
                // DEBUG
                System.out.println(" done.");
            } else {
                // DEBUG
                System.out.print(" links, done.");
            }
            // format date
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
            // find current date
            String experimentFolder = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/generated/random/" + dateFormat.format(new Date());
            // make folder for this set of experiments
            new File(experimentFolder).mkdir();
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
            // now that all the information is gathered, execute the experiments
            for (int exp = 0; exp < experiments; exp++) {
                // random network
                UndirectedSparseGraph<Node, Edge> random = new UndirectedSparseGraph<Node, Edge>();
                // copy nodes from empirical
                for (Node v : empirical.getVertices()) {
                    // node placeholder
                    Node n;
                    // clone this node from empirical to random network...
                    if(v.getName().startsWith("u")){
                        // node is a user
                        n = new User();
                    } else {
                        // node is a thread
                        n = new Thread();
                    }
                    // set name value
                    n.setName(v.getName());
                    // set degree value
                    n.setDegree(v.getDegree());
                    // add the cloned node to random network
                    random.addVertex(n);
                }
                // DEBUG
                System.out.print("Separing users from threads in random network...");
                // 
                List<Node> randomUsers = new ArrayList<Node>();
                // threads  
                List<Node> randomThreads = new ArrayList<Node>();
                // fill new arrays
                for (Node n : random.getVertices()) {
                    // if node is a user
                    if (n.getName().startsWith("u")) {
                        // add it to users array
                        randomUsers.add((User) n);
                        // otherwise
                    } else {
                        // add it to threads
                        randomThreads.add((Thread) n);
                    }
                }
                // DEBUG
                System.out.println(" done!");
                // DEBUG
                System.out.println("Experiment " + exp + ", building random network...");
                // according to selected method...
                //<editor-fold defaultstate="collapsed" desc="...BUILD RANDOM NETWORK">
                if (method.equals("links")) {
                    // build as many edges as L
                    int l = L;
                    // while l is bigger than zero
                    while (l > 0) {
                        // find a random user
                        User u = (User)randomUsers.get(cntxt.getRNG().nextInt(randomUsers.size()));
                        // find a random thread
                        Thread t = (Thread)randomThreads.get(cntxt.getRNG().nextInt(randomThreads.size()));
                        // check that they are not already connected
                        if (!random.getNeighbors(t).contains(u)) {
                            // add a link between the two
                            random.addEdge(new Edge(0), u, t, EdgeType.UNDIRECTED);
                            // decrement links counter
                            l--;
                        }
                    }
                    // DEBUG
                    System.out.println(" done building " + random.getEdgeCount() + " links (fixed density).");
                } else {
                    // build links according to degree distribution
                    int l = L;
                    // while there are links to do...
                    while (l > 0) {
                        // flag if the user is *not* found
                        boolean checkU = true;
                        // flag if the thread is *not* found;
                        boolean checkT = true;
                        // the selected user
                        User u = null;
                        // the selected thread
                        Thread t = null;
                        // while user is not found
                        //<editor-fold defaultstate="collapsed" desc="find a user">
                        while (checkU) {
                            // if no user available
                            if (randomUsers.isEmpty()) {
                                // raise exception
                                throw new ExtensionException("ERROR: no user available but " + l + " links are still missing.");
                                // else if users are available
                            } else {
                                // you should pick big users first to avoid the risk
                                // of leaving very big users without threads available
                                u = (User)pickHigherDegreeUser(randomUsers, cntxt);
                                // if user is not null
                                if(u != null){
                                    // decrement u degree ability
                                    u.setDegree(u.getDegree() - 1);
                                    // user found, update flag to exit the user while loop
                                    checkU = false;
                                }
                            }
                        }
                        //</editor-fold>
                        // while thread is not found
                        //<editor-fold defaultstate="collapsed" desc="find a thread for this user">
                        while (checkT) {
                            // if no thread is available
                            if (randomThreads.isEmpty()) {
                                // raise exception
                                throw new ExtensionException("ERROR: no thread available but " + l + " links are still missing.");
                                // else if threads are available
                            } else {
                                // among available threads for this user, with higher
                                // probability pick one with higher degree
                                t = (Thread)pickAvailableThread(u, cntxt, randomThreads, random);
                                // if thread is not null
                                if(t != null){
                                    // decrement t degree ability
                                    t.setDegree(t.getDegree() - 1);
                                    // thread found, update flag to exit the thread while loop
                                    checkT = false;
                                }
                            }
                        }
                        //</editor-fold>
                        // add a link between the two
                        random.addEdge(new Edge(0), u, t, EdgeType.UNDIRECTED);
                        // decrement links counter
                        l--;
                    }
                    // DEBUG
                    System.out.println(" done building " + random.getEdgeCount() + " links (degree distribution).");
                }
                //</editor-fold>
                // write this random network to file in experiments folder
                //<editor-fold defaultstate="collapsed" desc="WRITE NETWORK TO FILE IN EXPERIMENTS FOLDER">
                // random network in this experiment is called
                String experimentName = experimentFolder + "/" + forumName + "-" + method + "-" + exp + ".txt";
                // once created, write random network to file
                BufferedWriter writeToFile = new BufferedWriter(new FileWriter(experimentName));
                // write down the edgelist format
                for (Node n : random.getVertices()) {
                    if (n.getName().startsWith("u")) {
                        Collection<Node> neighbors = random.getNeighbors(n);
                        for (Node nei : neighbors) {
                            writeToFile.append(n.getName() + " " + nei.getName() + " 0");
                            writeToFile.newLine();
                        }
                    }
                }
                writeToFile.flush();
                writeToFile.close();
                //</editor-fold>
                /** Find degree distribution. */
                // DEBUG
                System.out.print("Find users degree distribution...");
                // find users degree distribution
                HashMap<Integer, Collection<Node>> usersWithDegree = WriteStats.findNodesForDegree(randomUsers, random);
                // write degree distribution to file
                WriteStats.writeDegreeDistribution(usersWithDegree, randomUsers.size(), exp, usersDegreeDistribution);
                // DEBUG
                System.out.println(" done.");
                // DEBUG
                System.out.print("Find threads degree distribution...");
                // find threads degree distribution
                HashMap<Integer, Collection<Node>> threadsWithDegree = WriteStats.findNodesForDegree(randomThreads, random);
                // write degree distribution to file
                WriteStats.writeDegreeDistribution(threadsWithDegree, randomThreads.size(), exp, threadsDegreeDistribution);
                // DEBUG
                System.out.println(" done.");
                /** Find average degree of nei of nodes with degree. */
                // DEBUG
                System.out.print("Find average degree of neighbors of users with degree...");
                // find users redundancy
                WriteStats.writeAverageDegreeOfNeiOfNodesWithDegree(usersWithDegree, exp, usersAverageDegreeOfNei, random);
                // DEBUG
                System.out.println(" done.");
                // DEBUG
                System.out.print("Find average degree of neighbors of threads with degree...");
                // find users redundancy
                WriteStats.writeAverageDegreeOfNeiOfNodesWithDegree(threadsWithDegree, exp, threadsAverageDegreeOfNei, random);
                // DEBUG
                System.out.println(" done.");
                /** Find number of 2-distance neighbours of nodes with degree. */
                // DEBUG
                System.out.print("Find number of distance 2 neighbors of users with degree....");
                // find users redundancy
                WriteStats.writeNumberOfDistanceTwoNeighborsOfNodesWithDegree(usersWithDegree, exp, usersNumberOfTwoDistNei, random);
                // DEBUG
                System.out.println(" done.");
                // DEBUG
                System.out.print("Find number of distance 2 neighbors of threads with degree...");
                // find users redundancy
                WriteStats.writeNumberOfDistanceTwoNeighborsOfNodesWithDegree(threadsWithDegree, exp, threadsNumberOfTwoDistNei, random);
                // DEBUG
                System.out.println(" done.");
                /** Find redundancy of nodes with degree. */
                // DEBUG
                System.out.print("Find redundancy of users with degree...");
                // write users redundancy to file
                WriteStats.writeRedundancyOfNodesWithDegree(usersWithDegree, randomUsers.size(), exp, usersRedundancy);
                // DEBUG
                System.out.println(" done.");
                // DEBUG
                System.out.print("Find redundancy of threads with degree...");
                // write threads redundancy to file
                WriteStats.writeRedundancyOfNodesWithDegree(threadsWithDegree, randomThreads.size(), exp, threadsRedundancy);
                // DEBUG
                System.out.println(" done.");
            }
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
    private User findUser(ArrayList<User> users, String pickMe) {
        // for each user
        for (User u : users) {
            // if user name equals pickme
            if (u.getName().equals(pickMe)) {
                // then return the node
                return u;
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
    private Thread findThread(ArrayList<Thread> threads, String pickMe) {
        // for each user
        for (Thread t : threads) {
            // if user name equals pickme
            if (t.getName().equals(pickMe)) {
                // then return the node
                return t;
            }
        }
        // in case an error occurs, return null
        return null;
    }

    /**
     * Sort users according to their degree, and then picks the one with higher 
     * degree first.
     *
     * @param randomUsers, the list of users available
     * @param cntxt, the NetLogo Context
     * @return User
     */
    private Node pickHigherDegreeUser(List<Node> randomUsers, Context cntxt) {
        // sort random users in decreasing order of degree
        Collections.sort(randomUsers, COMPARATOR);
        // return the bigger user according to degree, i.e. posts that user has to do yet
        return randomUsers.get(0);
    }

    /**
     * Finds available threads for a particular user, then extracts with higher 
     * probability among availables the ones with higher degree.
     *
     * @param u, the user
     * @param cntxt, the NetLogo Context
     * @param randomThreads, all the threads
     * @param random, the graph
     * @return Thread
     * @throws ExtensionException
     */
    private Node pickAvailableThread(Node u, Context cntxt, List<Node> randomThreads, Graph<Node, Edge> random) throws ExtensionException {
        Collection<Node> neighbors = random.getNeighbors(u);
        ArrayList<Node> available = new ArrayList<Node>();
        for (Node t : randomThreads) {
            if (!neighbors.contains(t)) {
                available.add(t);
            }
        }
        if (available.isEmpty()) {
            throw new ExtensionException("ERROR: No available threads for user " + u.getName());
        } else {
            int sum = 0;
            for (Node n : available) {
                sum += n.getDegree();
            }
            int pick = cntxt.getRNG().nextInt(sum);
            Node t = null;
            Collections.shuffle(available);
            for (Node n : available) {
                if (t == null) {
                    if (n.getDegree() > pick) {
                        t = n;
                    } else {
                        pick -= n.getDegree();
                    }
                }
            }
            return t;
        }
    }
    
    private static Comparator<Node> COMPARATOR = new Comparator<Node>(){
	// This is where the sorting happens.
        @Override
        public int compare(Node o1, Node o2)
        {
            return o2.getDegree() - o1.getDegree();
        }
    };
}
