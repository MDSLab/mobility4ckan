package it.unime.embeddedsystems;


import retrofit.Callback;
import retrofit.http.GET;

public interface RestInterface {

    @GET("/weather?q=London,uk")
    void getWheatherReport(Callback<Model> cb);

}