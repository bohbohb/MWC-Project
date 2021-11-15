package com.usi.mwc.justmove.api;

import com.usi.mwc.justmove.model.Stations;

import retrofit2.Call;
import retrofit2.http.GET;

public interface publibikeAPIInterface {

    @GET("partner/stations/")
    Call<Stations> getNoticeData();
}
