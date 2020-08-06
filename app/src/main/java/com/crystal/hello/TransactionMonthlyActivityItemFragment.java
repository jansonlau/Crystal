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

import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

//        String oldestTransactionDate = getArguments().getString("com.crystal.hello.OLDEST_TRANSACTION");
//        String latestTransactionDate = getArguments().getString("com.crystal.hello.LATEST_TRANSACTION");
        int currentPosition = getArguments().getInt("com.crystal.hello.ITEM_POSITION");
        int monthsCount = getArguments().getInt("com.crystal.hello.ITEM_COUNT");
        originalPosition = monthsCount - 1;

        LocalDate endDate = new LocalDate().minusMonths(originalPosition - currentPosition);
        LocalDate startDate = endDate.withDayOfMonth(1).minusMonths(originalPosition - currentPosition + 1);

        monthlyActivityItemViewModel.getTransactionsByCategoryAndMonthFromDatabase("Shopping"
                , Collections.singletonList("Shops")
                , startDate.toString()
                , endDate.toString());
        monthlyActivityItemViewModel.getTransactionsByCategoryAndMonthFromDatabase("Food & Drinks"
                , Collections.singletonList("Food and Drink")
                , startDate.toString()
                , endDate.toString());
        monthlyActivityItemViewModel.getTransactionsByCategoryAndMonthFromDatabase("Travel"
                , Collections.singletonList("Travel")
                , startDate.toString()
                , endDate.toString());
        monthlyActivityItemViewModel.getTransactionsByCategoryAndMonthFromDatabase("Entertainment"
                , Collections.singletonList("Recreation")
                , startDate.toString()
                , endDate.toString());
        monthlyActivityItemViewModel.getTransactionsByCategoryAndMonthFromDatabase("Health"
                , Collections.singletonList("Healthcare")
                , startDate.toString()
                , endDate.toString());
        monthlyActivityItemViewModel.getTransactionsByCategoryAndMonthFromDatabase("Services"
                , Arrays.asList("Service", "Community", "Payment", "Bank Fees", "Interest", "Tax", "Transfer")
                , startDate.toString()
                , endDate.toString());

        monthlyActivityItemViewModel.getMutableTransactionsByCategoryMap().observe(getViewLifecycleOwner(), new Observer<Map<String, List<DocumentSnapshot>>>() {
            @Override
            public void onChanged(Map<String, List<DocumentSnapshot>> transactionsByCategoryMap) {
                final int numberOfCategories = 6;
                if (transactionsByCategoryMap.size() == numberOfCategories) {
                    final MonthlyActivityItemBudgetRecyclerAdapter monthlyActivityItemBudgetRecyclerAdapter =
                            new MonthlyActivityItemBudgetRecyclerAdapter(getActivity()
                                    , transactionsByCategoryMap);

                    RecyclerView recyclerView = root.findViewById(R.id.recyclerViewBudget);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(monthlyActivityItemBudgetRecyclerAdapter);
                }
            }
        });

        String monthAndYearString = endDate.monthOfYear().getAsText() + " " + endDate.getYear();
        TextView monthAndYearTextView = root.findViewById(R.id.monthAndYearTextView);
        monthAndYearTextView.setText(monthAndYearString);

        return root;
    }
}