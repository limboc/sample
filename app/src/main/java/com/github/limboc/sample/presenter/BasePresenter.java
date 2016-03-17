package com.github.limboc.sample.presenter;

import com.github.limboc.sample.DrakeetFactory;
import com.github.limboc.sample.GankApi;
import com.github.limboc.sample.data.SimpleResult;
import com.github.limboc.sample.presenter.iview.IBaseView;
import com.github.limboc.sample.utils.ApiException;

import java.net.ConnectException;
import java.net.SocketException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class BasePresenter<T extends IBaseView> implements IPresenter<T> {

    private T mView;
    public CompositeSubscription mCompositeSubscription;
    public GankApi mDataManager;


    @Override
    public void attachView(T mvpView) {
        this.mView = mvpView;
        this.mCompositeSubscription = new CompositeSubscription();
        this.mDataManager = DrakeetFactory.getGankIOSingleton();
    }

    @Override
    public void detachView() {
        this.mView = null;
        this.mCompositeSubscription.unsubscribe();
        this.mCompositeSubscription = null;
        this.mDataManager = null;
    }

    public boolean isViewAttached() {
        return mView != null;
    }

    public T getView() {
        return mView;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new MvpViewNotAttachedException();
    }

    public static class MvpViewNotAttachedException extends RuntimeException {
        public MvpViewNotAttachedException() {
            super("Please call Presenter.attachView(MvpView) before" +
                    " requesting data to the Presenter");
        }
    }

    public <T> void toSubscribe(Observable<T> o, Subscriber<T> s){
        o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s);
    }

    public class HttpResultFunc<T> implements Func1<SimpleResult<T>, T> {

        @Override
        public T call(SimpleResult<T> simpleResult) {
            if (simpleResult.getResultCode() == -1) {
                throw new ApiException(100);
            }
            return simpleResult.getResults();
        }
    }

    public void handleError(Throwable throwable){
        throwable.printStackTrace();
        if(throwable instanceof SocketException || throwable instanceof ConnectException){
            getView().showMessage("连不到服务器");
        }else{
            getView().showMessage(throwable.getMessage());
        }
    }
}
