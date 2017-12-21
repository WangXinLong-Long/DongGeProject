package com.invengo.util.okHttp.Rxjava;


import android.text.TextUtils;


import com.invengo.app.Appli;
import com.invengo.exception.ApiException;
import com.invengo.sample.R;
import com.invengo.util.okHttp.BaseEntity;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;

/**
 * User: Axl_Jacobs(Axl.Jacobs@gmail.com)
 * Date: 2016-09-01
 * Time: 20:27
 * FIXME
 * Rx处理服务器返回
 */
class RxResultHelper {
//    static <T> ObservableTransformer<Reply<T>, T> cacheResult() {
//        return upstream -> upstream.flatMap(new Function<Reply<T>, ObservableSource<T>>() {
//            @Override
//            public ObservableSource<T> apply(Reply<T> resultBeanReply) throws Exception {
//                return Observable.just(resultBeanReply.getData());
//            }
//        });
//    }


    static ObservableTransformer<ResponseBody, String> filterResultToString() {
        return tObservabe -> tObservabe.flatMap(new Function<ResponseBody, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(ResponseBody responseBody) throws Exception {
                String string = "";
                try {
                    string = responseBody.string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return createData(string);
            }
        });
    }


    public static <T> ObservableTransformer<BaseEntity<T>, T> handleResult() {
        return tObservable -> tObservable.flatMap(new Function<BaseEntity<T>, ObservableSource<T>>() {
            @Override
            public ObservableSource<T> apply(BaseEntity<T> entity) throws Exception {
                String code = "5000", msg = Appli.getContext().getString(R.string.net_error_msg_unknow_str);

                if (entity != null && entity.isOk()) {
                    //防止某些接口返回data为null
                    if (entity.data == null) {
                        entity.data = (T) Boolean.valueOf(true);
                    }

                    return createData(entity.data);
                } else {
                    if (entity != null) {
                        code = entity.code;
                        //添加错误消息判定
                        if (!TextUtils.isEmpty(entity.message)) {
                            msg = entity.message;
                        } else {
                             /* if (!TextUtils.isEmpty(entity.result))
                            msg = entity.result;*/

                            //如果服务器返回的消息为空，则通过错误码判定
//                            if (TextUtils.equals(entity.code, "0001")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_0001_str);
//                            } else if (TextUtils.equals(entity.code, "1001")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1001_str);
//                            } else if (TextUtils.equals(entity.code, "1002")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1002_str);
//                            } else if (TextUtils.equals(entity.code, "1003")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1003_str);
//                            } else if (TextUtils.equals(entity.code, "1004")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1004_str);
//                            } else if (TextUtils.equals(entity.code, "1005")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1005_str);
//                            } else if (TextUtils.equals(entity.code, "1006")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1006_str);
//                                ApiClient.toRefToken((Callback) null);
//                            } else if (TextUtils.equals(entity.code, "1007")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1007_str);
//                                Utils.toLogin();
//                            } else if (TextUtils.equals(entity.code, "1008")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1008_str);
//                            } else if (TextUtils.equals(entity.code, "1009")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1009_str);
//                            } else if (TextUtils.equals(entity.code, "1010")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1010_str);
//                            } else if (TextUtils.equals(entity.code, "0002")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_0002_str);
//                            } else if (TextUtils.equals(entity.code, "0003")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_0003_str);
//                            } else if (TextUtils.equals(entity.code, "1011")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1011_str);
//                            } else if (TextUtils.equals(entity.code, "1012")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1012_str);
//                            } else if (TextUtils.equals(entity.code, "1013")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1013_str);
//                            } else if (TextUtils.equals(entity.code, "1014")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1014_str);
//                            } else if (TextUtils.equals(entity.code, "1015")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1015_str);
//                            } else if (TextUtils.equals(entity.code, "1016")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1016_str);
//                            } else if (TextUtils.equals(entity.code, "1017")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1017_str);
//                            } else if (TextUtils.equals(entity.code, "1018")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1018_str);
//                            } else if (TextUtils.equals(entity.code, "1019")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1019_str);
//                            } else if (TextUtils.equals(entity.code, "1020")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1020_str);
//                            } else if (TextUtils.equals(entity.code, "1031")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_1031_str);
//                            } else if (TextUtils.equals(entity.code, "3001")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_3001_str);
//                            } else if (TextUtils.equals(entity.code, "3002")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_3002_str);
//                            } else if (TextUtils.equals(entity.code, "3003")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_3003_str);
//                            } else if (TextUtils.equals(entity.code, "3004")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_3004_str);
//                            } else if (TextUtils.equals(entity.code, "3005")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_3005_str);
//                            } else if (TextUtils.equals(entity.code, "3006")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_3006_str);
//                            } else if (TextUtils.equals(entity.code, "3007")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_3007_str);
//                            } else if (TextUtils.equals(entity.code, "3008")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_3008_str);
//                            } else if (TextUtils.equals(entity.code, "3009")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_3009_str);
//                            } else if (TextUtils.equals(entity.code, "3010")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_3010_str);
//                            } else if (TextUtils.equals(entity.code, "3011")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_3011_str);
//                            } else if (TextUtils.equals(entity.code, "3012")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_3012_str);
//                            } else if (TextUtils.equals(entity.code, "4001")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_4001_str);
//                            } else if (TextUtils.equals(entity.code, "4002")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_4002_str);
//                            } else if (TextUtils.equals(entity.code, "4003")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_4003_str);
//                            } else if (TextUtils.equals(entity.code, "6001")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6001_str);
//                            } else if (TextUtils.equals(entity.code, "6002")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6002_str);
//                            } else if (TextUtils.equals(entity.code, "6003")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6003_str);
//                            } else if (TextUtils.equals(entity.code, "6004")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6004_str);
//                            } else if (TextUtils.equals(entity.code, "6011")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6011_str);
//                            } else if (TextUtils.equals(entity.code, "6012")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6012_str);
//                            } else if (TextUtils.equals(entity.code, "6013")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6013_str);
//                            } else if (TextUtils.equals(entity.code, "6014")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6014_str);
//                            } else if (TextUtils.equals(entity.code, "6015")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6015_str);
//                            } else if (TextUtils.equals(entity.code, "6016")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6016_str);
//                            } else if (TextUtils.equals(entity.code, "6017")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6017_str);
//                            } else if (TextUtils.equals(entity.code, "6021")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6021_str);
//                            } else if (TextUtils.equals(entity.code, "6031")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6031_str);
//                            } else if (TextUtils.equals(entity.code, "6032")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6032_str);
//                            } else if (TextUtils.equals(entity.code, "6040")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6040_str);
//                            } else if (TextUtils.equals(entity.code, "6041")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_6041_str);
//                            } else if (TextUtils.equals(entity.code, "7001")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7001_str);
//                            } else if (TextUtils.equals(entity.code, "7002")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7002_str);
//                            } else if (TextUtils.equals(entity.code, "7003")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7003_str);
//                            } else if (TextUtils.equals(entity.code, "7004")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7004_str);
//                            } else if (TextUtils.equals(entity.code, "7005")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7005_str);
//                            } else if (TextUtils.equals(entity.code, "7006")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7006_str);
//                            } else if (TextUtils.equals(entity.code, "7007")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7007_str);
//                            } else if (TextUtils.equals(entity.code, "7011")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7011_str);
//                            } else if (TextUtils.equals(entity.code, "7012")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7012_str);
//                            } else if (TextUtils.equals(entity.code, "7013")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7013_str);
//                            } else if (TextUtils.equals(entity.code, "7014")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7014_str);
//                            } else if (TextUtils.equals(entity.code, "7051")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7051_str);
//                            } else if (TextUtils.equals(entity.code, "7052")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7052_str);
//                            } else if (TextUtils.equals(entity.code, "7053")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7053_str);
//                            } else if (TextUtils.equals(entity.code, "7054")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7054_str);
//                            } else if (TextUtils.equals(entity.code, "7055")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7055_str);
//                            } else if (TextUtils.equals(entity.code, "7061")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7061_str);
//                            } else if (TextUtils.equals(entity.code, "7071")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_7071_str);
//                            } else if (TextUtils.equals(entity.code, "8001")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_8001_str);
//                            } else if (TextUtils.equals(entity.code, "8002")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_8002_str);
//                            } else if (TextUtils.equals(entity.code, "8003")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_8003_str);
//                            } else if (TextUtils.equals(entity.code, "8004")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_8004_str);
//                            } else if (TextUtils.equals(entity.code, "9001")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_9001_str);
//                            } else if (TextUtils.equals(entity.code, "9002")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_9002_str);
//                            } else if (TextUtils.equals(entity.code, "9003")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_9003_str);
//                            } else if (TextUtils.equals(entity.code, "10000")) {
//                                msg = Appli.getContext().getString(R.string.net_error_msg_10000_str);
//                            } else {
//                                msg = entity.result;
//                            }
                            msg = entity.result;
                        }


                    }
                }
                return Observable.error(new ApiException(code, msg));
            }
        });
    }

    private static <T> Observable<T> createData(T t) {
        return Observable.create(emitter -> {
            try {
                emitter.onNext(t);
                emitter.onComplete();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }
}