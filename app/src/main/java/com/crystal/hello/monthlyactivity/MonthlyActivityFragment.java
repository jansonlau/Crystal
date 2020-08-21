package com.crystal.hello.monthlyactivity;

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

import com.crystal.hello.R;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MonthlyActivityFragment extends Fragment {
    private MonthlyActivityViewModel viewModel;
    private View root;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MonthlyActivityViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_monthly_activity, container, false);
        observeInitializePagerBoolean();
        return root;
    }

    private void observeInitializePagerBoolean() {
        viewModel.getMutableInitializePagerBoolean().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean && getActivity() != null) {
                ViewPager2 viewPager = root.findViewById(R.id.pager);
                FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(getActivity());
                viewPager.setAdapter(pagerAdapter);

                if (viewPager.getCurrentItem() != viewModel.getMonths() - 1) {
                    viewPager.setCurrentItem(viewModel.getMonths() - 1, false);
                }
            }
        });
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap;
        List<DocumentSnapshot> oneMonthNegativeAmountPaymentsList;
        List<DocumentSnapshot> oneMonthNegativeAmountRefundsList;

        public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NotNull
        @Override
        public Fragment createFragment(int position) {
            // Reinitialize containers for each month
            oneMonthPositiveAmountTransactionsByCategoryMap = new HashMap<>();
            oneMonthNegativeAmountPaymentsList = new ArrayList<>();
            oneMonthNegativeAmountRefundsList = new ArrayList<>();

            Map<String, List<DocumentSnapshot>> oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap = viewModel.getAllTransactionsByCategoryList().get(position);
            List<Map.Entry<String, Double>> sortedPositiveAmountByCategoryList = getSortedListOfAmountsByCategories(oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap);

            // Sort refund transactions
            Collections.sort(oneMonthNegativeAmountRefundsList, new Comparator<DocumentSnapshot>() {
                @Override
                public int compare(DocumentSnapshot X, DocumentSnapshot Y) {
                    return String.valueOf(Y.get("date")).compareTo(String.valueOf(X.get("date")));
                }
            });

            final Bundle bundle = new Bundle();
            bundle.putString("com.crystal.hello.MONTH_YEAR", viewModel.getMonthAndYearList().get(position));
            bundle.putSerializable("com.crystal.hello.POSITIVE_TRANSACTIONS_MAP", (Serializable) oneMonthPositiveAmountTransactionsByCategoryMap);
            bundle.putSerializable("com.crystal.hello.NEGATIVE_PAYMENTS_LIST", (Serializable) oneMonthNegativeAmountPaymentsList);
            bundle.putSerializable("com.crystal.hello.NEGATIVE_REFUNDS_LIST", (Serializable) oneMonthNegativeAmountRefundsList);
            bundle.putSerializable("com.crystal.hello.SORTED_POSITIVE_AMOUNTS_LIST", (Serializable) sortedPositiveAmountByCategoryList);

            Fragment transactionMonthlyActivityItemFragment = new MonthlyActivityItemFragment();
            transactionMonthlyActivityItemFragment.setArguments(bundle);

//            for (Map.Entry<String, List<DocumentSnapshot>> entry : oneMonthPositiveAmountTransactionsByCategoryMap.entrySet()) {
//                for (DocumentSnapshot doc : entry.getValue()) {
//                    System.out.println(entry.getKey() + " " + doc.get("date") + " " + doc.get("amount") + " " + doc.get("name"));
//                }
//            }

//            for (DocumentSnapshot doc : oneMonthNegativeAmountPaymentsList) {
//                System.out.println("NEGATIVE" + " " + doc.get("date") + " " + doc.get("amount") + " " + doc.get("name"));
//            }

//            for (DocumentSnapshot doc : oneMonthNegativeAmountRefundsList) {
//                System.out.println("REFUND" + " " + doc.get("date") + " " + doc.get("amount") + " " + doc.get("category") + " " + doc.get("name"));
//            }

            return transactionMonthlyActivityItemFragment;
        }

        @Override
        public int getItemCount() {
            return viewModel.getMonths();
        }

        // Filter positive and negative amounts from oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap
        // to oneMonthPositiveAmountTransactionsByCategoryMap with positive amounts
        private List<Map.Entry<String, Double>> getSortedListOfAmountsByCategories(Map<String, List<DocumentSnapshot>> oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap) {
            Map<String, Double> sortedPositiveAmountByCategoryList = new HashMap<>();

            // Get only positive amount transactions for each category
            for (Map.Entry<String, List<DocumentSnapshot>> entry : oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap.entrySet()) {
                String category = entry.getKey();
                List<DocumentSnapshot> positiveAndNegativeTransactionsList = entry.getValue();

//                for (DocumentSnapshot doc : positiveAndNegativeTransactionsList) {
//                    System.out.println(category + " " + doc.get("date") + " " + doc.get("amount") + " " + doc.get("name"));
//                }

                final double totalTransactionAmount = getTotalTransactionAmount(category, positiveAndNegativeTransactionsList);
                if (oneMonthPositiveAmountTransactionsByCategoryMap.get(category) != null) {
                    sortedPositiveAmountByCategoryList.put(category, totalTransactionAmount);
                }
            }

            // Sort positive amount transactions in descending order then return as list to keep order
            return sortedPositiveAmountByCategoryList.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(new Comparator<Double>() {
                        @Override
                        public int compare(Double X, Double Y) {
                            return Y.compareTo(X);
                        }
                    }))
                    .collect(Collectors.toList());
        }

        // Calculate total amount for a category
        // Add positive amount into oneMonthPositiveAmountTransactionsByCategoryMap
        // Add negative amount payments into oneMonthNegativeAmountPaymentsList
        // Add negative amount refunds into oneMonthNegativeAmountRefundsList
        private double getTotalTransactionAmount(String category, @NotNull List<DocumentSnapshot> positiveAndNegativeTransactionsList) {
            double totalTransactionAmount = 0;
            List<DocumentSnapshot> positiveAmountTransactionsList = new ArrayList<>();

            for (DocumentSnapshot document : positiveAndNegativeTransactionsList) {
                Map<String, Object> data = document.getData();
                double amount = (double) Objects.requireNonNull(data).get("amount");
                List<String> categoriesList = (List<String>) data.get("category");

                if (amount >= 0) { // Positive transactions include $0 amounts
                    totalTransactionAmount += amount;
                    positiveAmountTransactionsList.add(document);
                } else if (Objects.requireNonNull(categoriesList).get(0).equals("Transfer")) { // Payments to credit card with negative amounts (Already sorted because it's in Services category)
                    oneMonthNegativeAmountPaymentsList.add(document);
                } else { // Refunds with negative amounts from any category
                    oneMonthNegativeAmountRefundsList.add(document);
                }
            }

            if (!positiveAmountTransactionsList.isEmpty()) {
                oneMonthPositiveAmountTransactionsByCategoryMap.put(category, positiveAmountTransactionsList);
            }
            return totalTransactionAmount;
        }
    }
}