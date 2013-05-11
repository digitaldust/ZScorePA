/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.zscorepa;

/**
 *
 * @author Simone Gabbriellini
 */
public class Thread extends Node {
    
    private User startedBy;
    private double chance;

    /**
     * @return the startedBy
     */
    public User getStartedBy() {
        return startedBy;
    }

    /**
     * @param startedBy the startedBy to set
     */
    public void setStartedBy(User startedBy) {
        this.startedBy = startedBy;
    }

    /**
     * @return the chance
     */
    public double getChance() {
        return chance;
    }

    /**
     * @param chance the chance to set
     */
    public void setChance(double chance) {
        this.chance = chance;
    }
}
