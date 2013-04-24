/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zscorepa;

/**
 *
 * @author ogabbrie
 */
public class Edge {
    private int id;
    private String fromid;
    private String toid;
    private String color;
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
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    
}
