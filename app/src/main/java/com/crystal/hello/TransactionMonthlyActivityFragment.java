package com.crystal.hello;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;

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
import org.joda.time.Months;

//public class TransactionMonthlyActivityFragment extends Fragment {
public class TransactionMonthlyActivityFragment extends AppCompatActivity {
//    private TransactionMonthlyActivityViewModel mViewModel;
//    private View root;
    private final String TAG = TransactionMonthlyActivityFragment.class.getSimpleName();
    private DocumentReference docRef;
    private int months;
    private String oldestTransactionDate;
    private String latestTransactionDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_transaction_monthly_activity);
        docRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        getOldestTransactionDateFromDatabase();
    }

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
                                initializeScreenSlidePagerAdapter();
//                                getLatestTransactionDateFromDatabase(oldestTransactionDate);
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

//    private void getLatestTransactionDateFromDatabase(String oldestTransactionDate) {
//        docRef.collection("transactions")
//                .orderBy("date", Query.Direction.DESCENDING).limit(1)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
//                            if (document.exists()) {
//                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//                                latestTransactionDate = String.valueOf(document.getData().get("date"));
//                                months = getMonthsBetween(oldestTransactionDate, latestTransactionDate);
//                                initializeScreenSlidePagerAdapter();
//                            } else {
//                                Log.d(TAG, "No such document");
//                            }
//                        } else {
//                            Log.d(TAG, "get failed with ", task.getException());
//                        }
//                    }
//                });
//    }

    private void initializeScreenSlidePagerAdapter() {
        ViewPager2 viewPager = findViewById(R.id.pager);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(pagerAdapter.getItemCount() - 1, false);
    }

    // Count whole months between oldest transaction and this month
    private int getMonthsBetween(String oldestTransactionDate) {
        DateTime start = new DateTime(oldestTransactionDate).withDayOfMonth(1)
                .withTimeAtStartOfDay();
        DateTime end = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay()
                .plusMonths(1);
        return Months.monthsBetween(start, end).getMonths();
    }

//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        root = inflater.inflate(R.layout.fragment_transaction_monthly_activity, container, false);
//        return root;
//    }
//
//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        mViewModel = ViewModelProviders.of(this).get(TransactionMonthlyActivityViewModel.class);
//    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NotNull
        @Override
        public Fragment createFragment(int position) {
            Fragment transactionMonthlyActivityItemFragment = new TransactionMonthlyActivityItemFragment();

            Bundle bundle = new Bundle();
            bundle.putString("com.crystal.hello.OLDEST_TRANSACTION", oldestTransactionDate);
//            bundle.putString("com.crystal.hello.LATEST_TRANSACTION", new DateTime().toString());
            bundle.putInt("com.crystal.hello.ITEM_POSITION", position);
            bundle.putInt("com.crystal.hello.ITEM_COUNT", getItemCount());
            transactionMonthlyActivityItemFragment.setArguments(bundle);

            return transactionMonthlyActivityItemFragment;
//            return new TransactionMonthlyActivityItemFragment();
        }

        @Override
        public int getItemCount() {
            return months;
        }
    }
}