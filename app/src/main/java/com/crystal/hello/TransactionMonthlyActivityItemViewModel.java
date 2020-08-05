package com.crystal.hello;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.crystal.hello.ui.home.HomeViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionMonthlyActivityItemViewModel extends ViewModel {
    private final String TAG = TransactionMonthlyActivityItemViewModel.class.getSimpleName();
    private final FirebaseFirestore db;
    private final DocumentReference docRef;
    private List<Map<String, List<DocumentSnapshot>>> categoriesByMonthList;

    public TransactionMonthlyActivityItemViewModel() {
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    // Categories: Shopping, Food & Drinks, Services, Travel, Entertainment, Health
    protected void getPositiveTransactionsByMonthFromDatabase(String category, String startDate, String endDate) {
        docRef.collection("transactions")
                .orderBy                    ("date"     , Query.Direction.DESCENDING)
                .whereArrayContains         ("category" , category)
                .whereGreaterThanOrEqualTo  ("date"     , startDate)
                .whereLessThan              ("date"     , endDate)
//                .whereGreaterThanOrEqualTo  ("amount"   , 0.0)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {



                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}