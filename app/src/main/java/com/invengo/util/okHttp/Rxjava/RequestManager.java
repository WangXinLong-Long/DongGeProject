package com.invengo.util.okHttp.Rxjava;

import com.google.gson.JsonObject;
import com.invengo.util.Body.UserBody;
import com.invengo.util.User;
import com.invengo.util.okHttp.BaseEntity;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2017/12/21.
 */

interface RequestManager {
    @POST("login")
    Observable<BaseEntity<List<JsonObject>>> getUsers(@Body UserBody user);
}
