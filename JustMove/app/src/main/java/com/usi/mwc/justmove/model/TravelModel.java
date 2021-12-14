package com.usi.mwc.justmove.model;

import java.io.Serializable;
import java.util.ArrayList;

public class TravelModel implements Serializable {

    public TravelModel(Integer id, String name, String comment, Double distance, String time, String dateTravel, ArrayList<PointModel> points, Integer steps, Integer publibike) {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.distance = distance;
        this.time = time;
        this.dateTravel = dateTravel;
        this.points = points;
        this.nbSteps = steps;
        this.publibike = publibike;
    }

    public TravelModel() {
        this.id = 0;
        this.name = "";
        this.comment = "";
        this.distance = 0.0;
        this.time = "";
        this.dateTravel = "";
        this.points = new ArrayList<>();
        this.nbSteps = 0;
        this.publibike = 0;
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

    public int getTimeMillisec() {
        String[] timeArr = time.split(":");
        int millisecTime = 0;

        for (int i = 0; i < 3; i ++) {
            if (i == 0) {
                millisecTime += Integer.parseInt(timeArr[i])*3600000;
            } else if (i == 1) {
                millisecTime += Integer.parseInt(timeArr[i])*60000;
            } else {
                millisecTime += Integer.parseInt(timeArr[i])*1000;
            }
        }
        return millisecTime;
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

    public Integer getNbSteps() {
        return nbSteps;
    }

    public void setNbSteps(Integer nbSteps) {
        this.nbSteps = nbSteps;
    }

    public Integer getPublibike() {
        return publibike;
    }

    public void setPublibike(Integer publibike) {
        this.publibike = publibike;
    }

    private String dateTravel;
    private Integer nbSteps;
    private Integer publibike;
    private ArrayList<PointModel> points;
}
