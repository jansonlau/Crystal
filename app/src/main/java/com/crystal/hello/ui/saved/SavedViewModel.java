package com.crystal.hello.ui.saved;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SavedViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SavedViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is where you'll see your saved transactions. Coming soon!");
    }

    public LiveData<String> getText() {
        return mText;
    }
}