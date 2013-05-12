/**
 * TODO: revise me... in netlogo the model does not build any network... :(
 *
 */
package org.nlogo.extensions.zscorepa;

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
import java.util.Date;
import java.util.HashMap;
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
    double maxThreadSize;
    double expNum;
    PsRandom generator;
    String networkPath;
    public static UndirectedSparseGraph<Node, Edge> zpa;
    // USERS ARRAY
    ArrayList<User> users;
    // THREADS ARRAY
    ArrayList<Thread> threads;
    Context context;
    String usersStatsPath;
    String threadsStatsPath;
    String activityPath;
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
        attributesFilePath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_network_attributes/" + forumName + "-empirical-attributes.txt";
        // PROB TO CHOSE MECHANISM ONE OR MECHANISM TWO
        helpStrength = argmnts[1].getDoubleValue();
        // MAX THREAD SIZE
        maxThreadSize = argmnts[2].getDoubleValue();
        // EXP NUMBER
        expNum = argmnts[3].getDoubleValue();
        // RANDOM GENERATOR
        generator = new PsRandom();
        // CONTEXT
        context = cntxt;
        // 

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
                    Collections.shuffle(threads);                               // RANDOMIZE THREADS ORDER
                    Thread t = selectAppealingThread(u);                        // SELECT AN APPEALING THREAD ACCORDING TO THIS USER'S FEATURES
                    // add the link only if it is not already there
                    Collection<Node> neighbors = zpa.getNeighbors(u);
                    if (!neighbors.contains(t)) {
                        zpa.addEdge(new Edge(u, t), u, t, EdgeType.UNDIRECTED);  // ADD THIS UNDIRECTED LINK TO THE NETWORK
                        // update zindex for thread
                        Collection<Node> neighbors1 = zpa.getNeighbors(t);
                        double newZindex = 0;
                        for (Node uu : neighbors1) {
                            if (uu.getZindex() > 5.271) {
                                newZindex += uu.getZindex();
                            } else {
                                newZindex += t.getStartedBy().getZindex();
                            }
                        }
                        newZindex = (newZindex + t.getStartedBy().getZindex()) / neighbors1.size();
                        t.setZindex(newZindex);
                        //System.out.println("new zindex is " + newZindex);
                    }
                    /**
                     * UPDATE USER STATS.
                     */
                    u.setPostsToDo(u.getPostsToDo() - 1);                       // DECREMENT POSTS TO DO
                    u.setPostsDone(u.getPostsDone() + 1);                       // INCREMENT POSTS DONE
                    /**
                     * UPDATE THREAD STATS.
                     */
//                    if (t.getDegree() > maxThreadSize) {                        // REMOVE THREAD IF TOO BIG
//                        threads.remove(t);
//                    }
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

    private Thread selectAppealingThread(final User u) throws ExtensionException {

        if (u.getZindex() < 1.134) {// 1 quartile
            // chiacchiera in thread di gente media
            return threadWithThreshold(5.271, 6.325, u);

        } else if (u.getZindex() >= 1.134 && u.getZindex() < 6.325) { // tra I e III quartile
            // chiacchiera dove ci conosci qualcuno
            return threadWithThresholdAndFriends(1.134, 67.930, u);

        } else { // outliers - proxy to expert people very active in the forum

            ArrayList<Thread> sample = new ArrayList<Thread>();
            for (Thread t : threads) {
                if (zpa.degree(t) < 2 && t.getZindex() < 5.271) {
                    sample.add(t);
                }
            }
            //
            if (!sample.isEmpty()) { // rispondo a un niubbo
                //
                return sample.get(context.getRNG().nextInt(sample.size()));

            } else {
                //
                return threadWithThreshold(6.325, 67.930, u);
            }

        }

//        if (nextDouble < helpStrength) {
//            /**
//             * *****************************
//             * BIG USER TRIES TO HELP NEWBY, WHILE NEWBY THANKS THEM
//             */
//            // ASK EACH THREAD
//            for (Thread t : threads) {
//                chance = n.getZindex() - t.getZindex(); // rende meno interessanti i thread con l'andare del tempo
//                if (chance <= 0) {
//                    chance = 0;
//                }
//                if (myThreads.contains(t)) {                                // se ci sono già connesso, riduco la probabilità di sceglierlo
//                        chance *= generator.nextDouble(0, 0.5);           // the smaller the random value, the less probable is to select it
//                }
//                //chance = Math.exp(chance);
//                t.setChance(chance);
//                cum += chance;
//            }
//        } else {
//            for (Thread t : threads) {
//                double degree = t.getDegree();
//                chance = t.getZindex() * degree - n.getZindex();
//                if (chance <= 0) {
//                    chance = 0;
//                }
//                if (!myThreads.contains(t)) {                                       // se ci sono già connesso, riduco la probabilità di sceglierlo
//                    if (chance > 0) {
//                        chance *= getDouble(0, 0.2);                              // the smaller the random value, the less probable is to select it
//                    } else {
//                        chance = degree * getDouble(0, 0.2);
//                    }
//                }
//                chance = Math.exp(chance);
//                t.setChance(chance);
//                cum += chance;
//            }
//        }
        // RETURN A THREAD
//        return null;
    }

// INITIALIZE THE NETWORK WITH THE ATTRIBUTES FILE
    private double createInitialNetwork(BufferedReader attributeFile) throws ExtensionException {
        // CREATE ARRAYS TO HOLD USERS AND THREADS
        users = new ArrayList<User>();
        threads = new ArrayList<Thread>();
        // STRING TO HOLD THE BUFFER LINE
        String line;
        // COUNTER FOR SETTING THREAD ID
        int threadCounter = 0;
        // INITIALIZE TOTAL ACTIVITY - I.E. THE TOTAL NUMBER OF POSTS
        double totalActivity = 0;
        // TRY
        try {
            // FOR EACH LINE IN THE BUFFER
            while ((line = attributeFile.readLine()) != null) {
                // CREATE THE ARRAY
                String[] splitUser = line.split(",");
                // CREATE USER
                User u = new User();
                // SET USER'S ID
                u.setId(Integer.valueOf(splitUser[0].substring(1)));
                // SET HIS NAME
                u.setName(splitUser[0]);
                // SET HIS COLOR
                u.setColor("red");
                // SET HIS TOTAL NUMBER OF POSTS
                u.setPostsToDo(Double.valueOf(splitUser[1]));
                // INCREMENT TOTAL ACTIVITY WITH THE POSTS MADE BY THIS USER
                totalActivity += u.getPostsToDo();
                // SET HIS TOTAL NUMBER OF THREADS
                u.setThreadsToDo(Double.valueOf(splitUser[2]));
                // CALCULATE USER'S Z-INDEX
                double zindex = (u.getPostsToDo() - u.getThreadsToDo()) / Math.sqrt(u.getPostsToDo() + u.getThreadsToDo());
                // SET HIS Z-INDEX
                u.setZindex(zindex);
                // ADD THIS USER TO THE NETWORK
                zpa.addVertex(u);
                // ADD THIS USER TO THE LIST OF USERS
                users.add(u);
                // USED TO CALCULATE ACTIVITY STATS AND LEAVE THREADS() FREE FOR MANIPULATION
                u.setThreadsDone(u.getThreadsToDo());
                // CREATE ALL THE THREADS STARTED BY THIS USER
                int limit = (int) u.getThreadsToDo();
                // FOR EACH THREAD
                for (int i = 0; i < limit; i++) {
                    // CREATE THREAD
                    Thread t = new Thread();
                    t.setId(threadCounter);                                     // SET THREAD ID
                    t.setName("t" + threadCounter);                             // SET THREAD NAME
                    t.setColor("blue");                                         // SET THREAD COLOR
                    t.setChance(0.0);                                           // SET PROBABILITY TO GET CHOSEN - FOR THREADS ONLY
                    t.setStartedBy(u);                                          // SET WHO STARTED THIS THREAD
                    t.setZindex(u.getZindex());                                 // SET Z-INDEX OF THE THREAD - EQUALS THE Z-INDEX OF ITS STARTER
                    threadCounter++;                                            // INCREMENT THREAD COUNTER
                    zpa.addVertex(t);                                           // ADD THREAD TO THE NETWORK
                    threads.add(t);                                             // ADD THREAD TO THREADS LIST
                    zpa.addEdge(new Edge(u, t), u, t, EdgeType.UNDIRECTED);                  // ADD UNDIRECTED LINK TO THE NETWORK
                    u.setPostsToDo(u.getPostsToDo() - 1);                       // DECREMENT TOTAL NUMBER OF POSTS BY 1 - USER CONSUMES A POST TO START A THREAD
                    u.setPostsDone(u.getPostsDone() + 1);                       // USED TO CALCULATE POSTS ACTIVITY STATS AND LEAVE POSTS() FREE FOR MANIPULATION
                    totalActivity--;                                            // FOR THE SAME REASON, DECREMENT TOTAL ACTIVITY
                }
            }
            return totalActivity;
            // CATCH EVENTUAL EXCEPTION
        } catch (IOException ex) {
            // THROWS EXTENSION EXCEPTION WITH THE WHOLE ERROR STACK TRACE
            throw new ExtensionException(ex);
        }
    }

    // TRANSLATED FROM NETLOGO LOTTERY EXAMPLE
//    private Thread pickOne(double cum) {
//        // INITIALIZE WINNER THREAD
//        Thread winner = null;
//        // RANDOMIZE THREADS ORDER
//        Collections.shuffle(threads);
//        // PICK A RANDOM VALUE FROM THE LOTTERY BUCKET 
//        double pick = generator.nextDouble(cum);
//        // ASK EACH THREAD
//        for (Thread t : threads) {
//            // IF WINNER IS NULL AND THIS THREAD HAS A PROBABILITY GREATER THAN PICK
//            if (winner == null && t.getChance() > pick) {
//                // THIS THREAD IS THE ONE
//                winner = t;
//            } else {
//                // OTHERWISE DECREMENT PICK OF THIS THREAD PROBABILITY
//                pick -= t.getChance();
//            }
//        }
//        return winner;
//    }
    // 
    private void callStatsStuff(int expp) throws ExtensionException {

        Collection<Node> nodes = zpa.getVertices();                             // RETRIEVE ALL NODES
        ArrayList<Node> allUser = new ArrayList<Node>();                                          // CLEAN USERS LIST
        ArrayList<Node> allThread = new ArrayList<Node>();                                      // CLEAN THREADS LIST
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
        WriteStats.findValues(zpa);                                             // CALCULATE STATS FOR EACH NODES
        System.out.println("EXPERIMENT " + expp + " STATS");                    // TERMINAL OUTPUT
        System.out.println("FIND USERS DEGREE DISTRIBUTION");                   // TERMINAL OUTPUT
        HashMap<Double, Collection<Node>> usersDegreeDistr = WriteStats.findDegreeDistribution(allUser); // FIND USERS DEGREE DISTRIBUTION
        System.out.println("WRITE USERS STATS TO FILE");                        // TERMINAL OUTPUT
        WriteStats.writeBipartiteStats(zpa, usersDegreeDistr, allUser.size(), usersStatsPath);// WRITE USERS STATS TO FILE
        System.out.println("FIND THREADS DEGREE DISTRIBUTION");                 // TERMINAL OUTPUT
        HashMap<Double, Collection<Node>> threadsDegreeDistr = WriteStats.findDegreeDistribution(allThread);// FIND THREADS DEGREE DISTRIBUTION
        System.out.println("WRITE THREADS STATS TO FILE");                      // TERMINAL OUTPUT
        WriteStats.writeBipartiteStats(zpa, threadsDegreeDistr, allThread.size(), threadsStatsPath);// WRITE USERS STATS TO FILE
        System.out.println("EXPERIMENT " + expp + " ACTIVITY ANALYSYS");        // TERMINAL OUTPUT
        WriteStats.writeActivity(usersDegreeDistr, activityPath);        // WRITE ACTIVITY TO FILE
        System.out.println("EXPERIMENT " + expp + " DONE");                     // TERMINAL ACTIVITY

    }

    private Thread threadWithThreshold(double min, double max, User u) throws ExtensionException {
        Thread winner = null;
        Collection<Node> uNei = zpa.getNeighbors(u);
        ArrayList<Thread> sampleNei = new ArrayList<Thread>();
        ArrayList<Thread> sampleNotNei = new ArrayList<Thread>();
        for (Thread t : threads) {
            if (t.getZindex() >= min && t.getZindex() <= max) {
                if (uNei.contains(t)) {
                    sampleNei.add(t);
                } else {
                    sampleNotNei.add(t);
                }
            }
        }
        if (sampleNei.size() > 0 && sampleNotNei.size() > 0) {
            // 
            if (context.getRNG().nextDouble() < 0.6) {
                //
                winner = preferredExtraction(sampleNei);//sampleNei.get(context.getRNG().nextInt(sampleNei.size()));
            } else {
                //
                winner = preferredExtraction(sampleNotNei);// sampleNotNei.get(context.getRNG().nextInt(sampleNotNei.size()));
            }
        } else if (sampleNotNei.isEmpty() && sampleNei.isEmpty()) {
            //
            throw new ExtensionException("Non posso scegliere nessun thread.");

        } else if (sampleNei.isEmpty() && !sampleNotNei.isEmpty()) {
            //
            winner = preferredExtraction(sampleNotNei);//winner = sampleNotNei.get(context.getRNG().nextInt(sampleNotNei.size()));
        } else {
            //
            winner = preferredExtraction(sampleNei);//winner = sampleNei.get(context.getRNG().nextInt(sampleNei.size()));
        }
        return winner;
    }

    private Thread threadWithThresholdAndFriends(double min, double max, User u) {
        Thread winner;
        // find two dist nei
        ArrayList<Node> twoDistNei = new ArrayList<Node>();
        Collection<Node> myThreads = zpa.getNeighbors(u);
        for (Node t : myThreads) {
            Collection<Node> neighbors = zpa.getNeighbors(t);
            for (Node uu : neighbors) {
                if (!twoDistNei.contains(uu)) {
                    twoDistNei.add(uu);
                }
            }
        }
        ArrayList<Thread> sample = new ArrayList<Thread>();
        for (Thread t : threads) {
            if (t.getZindex() > min && t.getZindex() < max) {
                Collection<Node> tNei = zpa.getNeighbors(u);
                for (Node tt : tNei) {
                    if (twoDistNei.contains(tt)) {
                        sample.add(t);
                        break;
                    }
                }
            }
        }
        if (!sample.isEmpty()) {
            winner = sample.get(context.getRNG().nextInt(sample.size()));
        } else {
            winner = threads.get(context.getRNG().nextInt(threads.size()));
        }
        return winner;
    }

    private Thread preferredExtraction(ArrayList<Thread> sampleNei) {
        // zindex * degree
        zindexProb = new TreeMap<Double, Node>();
        double cum = 0;
        for(Thread t:sampleNei){
            double chance = t.getZindex() * zpa.degree(t);
            zindexProb.put(chance, t);
            if(cum < chance){
                cum = chance;
            }
        }
        double pick = generator.nextDouble(cum);
        return (Thread)zindexProb.higherEntry(pick).getValue();
    }
}
