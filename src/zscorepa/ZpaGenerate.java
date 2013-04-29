/**
 * TODO: revise me... in netlogo the model does not build any network... :(
 *
 */
package zscorepa;

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
import java.util.Iterator;
import java.util.Set;
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

    @Override
    public Syntax getSyntax() {
        return Syntax.commandSyntax(new int[]{Syntax.StringType(), Syntax.NumberType(), Syntax.NumberType(), Syntax.NumberType()});
    }

    @Override
    public void perform(Argument[] argmnts, Context cntxt) throws ExtensionException, LogoException {

        // attributes file name path
        String forumName = argmnts[0].getString();
        String attributesFilePath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_network_attributes/" + forumName + "-empirical-attributes.txt";
        // help strength
        double help_strength = argmnts[1].getDoubleValue();
        // average thread size for this empirical forum
        final double maxThreadSize = argmnts[2].getDoubleValue();
        // experiment number
        double expNum = argmnts[3].getDoubleValue();
        
        PsRandom generator = new PsRandom();
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
        String networkPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_bipartite_networks/" + forumName + "-zpa.txt";
        // for each experiment
        for (int exp = 0; exp < expNum; exp++) {
            // EXPERIMENT NUMBER, SHOULD MATCH FOLDER AND START WITH 1
            int expp = exp + 1;
            // TERMINAL OUTPUT
            System.out.println("START ZPA EXPERIMENT " + expp);
            // EXPERIMENT PATHS FOR USERS' STATS AND ACTIVITY AND THREADS STATS
            String usersStatsPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/experiments/" + forumName + "/EXP " + expp + "/users-stats-" + forumName + "-zpa.txt";
            String threadsStatsPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/experiments/" + forumName + "/EXP " + expp + "/threads-stats-" + forumName + "-zpa.txt";
            String activityPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/experiments/" + forumName + "/EXP " + expp + "/users-activity-" + forumName + "-zpa.txt";
            // CREATE AN EMPTY GRAPH 
            UndirectedSparseGraph<Node, Edge> zpa = new UndirectedSparseGraph<Node, Edge>();
            // READ IN EMPIRICAL ATTRIBUTES FILE
            BufferedReader attributeFile;
            try {
                attributeFile = new BufferedReader(new FileReader(attributesFilePath));
            } catch (FileNotFoundException ex) {
                throw new ExtensionException(ex);
            }
            // CREATE ARRAYS TO HOLD USERS AND THREADS
            ArrayList<Node> users = new ArrayList<Node>();
            ArrayList<Node> threads = new ArrayList<Node>();
            // STRING TO HOLD THE BUFFER LINE
            String line;
            int totalZindex = 0;
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
                    Node n = new Node();
                    // SET USER'S ID
                    n.setId(Integer.valueOf(splitUser[0].substring(1)));
                    // SET HIS NAME
                    n.setName(splitUser[0]);
                    // SET HIS COLOR
                    n.setColor("red");
                    // SET HIS TOTAL NUMBER OF POSTS
                    n.setPosts(Double.valueOf(splitUser[1]));
                    // INCREMENT TOTAL ACTIVITY WITH THE POSTS MADE BY THIS USER
                    totalActivity += n.getPosts();
                    // SET HIS TOTAL NUMBER OF THREADS
                    n.setThreads(Double.valueOf(splitUser[2]));
                    // CALCULATE USER'S Z-INDEX
                    double zindex = (n.getPosts() - n.getThreads()) / Math.sqrt(n.getPosts() + n.getThreads());
                    // SET HIS Z-INDEX
                    n.setZindex(zindex);
                    totalZindex += zindex;
                    // INITIALIZE HIS LIST OF 2-DIST NEI
                    n.initializeNeiOfNei();
                    // ADD THIS USER TO THE NETWORK
                    zpa.addVertex(n);
                    // ADD THIS USER TO THE LIST OF USERS
                    users.add(n);
                    // USED TO CALCULATE ACTIVITY STATS AND LEAVE THREADS() FREE FOR MANIPULATION
                    n.setZpaThreads(n.getThreads());
                    // CREATE ALL THE THREADS STARTED BY THIS USER
                    int limit = (int) n.getThreads();
                    // FOR EACH THREAD
                    for (int i = 0; i < limit; i++) {
                        // CREATE THREAD
                        Node thread = new Node();
                        // SET THREAD ID
                        thread.setId(threadCounter);
                        // SET THREAD NAME
                        thread.setName("t" + threadCounter);
                        // SET THREAD COLOR
                        thread.setColor("blue");
                        // SET PROBABILITY TO GET CHOSEN - FOR THREADS ONLY
                        thread.setZpaProb(0.0);
                        // SET WHO STARTED THIS THREAD
                        thread.setStartedBy(n);
                        // SET Z-INDEX OF THE THREAD - EQUALS THE Z-INDEX OF ITS STARTER
                        thread.setZindex(n.getZindex());
                        // INCREMENT THREAD COUNTER
                        threadCounter++;
                        // ADD THREAD TO THE NETWORK
                        zpa.addVertex(thread);
                        // ADD THREAD TO THREADS LIST
                        threads.add(thread);
                        // CREATE NEW EDGE
                        Edge e = new Edge();
                        // SET USER AS STARTER
                        e.setFromid(n.getName());
                        // SET THREAD AS TARGET
                        e.setToid(thread.getName());
                        // ADD UNDIRECTED LINK TO THE NETWORK
                        zpa.addEdge(e, n, thread, EdgeType.UNDIRECTED);
                        // SET INITIAL THREAD DEGREE TO 1 - THREAD IS CONNECTED ONLY TO ITS STARTER
                        thread.setDegree(1.0);
                        // DECREMENT NUMBER OF THREADS - BUT WHY?
//                        n.setThreads(n.getThreads() - 1);
                        // DECREMENT TOTAL NUMBER OF POSTS BY 1 - USER CONSUMES A POST TO START A THREAD
                        n.setPosts(n.getPosts() - 1);
                        // FOR THE SAME REASON, DECREMENT TOTAL ACTIVITY
                        totalActivity--;
                        // USED TO CALCULATE POSTS ACTIVITY STATS AND LEAVE POSTS() FREE FOR MANIPULATION
                        n.setZpaPosts(n.getZpaPosts() + 1);
                    }
                }
                // CATCH EVENTUAL EXCEPTION
            } catch (IOException ex) {
                // THROWS EXTENSION EXCEPTION WITH THE WHOLE ERROR STACK TRACE
                throw new ExtensionException(ex);
            }
            System.out.println("total activity is " + totalActivity);
            /**
             * *****************************************************************
             * PLEASE NOTE: I DO NOT MODEL THE AMOUNT OF PARTICIPATION BUT JUST
             * HOW IT IS DIRECTED AMONG THE AVAILABLE THREADS, WHICH ARE MAINLY
             * OPENED INDEPENDENTLY FROM ONE ANOTHER.
             * ****************************************************************
             */
            // WHILE USERS HAVE POSTS TO DO
            while (totalActivity > 0) {
                // FILTER USERS THAT HAVE POSTS TO DO
                CollectionUtils.filter(users, new Predicate() {
                    @Override
                    public boolean evaluate(Object object) {
                        return ((Node) object).getPosts() > 0;
                    }
                });
                // RANDOMIZE AVAILABLE USERS' ORDER BEFORE ASKING EACH OF THEM
                Collections.shuffle(users);
                // MAKE A USERS ITERATOR
                Iterator<Node> iterator = users.iterator();
                // 
                double maxThreadDeg = 0.0;
                // ASK EACH USER
                while (iterator.hasNext()) {
                    // RETRIEVE THIS USER
                    Node n = iterator.next();
                    // FILTER THREADS WITH DEGREE <= THAN MAX EMPIRICAL VALUE
                    CollectionUtils.filter(threads, new Predicate() {
                        @Override
                        public boolean evaluate(Object object) {
                            return ((Node) object).getDegree() <= maxThreadSize;
                        }
                    });
                    // RANDOMIZE THREADS ORDER
                    Collections.shuffle(threads);
                    // SET CUM PROB TO ZERO FOR NEXT ROUND
                    clearZpaProb(threads);
                    // SELECT AN APPEALING THREAD ACCORDING TO THIS USER'S FEATURES
                    Node t = selectAppealingThread(zpa, n, threads, help_strength, totalZindex, generator);
                    // IF A THREAD HAS BEEN FOUND
                    if (t != null) {
                        // MAKE NEW LINK
                        Edge e = new Edge();
                        // SET USER AS STARTER
                        e.setFromid(n.getName());
                        // SET THREAD AS TARGET
                        e.setToid(t.getName());
                        // ADD THIS UNDIRECTED LINK TO THE NETWORK
                        zpa.addEdge(e, n, t, EdgeType.UNDIRECTED);
                        // UPDATE THREAD DEGREE
                        t.setDegree(zpa.degree(t));
                        // find max degree
                        if (t.getDegree() > maxThreadDeg) {
                            maxThreadDeg = t.getDegree();
                        }

                        // UPDATE USER'S DEGREE
                        n.setDegree(zpa.degree(n));
                        // DECREMENT BY 1 USER'S AVAILABLE POSTS
                        n.setPosts(n.getPosts() - 1);
                        // FIXME: LOOKS LIKE IT'S HOW MANY POSTS THIS USER'S HAVE DONE
                        n.setZpaPosts(n.getZpaPosts() + 1);
                        // UPDATE USER'S 2-DIST NEI LIST
                        ArrayList<Node> neiOfNei = n.getNeiOfNei();
                        // RETRIEVE USERS CONNECTED TO THIS THREAD
                        Collection<Node> usersNeiOfThread = zpa.getNeighbors(t);
                        // MAKE ITERATOR 
                        Iterator<Node> usersNeiOfThreadIter = usersNeiOfThread.iterator();
                        // ASK EACH OF THE USERS NEI OF THE SELECTED THREAD
                        while (usersNeiOfThreadIter.hasNext()) {
                            // RETRIEVE A USER
                            Node next1 = usersNeiOfThreadIter.next();
                            // IF USER IS NOT YET IN MY 2-DIST NEI AND USER IS NOT ME
                            if (!neiOfNei.contains(next1) && !next1.equals(n)) {
                                // ADD USER TO MY 2-DIST NEI
                                n.setNeiOfNei(next1);
                            }
                        }
                        // DECREMENT TOTAL ACTIVITY BY ONE
                        totalActivity--;
                    }
                }
                // RETRIEVE DATE
                Date date = new Date();
                // TERMINAL OUTPUT
                System.out.println(totalActivity + " at time " + date.toString() + " after a round.");
            }
            /**
             * end zpa model procedure.
             */
            // RETRIEVE ALL NODES
            Collection<Node> vertices = zpa.getVertices();
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
            // TERMINAL OUTPUT
            System.out.println("EXPERIMENT " + expp + " WRITE NETWORK");
            // WRITE ZPA NETWORK TO FILE
            WriteStats.writeNetwork(zpa, networkPath);
            // TERMINAL OUTPUT
            System.out.println("CALCULATE STATS FOR EACH NODES");
            // CALCULATE STATS FOR EACH NODES
            WriteStats.findValues(zpa);
            // TERMINAL OUTPUT
            System.out.println("EXPERIMENT " + expp + " STATS");
            // TERMINAL OUTPUT
            System.out.println("FIND USERS DEGREE DISTRIBUTION");
            // FIND USERS DEGREE DISTRIBUTION
            HashMap<Double, Collection<Node>> usersDegreeDistr = WriteStats.findDegreeDistribution(users);
            // TERMINAL OUTPUT
            System.out.println("WRITE USERS STATS TO FILE");
            // WRITE USERS STATS TO FILE
            WriteStats.writeBipartiteStats(zpa, usersDegreeDistr, users.size(), usersStatsPath);
            // TERMINAL OUTPUT
            System.out.println("FIND THREADS DEGREE DISTRIBUTION");
            // FIND THREADS DEGREE DISTRIBUTION
            HashMap<Double, Collection<Node>> threadsDegreeDistr = WriteStats.findDegreeDistribution(threads);
            // TERMINAL OUTPUT
            System.out.println("WRITE THREADS STATS TO FILE");
            // WRITE USERS STATS TO FILE
            WriteStats.writeBipartiteStats(zpa, threadsDegreeDistr, threads.size(), threadsStatsPath);
            // TERMINAL OUTPUT
            System.out.println("EXPERIMENT " + expp + " ACTIVITY ANALYSYS");
            // WRITE ACTIVITY TO FILE
            WriteStats.writeActivity(usersDegreeDistr, users, activityPath);
            // TERMINAL ACTIVITY
            System.out.println("EXPERIMENT " + expp + " DONE");
        }
        // TERMINAL ACTIVITY
        System.out.println("ALL EXPERIMENTS DONE.");
    }

    private Node selectAppealingThread(UndirectedSparseGraph<Node, Edge> zpa, Node n, ArrayList<Node> threads, double help_strength, int totalZindex, PsRandom generator) {

        // INITIALIZE WINNER THREAD TO NULL
        Node winner = null;
        // INITIALIZE LOTTERY BUCKET
        double cum = 0;
        // PICK A RANDOM DOUBLE BETWEEN 0 AND 1
        double nextDouble = generator.nextDouble();
        // IF HELP-NEWBY MECHANISM IS SELECTED
        if (nextDouble <= help_strength) {
            /*******************************
             * BIG USER TRIES TO HELP NEWBY.
             ******************************/
            // MAKE THREADS ITERATOR
            Iterator<Node> threadsIter = threads.iterator();
            // ASK EACH THREAD
            while (threadsIter.hasNext()) {
                // RETRIEVE THIS THREAD
                Node aThread = threadsIter.next();
                // FIND THIS THREAD PROBABILITY TO GET CHOSEN 
                double dist = Math.abs(n.getZindex() - aThread.getZindex());// / (n.getPosts() + n.getZpaPosts());
                // SET THIS THREAD PROBABILITY TO GET CHOSEN
                aThread.setZpaProb(dist);
                // INCREMENT LOTTERY BUCKET
                cum += dist;
            }
        } else {
            /**********************************************
             * USER TRIES TO MEET WITH HIS BUDDIES TO CHAT.
             *********************************************/
            // RETRIEVE MY 2-DIST NEIGHBORS
            ArrayList<Node> twoDistUsers = n.getNeiOfNei();
            // MAKE ITERATOR
            Iterator<Node> twoDistUsersIterator = twoDistUsers.iterator();
            // ASK EACH 2-DIST NEI
            while (twoDistUsersIterator.hasNext()) {
                // RETRIEVE 2-DIST USER
                Node twoDistUser = twoDistUsersIterator.next();
                // GET 2-DIST USER'S THREADS
                Collection<Node> twoDistUserThreads = zpa.getNeighbors(twoDistUser);
                // MAKE ITERATOR
                Iterator<Node> twoDistUserThreadsIterator = twoDistUserThreads.iterator();
                // ASK EACH THREAD
                while (twoDistUserThreadsIterator.hasNext()) {
                    // RETRIEVE A THREAD
                    Node aThread = twoDistUserThreadsIterator.next();
                    // 
                    if(zpa.getNeighbors(n).contains(aThread)){
                        // INCREMENT PROBABILITY BY 1
                        aThread.setZpaProb(aThread.getZpaProb() + 1);
                    } else {
                        // INCREMENT PROBABILITY BY 2
                        aThread.setZpaProb(aThread.getZpaProb() + 2);
                    }
                }
            }
            Iterator<Node> iterator1 = threads.iterator();
            while (iterator1.hasNext()) {
                Node next = iterator1.next();
                double zpaProb = 1.0 / (next.getZpaProb() * next.getZindex()); // getDegree peggiora, non lo rifare.
                if(Double.isNaN(zpaProb) || Double.isInfinite(zpaProb)){
                    zpaProb = 1.0;
                }
                next.setZpaProb(zpaProb);                                  // 1 / serve a vedere se inverto la tendenza per grandi e piccoli
                cum += zpaProb;
            }
        }
        /**
         * RETURN A THREAD FOR MECHANISM ONE
         */
        Collections.shuffle(threads);
//        System.out.println("cum is " + cum);
        // IF LOTTERY BUCKET IS EMPTY
        if (cum <= 0) {
            // IF MECHANISM ONE, THREADS HAVE PROBABILITY PROPORTIONAL TO ZINDEX - ASSUMPTION
//            if (nextDouble <= help_strength) {
                // SET CUM EQUALS TO TOTAL ZINDEX
                cum = totalZindex;
                /**
                 * THIS METHOD IS TRANSLATED FROM NETLOGO LOTTERY EXAMPLE
                 * PLEASE REFER TO THAT MODEL FOR FURTHER INFO.
                 */
                // PICK A RANDOM VALUE FROM THE LOTTERY BUCKET 
                double pick = generator.nextDouble(cum);
                // MAKE ITERATOR
                Iterator<Node> threadsIter = threads.iterator();
                // ASK EACH THREAD
                while (threadsIter.hasNext()) {
                    // RETRIEVE THIS THREAD
                    Node t = threadsIter.next();
                    // IF WINNER IS NULL AND THIS THREAD HAS A PROBABILITY GREATER THAN PICK
                    if (winner == null && t.getZindex() > pick) {
                        // THIS THREAD IS THE ONE
                        return t;
                    } else {
                        // OTHERWISE DECREMENT PICK OF THIS THREAD PROBABILITY
                        pick -= t.getZindex();
                    }
                }
            // IF MECHANISM TWO, THREADS HAVE EQUAL PROBABILITY TO GET CHOSEN
//            } else {
//                // RETURN A RANDOM INDEXED THREAD
//                return threads.get(generator.nextInteger(threads.size()-1));
//            }
        } else {
            /**
             * THIS METHOD IS TRANSLATED FROM THE NETLOGO LOTTERY EXAMPLE PLEASE
             * REFER TO THAT MODEL FOR FURTHER INFO.
             */
            // PICK A RANDOM VALUE FROM THE LOTTERY BUCKET 
            double pick = generator.nextDouble(cum);
            // MAKE ITERATOR
            Iterator<Node> threadsIter = threads.iterator();
            // ASK EACH THREAD
            while (threadsIter.hasNext()) {
                // RETRIEVE THIS THREAD
                Node t = threadsIter.next();
                // IF WINNER IS NULL AND THIS THREAD HAS A PROBABILITY GREATER THAN PICK
                if (winner == null && t.getZpaProb() > pick) {
                    // THIS THREAD IS THE ONE
                    return t;
                } else {
                    // OTHERWISE DECREMENT PICK OF THIS THREAD PROBABILITY
                    pick -= t.getZpaProb();
                }
            }
        }
        return null;
    }

    private ArrayList<Node> sampling(int i, Context cntxt, ArrayList<Node> threads) {
        ArrayList<Node> winners = new ArrayList<Node>();
        while (i > 0) {
            int iddu = cntxt.getRNG().nextInt(threads.size() - 1);
            winners.add(threads.get(iddu));
            i--;
        }
        return winners;
    }

    private void clearZpaProb(ArrayList<Node> threads) {
        Iterator<Node> iterator1 = threads.iterator();
        while (iterator1.hasNext()) {
            iterator1.next().setZpaProb(0);
        }
    }
}
