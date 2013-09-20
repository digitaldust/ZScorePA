/**
 * TODO: revise me... in netlogo the model does not build any network... :(
 *
 */
package org.nlogo.extensions.zscorepa;

import edu.uci.ics.jung.algorithms.filters.Filter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import flanagan.math.PsRandom;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
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
public class ZpaGenerate extends DefaultCommand {

    String forumName;
    String attributesFilePath;
    double helpStrength;
    double maxAppeal;
    double expNum;
    PsRandom generator;
    String networkPath;
    public static UndirectedSparseGraph<Node, Edge> zpa;
    // USERS ARRAY
    List<User> users;
    // THREADS ARRAY
    List<Thread> threads;
    Context context;
    String usersStatsPath;
    String threadsStatsPath;
    String activityPath;
    String threadactivityPath;
    TreeMap<Double, Node> zindexProb;

    @Override
    public Syntax getSyntax() {
        return Syntax.commandSyntax(new int[]{Syntax.StringType(), Syntax.NumberType(), Syntax.NumberType(), Syntax.NumberType()});
    }

    @Override
    public void perform(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {

        // FORUM NAME
        forumName = argmnts[0].getString();
        // attributes file name path
        attributesFilePath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_network_attributes/" + forumName + "-empirical-zpa-attributes.txt";
        // PROB TO CHOSE MECHANISM ONE OR MECHANISM TWO
        helpStrength = argmnts[1].getDoubleValue();
        // EXP NUMBER
        expNum = argmnts[3].getDoubleValue();
        // RANDOM GENERATOR
        generator = new PsRandom();
        // CONTEXT
        context = cntxt;
        // 
        maxAppeal = 0;
        /**
         * Start experiments round and statistics analysis. Basically, a zpa
         * network is created then its activity is analyzed, then on the very
         * same network statistics are calculated, this should drop down
         * computation time, because you don't have to load and generate new
         * networks each time but all the analysis is done on the same network.
         * Then R should be called and the output analysis generated - for
         * convenience, because it is a pity to manually run it each time when
         * the script is already made.
         */
        // network path is always the same, nets are being rewrited each time on the same file.
        networkPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_bipartite_networks/" + forumName + "-zpa.txt";
        // for each experiment
        for (int exp = 0; exp < expNum; exp++) {
            // EXPERIMENT NUMBER, SHOULD MATCH FOLDER AND START WITH 1
            int expp = exp + 1;
            // TERMINAL OUTPUT
            System.out.println("START ZPA EXPERIMENT " + expp);
            // EXPERIMENT PATHS FOR USERS' STATS AND ACTIVITY AND THREADS STATS
            usersStatsPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/experiments/" + forumName + "/EXP " + expp + "/users-stats-" + forumName + "-zpa.txt";
            threadsStatsPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/experiments/" + forumName + "/EXP " + expp + "/threads-stats-" + forumName + "-zpa.txt";
            activityPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/experiments/" + forumName + "/EXP " + expp + "/users-activity-" + forumName + "-zpa.txt";
            threadactivityPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/experiments/" + forumName + "/EXP " + expp + "/thread-activity-" + forumName + "-zpa.txt";
            // CREATE AN EMPTY GRAPH 
            zpa = new UndirectedSparseGraph<Node, Edge>();
            // TOTAL ACTIVITY TO BE MANAGED BY THE MODEL
            double totalActivity;
            // READ IN EMPIRICAL ATTRIBUTES FILE
            BufferedReader attributeFile;
            try {
                // READ IN ATTRIBUTES FILE
                attributeFile = new BufferedReader(new FileReader(attributesFilePath));
                // FILL INITIAL NETWORK
                totalActivity = createInitialNetwork(attributeFile);

            } catch (FileNotFoundException ex) {
                // RAISE EXTENSION EXCEPTION ON ERROR
                throw new ExtensionException(ex);
            }

            /**
             * PLEASE NOTE: I DO NOT MODEL THE AMOUNT OF PARTICIPATION BUT JUST
             * HOW IT IS DIRECTED AMONG THE AVAILABLE THREADS, WHICH ARE MAINLY
             * OPENED INDEPENDENTLY FROM ONE ANOTHER.
             */
            /**
             * Begin ZPA procedure.
             */
            while (totalActivity > 0) {                                         // WHILE USERS HAVE POSTS TO DO
                CollectionUtils.filter(users, new Predicate() {                 // FILTER USERS THAT HAVE POSTS TO DO
                    @Override
                    public boolean evaluate(Object object) {
                        return ((User) object).getPostsToDo() > 0;
                    }
                });

                Collections.shuffle(users);                                     // RANDOMIZE AVAILABLE USERS' ORDER BEFORE ASKING
                for (User u : users) {                                          // ASK EACH USER
                    //Collections.shuffle(threads);                               // RANDOMIZE THREADS ORDER
                    Thread t = selectAppealingThread(u);                        // SELECT AN APPEALING THREAD ACCORDING TO THIS USER'S FEATURES
                    // add the link only if it is not already there
                    Collection<Node> neighbors = zpa.getNeighbors(u);
                    if (!neighbors.contains(t)) {
                        zpa.addEdge(new Edge(0), u, t, EdgeType.UNDIRECTED);  // ADD THIS UNDIRECTED LINK TO THE NETWORK
                    }
                    /**
                     * UPDATE USER STATS.
                     */
                    u.setPostsToDo(u.getPostsToDo() - 1);                       // DECREMENT POSTS TO DO
                    u.setPostsDone(u.getPostsDone() + 1);                       // INCREMENT POSTS DONE
                    /**
                     * UPDATE TOTAL ACTIVITY.
                     */
                    totalActivity--;                                            // DECREMENT TOTAL ACTIVITY
                }
                Date date = new Date();                                         // RETRIEVE DATE
                System.out.println(totalActivity + " at time " + // TERMINAL OUTPUT 
                        date.toString() + " after a round.");                   // 
            }

            /**
             * End ZPA procedure.
             */
            callStatsStuff(expp);                                               // ANALYZE AND WRITE STUFF
        }
        System.out.println("ALL EXPERIMENTS DONE.");                            // TERMINAL ACTIVITY
    }

    private Thread selectAppealingThread(User u) throws ExtensionException {

        //
        Thread winner;
        if (generator.nextDouble() < 0.38) {
            Collection<Node> neighbors = zpa.getNeighbors(u);
            List<Thread> sample = new ArrayList<Thread>();
            for (Thread t : threads) {
                if (!neighbors.contains(t)) {
                    sample.add(t);
                }
            }
            winner = logExtract(u, sample);
        } else {
            // threads dove ci conosci qualcuno
            Collection<Node> neighbors = zpa.getNeighbors(u);
            List<Thread> list = new ArrayList<Thread>();
            for (Node subset : neighbors) {
                list.add((Thread) subset);
            }
            if (list.isEmpty()) { // non si può fare, quindi è come se la prima condizione avesse fallito
                List<Thread> sample = new ArrayList<Thread>();
                for (Thread t : threads) {
                    if (!neighbors.contains(t)) {
                        sample.add(t);
                    }
                }
                winner = logExtract(u, sample);
            } else {
                // ci sono già connesso, non importa a chi scrivo.
                winner = list.get(context.getRNG().nextInt(list.size()));
            }
        }
        if (winner == null) {
            throw new ExtensionException("winner is null");
        }
        return winner;
    }

    private Thread logExtract(User u, List<Thread> sample) {
        Thread winner = null;
        Collections.sort(sample, new Comparator<Thread>() {
            @Override
            public int compare(Thread one, Thread other) {
                return one.getZindex().compareTo(other.getZindex());
            }
        });
//        double cum = 0;
        double pick = generator.nextLogNormal(3, 1);
        if (pick < 0) {
            pick = 0;
        }
        if (pick > 62) {
            pick = 62;
        }
        System.out.println("pick is " + pick);
        System.out.println("sample is " + sample.size());
        int counter = 0;
        for (Thread t : sample) {
//            System.out.println("thread zindex is " + t.getZindex());
            if (t.getZindex() >= pick) {
//                System.out.println("ritorno " + t.getZindex());
                winner = t;
                break;
            }
            counter++;
            if (counter == sample.size()) {
                winner = t;
                break;
            }
        }
        //
//        for (Thread t : sample) {
//            //if (u.getZindex() < 13.5) {
//            t.setChance((t.getAppeal() / maxAppeal) * zpa.degree(t)); // il mio parametro per aiutare il fit
//
//            if (u.getZindex() < 6) {
//            } else {
//            }
//            //}
//
//            cum += t.getChance();
//        }
//        double pick = generator.nextDouble(cum);
//        // ASK EACH THREAD
//        for (Thread t : sample) {
//            // IF WINNER IS NULL AND THIS THREAD HAS A PROBABILITY GREATER THAN PICK
//            if (winner == null) {
//                if (t.getChance() > pick) {
//                    // THIS THREAD IS THE ONE
//                    winner = t;
//                } else {
//                    // OTHERWISE DECREMENT PICK OF THIS THREAD PROBABILITY
//                    pick -= t.getChance();
//                }
//            }
//        }
        return winner;
    }

    // INITIALIZE THE NETWORK WITH THE ATTRIBUTES FILE
    private double createInitialNetwork(BufferedReader attributeFile) throws ExtensionException {
        // CREATE ARRAYS TO HOLD USERS AND THREADS
        users = new ArrayList<User>();
        threads = new ArrayList<Thread>();
        // STRING TO HOLD THE BUFFER LINE
        String line;
        // INITIALIZE TOTAL ACTIVITY - I.E. THE TOTAL NUMBER OF POSTS
        double totalActivity = 0;
        // TRY
        try {
            // FOR EACH LINE IN THE BUFFER
            while ((line = attributeFile.readLine()) != null) {
                // CREATE THE ARRAY
                String[] split = line.split(";");
                // BUILD USERS FIRST
                if (split[0].startsWith("u")) {
                    // CREATE USER
                    User u = new User();
                    // SET USER'S ID
                    u.setId(Integer.valueOf(split[0].substring(1)));
                    // SET HIS NAME
                    u.setName(split[0]);
                    // SET HIS COLOR
                    u.setColor("red");
                    // SET HIS TOTAL NUMBER OF POSTS
                    u.setPostsToDo(Double.valueOf(split[1]));
                    // INCREMENT TOTAL ACTIVITY WITH THE POSTS MADE BY THIS USER
                    totalActivity += u.getPostsToDo();
                    // SET HIS TOTAL NUMBER OF THREADS
                    u.setThreadsToDo(Double.valueOf(split[2]));
                    // SET HIS Z-INDEX
                    u.setZindex(Double.valueOf(split[3]));
                    // ADD THIS USER TO THE NETWORK
                    zpa.addVertex(u);
                    // ADD THIS USER TO THE LIST OF USERS
                    users.add(u);
                    // USED TO CALCULATE ACTIVITY STATS AND LEAVE THREADS() FREE FOR MANIPULATION
                    u.setThreadsDone(u.getThreadsToDo());
                } else {
                    // CREATE THREAD
                    Thread t = new Thread();
                    t.setId(Integer.valueOf(split[0].substring(1)));                                     // SET THREAD ID
                    t.setName(split[0]);                             // SET THREAD NAME
                    t.setColor("blue");                                         // SET THREAD COLOR
                    t.setChance(0.0);                                           // SET PROBABILITY TO GET CHOSEN - FOR THREADS ONLY
                    User uu = getUser(split[1]);
                    t.setStartedBy(uu);                                          // SET WHO STARTED THIS THREAD
                    t.setZindex(Double.valueOf(split[2]));                                 // SET Z-INDEX OF THE THREAD - EQUALS THE Z-INDEX OF ITS STARTER
                    t.setAppeal(Double.valueOf(split[3]));
                    if (maxAppeal < t.getAppeal()) {
                        maxAppeal = t.getAppeal();
                    }
                    zpa.addVertex(t);                                           // ADD THREAD TO THE NETWORK
                    threads.add(t);                                             // ADD THREAD TO THREADS LIST
                    zpa.addEdge(new Edge(0), uu, t, EdgeType.UNDIRECTED);                  // ADD UNDIRECTED LINK TO THE NETWORK
                    uu.setPostsToDo(uu.getPostsToDo() - 1);                       // DECREMENT TOTAL NUMBER OF POSTS BY 1 - USER CONSUMES A POST TO START A THREAD
                    uu.setPostsDone(uu.getPostsDone() + 1);                       // USED TO CALCULATE POSTS ACTIVITY STATS AND LEAVE POSTS() FREE FOR MANIPULATION
                    totalActivity--;
                }
            }
            return totalActivity;
            // CATCH EVENTUAL EXCEPTION
        } catch (IOException ex) {
            // THROWS EXTENSION EXCEPTION WITH THE WHOLE ERROR STACK TRACE
            throw new ExtensionException(ex);
        }
    }

    // get a user
    private User getUser(String s) {
        for (User n : users) {
            if (n.getName().equals(s)) {
                return n;
            }
        }
        return null;
    }

    // DO STATS AND WRITE TO FILE
    private void callStatsStuff(int expp) throws ExtensionException {

        Collection<Node> nodes = zpa.getVertices();                             // RETRIEVE ALL NODES
        List<Node> allUser = new ArrayList<Node>();                                          // CLEAN USERS LIST
        List<Node> allThread = new ArrayList<Node>();                                      // CLEAN THREADS LIST
        for (Node n : nodes) {                                                  // ASK EACH NODE
            if (n.getColor().equals("red")) {                                   // IF NODE IS A USER
                allUser.add(n);                                            // ADD TO USERS LIST
            } else {
                allThread.add(n);                                        // ADD TO THREADS LIST
            }
        }
        System.out.println("NETWORK " + expp + " HAS " + zpa.getEdgeCount() + " LINKS");                     // TERMINAL ACTIVITY
        System.out.println("EXPERIMENT " + expp + " WRITE NETWORK");            // TERMINAL OUTPUT
        WriteStats.writeNetwork(zpa, networkPath);                              // WRITE ZPA NETWORK TO FILE
        System.out.println("CALCULATE STATS FOR EACH NODES");                   // TERMINAL OUTPUT
       // WriteStats.findValues(zpa);                                             // CALCULATE STATS FOR EACH NODES
        System.out.println("EXPERIMENT " + expp + " STATS");                    // TERMINAL OUTPUT
        System.out.println("FIND USERS DEGREE DISTRIBUTION");                   // TERMINAL OUTPUT
        HashMap<Double, Collection<Node>> usersDegreeDistr = WriteStats.findNodesWithDegree(allUser); // FIND USERS DEGREE DISTRIBUTION
        System.out.println("WRITE USERS STATS TO FILE");                        // TERMINAL OUTPUT
//        WriteStats.writeBipartiteStats(usersDegreeDistr, allUser.size(), usersStatsPath);// WRITE USERS STATS TO FILE
        System.out.println("FIND THREADS DEGREE DISTRIBUTION");                 // TERMINAL OUTPUT
        HashMap<Double, Collection<Node>> threadsDegreeDistr = WriteStats.findNodesWithDegree(allThread);// FIND THREADS DEGREE DISTRIBUTION
        System.out.println("WRITE THREADS STATS TO FILE");                      // TERMINAL OUTPUT
 //       WriteStats.writeBipartiteStats(threadsDegreeDistr, allThread.size(), threadsStatsPath);// WRITE USERS STATS TO FILE
        System.out.println("EXPERIMENT " + expp + " ACTIVITY ANALYSYS");        // TERMINAL OUTPUT
        WriteStats.writeActivity(zpa, usersDegreeDistr, threadsDegreeDistr, activityPath, threadactivityPath);        // WRITE ACTIVITY TO FILE
        System.out.println("EXPERIMENT " + expp + " DONE");                     // TERMINAL ACTIVITY

    }

    // filter threads
//    private List<Thread> filteredThreads(User u) throws ExtensionException {
//        //
//        List<Thread> sample = new ArrayList<Thread>();
//        for (Thread t : threads) {
//            t.setChance(t.getZindex() * zpa.degree(t));
//            sample.add((Thread) t);
//        }
//        return sample;
//    }
//
//    // retrieves threads where my 2-dist nei have posted and filtered

    private Set<Thread> twoDistThreads(User u) throws ExtensionException {
        // find two dist nei
        Filter<Node, Edge> filter = new KNeighborhoodFilter(u, 2, KNeighborhoodFilter.EdgeType.IN_OUT);
        Graph<Node, Edge> transform = filter.transform(zpa);
        // take threads that are in common with my 2-dist-nei but not already in my neighborhood.
        Collection<Node> vertices = transform.getVertices();
        Set<Thread> twoDistThreads = new HashSet<Thread>();
        Collection<Node> neighbors = zpa.getNeighbors(u);
        for (Node v : neighbors) {
//            if (v.getColor().equals("blue")) {
            Thread t = (Thread) v;
            t.setChance(t.getZindex() * zpa.degree(t)); // passa il tempo e i thread diventano meno appetibili
            twoDistThreads.add(t);
//            }
        }
        return twoDistThreads;
    }

}
