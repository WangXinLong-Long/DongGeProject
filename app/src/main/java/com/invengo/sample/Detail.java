package com.invengo.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.invengo.util.Body.UserBody;
import com.invengo.util.okHttp.Rxjava.ApiClient;

import static android.content.ContentValues.TAG;


/**
 * Created by Administrator on 2017/12/21.
 */

public class Detail extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        System.out.println("220000");
        request();
//        Goodbean good = new Goodbean();
//        Goodbean.DataBean dataBean = new Goodbean.DataBean();
//        dataBean.setRfid("213123135");
//        good.setData(dataBean);
//        good.setAction("get_goods_baseinfo");
//        good.setAppid("dmgb930887eadd538cc");
//        good.setDvid("master");
//        good.setAccess_token("ea1fac5431b0b812f84a8145a6e28eed08a9b059");
//        Gson gson = new Gson();
//        String jsonStr = gson.toJson(good);
//        HttpUtils http = new HttpUtils();
//        RequestParams param = new RequestParams();
//        /* private String appid;
//        private String dvid;
//        private String access_token;
//        private String action;
//        private DataBean data;*/
//        param.addBodyParameter("appid", "dmgb930887eadd538cc");
//        param.addBodyParameter("dvid", "dmgb930887eadd538cc");
//        param.addBodyParameter("access_token", "dmgb930887eadd538cc");
//        param.addBodyParameter("action", "dmgb930887eadd538cc");
////data : {"rfid":"213123135"}
//        param.addBodyParameter("data", "{\"rfid\":\"213123135\"}");
//        BodyParamsEntity bodyParamsEntity = new BodyParamsEntity();
////        bodyParamsEntity.
////        param.setBodyEnti,ty(new StringEntity(jsonStr,charset));
//        http.send(HttpRequest.HttpMethod.POST, "https://wx.dreamgo.shop/sapi/", param, new RequestCallBack<String>() {
//
//            @Override
//            public void onSuccess(ResponseInfo<String> responseInfo) {
//                Toast.makeText(Detail.this, responseInfo.result, Toast.LENGTH_SHORT).show();
//                System.out.println(responseInfo.result);
//
//
//            }
//
//            @Override
//            public void onFailure(HttpException error, String msg) {
//                Toast.makeText(Detail.this, "shiabbn", Toast.LENGTH_SHORT).show();
//            }
//        });


    }

    private void request() {
        ApiClient.getObserList(new UserBody()).subscribe(bean -> {
            Log.i(TAG, "request: "+bean);
//            getView().delteSuccess();
        }, throwable -> {
            Log.e(TAG, "request: ");
//            getView().delteFailed();
        });

    }


}






