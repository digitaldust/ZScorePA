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
    double getDegree();
    /**
     * @param degree the degree to set
     */
    void setDegree(double degree);
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
    ArrayList<Node> getNei();
    /**
     * @param neiDeg the neiDeg to set
     */
    void setNei(Node nei);
    /**
     * @return the clustDot
     */
    double getClustDot();
    /**
     * @param clustDot the clustDot to set
     */
    void setClustDot(double clustDot);
    /**
     * @return the clustLowDot
     */
    double getClustLowDot();
    /**
     * @param clustLowDot the clustLowDot to set
     */
    void setClustLowDot(double clustLowDot);
    /**
     * @return the clustTopDot
     */
    double getClustTopDot();
    /**
     * @param clustTopDot the clustTopDot to set
     */
    void setClustTopDot(double clustTopDot);
    /**
     * @return the neiOfNei
     */
    Set<Node> getTwoDistNei();
    /**
     * @param neiOfNei the neiOfNei to set
     */
    void setTwoDistNei(Set<Node> aTwoDistNei);
    
    
    void initializeNei();
    /**
     * @return the zindex
     */
    Double getZindex();
    /**
     * @param zindex the zindex to set
     */
    void setZindex(Double zindex);
}
