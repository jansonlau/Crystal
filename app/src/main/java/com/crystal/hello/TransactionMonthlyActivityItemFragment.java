package com.crystal.hello;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Map;

public class TransactionMonthlyActivityItemFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TransactionMonthlyActivityItemViewModel monthlyActivityItemViewModel = new ViewModelProvider(this).get(TransactionMonthlyActivityItemViewModel.class);
        View root = inflater.inflate(R.layout.fragment_transaction_monthly_activity_item, container, false);

        Map<String, List<DocumentSnapshot>> oneMonthTransactionsByCategoryMap =
                (Map<String, List<DocumentSnapshot>>) getArguments().getSerializable("com.crystal.hello.TRANSACTIONS_MAP");

        final MonthlyActivityItemBudgetRecyclerAdapter monthlyActivityItemBudgetRecyclerAdapter =
                new MonthlyActivityItemBudgetRecyclerAdapter(getActivity(), oneMonthTransactionsByCategoryMap);

        RecyclerView recyclerView = root.findViewById(R.id.recyclerViewBudget);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(monthlyActivityItemBudgetRecyclerAdapter);

        String monthAndYearString = getArguments().getString("com.crystal.hello.MONTH_YEAR");
        TextView monthAndYearTextView = root.findViewById(R.id.monthAndYearTextView);
        monthAndYearTextView.setText(monthAndYearString);
        return root;
    }
}