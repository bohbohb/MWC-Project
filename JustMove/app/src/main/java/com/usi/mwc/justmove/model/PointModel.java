package com.usi.mwc.justmove.model;

import java.io.Serializable;
import java.util.Objects;

public class PointModel implements Serializable {
    private Integer id;
    private Double lat;
    private Double lon;
    private Integer idNextPoint;

    public PointModel(Double lat, Double lon, Integer idNextPoint, Integer idTravel) {
        this.id = 0;
        this.lat = lat;
        this.lon = lon;
        this.idNextPoint = idNextPoint;
        this.idTravel = idTravel;
    }

    public PointModel(Double lat, Double lon, Integer idTravel) {
        this.lat = lat;
        this.lon = lon;
        this.idTravel = idTravel;
    }

    public PointModel(double latitude, double longitude) {
        this.id = 0;
        this.lat = latitude;
        this.lon = longitude;
        this.idNextPoint = -1;
        this.idTravel = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointModel that = (PointModel) o;
        return lat.equals(that.lat) && lon.equals(that.lon);
    }

    public PointModel(Integer id, Double lat, Double lon, Integer idNextPoint, Integer idTravel) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.idNextPoint = idNextPoint;
        this.idTravel = idTravel;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getIdNextPoint() {
        return idNextPoint;
    }

    public void setIdNextPoint(Integer idNextPoint) {
        this.idNextPoint = idNextPoint;
    }

    public Integer getIdTravel() {
        return idTravel;
    }

    public void setIdTravel(Integer idTravel) {
        this.idTravel = idTravel;
    }

    private Integer idTravel;
}
