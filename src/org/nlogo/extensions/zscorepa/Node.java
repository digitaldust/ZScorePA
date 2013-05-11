/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.zscorepa;

import java.util.ArrayList;
import java.util.Set;

/**
 * 
 * @author Simone Gabbriellini
 */
public class Node implements NodeInterface {
    private int id;
    private String name;
    private String color;
    private double degree;
    private double clustDot;
    private double clustLowDot;
    private double clustTopDot;
    private double redundancy;
    private double zindex;
    private ArrayList<Node> nei;
    private Set<Node> twoDistNei;
    private Set<Node> neiOfNei;
    private ArrayList<Double> neiDeg;

    /**
     * @return the node
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * @param node the node to set
     */
    @Override
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @return the node
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param node the node to set
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

 
    /**
     * @return the color
     */
    @Override
    public String getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    @Override
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * @return the degree
     */
    @Override
    public double getDegree() {
        return degree;
    }

    /**
     * @param degree the degree to set
     */
    @Override
    public void setDegree(double degree) {
        this.degree = degree;
    }    

    /**
     * @return the redundancy
     */
    @Override
    public double getRedundancy() {
        return redundancy;
    }

    /**
     * @param redundancy the redundancy to set
     */
    @Override
    public void setRedundancy(double redundancy) {
        this.redundancy = redundancy;
    }

    /**
     * @return the neiDeg
     */
    @Override
    public ArrayList<Node> getNei() {
        return nei;
    }

    /**
     * @param neiDeg the neiDeg to set
     */
    @Override
    public void setNei(Node aNei) {
        this.nei.add(aNei);
    }
    
    @Override
    public void initializeNei(){
        this.nei = new ArrayList<Node>();
    }
    /**
     * @return the clustDot
     */
    @Override
    public double getClustDot() {
        return clustDot;
    }

    /**
     * @param clustDot the clustDot to set
     */
    @Override
    public void setClustDot(double clustDot) {
        this.clustDot = clustDot;
    }

    /**
     * @return the clustLowDot
     */
    @Override
    public double getClustLowDot() {
        return clustLowDot;
    }

    /**
     * @param clustLowDot the clustLowDot to set
     */
    @Override
    public void setClustLowDot(double clustLowDot) {
        this.clustLowDot = clustLowDot;
    }

    /**
     * @return the clustTopDot
     */
    @Override
    public double getClustTopDot() {
        return clustTopDot;
    }

    /**
     * @param clustTopDot the clustTopDot to set
     */
    @Override
    public void setClustTopDot(double clustTopDot) {
        this.clustTopDot = clustTopDot;
    }


    /**
     * @return the neiOfNei
     */
    
    @Override
    public Set<Node> getTwoDistNei() {
        return twoDistNei;
    }

    /**
     * @param neiOfNei the neiOfNei to set
     */
    @Override
    public void setTwoDistNei(Set<Node> aTwoDistNei) {
        this.twoDistNei = aTwoDistNei;
    }
    
    public void setAllTwoDistNei(Set<Node> aTwoDistNei) {
        this.twoDistNei = aTwoDistNei;
    }
    
    /**
     * @return the zindex
     */
    @Override
    public double getZindex() {
        return zindex;
    }

    /**
     * @param zindex the zindex to set
     */
    @Override
    public void setZindex(double zindex) {
        this.zindex = zindex;
    }

    /**
     * @return the neiOfNei
     */
    public Set<Node> getNeiOfNei() {
        return neiOfNei;
    }

    /**
     * @param neiOfNei the neiOfNei to set
     */
    public void setNeiOfNei(Set<Node> neiOfNei) {
        this.neiOfNei = neiOfNei;
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
}
