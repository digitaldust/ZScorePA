/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.zscorepa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Simone Gabbriellini
 */
public class Node implements NodeInterface  {
    private int id;
    private String name;
    private String color;
    private Integer degree;
    private Set<Node> nei;
    private List<Integer> neiDegree;
    private Set<Node> twoDistNei;
    private Double redundancy;
    private Double CC;
    private Double CClow;
    private Double CCtop;
    
    // don't know
    private Double zindex;
    private List<Node> neiOfNei;
    private List<Double> neiDeg;

    public Node(){
        this.nei = new HashSet<Node>();
        this.twoDistNei = new HashSet<Node>();
        this.neiDegree = new ArrayList<Integer>();
    }
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
    public Integer getDegree() {
        return degree;
    }

    /**
     * @param degree the degree to set
     */
    @Override
    public void setDegree(Integer degree) {
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
    public Set<Node> getNei() {
        return nei;
    }

    /**
     * @param neiDeg the neiDeg to set
     */
    @Override
    public void setNei(Collection<Node> myNei) {
        this.nei.addAll(myNei);
    }
    
    @Override
    public void initializeNei(){
        this.nei = new HashSet<Node>();
    }
    /**
     * @return the clustDot
     */
    @Override
    public double getCC() {
        return CC;
    }

    /**
     * @param clustDot the clustDot to set
     */
    @Override
    public void setCC(double clustDot) {
        this.CC = clustDot;
    }

    /**
     * @return the clustLowDot
     */
    @Override
    public double getCClow() {
        return CClow;
    }

    /**
     * @param clustLowDot the clustLowDot to set
     */
    @Override
    public void setCClow(double clustLowDot) {
        this.CClow = clustLowDot;
    }

    /**
     * @return the clustTopDot
     */
    @Override
    public double getCCtop() {
        return CCtop;
    }

    /**
     * @param clustTopDot the clustTopDot to set
     */
    @Override
    public void setCCtop(double clustTopDot) {
        this.CCtop = clustTopDot;
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
    public void setTwoDistNei(Node aTwoDistNei) {
        this.twoDistNei.add(aTwoDistNei);
    }
    
    public void setAllTwoDistNei(Set<Node> aTwoDistNei) {
        this.twoDistNei = aTwoDistNei;
    }
    
    /**
     * @return the zindex
     */
    @Override
    public Double getZindex() {
        return zindex;
    }

    /**
     * @param zindex the zindex to set
     */
    @Override
    public void setZindex(Double zindex) {
        this.zindex = zindex;
    }

    /**
     * @return the neiOfNei
     */
    public List<Node> getNeiOfNei() {
        return neiOfNei;
    }

    /**
     * @param neiOfNei the neiOfNei to set
     */
    public void setNeiOfNei(List<Node> neiOfNei) {
        this.neiOfNei = neiOfNei;
    }

    /**
     * @return the neiDeg
     */
    public List<Double> getNeiDeg() {
        return neiDeg;
    }

    /**
     * @param neiDeg the neiDeg to set
     */
    public void setNeiDeg(ArrayList<Double> neiDeg) {
        this.neiDeg = neiDeg;
    }

    @Override
    public void setNeiDegree(Integer neiDegree) {
        this.neiDegree.add(neiDegree);
    }

    @Override
    public List<Integer> getNeiDegree() {
        return neiDegree;
    }

}
