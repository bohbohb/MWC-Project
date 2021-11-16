package com.usi.mwc.justmove.api;

import com.usi.mwc.justmove.model.Station;
import com.usi.mwc.justmove.model.Stations;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PublibikeAPIInterface {

    /**
     *
     * @return all visible PubliBike stations including all the vehicles currently at a station.
     */
    @GET("partner/stations")
    Call<Stations> getStationsData();

    /**
     *
     * @return display the location of the stations on a map and whether or not bikes are available.
     * (id, lat, log, state)
     */
    @GET("stations")
    Call<Stations> getLocationsData();

    /**
     *
     * @param stationId
     * @return Returns detailed information about a specific station and the vehicels currently at this station.
     */
    @GET("partner/stations/{id}")
    Call<Station> getStationData(@Path("id") int stationId);

}
