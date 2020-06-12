package com.crystal.hello.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.HomeRecyclerAdapter;
import com.crystal.hello.R;
import com.plaid.client.response.TransactionsGetResponse;

import java.util.List;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private View root;

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    // https://guides.codepath.com/android/Creating-and-Using-Fragments
    // https://developer.android.com/guide/components/fragments
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);

        observeTransactionList();
        return root;
    }

    private void observeTransactionList() {
        homeViewModel.getList().observe(getViewLifecycleOwner(), new Observer<List<TransactionsGetResponse.Transaction>>() {
            @Override
            public void onChanged(List<TransactionsGetResponse.Transaction> list) {
                final HomeRecyclerAdapter recyclerAdapter = new HomeRecyclerAdapter(getActivity(), list);
                RecyclerView recyclerView = root.findViewById(R.id.recyclerHome);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setAdapter(recyclerAdapter);
            }
        });
    }
}