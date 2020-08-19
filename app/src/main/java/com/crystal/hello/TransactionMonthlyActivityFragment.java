package com.crystal.hello;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TransactionMonthlyActivityFragment extends Fragment {
    private TransactionMonthlyActivityViewModel viewModel;
    private View root;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionMonthlyActivityViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_transaction_monthly_activity, container, false);
        observeInitializePagerBoolean();
        return root;
    }

    private void observeInitializePagerBoolean() {
        viewModel.getMutableInitializePagerBoolean().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean && getActivity() != null) {
                ViewPager2 viewPager = root.findViewById(R.id.pager);
                FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(getActivity());
                viewPager.setAdapter(pagerAdapter);
                viewPager.setCurrentItem(viewModel.getMonths() - 1, false);
            }
        });
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap = new HashMap<>();

        public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NotNull
        @Override
        public Fragment createFragment(int position) {
            Map<String, List<DocumentSnapshot>> oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap = viewModel.getAllTransactionsByCategoryList().get(position);
            List<Map.Entry<String, Double>> sortedPositiveAmountByCategoryList = getSortedListOfAmountsByCategories(oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap);

            Fragment transactionMonthlyActivityItemFragment = new TransactionMonthlyActivityItemFragment();
            Bundle bundle = new Bundle();
            bundle.putString("com.crystal.hello.MONTH_YEAR", viewModel.getMonthAndYearList().get(position));
            bundle.putSerializable("com.crystal.hello.POSITIVE_TRANSACTIONS_MAP", (Serializable) oneMonthPositiveAmountTransactionsByCategoryMap);
            bundle.putSerializable("com.crystal.hello.SORTED_POSITIVE_AMOUNTS_LIST", (Serializable) sortedPositiveAmountByCategoryList);
            transactionMonthlyActivityItemFragment.setArguments(bundle);
            return transactionMonthlyActivityItemFragment;
        }

        @Override
        public int getItemCount() {
            return viewModel.getMonths();
        }

        // Filter positive and negative amounts from allTransactionsByCategoryList
        // to positiveAmountTransactionsByCategoryMap with positive amounts
        private List<Map.Entry<String, Double>> getSortedListOfAmountsByCategories(Map<String, List<DocumentSnapshot>> oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap) {
            Map<String, Double> sortedPositiveAmountByCategoryList = new HashMap<>();

            // Get only positive amount transactions
            for (Map.Entry<String, List<DocumentSnapshot>> entry : oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap.entrySet()) {
                String category = entry.getKey();
                List<DocumentSnapshot> documents = entry.getValue();
                sortedPositiveAmountByCategoryList.put(category, getTotalTransactionAmount(category, documents));
            }

            // Sort positive amounts in descending order then add to list to keep order
            return sortedPositiveAmountByCategoryList.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(new Comparator<Double>() {
                        @Override
                        public int compare(Double K, Double V) {
                            return V.compareTo(K);
                        }
                    }))
                    .collect(Collectors.toList());
        }

        // Calculate total amount for a category
        // Put positive amount into positiveAmountTransactionsByCategoryMap
        private double getTotalTransactionAmount(String category, @NotNull List<DocumentSnapshot> documents) {
            double total = 0;
            List<DocumentSnapshot> positiveAmountTransactionsList = new ArrayList<>();
            for (DocumentSnapshot document : documents) {
                Map<String, Object> data = document.getData();
                double amount = (double) Objects.requireNonNull(data).get("amount");
                List<String> categoriesList = (List<String>) data.get("category");

                if (amount >= 0.0) {
                    total += amount;
                    positiveAmountTransactionsList.add(document);
                } else if (Objects.requireNonNull(categoriesList).get(0).equals("Transfer")) {

                }
            }
            oneMonthPositiveAmountTransactionsByCategoryMap.put(category, positiveAmountTransactionsList);
            return total;
        }
    }
}