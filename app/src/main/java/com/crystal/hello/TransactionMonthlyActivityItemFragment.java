package com.crystal.hello;

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

import org.joda.time.DateTime;

public class TransactionMonthlyActivityItemFragment extends Fragment {
    private TransactionMonthlyActivityItemViewModel monthlyActivityItemViewModel;
    private View root;
    private int originalPosition;

    public static TransactionMonthlyActivityItemFragment newInstance() {
        return new TransactionMonthlyActivityItemFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        monthlyActivityItemViewModel = new ViewModelProvider(this).get(TransactionMonthlyActivityItemViewModel.class);
        root = inflater.inflate(R.layout.fragment_transaction_monthly_activity_item, container, false);

        final MonthlyActivityItemBudgetRecyclerAdapter monthlyActivityItemBudgetRecyclerAdapter =
                new MonthlyActivityItemBudgetRecyclerAdapter(getActivity());
        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewBudget);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(monthlyActivityItemBudgetRecyclerAdapter);

        String oldestTransactionDate = getArguments().getString("com.crystal.hello.OLDEST_TRANSACTION");
//        String latestTransactionDate = getArguments().getString("com.crystal.hello.LATEST_TRANSACTION");
        int currentPosition = getArguments().getInt("com.crystal.hello.ITEM_POSITION");
        int monthsCount = getArguments().getInt("com.crystal.hello.ITEM_COUNT");
        originalPosition = monthsCount - 1;

        DateTime endDate = new DateTime();
        DateTime startDate = endDate.minusMonths(1);
        monthlyActivityItemViewModel.getPositiveTransactionsByMonthFromDatabase("Shopping", startDate.toString(), endDate.toString());

        return root;
    }
}