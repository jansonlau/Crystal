package com.crystal.hello.ui.saved;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.crystal.hello.R;

public class SavedFragment extends Fragment {

    private SavedViewModel savedViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        savedViewModel =
                new ViewModelProvider(this).get(SavedViewModel.class);
        View root = inflater.inflate(R.layout.fragment_saved, container, false);

        return root;
    }
}