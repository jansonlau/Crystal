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

public class MonthlyActivityItemFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_monthly_activity_item, container, false);
        final String monthAndYearString = requireArguments().getString("com.crystal.hello.MONTH_YEAR");
        final TextView monthAndYearTextView = root.findViewById(R.id.monthAndYearTextView);
        monthAndYearTextView.setText(monthAndYearString);

        initializeBudgetAndCategories(root);
        initializeMerchants(root);
        initializePaymentsAndRefunds(root);
        return root;
    }

    private void initializeBudgetAndCategories(View root) {
        final Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap =
                (Map<String, List<DocumentSnapshot>>) requireArguments().getSerializable("com.crystal.hello.POSITIVE_TRANSACTIONS_MAP");

        if (oneMonthPositiveAmountTransactionsByCategoryMap != null) {
            final List<Map<String, Double>> oneMonthSortedPositiveAmountByCategoryList =
                    (List<Map<String, Double>>) requireArguments().getSerializable("com.crystal.hello.SORTED_POSITIVE_AMOUNTS_LIST");
            final Map<String, Object> budgetsMap =
                    (Map<String, Object>) requireArguments().getSerializable("com.crystal.hello.BUDGETS_MAP");
            final MonthlyActivityItemBudgetRecyclerAdapter monthlyActivityItemBudgetRecyclerAdapter = new MonthlyActivityItemBudgetRecyclerAdapter(getActivity()
                    , oneMonthPositiveAmountTransactionsByCategoryMap
                    , oneMonthSortedPositiveAmountByCategoryList
                    , budgetsMap);

            final RecyclerView budgetRecyclerView = root.findViewById(R.id.budgetRecyclerView);
            budgetRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            budgetRecyclerView.setAdapter(monthlyActivityItemBudgetRecyclerAdapter);

            requireArguments().remove("com.crystal.hello.POSITIVE_TRANSACTIONS_MAP");
            requireArguments().remove("com.crystal.hello.SORTED_POSITIVE_AMOUNTS_LIST");
            requireArguments().remove("com.crystal.hello.BUDGETS_MAP");
        } else {
            root.findViewById(R.id.monthlyActivityNoTransactionsTextView).setVisibility(View.VISIBLE);
        }
    }

    private void initializeMerchants(View root) {
        final Map<String, List<DocumentSnapshot>> oneMonthMerchantTransactionsMap =
                (Map<String, List<DocumentSnapshot>>) requireArguments().getSerializable("com.crystal.hello.MERCHANT_TRANSACTIONS_MAP");

        if (oneMonthMerchantTransactionsMap != null) {
            final List<Map<String, Double>> oneMonthAmountByMerchantNameList =
                    (List<Map<String, Double>>) requireArguments().getSerializable("com.crystal.hello.MERCHANT_AMOUNTS_LIST");
            final MonthlyActivityItemMerchantRecyclerAdapter monthlyActivityItemMerchantRecyclerAdapter = new MonthlyActivityItemMerchantRecyclerAdapter(getActivity()
                    , oneMonthMerchantTransactionsMap
                    , oneMonthAmountByMerchantNameList);

            final RecyclerView merchantRecyclerView = root.findViewById(R.id.merchantRecyclerView);
            merchantRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            merchantRecyclerView.setAdapter(monthlyActivityItemMerchantRecyclerAdapter);

            requireArguments().remove("com.crystal.hello.MERCHANT_TRANSACTIONS_MAP");
            requireArguments().remove("com.crystal.hello.MERCHANT_AMOUNTS_LIST");
        } else {
            root.findViewById(R.id.merchantsTextView).setVisibility(View.GONE);
        }
    }

    private void initializePaymentsAndRefunds(View root) {
        final Map<String, List<DocumentSnapshot>> oneMonthNegativeAmountTransactionsByCategoryMap =
                (Map<String, List<DocumentSnapshot>>) requireArguments().getSerializable("com.crystal.hello.NEGATIVE_TRANSACTIONS_MAP");

        if (oneMonthNegativeAmountTransactionsByCategoryMap != null) {
            final List<Map<String, Double>> oneMonthNegativeAmountByCategoryList =
                    (List<Map<String, Double>>) requireArguments().getSerializable("com.crystal.hello.NEGATIVE_AMOUNTS_LIST");
            MonthlyActivityItemCreditRecyclerAdapter monthlyActivityItemCreditRecyclerAdapter = new MonthlyActivityItemCreditRecyclerAdapter(getActivity()
                    , oneMonthNegativeAmountTransactionsByCategoryMap
                    , oneMonthNegativeAmountByCategoryList);

            final RecyclerView creditRecyclerView = root.findViewById(R.id.creditRecyclerView);
            creditRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            creditRecyclerView.setAdapter(monthlyActivityItemCreditRecyclerAdapter);

            requireArguments().remove("com.crystal.hello.NEGATIVE_TRANSACTIONS_MAP");
            requireArguments().remove("com.crystal.hello.NEGATIVE_AMOUNTS_LIST");
        } else {
            root.findViewById(R.id.textViewMerchants).setVisibility(View.GONE);
        }
    }
}