package com.crystal.hello.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ProfileViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Hi there! " +
                "\n\n" +
                "Profile page is currently in progress. This is where you'll be able to add more credit card accounts and sign out. " +
                "\n\nA lot more features will be coming soon like saved transactions, transaction history, graph of your balance for all accounts, and upcoming bills. " +
                "\n\nIn the meantime, if you'd like to log out or have any questions or feedback, please send us an email!" +
                "\n\n" +
                "Best,\n" +
                "Crystal team\n" +
                "San Francisco, CA\n" +
                "support@crystalspend.com\n");
    }

    public LiveData<String> getText() {
        return mText;
    }
}