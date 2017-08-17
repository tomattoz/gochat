package red.tel.chat.ui.presenter;

import android.support.annotation.UiThread;

import red.tel.chat.ui.views.BaseView;

/**
 * Created by hoanghiep on 8/17/17.
 */

public interface BasePresenter<V extends BaseView> {
    @UiThread
    void attachView(V view);

    @UiThread
    void detachView();
}
