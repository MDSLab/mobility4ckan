package it.unime.embeddedsystems;

import com.google.gson.annotations.Expose;

/**
 * Created by luca on 17/05/16.
 */
public class Model {


    @Expose
    private String base;
    @Expose
    private Integer visibility;
    @Expose
    private Integer dt;
    @Expose
    private Integer id;
    @Expose
    private String name;
    @Expose
    private Integer cod;


    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }


}
