package com.crystal.hello;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TransactionMonthlyActivityItemFragment extends Fragment {
    private TransactionMonthlyActivityItemViewModel mViewModel;
    private View root;

    public static TransactionMonthlyActivityItemFragment newInstance() {
        return new TransactionMonthlyActivityItemFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_transaction_monthly_activity_item, container, false);

        final MonthlyActivityItemBudgetRecyclerAdapter monthlyActivityItemBudgetRecyclerAdapter = new MonthlyActivityItemBudgetRecyclerAdapter(getActivity());
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewBudget);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(monthlyActivityItemBudgetRecyclerAdapter);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(TransactionMonthlyActivityItemViewModel.class);
        // TODO: Use the ViewModel
    }

}