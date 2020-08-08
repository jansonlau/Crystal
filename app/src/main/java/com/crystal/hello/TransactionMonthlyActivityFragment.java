package com.crystal.hello;

import android.os.Bundle;
import android.util.Log;
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
    private TransactionMonthlyActivityViewModel viewModel;
    private View root;
    private final String TAG = TransactionMonthlyActivityFragment.class.getSimpleName();
    private DocumentReference docRef;
    private int months;
    private String oldestTransactionDate;
    private int monthIndex;
    private final int numberOfCategories = 6;
    private List<Map<String, List<DocumentSnapshot>>> allTransactionsByCategoryList;
    private List<String> monthAndYearList;
    private Map<String, List<DocumentSnapshot>> oneMonthTransactionsByCategoryMap;
    private HashMap<String, List<String>> categoriesMap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel                           = new ViewModelProvider(this).get(TransactionMonthlyActivityViewModel.class);
        allTransactionsByCategoryList       = new ArrayList<>();
        monthAndYearList                    = new ArrayList<>();
        oneMonthTransactionsByCategoryMap   = new HashMap<>();
        categoriesMap = new HashMap<>();
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
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = Objects.requireNonNull(task.getResult()).getDocuments().get(0);
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                oldestTransactionDate = String.valueOf(Objects.requireNonNull(document.getData()).get("date"));
                                months = getMonthsBetween(oldestTransactionDate);
                                getAllTransactionsByCategory();
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    // Count whole months between oldest transaction and current month
    private int getMonthsBetween(String oldestTransactionDate) {
        LocalDate start = new LocalDate(oldestTransactionDate).withDayOfMonth(1);
        LocalDate end = new LocalDate().withDayOfMonth(1).plusMonths(1);
        return Months.monthsBetween(start, end).getMonths();
    }

    private void initializeScreenSlidePagerAdapter() {
        ViewPager2 viewPager = root.findViewById(R.id.pager);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(getActivity());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(months - 1, false);
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NotNull
        @Override
        public Fragment createFragment(int position) {
            // Filter positive and negative amounts from allTransactionsByCategoryList
            // to positiveAmountTransactionsByCategoryMap with positive amounts
            Map<String, List<DocumentSnapshot>> positiveAndNegativeAmountTransactionsByCategoryMap = allTransactionsByCategoryList.get(position);
            Map<String, List<DocumentSnapshot>> positiveAmountTransactionsByCategoryMap = new HashMap<>();
            Map<String, Double> positiveAmountByCategoryMap = new HashMap<>();

            // Get only positive amount transactions
            for (Map.Entry<String, List<DocumentSnapshot>> entry : positiveAndNegativeAmountTransactionsByCategoryMap.entrySet()) {
                String category = entry.getKey();
                List<DocumentSnapshot> documents = entry.getValue();
                positiveAmountByCategoryMap.put(category, getTotalTransactionAmount(category, documents, positiveAmountTransactionsByCategoryMap));
            }

            // Sort positive amounts then add to list to keep order
            List<Map.Entry<String, Double>> sortedPositiveAmountByCategoryList = positiveAmountByCategoryMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(new Comparator<Double>() {
                        @Override
                        public int compare(Double K, Double V) {
                            return V.compareTo(K);
                        }
                    }))
                    .collect(Collectors.toList());

            Fragment transactionMonthlyActivityItemFragment = new TransactionMonthlyActivityItemFragment();
            Bundle bundle = new Bundle();
            bundle.putString("com.crystal.hello.MONTH_YEAR", monthAndYearList.get(position));
            bundle.putSerializable("com.crystal.hello.TRANSACTIONS_MAP", (Serializable) positiveAmountTransactionsByCategoryMap);
            bundle.putSerializable("com.crystal.hello.SORTED_POSITIVE_AMOUNTS_LIST", (Serializable) sortedPositiveAmountByCategoryList);
            transactionMonthlyActivityItemFragment.setArguments(bundle);

            return transactionMonthlyActivityItemFragment;
        }

        @Override
        public int getItemCount() {
            return months;
        }
    }

    private double getTotalTransactionAmount(String category, @NotNull List<DocumentSnapshot> documents, Map<String, List<DocumentSnapshot>> positiveAmountTransactionsByCategoryMap) {
        double total = 0;
        List<DocumentSnapshot> positiveAmountTransactionsList = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            double amount = (double) Objects.requireNonNull(document.getData()).get("amount");
            if (amount >= 0.0) {
                total += amount;
                positiveAmountTransactionsList.add(document);
            }
        }
        positiveAmountTransactionsByCategoryMap.put(category, positiveAmountTransactionsList);
        return total;
    }

    // Key: Crystal categories
    // Value: Plaid categories
    private void initializeCategoriesMap() {
        categoriesMap.put("Shopping"       , Collections.singletonList("Shops"));
        categoriesMap.put("Food & Drinks"  , Collections.singletonList("Food and Drink"));
        categoriesMap.put("Travel"         , Collections.singletonList("Travel"));
        categoriesMap.put("Entertainment"  , Collections.singletonList("Recreation"));
        categoriesMap.put("Health"         , Collections.singletonList("Healthcare"));
        categoriesMap.put("Services"       , Arrays.asList("Service", "Community", "Payment"
                , "Bank Fees", "Interest", "Tax", "Transfer"));
    }

    // Start monthly activity with the oldest transaction up to current month
    // Get all 6 categories for each month
    private void getAllTransactionsByCategory() {
        LocalDate startDate = new LocalDate(oldestTransactionDate).withDayOfMonth(1).plusMonths(monthIndex);
        LocalDate endDate = startDate.withDayOfMonth(1).plusMonths(1);
        monthAndYearList.add(startDate.monthOfYear().getAsText() + " " + startDate.getYear());
        monthIndex++;

        for (Map.Entry<String, List<String>> entry : categoriesMap.entrySet()) {
            getTransactionsByCategory(entry.getKey()
                    , entry.getValue()
                    , startDate.toString()
                    , endDate.toString());
        }
    }

    private void getTransactionsByCategory(String category, List<String> categoryList, String startDate, String endDate) {
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
                            oneMonthTransactionsByCategoryMap.put(category, Objects.requireNonNull(task.getResult()).getDocuments());

                            if (oneMonthTransactionsByCategoryMap.size() == numberOfCategories) {
                                allTransactionsByCategoryList.add(oneMonthTransactionsByCategoryMap);
                                if (monthIndex < months) { // More months of transactions to get
                                    oneMonthTransactionsByCategoryMap = new HashMap<>();
                                    getAllTransactionsByCategory();
                                } else {
                                    initializeScreenSlidePagerAdapter();
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}