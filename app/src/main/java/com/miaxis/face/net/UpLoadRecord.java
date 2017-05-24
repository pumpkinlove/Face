package com.miaxis.face.net;

import com.miaxis.face.bean.AjaxResponse;
import com.miaxis.face.bean.Record;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2017/5/23 0023.
 */

public interface UpLoadRecord {

    @POST("faceid/person/uploadPerson")
    @FormUrlEncoded
    Call<AjaxResponse> upLoadRecord(
            @Field("id") String id,
            @Field("cardNo") String cardNo,
            @Field("name") String name,
            @Field("sex") String sex,
            @Field("birthday") String birthday,
            @Field("address") String address,
            @Field("busEntity") String busEntity,
            @Field("status") String status,
            @Field("cardImg") String cardImg,
            @Field("faceImg") String faceImg,
            @Field("finger0") String finger0,
            @Field("finger1") String finger1,
            @Field("printFinger") String printFinger,
            @Field("location") String location,
            @Field("longitude") String longitude,
            @Field("latitude") String latitude,
            @Field("createDate") String createDate,
            @Field("devsn") String devsn,
            @Field("cardId") String cardId
    );
}
