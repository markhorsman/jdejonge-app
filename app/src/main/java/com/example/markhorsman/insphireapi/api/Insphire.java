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

    @GET("stock/{barcode}/{contno}/{acct}")
    Call<Stock> getStockItem(@Header("Authorization") String authHeader, @Path("barcode") String barcode, @Path("contno") String contno, @Path("acct") String acct);

    @GET("customer/{reference}")
    Call<CustomerContact> getCustomerContact(@Header("Authorization") String authHeader, @Path("reference") String reference);

    @PUT("stock/status/{itemno}/{contno}")
    Call<Status> updateStockStatus(@Header("Authorization") String authHeader, @Path("itemno") String itemno, @Path("contno") String contno, @Body Stock stock);

    @POST("contitem/{itemno}/{contstatus}/{stockstatus}/{qty}")
    Call<Status> insertContItem(
            @Header("Authorization") String authHeader,
            @Path("itemno") String itemno,
            @Path("contstatus") int contStatus,
            @Path("stockstatus") int stockStatus,
            @Path("qty") int qty,
            @Body CustomerContact customerContact
    );
}
