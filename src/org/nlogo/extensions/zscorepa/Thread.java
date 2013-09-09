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
    private double posts;
    private double appeal;

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
     * @return the appeal
     */
    public double getAppeal() {
        return appeal;
    }

    /**
     * @param appeal the appeal to set
     */
    public void setAppeal(double appeal) {
        this.appeal = appeal;
    }
}
