package com.crystal.hello.monthlyactivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MonthlyActivityItemFragment extends Fragment {
    private MonthlyActivityItemBudgetRecyclerAdapter monthlyActivityItemBudgetRecyclerAdapter;
    private String monthAndYearString;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap =
                (Map<String, List<DocumentSnapshot>>) Objects.requireNonNull(getArguments()).getSerializable("com.crystal.hello.POSITIVE_TRANSACTIONS_MAP");

        final List<DocumentSnapshot> oneMonthNegativeAmountPaymentsList =
                (List<DocumentSnapshot>) Objects.requireNonNull(getArguments()).getSerializable("com.crystal.hello.NEGATIVE_PAYMENTS_LIST");

        final List<DocumentSnapshot> oneMonthNegativeAmountRefundsList =
                (List<DocumentSnapshot>) Objects.requireNonNull(getArguments()).getSerializable("com.crystal.hello.NEGATIVE_REFUNDS_LIST");

        final List<Map.Entry<String, Double>> sortedPositiveAmountByCategoryList =
                (List<Map.Entry<String, Double>>) Objects.requireNonNull(getArguments()).getSerializable("com.crystal.hello.SORTED_POSITIVE_AMOUNTS_LIST");

        monthAndYearString = Objects.requireNonNull(getArguments()).getString("com.crystal.hello.MONTH_YEAR");
        monthlyActivityItemBudgetRecyclerAdapter = new MonthlyActivityItemBudgetRecyclerAdapter(getActivity()
                , oneMonthPositiveAmountTransactionsByCategoryMap
                , sortedPositiveAmountByCategoryList);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_monthly_activity_item, container, false);
        final RecyclerView budgetRecyclerView = root.findViewById(R.id.budgetRecyclerView);
        final TextView monthAndYearTextView = root.findViewById(R.id.monthAndYearTextView);

        budgetRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        budgetRecyclerView.setAdapter(monthlyActivityItemBudgetRecyclerAdapter);
        monthAndYearTextView.setText(monthAndYearString);
        return root;
    }
}