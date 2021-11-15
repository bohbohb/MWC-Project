package com.usi.mwc.justmove.model;

import java.io.Serializable;

public class InterestPointModel implements Serializable {
    private Integer id;
    private String name;
    private Double lat;
    private Double lon;
    private Integer idTravel;

    public InterestPointModel(Integer id, String name, Double lat, Double lon, Integer idTravel) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.idTravel = idTravel;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Integer getIdTravel() {
        return idTravel;
    }

    public void setIdTravel(Integer idTravel) {
        this.idTravel = idTravel;
    }
}
