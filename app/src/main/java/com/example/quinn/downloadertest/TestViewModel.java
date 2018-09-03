package com.example.quinn.downloadertest;

import android.arch.lifecycle.MutableLiveData;

/**
 * @author 黄兴伟 (xwd9989@gamil.com)
 * @since 2018/7/10
 */
public class TestViewModel extends android.arch.lifecycle.ViewModel {

    static final MutableLiveData<String> gMutableLiveData = new MutableLiveData<>();

    public TestViewModel() {
        gMutableLiveData.setValue("hello world !");
    }


    public MutableLiveData<String> get() {
        return gMutableLiveData;
    }

}
