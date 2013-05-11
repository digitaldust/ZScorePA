/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.zscorepa;

/**
 *
 * @author digitaldust
 */
public class User extends Node {

    private double postsToDo;
    private double postsDone;
    private double threadsToDo;
    private double threadsDone;

    /**
     * @return the postsToDo
     */
    public double getPostsToDo() {
        return postsToDo;
    }

    /**
     * @param postsToDo the postsToDo to set
     */
    public void setPostsToDo(double postsToDo) {
        this.postsToDo = postsToDo;
    }

    /**
     * @return the postsDone
     */
    public double getPostsDone() {
        return postsDone;
    }

    /**
     * @param postsDone the postsDone to set
     */
    public void setPostsDone(double postsDone) {
        this.postsDone = postsDone;
    }

    /**
     * @return the threadsToDo
     */
    public double getThreadsToDo() {
        return threadsToDo;
    }

    /**
     * @param threadsToDo the threadsToDo to set
     */
    public void setThreadsToDo(double threadsToDo) {
        this.threadsToDo = threadsToDo;
    }

    /**
     * @return the threadsDone
     */
    public double getThreadsDone() {
        return threadsDone;
    }

    /**
     * @param threadsDone the threadsDone to set
     */
    public void setThreadsDone(double threadsDone) {
        this.threadsDone = threadsDone;
    }
    
}
