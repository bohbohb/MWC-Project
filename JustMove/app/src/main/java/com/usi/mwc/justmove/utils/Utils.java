package com.usi.mwc.justmove.utils;

import android.os.SystemClock;
import android.widget.Chronometer;

import com.google.android.gms.maps.model.LatLng;
import com.usi.mwc.justmove.model.InterestPointModel;
import com.usi.mwc.justmove.model.PointModel;
import com.usi.mwc.justmove.model.TravelModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.OptionalDouble;

public class Utils {

    public static String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss, dd.MM.yyyy");
        return simpleDateFormat.format(new Date());
    }

    public static Double distanceM(PointModel p1, PointModel p2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(p2.getLat() - p1.getLat());
        double lonDistance = Math.toRadians(p2.getLon() - p1.getLon());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(p1.getLat())) * Math.cos(Math.toRadians(p2.getLat()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

    public Double distancePersonPoint(LatLng person, LatLng point){
        int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(point.latitude - person.latitude);
        double lonDistance = Math.toRadians(point.longitude - person.longitude);
        double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + (Math.cos(Math.toRadians(person.latitude)) * Math.cos(Math.toRadians(person.longitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        distance = Math.pow(distance, 2.0);
        return Math.sqrt(distance);
    }

    public static Double getDistanceForTravel(TravelModel t) {
        double dist = 0.0;

//        for (i in b.points.indices) {
//            if ((i + 1) < b.points.size)
//                dist += distance(b.points[i], b.points[i + 1])
//        }

        ArrayList<PointModel> points = t.getPoints();
        int count = 0;
        while (count < points.size() - 2) {
            dist += distanceM(points.get(count), points.get(count + 1));
            count ++;
        }

        return dist;
    }

    public static Double[] centroid(ArrayList<PointModel> points) {
        Double[] centroid = new Double[]{0.0, 0.0};

        // TODO : Convert to Java
//        for (i in points.indices) {
//            centroid[0] += points[i].lat
//            centroid[1] += points[i].lon
//        }
        int totalPoints = points.size();
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;
        return centroid;
    }

    public String getMoyPerKm(String t, Double d) {
        if (d != 0.0) {
            String[] p = t.split(":");
            double totMins = Double.parseDouble(p[0]) * 60.0 + Double.parseDouble(p[1]) + Double.parseDouble(p[2]) / 60.0;
            totMins /= d / 1000;
            int decPart = (int) (totMins - Integer.parseInt(String.valueOf(totMins)));
            return String.valueOf((int)totMins) + '\'' + String.valueOf((int)(decPart * 60.0)) + '\"';
        } else {
            return "0'00\"";
        }
    }

    public String getPointsForStaticMap(TravelModel t) {
        StringBuilder str = new StringBuilder();
        t.getPoints().forEach(p -> {
            str.append(p.getLat().toString() + "," + p.getLon().toString() + "|");
        });

        return str.substring(0, str.length() - 1);
    }

    public String getMarkersPointsForStaticMap(List<InterestPointModel> ms) {
        StringBuilder str = new StringBuilder();

        ms.forEach(m -> {
            str.append(m.getLat().toString() + "," + m.getLon().toString() + "|");
        });

        return str.substring(0, str.length() - 1);
    }

    public static String ticksToHHMMSS(Chronometer chronometer) {
        double time = SystemClock.elapsedRealtime() - chronometer.getBase();
        int h = (int) (time / 3600000);
        int m = (int) (time - h * 3600000) / 60000;
        int s = (int) (time - h * 3600000 - m * 60000) / 1000;
        return String.format("%s:%s:%s",
                (h < 10) ? String.format("0%d", h) : String.valueOf(h),
                (m < 10) ? String.format("0%d", m) : String.valueOf(m),
                (s < 10) ? String.format("0%d", s) : String.valueOf(s)
        );
    }

    public static double distanceKM(PointModel p1, PointModel p2) {
        return distanceM(p1, p2) / 1000.0;
    }
}
