package com.crystal.hello;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.crystal.hello.ui.home.HomeViewModel;

public class TransactionItemDetailViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private MutableLiveData<String> mText;

    public TransactionItemDetailViewModel() {
        mText = new MutableLiveData<>();
//        mText.setValue("This is item detail fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}