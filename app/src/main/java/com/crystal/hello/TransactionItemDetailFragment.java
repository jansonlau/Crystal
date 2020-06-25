package com.crystal.hello;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crystal.hello.ui.profile.ProfileViewModel;

public class TransactionItemDetailFragment extends Fragment {

    private TransactionItemDetailViewModel transactionItemDetailViewModel;

    public static TransactionItemDetailFragment newInstance() {
        return new TransactionItemDetailFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_transaction_item_detail, container, false);

        transactionItemDetailViewModel = new ViewModelProvider(this).get(TransactionItemDetailViewModel.class);
        View root = inflater.inflate(R.layout.fragment_transaction_item_detail, container, false);
        final TextView textView = root.findViewById(R.id.text_detail);
        transactionItemDetailViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
    }

}