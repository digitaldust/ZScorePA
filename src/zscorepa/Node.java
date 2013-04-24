/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zscorepa;

import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author ogabbrie
 */
public class Node {
    private int id;
    private String name;
    private String color;
    private double degree;
    private double clustDot;
    private double clustLowDot;
    private double clustTopDot;
    private double redundancy;
    private Set<Node> twoDistNei;
    private ArrayList<Double> neiDeg;
    private double posts;
    private double threads;
    private double zindex;
    private double cumProbZ;
    private Node startedBy;
    private double zpaPosts;
    private double zpaThreads;
    private double zpaZindex;
    private ArrayList<Node> neiOfNei;

    /**
     * @return the node
     */
    public int getId() {
        return id;
    }

    /**
     * @param node the node to set
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @return the node
     */
    public String getName() {
        return name;
    }

    /**
     * @param node the node to set
     */
    public void setName(String name) {
        this.name = name;
    }

 
    /**
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * @return the degree
     */
    public double getDegree() {
        return degree;
    }

    /**
     * @param degree the degree to set
     */
    public void setDegree(double degree) {
        this.degree = degree;
    }    

    /**
     * @return the redundancy
     */
    public double getRedundancy() {
        return redundancy;
    }

    /**
     * @param redundancy the redundancy to set
     */
    public void setRedundancy(double redundancy) {
        this.redundancy = redundancy;
    }

    /**
     * @return the twoDistNei
     */
    public Set<Node> getTwoDistNei() {
        return twoDistNei;
    }

    /**
     * @param twoDistNei the twoDistNei to set
     */
    public void setTwoDistNei(Set<Node> twoDistNei) {
        this.twoDistNei = twoDistNei;
    }

    /**
     * @return the neiDeg
     */
    public ArrayList<Double> getNeiDeg() {
        return neiDeg;
    }

    /**
     * @param neiDeg the neiDeg to set
     */
    public void setNeiDeg(ArrayList<Double> neiDeg) {
        this.neiDeg = neiDeg;
    }

    /**
     * @return the clustDot
     */
    public double getClustDot() {
        return clustDot;
    }

    /**
     * @param clustDot the clustDot to set
     */
    public void setClustDot(double clustDot) {
        this.clustDot = clustDot;
    }

    /**
     * @return the clustLowDot
     */
    public double getClustLowDot() {
        return clustLowDot;
    }

    /**
     * @param clustLowDot the clustLowDot to set
     */
    public void setClustLowDot(double clustLowDot) {
        this.clustLowDot = clustLowDot;
    }

    /**
     * @return the clustTopDot
     */
    public double getClustTopDot() {
        return clustTopDot;
    }

    /**
     * @param clustTopDot the clustTopDot to set
     */
    public void setClustTopDot(double clustTopDot) {
        this.clustTopDot = clustTopDot;
    }

    /**
     * @return the posts
     */
    public double getPosts() {
        return posts;
    }

    /**
     * @param posts the posts to set
     */
    public void setPosts(double posts) {
        this.posts = posts;
    }

    /**
     * @return the threads
     */
    public double getThreads() {
        return threads;
    }

    /**
     * @param threads the threads to set
     */
    public void setThreads(double threads) {
        this.threads = threads;
    }

    /**
     * @return the zindex
     */
    public double getZindex() {
        return zindex;
    }

    /**
     * @param zindex the zindex to set
     */
    public void setZindex(double zindex) {
        this.zindex = zindex;
    }

    /**
     * @return the cumProbZ
     */
    public double getCumProbZ() {
        return cumProbZ;
    }

    /**
     * @param cumProbZ the cumProbZ to set
     */
    public void setCumProbZ(double cumProbZ) {
        this.cumProbZ = cumProbZ;
    }

    /**
     * @return the startedBy
     */
    public Node getStartedBy() {
        return startedBy;
    }

    /**
     * @param startedBy the startedBy to set
     */
    public void setStartedBy(Node startedBy) {
        this.startedBy = startedBy;
    }

    /**
     * @return the zpaPosts
     */
    public double getZpaPosts() {
        return zpaPosts;
    }

    /**
     * @param zpaPosts the zpaPosts to set
     */
    public void setZpaPosts(double zpaPosts) {
        this.zpaPosts = zpaPosts;
    }

    /**
     * @return the zpaThreads
     */
    public double getZpaThreads() {
        return zpaThreads;
    }

    /**
     * @param zpaThreads the zpaThreads to set
     */
    public void setZpaThreads(double zpaThreads) {
        this.zpaThreads = zpaThreads;
    }

    /**
     * @return the zpaZindex
     */
    public double getZpaZindex() {
        return zpaZindex;
    }

    /**
     * @param zpaZindex the zpaZindex to set
     */
    public void setZpaZindex(double zpaZindex) {
        this.zpaZindex = zpaZindex;
    }

    /**
     * @return the neiOfNei
     */
    public ArrayList<Node> getNeiOfNei() {
        return neiOfNei;
    }

    /**
     * @param neiOfNei the neiOfNei to set
     */
    public void setNeiOfNei(Node aNeiOfNei) {
        this.neiOfNei.add(aNeiOfNei);
    }
    
    public void initializeNeiOfNei(){
        this.neiOfNei = new ArrayList<Node>();
    }
    
}
