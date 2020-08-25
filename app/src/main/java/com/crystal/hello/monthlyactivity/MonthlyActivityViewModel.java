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

import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;
import org.joda.time.Months;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MonthlyActivityViewModel extends ViewModel {
    private final DocumentReference docRef;
    private String oldestTransactionDate;
    private int months;
    private int onCompleteCount;
    private final List<Map<String, List<DocumentSnapshot>>> allTransactionsByCategoryList; // Each index represents a month. Each map contains category, documents pair.
    private final List<String> monthAndYearList;
    private final Map<String, List<String>> categoriesMap; // Key: Crystal categories. Value: Plaid categories
    private final MutableLiveData<Boolean> mutableInitializePagerBoolean;
    private double oneMonthPaymentsAmount;
    private double oneMonthRefundsAmount;

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
        final LocalDate start = new LocalDate(oldestTransactionDate).withDayOfMonth(1);
        final LocalDate end = new LocalDate().withDayOfMonth(1).plusMonths(1);
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
                , "Bank Fees"
                , "Interest"
                , "Tax"
                , "Transfer"));
    }

    // Start monthly activity with the oldest transaction up to current month for all months
    private void getAllTransactionsByCategory() {
//        final List<DocumentSnapshot> placeholderList = new ArrayList<>();
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
            final LocalDate startDate = new LocalDate(oldestTransactionDate).withDayOfMonth(1).plusMonths(i);
            final LocalDate endDate = startDate.withDayOfMonth(1).plusMonths(1);
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

    private int getMonthsBetweenOldestTransactionAndEndDate(final String endDate) {
        LocalDate start = new LocalDate(oldestTransactionDate).withDayOfMonth(1);
        LocalDate end = new LocalDate(endDate).withDayOfMonth(1);
        return Months.monthsBetween(start, end).getMonths();
    }

    private void getTransactionsByCategory(final String category,
                                           final List<String> categoryList,
                                           final String startDate,
                                           final String endDate) {
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

    // Filter positive and negative amounts from oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap
    // to oneMonthPositiveAmountTransactionsByCategoryMap with positive amounts
    protected List<Map.Entry<String, Double>> getSortedListOfAmountsByCategories(final Map<String, List<DocumentSnapshot>> oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap,
                                                                                 final Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap,
                                                                                 final Map<String, List<DocumentSnapshot>> oneMonthNegativeAmountTransactionsByCategoryMap,
                                                                                 final List<Map<String, Double>> oneMonthNegativeAmountByCategoryList) {

        final Map<String, Double> oneMonthSortedPositiveAmountByCategoryList = new HashMap<>();
        final List<DocumentSnapshot> oneMonthNegativeAmountPaymentTransactionsList = new ArrayList<>();
        final List<DocumentSnapshot> oneMonthNegativeAmountRefundTransactionsList = new ArrayList<>();
        oneMonthPaymentsAmount = 0;
        oneMonthRefundsAmount = 0;

        // Get only positive amount transactions for each category
        for (Map.Entry<String, List<DocumentSnapshot>> entry : oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap.entrySet()) {
            final String category = entry.getKey();
            final List<DocumentSnapshot> positiveAndNegativeTransactionsList = entry.getValue();

            final double totalPositiveTransactionAmount = getTotalTransactionAmounts(category
                    , positiveAndNegativeTransactionsList
                    , oneMonthPositiveAmountTransactionsByCategoryMap
                    , oneMonthNegativeAmountPaymentTransactionsList
                    , oneMonthNegativeAmountRefundTransactionsList);

            if (oneMonthPositiveAmountTransactionsByCategoryMap.get(category) != null) {
                oneMonthSortedPositiveAmountByCategoryList.put(category, totalPositiveTransactionAmount);
            }
        }

        // Payments
        if (!oneMonthNegativeAmountPaymentTransactionsList.isEmpty()) {
            oneMonthNegativeAmountTransactionsByCategoryMap.put("Payments", oneMonthNegativeAmountPaymentTransactionsList);

            final Map<String, Double> negativePaymentAmountByCategoryMap = new HashMap<>();
            negativePaymentAmountByCategoryMap.put("Payments", oneMonthPaymentsAmount);
            oneMonthNegativeAmountByCategoryList.add(negativePaymentAmountByCategoryMap);
        }

        // Refunds
        if (!oneMonthNegativeAmountRefundTransactionsList.isEmpty()) {
            oneMonthNegativeAmountTransactionsByCategoryMap.put("Refunds", oneMonthNegativeAmountRefundTransactionsList);

            final Map<String, Double> negativeRefundAmountByCategoryMap = new HashMap<>();
            negativeRefundAmountByCategoryMap.put("Refunds", oneMonthRefundsAmount);
            oneMonthNegativeAmountByCategoryList.add(negativeRefundAmountByCategoryMap);

            // Sort refund transactions by date in descending order
            if (oneMonthNegativeAmountRefundTransactionsList.size() > 1) {
                Collections.sort(oneMonthNegativeAmountRefundTransactionsList, new Comparator<DocumentSnapshot>() {
                    @Override
                    public int compare(DocumentSnapshot x, DocumentSnapshot y) {
                        return String.valueOf(y.get("date")).compareTo(String.valueOf(x.get("date")));
                    }
                });
            }
        }

        // Sort positive total transaction by amount in descending order then return as list to keep order
        if (oneMonthSortedPositiveAmountByCategoryList.size() > 1) {
            return oneMonthSortedPositiveAmountByCategoryList.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(new Comparator<Double>() {
                        @Override
                        public int compare(Double x, Double y) {
                            return y.compareTo(x);
                        }
                    }))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    // Calculate total amount for a category
    // Add positive amount into oneMonthPositiveAmountTransactionsByCategoryMap
    // Add negative amount payments into oneMonthNegativeAmountPaymentsList
    // Add negative amount refunds into oneMonthNegativeAmountRefundsList
    protected double getTotalTransactionAmounts(final String category,
                                                @NotNull final List<DocumentSnapshot> positiveAndNegativeTransactionsList,
                                                final Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap,
                                                final List<DocumentSnapshot> oneMonthNegativeAmountPaymentTransactionsList,
                                                final List<DocumentSnapshot> oneMonthNegativeAmountRefundTransactionsList) {
        double totalTransactionAmount = 0;
        List<DocumentSnapshot> positiveAmountTransactionsList = new ArrayList<>();

        for (DocumentSnapshot document : positiveAndNegativeTransactionsList) {
            final Map<String, Object> data = document.getData();
            final double amount = (double) Objects.requireNonNull(data).get("amount");
            final List<String> categoriesList = (List<String>) data.get("category");

            if (amount >= 0) { // Positive transactions include $0 amounts
                totalTransactionAmount += amount;
                positiveAmountTransactionsList.add(document);
            } else if (Objects.requireNonNull(categoriesList).get(0).equals("Transfer")) { // Payments to credit card with negative amounts (Already sorted because it's in Services category)
                oneMonthPaymentsAmount += amount;
                oneMonthNegativeAmountPaymentTransactionsList.add(document);
            } else { // Refunds with negative amounts from any category
                oneMonthRefundsAmount += amount;
                oneMonthNegativeAmountRefundTransactionsList.add(document);
            }
        }

        if (!positiveAmountTransactionsList.isEmpty()) {
            oneMonthPositiveAmountTransactionsByCategoryMap.put(category, positiveAmountTransactionsList);
        }

        return totalTransactionAmount;
    }
}