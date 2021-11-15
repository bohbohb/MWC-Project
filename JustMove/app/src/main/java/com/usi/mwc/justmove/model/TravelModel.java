package com.usi.mwc.justmove.model;

import java.io.Serializable;
import java.util.ArrayList;

public class TravelModel implements Serializable {

    public TravelModel(Integer id, String name, String comment, Double distance, String time, String dateTravel, ArrayList<PointModel> points) {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.distance = distance;
        this.time = time;
        this.dateTravel = dateTravel;
        this.points = points;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDateTravel() {
        return dateTravel;
    }

    public void setDateTravel(String dateTravel) {
        this.dateTravel = dateTravel;
    }

    public ArrayList<PointModel> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<PointModel> points) {
        this.points = points;
    }

    private Integer id;
    private String name;
    private String comment;
    private Double distance;
    private String time;
    private String dateTravel;
    private ArrayList<PointModel> points;
}