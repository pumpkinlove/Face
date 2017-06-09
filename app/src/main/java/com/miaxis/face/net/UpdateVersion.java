package com.miaxis.face.net;

import com.miaxis.face.bean.AjaxResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by xu.nan on 2017/5/26.
 */

public interface UpdateVersion {

    @POST("faceid/app/getAppInfo")
    Call<AjaxResponse> checkVersion();

    @Streaming
    @GET
    Call<ResponseBody> downVersion(@Url String url);

}
