/**
 * TODO: revise me... in netlogo the model does not build any network... :(
 *
 */
package zscorepa;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
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
                        // USED TO CALCULATE POSTS ACTIVITY STATS AND LEAVE POSTS() FREE FOR MANIPULATION
                        n.setZpaPosts(n.getZpaPosts() + 1);
                    }
                }
                // CATCH EVENTUAL EXCEPTION
            } catch (IOException ex) {
                // THROWS EXTENSION EXCEPTION WITH THE WHOLE ERROR STACK TRACE
                throw new ExtensionException(ex);
            }
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
                    // SELECT AN APPEALING THREAD ACCORDING TO THIS USER'S FEATURES
                    Node t = selectAppealingThread(zpa, cntxt, n, threads, help_strength);
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
            System.out.println("EXPERIMENT " + expp + " STATS");
            // WRITE STATS TO FILE
            HashMap<Double, Collection<Node>> degreeDistr = WriteStats.writeBipartiteStats(zpa, users, threads, usersStatsPath, threadsStatsPath);
            // TERMINAL OUTPUT
            System.out.println("EXPERIMENT " + expp + " ACTIVITY ANALYSYS");
            // WRITE ACTIVITY TO FILE
            WriteStats.writeActivity(degreeDistr, users, activityPath);
            // TERMINAL ACTIVITY
            System.out.println("EXPERIMENT " + expp + " DONE");
        }
        // TERMINAL ACTIVITY
        System.out.println("ALL EXPERIMENTS DONE.");
    }

    private Node selectAppealingThread(UndirectedSparseGraph<Node, Edge> zpa, Context cntxt, final Node n, ArrayList<Node> sampled, double help_strength) {

        // selected thread
        Node winner = null;
        // cumulative probabilities
        double cum = 0;
        // distances
        HashMap<Node, Double> distances = new HashMap<Node, Double>();
        //sampler(threads, limit, cntxt, zpa, threadSize);
        // if help_strength is high then this behavior is encouraged - bigs help small
        if (cntxt.getRNG().nextDouble() <= help_strength) {
            //System.out.println("sample size is " + sampled.size());
            //System.out.println("after filter sample size is " + sampled.size());
            Iterator<Node> sampledIter = sampled.iterator();
            while (sampledIter.hasNext()) {
                Node aThread = sampledIter.next();
                double dist = (n.getZindex() - aThread.getZindex());
                //System.out.println(n.getName() + " has zindex " + n.getZindex() + " and " + t.getName() + " has prob " + dist + " and has appeal " + t.getZindex());
                if (dist < 0) {
                    dist = 0;
                }
                cum += dist;
                distances.put(aThread, dist);
            }
            //System.out.println("cum value is " + cum);
        } else {
            // si prova a vedere con il sampling che succede?
            ArrayList<Node> sampledTwo = sampling(400, cntxt, sampled);
            // se io sono uno piccolo, posto in thread grandi - discorsivi
            // se io sono uno grande (degree * zindex)
            // un piccolo non posta nei thread piccoli perché non conosce nessuno
            // un grande lo fa per aiutare, ma è modellato già nel meccanismo precedente
            // quindi questo meccanismo deve solo mettere i piccoli in thread grandi e i grandi in thread medio grandi
            double me = n.getDegree() * n.getZindex();
            if (me < 1) {
                me = 1.0;
            }
            ArrayList<Node> twoDistUsers = n.getNeiOfNei();
            Iterator<Node> sampledIter = sampledTwo.iterator();
            while (sampledIter.hasNext()) {
                Node aThread = sampledIter.next();
                double count = 0;
                Collection<Node> aThreadUsers = zpa.getNeighbors(aThread);
                int size = aThreadUsers.size();
                Iterator<Node> aThreadUsersIter = aThreadUsers.iterator();
                while (aThreadUsersIter.hasNext()) {
                    Node aTwoDistNei = aThreadUsersIter.next();
                    if (twoDistUsers.contains(aTwoDistNei)) {
                        count++;
                        if (count > size / 5) {
                            break;
                        }
                    }
                }
                if (count == 0) {
                    count = 1 / aThread.getDegree() + 1;
                }
                me *= count;
                // END ADDED NOW
                double value = (double) cntxt.getRNG().nextInt((int) me);// qui ho cambiato! ora peggiora un po' ma va bene, devo solo 
//                if (!myThreads.contains(aThread)) {       // fare in modo che facciano più threads
//                    // filter sampled to find threads where I have not already commented
//                    value /= cntxt.getRNG().nextDouble() * 1000; // if zindex == 0
//                }
//
//                if (value < 1 || Double.isNaN(value) || Double.isInfinite(value)) {
//                    value = 1;
//                }
                //System.out.println("n has me " + me + " and value is " + value);
                distances.put(aThread, value);
                cum += value;
                //System.out.println("cum is " + cum + " and value is " + value);

            }
        }
//        {
//            // embedding 
//            // this reduces the degree of users, since they pick up a thread they are already connected to
//            // but it is good because otherwise we end up with users with a huge degree.
//            ArrayList<Node> twoDistNei = new ArrayList<Node>();
//            final Collection<Node> threadsWhereIHaveBeen = zpa.getNeighbors(n);
//            Iterator<Node> threadsWhereIHaveBeenIterator = threadsWhereIHaveBeen.iterator();
//            while (threadsWhereIHaveBeenIterator.hasNext()) {
//                Node next = threadsWhereIHaveBeenIterator.next();
//                // NO MATTER THE PROBABILITY OF THIS, THEY ARE ALREADY CONNECTED WITH THIS THREAD... LOOKS LIKE, BUT
//                // WELL ACTUALLY THIS PROBABILITY IS OF BIG IMPORTANCE, 1.0 FITS GOOD BUT 
//                // I HAVE TO THINK ABOUT A BETTER RESULT
//                double value = next.getDegree();
//                //
//                if (value < 1) {
//                    value = 1;
//                }
////                System.out.println("thread " + next.getName() + " has degree " + next.getDegree() + " and value " + value + " and node has degree " + n.getDegree());
//                distances.put(next, value);
//                cum += value;
//                //
//                Collection<Node> neiOfNei = zpa.getNeighbors(next);
//                Iterator<Node> neiOfNeiIterator = neiOfNei.iterator();
//                while (neiOfNeiIterator.hasNext()) {
//                    Node next1 = neiOfNeiIterator.next();
//                    if (!twoDistNei.contains(next1)) {
//                        twoDistNei.add(next1);
//                    }
//                }
//            }
//            // A NEW ONE IS ADDEDD ONCE IN A WHILE
//            if (cntxt.getRNG().nextDouble() < 0.1) { // se aumenti più di 0.1 peggiora.
//                //System.out.println("NOT FILTERED SIZE " + sampled.size());
//                // filter sampled to find threads where I have not already commented
//                CollectionUtils.filter(sampled, new Predicate() {
//                    @Override
//                    public boolean evaluate(Object object) {
//                        return !threadsWhereIHaveBeen.contains((Node) object);
//                    }
//                });
//                //System.out.println("FILTERED SIZE " + sampled.size());
//                // filtered sampled iterator
//                Iterator<Node> sampledIterator = sampled.iterator();
//                while (sampledIterator.hasNext()) {
//                    Node aThread = sampledIterator.next();
//                    double howManyIKnow = 0.0;
//                    Collection<Node> neiOfAThread = zpa.getNeighbors(aThread);
//                    Iterator<Node> neiOfAThreadIterator = neiOfAThread.iterator();
//                    while (neiOfAThreadIterator.hasNext()) {
//                        Node user = neiOfAThreadIterator.next();
//                        if (twoDistNei.contains(user)) {
//                            howManyIKnow++;
//                        }
//                    }
//                    distances.put(aThread, howManyIKnow / aThread.getDegree() * 100);
//                    cum = cum + howManyIKnow / aThread.getDegree() * 100;
//                }
//
//            }
//            //System.out.println("cum is " + cum + " and my 2distnei are " + twoDistNei.size() + " and threads where I have been are " + threadsWhereIHaveBeen.size());
//        }

//        for(Node t:distances.keySet()){
//            System.out.println(t.getName() + " has prob " + distances.get(t));
//        }
        // FINALLY EXTRACT ONE THREAD FROM THE DISTANCES HASHMAP
        if (cum <= 0) {
            int iddu = cntxt.getRNG().nextInt(threads.size() - 1);
            return threads.get(iddu);
        } else {
            //System.out.println(cum);
            // extract the winner using lottery example
            double pick = (double) cntxt.getRNG().nextInt((int) Math.floor(cum));
            //System.out.println("pick value is " + pick);
            Set<Node> keySet = distances.keySet();
            Iterator<Node> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                Node t = iterator.next();
                if (winner == null) {
                    if (distances.get(t) > pick) {
                        //System.out.println("pick value is " + pick + " and dist is " + distances.get(t));
                        return t;
                    } else {
                        pick -= distances.get(t);
                    }
                }
            }
        }
        return winner;
    }

    private ArrayList<Node> sampler(ArrayList<Node> threads, int limit, Context cntxt, UndirectedSparseGraph<Node, Edge> zpa, double threadSize) {

        ArrayList<Node> winners = new ArrayList<Node>();
        while (limit > 0) {
            boolean stop = true;
            while (stop) {
                int iddu = cntxt.getRNG().nextInt(threads.size() - 1);
                Node get = threads.get(iddu);
                if (zpa.getNeighbors(get).size() < threadSize) {
                    winners.add(get);
                    stop = false;
                    limit--;
                }
            }
        }
        return winners;
    }

    private ArrayList<Node> sampling(int i, Context cntxt, ArrayList<Node> sampled) {
        ArrayList<Node> winners = new ArrayList<Node>();
        while (i > 0) {
            int iddu = cntxt.getRNG().nextInt(sampled.size() - 1);
            winners.add(sampled.get(iddu));
            i--;
        }
        return winners;
    }

    private UndirectedSparseGraph<Node, Edge> buildNetwork() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
