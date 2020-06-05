package com.crystal.hello.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List> mList;

    public HomeViewModel() {
        mList = new MutableLiveData<>();
//        mText.setValue("This is home fragment");
    }

    public LiveData<List> getList() {
        return mList;
    }
}