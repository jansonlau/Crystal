package com.crystal.hello.monthlyactivity;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.joda.time.LocalDate;
import org.joda.time.Months;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MonthlyActivityViewModel extends ViewModel {
    private DocumentReference docRef;
    private String oldestTransactionDate;
    private int months;
    private int onCompleteCount;
    private List<Map<String, List<DocumentSnapshot>>> allTransactionsByCategoryList; // Each index represents a month. Each map contains category, documents pair.
    private List<String> monthAndYearList;
    private Map<String, List<String>> categoriesMap; // Key: Crystal categories, Value: Plaid categories
    private MutableLiveData<Boolean> mutableInitializePagerBoolean;

    public MonthlyActivityViewModel() {
        allTransactionsByCategoryList       = new ArrayList<>();
        monthAndYearList                    = new ArrayList<>();
        categoriesMap                       = new HashMap<>();
        mutableInitializePagerBoolean       = new MutableLiveData<>();
        docRef                              = FirebaseFirestore.getInstance()
                .collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        initializeCategoriesMap();
        getOldestTransactionDateFromDatabase();
    }

    public MutableLiveData<Boolean> getMutableInitializePagerBoolean() {
        return mutableInitializePagerBoolean;
    }

    public int getMonths() {
        return months;
    }

    public List<String> getMonthAndYearList() {
        return monthAndYearList;
    }

    public List<Map<String, List<DocumentSnapshot>>> getAllTransactionsByCategoryList() {
        return allTransactionsByCategoryList;
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

    // Crystal category keys, Plaid category values
    protected void initializeCategoriesMap() {
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
        List<DocumentSnapshot> placeholderList = new ArrayList<>();
        for (int i = 0; i < months; i++) {
            // To show only positive amounts, remove categoryPlaceholderMap
//            Map<String, List<DocumentSnapshot>> categoryPlaceholderMap = new HashMap<>();
//            categoryPlaceholderMap.put("Shopping"       , placeholderList);
//            categoryPlaceholderMap.put("Food & Drinks"  , placeholderList);
//            categoryPlaceholderMap.put("Travel"         , placeholderList);
//            categoryPlaceholderMap.put("Entertainment"  , placeholderList);
//            categoryPlaceholderMap.put("Health"         , placeholderList);
//            categoryPlaceholderMap.put("Services"       , placeholderList);
//            allTransactionsByCategoryList.add(categoryPlaceholderMap);

            allTransactionsByCategoryList.add(new HashMap<>());
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
                                mutableInitializePagerBoolean.setValue(true);
                            }
                        }
                    }
                });
    }
}