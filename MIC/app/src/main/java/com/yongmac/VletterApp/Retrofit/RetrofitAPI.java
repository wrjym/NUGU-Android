package com.yongmac.VletterApp.Retrofit;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RetrofitAPI {
    @GET("/NUGU/getUserList")
    Call<UserListVO> getUsers();

}
