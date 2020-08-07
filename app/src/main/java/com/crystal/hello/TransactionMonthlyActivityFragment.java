package com.crystal.hello;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Months;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionMonthlyActivityFragment extends Fragment {
//public class TransactionMonthlyActivityFragment extends AppCompatActivity {
    private TransactionMonthlyActivityViewModel mViewModel;
    private View root;
    private final String TAG = TransactionMonthlyActivityFragment.class.getSimpleName();
    private DocumentReference docRef;
    private int months;
    private String oldestTransactionDate;

    private int monthIndex;
    private List<Map<String, List<DocumentSnapshot>>> allTransactionsByCategoryList;
    private List<String> monthAndYearList;
    private Map<String, List<DocumentSnapshot>> oneMonthTransactionsByCategoryMap;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_transaction_monthly_activity, container, false);
        docRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        getOldestTransactionDateFromDatabase();

        allTransactionsByCategoryList = new ArrayList<>();
        monthAndYearList = new ArrayList<>();
        oneMonthTransactionsByCategoryMap = new HashMap<>();
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(TransactionMonthlyActivityViewModel.class);
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_transaction_monthly_activity);
//        docRef = FirebaseFirestore.getInstance()
//                .collection("users")
//                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
//        getOldestTransactionDateFromDatabase();
//
//        allTransactionsByCategoryList = new ArrayList<>();
//        monthAndYearList = new ArrayList<>();
//        oneMonthTransactionsByCategoryMap = new HashMap<>();
//    }

    private void getOldestTransactionDateFromDatabase() {
        docRef.collection("transactions")
                .orderBy("date", Query.Direction.ASCENDING).limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                oldestTransactionDate = String.valueOf(document.getData().get("date"));
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

    // Count whole months between oldest transaction and this month
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
            Fragment transactionMonthlyActivityItemFragment = new TransactionMonthlyActivityItemFragment();
            Bundle bundle = new Bundle();
            bundle.putString("com.crystal.hello.MONTH_YEAR", monthAndYearList.get(position));
            bundle.putSerializable("com.crystal.hello.TRANSACTIONS_MAP", (Serializable) allTransactionsByCategoryList.get(position));
            transactionMonthlyActivityItemFragment.setArguments(bundle);
            return transactionMonthlyActivityItemFragment;
        }

        @Override
        public int getItemCount() {
            return months;
        }
    }

    // Categories: Shopping, Food & Drinks, Services, Travel, Entertainment, Health
    protected void getAllTransactionsByCategory() {
        LocalDate startDate = new LocalDate(oldestTransactionDate).withDayOfMonth(1).plusMonths(monthIndex);
        LocalDate endDate = startDate.withDayOfMonth(1).plusMonths(1);
        monthAndYearList.add(startDate.monthOfYear().getAsText() + " " + startDate.getYear());

        getShoppingTransactionsByCategoryAndMonthFromDatabase(
                Collections.singletonList("Shops")
                , startDate.toString()
                , endDate.toString());
    }

    protected void getShoppingTransactionsByCategoryAndMonthFromDatabase(List<String> categoryList, String startDate, String endDate) {
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
                            oneMonthTransactionsByCategoryMap.put("Shopping", task.getResult().getDocuments());
                            getFoodAndDrinksTransactionsByCategoryAndMonthFromDatabase(
                                    Collections.singletonList("Food and Drink")
                                    , startDate
                                    , endDate);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    protected void getFoodAndDrinksTransactionsByCategoryAndMonthFromDatabase(List<String> categoryList, String startDate, String endDate) {
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
                            oneMonthTransactionsByCategoryMap.put("Food & Drinks", task.getResult().getDocuments());
                            getTravelTransactionsByCategoryAndMonthFromDatabase(
                                    Collections.singletonList("Travel")
                                    , startDate
                                    , endDate);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    protected void getTravelTransactionsByCategoryAndMonthFromDatabase(List<String> categoryList, String startDate, String endDate) {
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
                            oneMonthTransactionsByCategoryMap.put("Travel", task.getResult().getDocuments());
                            getEntertainmentTransactionsByCategoryAndMonthFromDatabase(
                                    Collections.singletonList("Recreation")
                                    , startDate
                                    , endDate);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    protected void getEntertainmentTransactionsByCategoryAndMonthFromDatabase(List<String> categoryList, String startDate, String endDate) {
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
                            oneMonthTransactionsByCategoryMap.put("Entertainment", task.getResult().getDocuments());
                            getHealthTransactionsByCategoryAndMonthFromDatabase(
                                    Collections.singletonList("Healthcare")
                                    , startDate
                                    , endDate);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    protected void getHealthTransactionsByCategoryAndMonthFromDatabase(List<String> categoryList, String startDate, String endDate) {
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
                            oneMonthTransactionsByCategoryMap.put("Health", task.getResult().getDocuments());
                            getServicesTransactionsByCategoryAndMonthFromDatabase(
                                    Arrays.asList("Service", "Community", "Payment", "Bank Fees", "Interest", "Tax", "Transfer")
                                    , startDate
                                    , endDate);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    protected void getServicesTransactionsByCategoryAndMonthFromDatabase(List<String> categoryList, String startDate, String endDate) {
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
                            oneMonthTransactionsByCategoryMap.put("Services", task.getResult().getDocuments());
                            monthIndex++;
                            allTransactionsByCategoryList.add(oneMonthTransactionsByCategoryMap);
//                            mutableAllTransactionsByCategoryList.setValue(allTransactionsByCategoryList);

                            if (monthIndex < months) {
                                oneMonthTransactionsByCategoryMap = new HashMap<>();
                                getAllTransactionsByCategory();
                            } else {
                                initializeScreenSlidePagerAdapter();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}