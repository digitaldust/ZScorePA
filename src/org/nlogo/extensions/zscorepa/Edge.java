/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nlogo.extensions.zscorepa;

/**
 *
 * @author ogabbrie
 */
public class Edge {
    private String fromid;
    private String toid;
    
    public Edge(Node user, Node thread){
        this.fromid = user.getName();
        this.toid = thread.getName();
    }
    /**
     * @return the fromid
     */
    public String getFromid() {
        return fromid;
    }

    /**
     * @param fromid the fromid to set
     */
    public void setFromid(String fromid) {
        this.fromid = fromid;
    }

    /**
     * @return the toid
     */
    public String getToid() {
        return toid;
    }

    /**
     * @param toid the toid to set
     */
    public void setToid(String toid) {
        this.toid = toid;
    }
    
}
