package com.example.markhorsman.insphireapi.api;

import com.example.markhorsman.insphireapi.model.CustomerContact;
import com.example.markhorsman.insphireapi.model.Status;
import com.example.markhorsman.insphireapi.model.Stock;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface Insphire {

    @GET("stock/{barcode}")
    Call<Stock> getStockItem(@Header("Authorization") String authHeader, @Path("barcode") String barcode);

    @GET("customer/{reference}")
    Call<CustomerContact> getCustomerContact(@Header("Authorization") String authHeader, @Path("reference") String reference);

    @PUT("stock/status/{itemno}")
    Call<Status> updateStockStatus(@Header("Authorization") String authHeader, @Path("itemno") String itemno, @Body Stock stock);

}
