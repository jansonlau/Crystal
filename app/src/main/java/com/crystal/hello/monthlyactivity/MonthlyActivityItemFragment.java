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
    private String monthAndYearString;
    private MonthlyActivityItemBudgetRecyclerAdapter monthlyActivityItemBudgetRecyclerAdapter;
    private MonthlyActivityItemCreditRecyclerAdapter monthlyActivityItemCreditRecyclerAdapter;
    private MonthlyActivityItemMerchantRecyclerAdapter monthlyActivityItemMerchantRecyclerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        monthAndYearString = Objects.requireNonNull(getArguments()).getString("com.crystal.hello.MONTH_YEAR");

        final Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap =
                (Map<String, List<DocumentSnapshot>>) Objects.requireNonNull(getArguments()).getSerializable("com.crystal.hello.POSITIVE_TRANSACTIONS_MAP");

        final Map<String, List<DocumentSnapshot>> oneMonthNegativeAmountTransactionsByCategoryMap =
                (Map<String, List<DocumentSnapshot>>) Objects.requireNonNull(getArguments()).getSerializable("com.crystal.hello.NEGATIVE_TRANSACTIONS_MAP");

        final Map<String, List<DocumentSnapshot>> oneMonthMerchantTransactionsMap =
                (Map<String, List<DocumentSnapshot>>) Objects.requireNonNull(getArguments()).getSerializable("com.crystal.hello.MERCHANT_TRANSACTIONS_MAP");

        final List<Map<String, Double>> oneMonthAmountByMerchantNameList =
                (List<Map<String, Double>>) Objects.requireNonNull(getArguments()).getSerializable("com.crystal.hello.MERCHANT_AMOUNTS_LIST");

        final List<Map<String, Double>> oneMonthSortedPositiveAmountByCategoryList =
                (List<Map<String, Double>>) Objects.requireNonNull(getArguments()).getSerializable("com.crystal.hello.SORTED_POSITIVE_AMOUNTS_LIST");

        final List<Map<String, Double>> oneMonthNegativeAmountByCategoryList =
                (List<Map<String, Double>>) Objects.requireNonNull(getArguments()).getSerializable("com.crystal.hello.NEGATIVE_AMOUNTS_LIST");

        monthlyActivityItemBudgetRecyclerAdapter = new MonthlyActivityItemBudgetRecyclerAdapter(getActivity()
                , oneMonthPositiveAmountTransactionsByCategoryMap
                , oneMonthSortedPositiveAmountByCategoryList);

        monthlyActivityItemCreditRecyclerAdapter = new MonthlyActivityItemCreditRecyclerAdapter(getActivity()
                , oneMonthNegativeAmountTransactionsByCategoryMap
                , oneMonthNegativeAmountByCategoryList);

        monthlyActivityItemMerchantRecyclerAdapter = new MonthlyActivityItemMerchantRecyclerAdapter(getActivity()
                , oneMonthMerchantTransactionsMap
                , oneMonthAmountByMerchantNameList);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_monthly_activity_item, container, false);
        final TextView monthAndYearTextView = root.findViewById(R.id.monthAndYearTextView);
        final RecyclerView budgetRecyclerView = root.findViewById(R.id.budgetRecyclerView);
        final RecyclerView creditRecyclerView = root.findViewById(R.id.creditRecyclerView);
        final RecyclerView merchantRecyclerView = root.findViewById(R.id.merchantRecyclerView);

        monthAndYearTextView.setText(monthAndYearString);

        budgetRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        budgetRecyclerView.setAdapter(monthlyActivityItemBudgetRecyclerAdapter);

        creditRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        creditRecyclerView.setAdapter(monthlyActivityItemCreditRecyclerAdapter);

        merchantRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        merchantRecyclerView.setAdapter(monthlyActivityItemMerchantRecyclerAdapter);
        return root;
    }
}