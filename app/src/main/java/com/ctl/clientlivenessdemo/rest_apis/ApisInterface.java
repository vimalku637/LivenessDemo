package com.ctl.clientlivenessdemo.rest_apis;


import com.google.gson.JsonObject;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApisInterface {
    @Headers({
            "Content-Type: application/json",
            "x-karza-key: ia5r4fD8zTID2QH8rot5"
    })
    @POST("v3/get-jwt")
    Call<JsonObject> getTokenForSilentLivenessSDK(@Body RequestBody body);
}
