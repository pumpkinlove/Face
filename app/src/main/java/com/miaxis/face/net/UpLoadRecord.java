package com.miaxis.face.net;

import com.miaxis.face.bean.AjaxResponse;
import com.miaxis.face.bean.Record;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2017/5/23 0023.
 */

public interface UpLoadRecord {

    @POST("person/uploadPerson")
    Call<AjaxResponse> upLoadRecord(@Body Record record);
}
