/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.zscorepa;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Simone Gabbriellini
 */
public interface NodeInterface {
    /**
     * @return the node
     */
    int getId();
    /**
     * @param node the node to set
     */
    void setId(int id);
    /**
     * @return the node
     */
    String getName();
    /**
     * @param node the node to set
     */
    void setName(String name);
    /**
     * @return the color
     */
    String getColor();
    /**
     * @param color the color to set
     */
    void setColor(String color);
    /**
     * @return the degree
     */
    Integer getDegree();
    /**
     * @param degree the degree to set
     */
    void setDegree(Integer degree);
    /**
     * @return the redundancy
     */
    double getRedundancy();
    /**
     * @param redundancy the redundancy to set
     */
    void setRedundancy(double redundancy);
    /**
     * @return the neiDeg
     */
    Set<Node> getNei();
    /**
     * @param neiDeg the neiDeg to set
     */
    void setNei(Collection<Node> nei);
    /**
     * @return the clustDot
     */
    double getCC();
    /**
     * @param clustDot the clustDot to set
     */
    void setCC(double clustDot);
    /**
     * @return the clustLowDot
     */
    double getCClow();
    /**
     * @param clustLowDot the clustLowDot to set
     */
    void setCClow(double clustLowDot);
    /**
     * @return the clustTopDot
     */
    double getCCtop();
    /**
     * @param clustTopDot the clustTopDot to set
     */
    void setCCtop(double clustTopDot);
    /**
     * @return the neiOfNei
     */
    Set<Node> getTwoDistNei();
    /**
     * @param neiOfNei the neiOfNei to set
     */
    void setTwoDistNei(Node aTwoDistNei);
    
    
    void initializeNei();
    /**
     * @return the zindex
     */
    Double getZindex();
    /**
     * @param zindex the zindex to set
     */
    void setZindex(Double zindex);
    
    /**
     *
     * @param neiDegree
     */
    void setNeiDegree(Integer neiDegree);
    
    /**
     *
     * @return
     */
    List<Integer> getNeiDegree();
}
