package com.usi.mwc.justmove.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class publibikeAPIClient {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://api.publibike.ch/v1/public/";

    /**
     * Create an instance of Retrofit object
     * */
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }


}
