package com.crystal.hello;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;
import org.joda.time.Months;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TransactionMonthlyActivityFragment extends Fragment {
//    private TransactionMonthlyActivityViewModel viewModel;
    private View root;
    private DocumentReference docRef;
    private String oldestTransactionDate;
    private int months;
    private int onCompleteCount;
    private List<Map<String, List<DocumentSnapshot>>> allTransactionsByCategoryList; // Each index represents a month. Each map contains category, documents pair.
    private List<String> monthAndYearList;
    private Map<String, List<String>> categoriesMap; // Key: Crystal categories, Value: Plaid categories

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        viewModel                           = new ViewModelProvider(this).get(TransactionMonthlyActivityViewModel.class);
        allTransactionsByCategoryList       = new ArrayList<>();
        monthAndYearList                    = new ArrayList<>();
        categoriesMap                       = new HashMap<>();
        docRef                              = FirebaseFirestore.getInstance()
                .collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        initializeCategoriesMap();
        getOldestTransactionDateFromDatabase();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_transaction_monthly_activity, container, false);
        return root;
    }

    private void getOldestTransactionDateFromDatabase() {
        docRef.collection("transactions")
                .orderBy("date", Query.Direction.ASCENDING).limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (Objects.requireNonNull(task.getResult()).isEmpty()) {
                            months = 1;
                        } else {
                            DocumentSnapshot document = Objects.requireNonNull(task.getResult()).getDocuments().get(0);
                            oldestTransactionDate = String.valueOf(Objects.requireNonNull(document.getData()).get("date"));
                            months = getMonthsBetweenOldestTransactionsAndNow();
                        }
                        getAllTransactionsByCategory();
                    }
                });
    }

    // Count whole months between oldest transaction and current month
    private int getMonthsBetweenOldestTransactionsAndNow() {
        LocalDate start = new LocalDate(oldestTransactionDate).withDayOfMonth(1);
        LocalDate end = new LocalDate().withDayOfMonth(1).plusMonths(1);
        return Months.monthsBetween(start, end).getMonths();
    }

    private void initializeScreenSlidePagerAdapter() {
        if (getActivity() != null) {
            ViewPager2 viewPager = root.findViewById(R.id.pager);
            FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(getActivity());
            viewPager.setAdapter(pagerAdapter);
            viewPager.setCurrentItem(months - 1, false);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap = new HashMap<>();

        public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NotNull
        @Override
        public Fragment createFragment(int position) {
            Map<String, List<DocumentSnapshot>> oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap = allTransactionsByCategoryList.get(position);
            List<Map.Entry<String, Double>> sortedPositiveAmountByCategoryList = getSortedListOfAmountsByCategories(oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap);

            Fragment transactionMonthlyActivityItemFragment = new TransactionMonthlyActivityItemFragment();
            Bundle bundle = new Bundle();
            bundle.putString("com.crystal.hello.MONTH_YEAR", monthAndYearList.get(position));
            bundle.putSerializable("com.crystal.hello.POSITIVE_TRANSACTIONS_MAP", (Serializable) oneMonthPositiveAmountTransactionsByCategoryMap);
            bundle.putSerializable("com.crystal.hello.SORTED_POSITIVE_AMOUNTS_LIST", (Serializable) sortedPositiveAmountByCategoryList);
            transactionMonthlyActivityItemFragment.setArguments(bundle);
            return transactionMonthlyActivityItemFragment;
        }

        @Override
        public int getItemCount() {
            return months;
        }

        // Filter positive and negative amounts from allTransactionsByCategoryList
        // to positiveAmountTransactionsByCategoryMap with positive amounts
        private List<Map.Entry<String, Double>> getSortedListOfAmountsByCategories(Map<String, List<DocumentSnapshot>> oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap) {
            Map<String, Double> positiveAmountByCategoryMap = new HashMap<>();

            // Get only positive amount transactions
            for (Map.Entry<String, List<DocumentSnapshot>> entry : oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap.entrySet()) {
                String category = entry.getKey();
                List<DocumentSnapshot> documents = entry.getValue();
                positiveAmountByCategoryMap.put(category, getTotalTransactionAmount(category, documents));
            }

            // Sort positive amounts in descending order then add to list to keep order
            return positiveAmountByCategoryMap.entrySet()
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
                double amount = (double) Objects.requireNonNull(document.getData()).get("amount");
                if (amount >= 0.0) {
                    total += amount;
                    positiveAmountTransactionsList.add(document);
                }
            }
            oneMonthPositiveAmountTransactionsByCategoryMap.put(category, positiveAmountTransactionsList);
            return total;
        }
    }

    private void initializeCategoriesMap() {
        categoriesMap.put("Shopping"       , Collections.singletonList("Shops"));
        categoriesMap.put("Food & Drinks"  , Collections.singletonList("Food and Drink"));
        categoriesMap.put("Travel"         , Collections.singletonList("Travel"));
        categoriesMap.put("Entertainment"  , Collections.singletonList("Recreation"));
        categoriesMap.put("Health"         , Collections.singletonList("Healthcare"));
        categoriesMap.put("Services"       , Arrays.asList("Service", "Community", "Payment"
                , "Bank Fees", "Interest", "Tax", "Transfer"));
    }

    // Start monthly activity with the oldest transaction up to current month for all months
    private void getAllTransactionsByCategory() {
        for (int i = 0; i < months; i++) {
            // To show only positive amounts, remove categoryPlaceholderMap
            Map<String, List<DocumentSnapshot>> categoryPlaceholderMap = new HashMap<>();
            categoryPlaceholderMap.put("Shopping"       , new ArrayList<>());
            categoryPlaceholderMap.put("Food & Drinks"  , new ArrayList<>());
            categoryPlaceholderMap.put("Travel"         , new ArrayList<>());
            categoryPlaceholderMap.put("Entertainment"  , new ArrayList<>());
            categoryPlaceholderMap.put("Health"         , new ArrayList<>());
            categoryPlaceholderMap.put("Services"       , new ArrayList<>());

            allTransactionsByCategoryList.add(categoryPlaceholderMap);
            LocalDate startDate = new LocalDate(oldestTransactionDate).withDayOfMonth(1).plusMonths(i);
            LocalDate endDate = startDate.withDayOfMonth(1).plusMonths(1);
            monthAndYearList.add(startDate.monthOfYear().getAsText() + " " + startDate.getYear());

            // Get all 6 categories for each month
            for (Map.Entry<String, List<String>> entry : categoriesMap.entrySet()) {
                getTransactionsByCategory(entry.getKey()
                        , entry.getValue()
                        , startDate.toString()
                        , endDate.toString());
            }
        }
    }

    private int getMonthsBetweenOldestTransactionAndEndDate(String endDate) {
        LocalDate start = new LocalDate(oldestTransactionDate).withDayOfMonth(1);
        LocalDate end = new LocalDate(endDate).withDayOfMonth(1);
        return Months.monthsBetween(start, end).getMonths();
    }

    private void getTransactionsByCategory(String category, List<String> categoryList, String startDate, String endDate) {
        final int numberOfCategories = 6;

        docRef.collection("transactions")
                .orderBy                    ("date"     , Query.Direction.DESCENDING)
                .whereGreaterThanOrEqualTo  ("date"     , startDate)
                .whereLessThan              ("date"     , endDate)
                .whereArrayContainsAny      ("category" , categoryList)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            onCompleteCount++;
                            List<DocumentSnapshot> documentsList = Objects.requireNonNull(task.getResult()).getDocuments();

                            if (!documentsList.isEmpty()) {
                                String documentDate = String.valueOf(documentsList.get(0).get("date"));
                                int monthsFromOldestTransactionIndex = getMonthsBetweenOldestTransactionAndEndDate(documentDate);
                                allTransactionsByCategoryList.get(monthsFromOldestTransactionIndex).put(category, documentsList);
                            }

                            // Show pager if all queries for 6 categories in each month are complete
                            if (onCompleteCount == numberOfCategories * months) {
                                initializeScreenSlidePagerAdapter();
                            }
                        }
                    }
                });
    }
}