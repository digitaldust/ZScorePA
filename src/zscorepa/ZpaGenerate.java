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
        double threadSize = argmnts[2].getDoubleValue();
        // experiment number
        double expNum = argmnts[3].getDoubleValue();
        /** 
         * Start experiments round and statistics analysis. Basically, a zpa network is created
         * then its activity is analyzed, then on the very same network statistics are calculated, this should 
         * drop down computation time, because you don't have to load and generate new networks each time
         * but all the analysis is done on the same network.
         * Then R should be called and the output analysis generated - for convenience, because it is a pity
         * to manually run it each time when the script is already made.
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
            String usersStatsPath   = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/experiments/" + forumName + "/EXP " + expp + "/users-stats-" + forumName + "-zpa.txt";
            String threadsStatsPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/experiments/" + forumName + "/EXP " + expp + "/threads-stats-" + forumName + "-zpa.txt";
            String activityPath     = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/experiments/" + forumName + "/EXP " + expp + "/users-activity-" + forumName + "-zpa.txt";
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
            // TRY
            try {
                // FOR EACH LINE IN THE BUFFER
                while ((line = attributeFile.readLine()) != null) {
                    // CREATE THE ARRAY
                    String[] splitUser = line.split(",");
                    // create user and save data
                    Node n = new Node();
                    n.setId(Integer.valueOf(splitUser[0].substring(1)));
                    n.setName(splitUser[0]);
                    n.setColor("red");
                    n.setPosts(Double.valueOf(splitUser[1]));
                    n.setThreads(Double.valueOf(splitUser[2]));
                    double zindex = (n.getPosts() - n.getThreads()) / Math.sqrt(n.getPosts() + n.getThreads());
                    n.setZindex(zindex);
                    n.initializeNeiOfNei();
                    zpa.addVertex(n);
                    users.add(n);
                    n.setZpaThreads(n.getThreads());
                    // create threads for this user
                    //System.out.println("user " + n.getName() + " has to do " + n.getThreads() + " threads.");
                    int limit = (int) Math.floor(n.getThreads());
                    for (int i = 0; i < limit; i++) {
                        Node thread = new Node();
                        thread.setId(threadCounter);
                        thread.setName("t" + threadCounter);
                        thread.setColor("blue");
                        thread.setStartedBy(n);
                        thread.setZindex(n.getZindex());
                        threadCounter++;
                        zpa.addVertex(thread);
                        threads.add(thread);
                        Edge e = new Edge();
                        e.setFromid(n.getName());
                        e.setToid(thread.getName());
                        zpa.addEdge(e, n, thread, EdgeType.UNDIRECTED);
                        thread.setDegree(zpa.degree(thread));
                        n.setThreads(n.getThreads() - 1);
                        n.setPosts(n.getPosts() - 1);
                        n.setZpaPosts(n.getZpaPosts() + 1);
                    }
                }
            } catch (IOException ex) {
                throw new ExtensionException(ex);
            }
            //
            double totalActivity = 0;
            for (Node n : users) {
                totalActivity += n.getPosts();
            }
            /**
             * I do not model the amount of participation, but just how it is directed.
             */
            // while users have posts to do
            while (totalActivity > 0) {
                // randomize
                Collections.shuffle(users);
                // users iterator
                Iterator<Node> iterator = users.iterator();
                // ask each user
                while (iterator.hasNext()) {
                    // this user
                    Node n = iterator.next();
                    // if this users has still a post to do
                    if (n.getPosts() > 0) {
                        // pick a thread according to how much this user finds a thread appealing
                        Node t = selectAppealingThread(zpa, cntxt, n, threads, help_strength, threadSize);
                        if (t != null) {
                            //System.out.println("selected thread is " + t.getName());
                            Edge e = new Edge();
                            e.setFromid(n.getName());
                            e.setToid(t.getName());
                            zpa.addEdge(e, n, t, EdgeType.UNDIRECTED);
                            t.setDegree(zpa.degree(t));
                            n.setDegree(zpa.degree(n));
                            n.setPosts(n.getPosts() - 1);
                            n.setZpaPosts(n.getZpaPosts() + 1);
                            ArrayList<Node> neiOfNei = n.getNeiOfNei();
                            Collection<Node> twoDistNei = zpa.getNeighbors(t);
                            Iterator<Node> twoDistNeiIter = twoDistNei.iterator();
                            while (twoDistNeiIter.hasNext()) {
                                Node next1 = twoDistNeiIter.next();
                                if (!neiOfNei.contains(next1) && !next1.equals(n)) { // tra i miei vicini non ci sono io!!!
                                    n.setNeiOfNei(next1);
                                }
                            }
                            // decrement total activity
                            totalActivity--;
                        }
                        //}
                    }
                }
                Date date = new Date();
                System.out.println(totalActivity + " at time " + date.toString() + " after a round.");
            }
            /**
             * end zpa model procedure.
             */
            // write activity file.
            System.out.println("EXPERIMENT " + expp + " ACTIVITY ANALYSYS");
            WriteStats.writeActivity(zpa, users, activityPath);
            System.out.println("EXPERIMENT " + expp + " WRITE NETWORK");
            WriteStats.writeNetwork(zpa, networkPath);
            System.out.println("EXPERIMENT " + expp + " STATS");
            WriteStats.writeBipartiteStats(zpa, users, threads, usersStatsPath, threadsStatsPath);
            System.out.println("EXPERIMENT " + expp + " DONE");
        }
        System.out.println("ALL EXPERIMENTS DONE.");
    }

    private Node selectAppealingThread(UndirectedSparseGraph<Node, Edge> zpa, Context cntxt, final Node n, ArrayList<Node> threads, double help_strength, double threadSize) {
// un thread DEVE saltare fuori.
// perchè l'utente ha fatto un post. Il punto è scegliere il thread migliore a seconda dell'utente.
// il top sarebbe: grazie al parametro help_strength dosare quanto i big preferiscono i thread small
// ipotesi - piccoli cercano aiuto, quindi parlano in thread piccoli
// ipotesi - grandi offrono aiuto, quindi parlano in thread piccoli rispondendo a quelli sopra
// ipotesi - grandi parlano con tutti, in conversazioni ad ampio respiro.
// for each pair, calculate the difference (or absolute difference, or difference squared). 
// then use that difference as weighting to select one pair. 
// calculate cumulative probabilities
// distance from my appeal and thread appeal
// if distance is large, it means that I am facing a small thread and I am big, so the chance I post there is high in order to give help to a newbie
// if distance is 0, then I am as big as the thread, thus I can post here no matter how
// if distance is < 0, then thread is much bigger than me, and I do not have a chance to post there.

        // selected thread
        Node winner = null;
        // cumulative probabilities
        double cum = 0;
        // distances
        HashMap<Node, Double> distances = new HashMap<Node, Double>();
        // array to hold sampled threads
        ArrayList<Node> sampled = new ArrayList<Node>();
        for (Node t : threads) {
            if (t.getDegree() <= 100) { // 100 FOR MMORPG
                sampled.add(t);
            }
        }
        // randomize threads order!!! THIS WAS A HUGE MISSING!!!
        Collections.shuffle(sampled);
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
