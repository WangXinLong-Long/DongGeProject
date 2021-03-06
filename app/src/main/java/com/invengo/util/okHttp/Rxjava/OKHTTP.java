package com.invengo.util.okHttp.Rxjava;



import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.invengo.app.Appli;
import com.invengo.exception.ApiException;
import com.invengo.util.okHttp.BaseEntity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;

import ch.ntb.usb.Utils;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * 网络请求基础类
 * Created by xiaoyu.zhang on 2016/11/7 16:07
 * Email:zhangxyfs@126.com
 */
public class OKHTTP {
    private static final String TAG = "OKHTTP";
    private static OKHTTP mInstance;
    private final OkHttpClient mClient;
//    private final RxCache mRxCache;

    public static final int HTTP_CONNECTION_TIMEOUT = 180 * 1000;
    private RequestManager requestManager;

    public static OKHTTP getInstance() {
        if (mInstance == null) {
            synchronized (OKHTTP.class) {
                if (mInstance == null) {
                    mInstance = new OKHTTP();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     */
    private OKHTTP() {
        //log 拦截器
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
//        if (Utils.isDebug()) {
            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        } else {
//            logInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
//        }
        //添加head 的拦截器
//        Interceptor headInterceptor = chain -> {
//            Context context = Appli.getContext();
//            Request originalRequest = chain.request();
//
//            String token = "";
//            UserToken userTokenInfo = SPreference.getUserToken();
//            if (userTokenInfo != null) {
//                token = userTokenInfo.accessToken;
//            }
//            token = TextUtils.isEmpty(token) ? "" : token;
//
//            Request.Builder builder = originalRequest.newBuilder();
//            builder.removeHeader(NetConfig.DefaultParams.contentType);
//
//            builder.addHeader(NetConfig.DefaultParams.accessToken, token);
//            builder.addHeader(NetConfig.DefaultParams.appVersion, String.valueOf(Utils.getVersionCode(context)));
//            builder.addHeader(NetConfig.DefaultParams.deviceId, Utils.getIMEI(context));
//            builder.addHeader(NetConfig.DefaultParams.os, "android");
//            builder.addHeader(NetConfig.DefaultParams.osVersion, Build.MODEL);
//            builder.addHeader(NetConfig.DefaultParams.contentType, "application/json");
//            builder.addHeader(NetConfig.DefaultParams.language, LanguageSettingUtil.get().getLanguage());
//            builder.addHeader(NetConfig.DefaultParams.time_zone, TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT));
//            Request authorised = builder.build();
//
//            Utils.log("\naccessToken:" + token + "\n" +
//                    "deviceId:" + Utils.getIMEI(Appli.getContext()) + "\n" +
//                    "version:" + Utils.getVersionCode(context) + "\n" +
//                    "osVersion" + Build.MODEL, "d");
//
//            return chain.proceed(authorised);
//        };
//
//        Interceptor ossHeadInterceptor = chain -> {
//            Request originalRequest = chain.request();
//            Request.Builder builder = originalRequest.newBuilder();
//            String url = chain.request().url().toString();
//            OSSDaoUtils ossDaoUtils = new OSSDaoUtils();
//            OSSPublicInfo publicInfo = ossDaoUtils.getPublicOSSInfo(SPreference.getUserId());
//            if (publicInfo != null && NetConfig.isNeedSignOSS
//                    && !(url.contains("//" + publicInfo.getBucketName() + ".") || url.contains("//caching."))) {//如果是public就不做签名
//                OSSInfo info = ossDaoUtils.getOSSInfo(SPreference.getUserId(), SPreference.getDefaultCompanyId());
//                String objectKey = "";
//                if (info != null && url.contains(info.getEndPoint())) {
//                    String urls[] = url.split(info.getEndPoint() + "\\/");
//                    if (urls.length > 1) {
//                        objectKey = urls[1];
//                    }
//                    OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(info.getAccessKeyId(),
//                            info.getAccessKeySecret(), info.getSecurityToken());
//                    OSSFederationToken federationToken = ((OSSStsTokenCredentialProvider) credentialProvider).getFederationToken();
//                    RequestMessage requestMessage = new RequestMessage();
//                    requestMessage.setIsAuthorizationRequired(true);//是否需要授权
//                    requestMessage.setMethod(HttpMethod.GET);
//                    requestMessage.setEndpoint(URI.create(info.getEndPoint()));
//                    requestMessage.setBucketName(info.getBucketName());
//                    requestMessage.setCredentialProvider(credentialProvider);
//                    requestMessage.getHeaders().put(OSSHeaders.DATE, DateUtil.formatRfc822Date(new Date()));
//                    requestMessage.setObjectKey(objectKey);
//                    OSSUtils.signRequest(requestMessage);
//
//                    builder.addHeader(OSSHeaders.OSS_SECURITY_TOKEN, federationToken.getSecurityToken());//1
//                    builder.addHeader(OSSHeaders.AUTHORIZATION, requestMessage.getHeaders().get(OSSHeaders.AUTHORIZATION));//2
//                    builder.addHeader(OSSHeaders.DATE, requestMessage.getHeaders().get(OSSHeaders.DATE));//3
//                }
//            }
//            ossDaoUtils.destory();
//            ossDaoUtils = null;
//
//            Request authorised = builder.build();
//            return chain.proceed(authorised);
//        };
//
//        //重试拦截器
        Interceptor relayInterceptor = chain -> {
            Request request = chain.request();
            Response response = chain.proceed(chain.request());
            ResponseBody responseBody = response.body();
            Charset UTF8 = Charset.forName("UTF-8");

//            Utils.log(TAG, response.request().url().toString() + " " + response.code(), "d");
            String message = "";
            if (response.code() != 200) {
                if (response.code() == 500) {
                    message = "请求错误";
                } else if (response.code() == 404) {
                    message = "请求地址错误";
                }
                httpCodeInterceptor(responseBody, UTF8, response, message);
            } else {

            }
            return response;
        };

        //身份验证拦截器 如果得到401 Not Authorized未授权的错误
//        Authenticator authenticator = (route, response) -> {
//            if (!response.isSuccessful())
//                throw new ApiException(String.valueOf(response.code()), "请求错误");
//            Utils.log(TAG, response.toString());
//            return null;
//        };

        OkHttpClient client = new OkHttpClient();
        mClient = client.newBuilder()
                .readTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(logInterceptor)//设置应用拦截器，主要用于设置公共参数，头信息，日志拦截等
//                .addInterceptor(relayInterceptor)
//                .addNetworkInterceptor(headInterceptor)//设置网络拦截器，主要用于重试或重写
//                .authenticator(authenticator)
 .build();

//        mGlideClient = client.newBuilder().readTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
//                .connectTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
//                .writeTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
//                .retryOnConnectionFailure(true)
//                .addInterceptor(logInterceptor)//设置应用拦截器，主要用于设置公共参数，头信息，日志拦截等
//                .addNetworkInterceptor(ossHeadInterceptor)
//                .authenticator(authenticator).build();

//        mOKGoClient = client.newBuilder().readTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
//                .connectTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
//                .writeTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
//                .retryOnConnectionFailure(true)
//                .addInterceptor(logInterceptor)//设置应用拦截器，主要用于设置公共参数，头信息，日志拦截等
//                .addNetworkInterceptor(ossHeadInterceptor)//设置网络拦截器，主要用于重试或重写
//                .authenticator(authenticator).build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(mClient)
                .baseUrl(NetConfig.SERVER_ADD + "/")
                .addConverterFactory(GsonConverterFactory.create())//json转换器
                .addConverterFactory(ScalarsConverterFactory.create())//字符串转换器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//RxJavaCallAdapterFactory
                .build();

        requestManager = retrofit.create(RequestManager.class);

//        mRxCache = new RxCache.Builder()
//                .appVersion(Utils.getVersionCode(Appli.getContext()))
//                .diskDir(new File(CacheManager.getCachePath(Appli.getContext(), CacheManager.HTTP_CACHE)))
//                .diskConverter(new GsonDiskConverter())//支持Serializable、Json(GsonDiskConverter)
//                .memoryMax(2 * 1024 * 1024)
//                .diskMax(20 * 1024 * 1024)
//                .build();
    }

    public OkHttpClient getOkClient() {
        return mClient;
    }

//    public RxCache getRxCache() {
//        return mRxCache;
//    }

    public static void refOKHTTPClient() {
        mInstance = new OKHTTP();
    }


    RequestManager getRequestManager() {
        return requestManager;
    }

    RequestManager getRequestManager(boolean isNeedReset) {
        if (isNeedReset) {
            mInstance = new OKHTTP();
        }
        return requestManager;
    }

    RequestManager getRequestManager(String serverUrl, boolean isNeedGson) {
        if (isNeedGson) {
            return getRequestManager(serverUrl);
        } else {
            return new Retrofit.Builder()
                    .client(mClient)
                    .baseUrl(serverUrl + "/")
//                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build().create(RequestManager.class);
        }
    }

    //用于oss
//    RequestManager getPrivateRequestManager(String serverUrl, String accessKey, String screctKey) {
//        OkHttpClient client = new OkHttpClient();
//        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
//        if (NetConfig.isLocal) {
//            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        } else {
//            logInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
//        }
//        Interceptor headInterceptor = chain -> {
//            Context context = Appli.getContext();
//            Request originalRequest = chain.request();
//
//            Request.Builder builder = originalRequest.newBuilder();
//            builder.removeHeader(NetConfig.DefaultParams.contentType);
//
//            builder.addHeader(NetConfig.DefaultParams.authorization, OSSUtils.sign(accessKey, screctKey, ""));
//            Request authorised = builder.build();
//
//
//            return chain.proceed(authorised);
//        };
//
//
//        OkHttpClient mClient = client.newBuilder().readTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
//                .connectTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
//                .writeTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
//                .retryOnConnectionFailure(true)
//                .addInterceptor(logInterceptor)//设置应用拦截器，主要用于设置公共参数，头信息，日志拦截等
//                .addNetworkInterceptor(headInterceptor)//设置网络拦截器，主要用于重试或重写
//                /*.authenticator(authenticator)*/.build();
//
//
//        return new Retrofit.Builder()
//                .client(mClient)
//                .baseUrl(serverUrl + "/")
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .build().create(RequestManager.class);
//    }


    RequestManager getRequestManager(String serverUrl) {
        return new Retrofit.Builder()
                .client(mClient)
                .baseUrl(serverUrl + "/")
                .addConverterFactory(GsonConverterFactory.create())
//                .addConverterFactory(ScalarsConverterFactory.create())//字符串转换器
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(RequestManager.class);
    }

    private void httpCodeInterceptor(ResponseBody responseBody, Charset UTF8, Response response, String msg) throws IOException {
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();

        Charset charset;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            try {
                charset = contentType.charset(UTF8);
            } catch (UnsupportedCharsetException e) {
                throw new ApiException(String.valueOf(response.code()), response.message());
            }

            if (responseBody.contentLength() != 0) {
                BaseEntity result = new Gson().fromJson(buffer.clone().readString(charset), BaseEntity.class);
                if (result != null && !TextUtils.isEmpty(result.result)) {
                    msg = result.result;
                }
                throw new ApiException(String.valueOf(response.code()), msg);
            }
        }
        throw new ApiException(String.valueOf(response.code()), response.message());
    }
}
