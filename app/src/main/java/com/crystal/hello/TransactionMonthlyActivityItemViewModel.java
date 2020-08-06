package com.crystal.hello;

import android.util.Log;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionMonthlyActivityItemViewModel extends ViewModel {
    private final String TAG = TransactionMonthlyActivityItemViewModel.class.getSimpleName();
    private final FirebaseFirestore db;
    private final DocumentReference docRef;
//    private List<Map<String, List<DocumentSnapshot>>> categoriesByMonthList;

    private MutableLiveData<Map<String, List<DocumentSnapshot>>> mutableTransactionsByCategoryMap;
    private Map<String, List<DocumentSnapshot>> transactionsByCategoryMap;
//    private static List<DocumentSnapshot> shoppingTransactionsList;

//    public List<DocumentSnapshot> getShoppingTransactionsList() {
//        return shoppingTransactionsList;
//    }

    public TransactionMonthlyActivityItemViewModel() {
        mutableTransactionsByCategoryMap    = new MutableLiveData<>();
        transactionsByCategoryMap           = new HashMap<>();
        db                                  = FirebaseFirestore.getInstance();
        docRef                              = db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    public MutableLiveData<Map<String, List<DocumentSnapshot>>> getMutableTransactionsByCategoryMap() {
        return mutableTransactionsByCategoryMap;
    }

    // Categories: Shopping, Food & Drinks, Services, Travel, Entertainment, Health
    protected void getTransactionsByCategoryAndMonthFromDatabase(String category, List<String> categoryList, String startDate, String endDate) {
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
                            transactionsByCategoryMap.put(category, task.getResult().getDocuments());
                            mutableTransactionsByCategoryMap.setValue(transactionsByCategoryMap);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}