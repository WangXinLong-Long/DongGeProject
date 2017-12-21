package com.invengo.util.okHttp.Rxjava;

import com.google.gson.JsonObject;
import com.invengo.util.Body.UserBody;
import com.invengo.util.User;
import com.invengo.util.okHttp.BaseEntity;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author wxl
 * @date on 2017/12/21.
 * @describe:
 */

public class ApiClient {
    public static Observable<List<JsonObject>> getObserList(UserBody body) {

        return OKHTTP.getInstance().getRequestManager().getUsers(body).compose(RxSchedulersHelper.io_main()).compose(RxResultHelper.handleResult());
    }
}
