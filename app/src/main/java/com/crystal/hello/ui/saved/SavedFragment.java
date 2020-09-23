package com.crystal.hello.ui.saved;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.R;
import com.crystal.hello.TransactionRecyclerAdapter;

public class SavedFragment extends Fragment {
    private View root;
    private SavedViewModel savedViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedViewModel = new ViewModelProvider(this).get(SavedViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_saved, container, false);
        observeSavedTransactionList();
        savedViewModel.getSavedTransactionsFromDatabase();
        return root;
    }

    private void observeSavedTransactionList() {
        final RecyclerView savedTransactionsRecyclerView = root.findViewById(R.id.savedTransactionsRecyclerView);
        savedTransactionsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        savedViewModel.getMutableSavedTransactionsList().observe(getViewLifecycleOwner(), list -> {
            final TransactionRecyclerAdapter recyclerAdapter = new TransactionRecyclerAdapter(getActivity(), list);
            savedTransactionsRecyclerView.setAdapter(recyclerAdapter);
        });
    }
}