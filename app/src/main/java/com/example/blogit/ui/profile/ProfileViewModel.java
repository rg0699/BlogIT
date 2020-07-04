package com.example.blogit.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileViewModel extends ViewModel {

    public static MutableLiveData<String> mUserName;
    public static MutableLiveData<String> mUserBio;
    public static MutableLiveData<String> mUserEmail;
    public static MutableLiveData<String> mUserWebsite;
    public static MutableLiveData<String> mUserDob;


    public LiveData<String> getUserName() {
        if (mUserName == null) {
            mUserName = new MutableLiveData<>();
            mUserName.setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        }
        return mUserName;
    }

    public LiveData<String> getUserBio() {
        if (mUserBio == null) {
            mUserBio = new MutableLiveData<>();
            mUserBio.setValue("My Bio");
        }
        return mUserBio;
    }

    public LiveData<String> getUserWebsite() {
        if (mUserWebsite == null) {
            mUserWebsite = new MutableLiveData<>();
            mUserWebsite.setValue("Website");
        }
        return mUserWebsite;
    }

    public LiveData<String> getUserDob() {
        if (mUserDob == null) {
            mUserDob = new MutableLiveData<>();
            mUserDob.setValue("Date of Birth");
        }
        return mUserDob;
    }

}