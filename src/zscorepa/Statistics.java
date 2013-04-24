package zscorepa;

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
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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

        // initialzie network object
        UndirectedSparseGraph<Node, Edge> g = new UndirectedSparseGraph<Node, Edge>();
        // retrieve bipartite file name
        String forum = argmnts[0].getString();
        String empiricalPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_bipartite_networks/" + forum + "-empirical.txt";
        String attributesPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_network_attributes/" + forum + "-empirical-attributes.txt";
        
        String usersDegreeDistribution = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_degrees_empiric_networks/users-degree-" + forum + "-empirical.txt";
        String threadsDegreeDistribution = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/_degrees_empiric_networks/threads-degree-" + forum + "-empirical.txt";
        String usersStatsPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/EXPERIMENTS/" + forum + "/users-stats-" + forum + "-empirical.txt";
        String threadsStatsPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/EXPERIMENTS/" + forum + "/threads-stats-" + forum + "-empirical.txt";
        String activityPath = "/Users/digitaldust/Dropbox/Gabbriellini-RFS/EXPERIMENTS/" + forum + "/users-activity-" + forum + "-empirical.txt";
        // arraylist to hold users
        ArrayList<Node> users = new ArrayList<Node>();
        // arraylist to hold threads
        ArrayList<Node> threads = new ArrayList<Node>();
        // read file from _bipartite_networks folder
        try {
            BufferedReader in = new BufferedReader(new FileReader(empiricalPath));
            String line;
            int i = 0;
            System.out.println("g has " + g.getVertexCount() + " vertici");
            System.out.println("g has " + g.getEdgeCount() + " links");
            while ((line = in.readLine()) != null) {
                String[] split = line.split(" ");
                // add user
                Node user = new Node();
                user.setId(Integer.valueOf(split[0].substring(1)));
                user.setColor("red");
                user.setName(split[0]);
                if (checkThreadExists(split[0], users)) {
                    users.add(user);
                    g.addVertex(user);
                } else {
                    user = retrieveNode(split[0], users);
                }
                // add thread
                Node thread = new Node();
                thread.setId(Integer.valueOf(split[1].substring(1)));
                thread.setColor("blue");
                thread.setName(split[1]);
                if (checkThreadExists(split[1], threads)) {
                    threads.add(thread);
                    g.addVertex(thread);
                } else {
                    thread = retrieveNode(split[1], threads);
                }
                // add edge between user and thread
                Edge edge = new Edge();
                edge.setId(i);
                edge.setFromid(user.getName());
                edge.setToid(thread.getName());
                g.addEdge(edge, user, thread, EdgeType.UNDIRECTED);
                i++;
            }
            in.close();
            System.out.println("g has " + g.getVertexCount() + " vertici");
            System.out.println("g has " + g.getEdgeCount() + " links");
            System.out.println("g has " + users.size() + " users");
            System.out.println("g has " + threads.size() + " threads");
        } catch (FileNotFoundException ex) {
            throw new ExtensionException(ex);
        } catch (IOException e) {
            throw new ExtensionException(e);
        }

        // if the network is the empirical one...
        // analyze only largest component
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
        ArrayList<Node> usersLcc = new ArrayList<Node>();
        ArrayList<Node> threadsLcc = new ArrayList<Node>();
        Iterator<Node> nodesLccIter = nodesLcc.iterator();
        while (nodesLccIter.hasNext()) {
            Node node = nodesLccIter.next();
            if (node.getColor().matches("red")) {
                usersLcc.add(node);
            } else {
                threadsLcc.add(node);
            }
        }
        // add users and threads attributes
        BufferedReader attributeFile;
        try {
            attributeFile = new BufferedReader(new FileReader(attributesPath));
            String attr;
            while ((attr = attributeFile.readLine()) != null) {
                final String[] splitUser = attr.split(",");
                for (Node n : usersLcc) {
                    if (n.getName().equals(splitUser[0])) {
                        n.setZpaPosts(Double.valueOf(splitUser[1]));
                        n.setZpaThreads(Double.valueOf(splitUser[2]));
                        double zindex = (n.getZpaPosts() - n.getZpaThreads()) / Math.sqrt(n.getZpaPosts() + n.getZpaThreads());
                        n.setZindex(Double.valueOf(zindex));
                    }
                }
            }
            attributeFile.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Statistics.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Statistics.class.getName()).log(Level.SEVERE, null, ex);
        }
        // 
        System.out.println("lcc has " + lcc.getVertexCount() + " vertici");
        System.out.println("lcc has " + usersLcc.size() + " user");
        System.out.println("lcc has " + threadsLcc.size() + " threads");
        // write activity file.
        System.out.println("EMPIRICAL ACTIVITY ANALYSYS");
        WriteStats.writeActivity(g, users, activityPath);
        System.out.println("EMPIRICAL WRITE DEGREE DISTRIBUTION");
        WriteStats.writeDegreeDistribution(g, usersLcc, usersDegreeDistribution);
        WriteStats.writeDegreeDistribution(g, threadsLcc, threadsDegreeDistribution);
        System.out.println("EMPIRICAL WRITE STATS");
        WriteStats.writeBipartiteStats(g, users, threads, usersStatsPath, threadsStatsPath);
        System.out.println("EMPIRICAL ANALYSIS DONE");
    }

    private boolean checkThreadExists(String name, ArrayList<Node> nodes) {
        for (Node n : nodes) {
            if (name.equals(n.getName())) {
                return false;
            }
        }
        return true;
    }

    private Node retrieveNode(String name, ArrayList<Node> nodes) {
        for (Node n : nodes) {
            if (name.equals(n.getName())) {
                return n;
            }
        }
        // if this happens, it means that the existence check failed beforehand!!!
        return null;
    }
}
