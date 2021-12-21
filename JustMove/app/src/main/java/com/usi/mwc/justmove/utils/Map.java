package com.usi.mwc.justmove.utils;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.usi.mwc.justmove.model.InterestPointModel;
import com.usi.mwc.justmove.model.PointModel;
import com.usi.mwc.justmove.model.TravelModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Map {
    /**
     * will move the camera to the specific position LatLng.
     * @param mGoogleMap
     * @param latLng
     */
    public static void moveCamera(GoogleMap mGoogleMap, LatLng latLng) {
        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17f));
    }

    /**
     * Draws on the map the interestPoints in the interesPointList.
     * @param mGoogleMap
     * @param interestPointList
     */
    public static void drawMarkersOnMap(GoogleMap mGoogleMap, ArrayList<InterestPointModel> interestPointList) {
        interestPointList.forEach(ip -> mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(ip.getLat(), ip.getLon())).title(ip.getName())));
    }

    /**
     * Draws the specific Travel on the map in the provided color (pathColor).
     * @param ctx
     * @param pathColor
     * @param travel
     * @param mGoogleMap
     */
    public static void drawPathOnMap(Context ctx, int pathColor, TravelModel travel, GoogleMap mGoogleMap) {
        List<LatLng> coordList = travel.getPoints().stream().map(p -> {
            return new LatLng(p.getLat(), p.getLon());
        }).collect(Collectors.toList());
        travel.getPoints().forEach(a -> mGoogleMap.addPolyline(
                new PolylineOptions()
                        .clickable(true)
                        .addAll(coordList)
                        .color(ContextCompat.getColor(ctx, pathColor))
        ));
    }
}
