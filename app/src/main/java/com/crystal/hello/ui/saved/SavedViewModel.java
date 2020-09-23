package com.crystal.hello.ui.saved;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Objects;

public class SavedViewModel extends ViewModel {
    private final MutableLiveData<List<DocumentSnapshot>> mutableSavedTransactionsList;
    private final DocumentReference docRef;

    public SavedViewModel() {
        mutableSavedTransactionsList = new MutableLiveData<>();
        docRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
    }

    public MutableLiveData<List<DocumentSnapshot>> getMutableSavedTransactionsList() {
        return mutableSavedTransactionsList;
    }

    protected void getSavedTransactionsFromDatabase() {
        docRef.collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .whereEqualTo("saved", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final List<DocumentSnapshot> savedTransactionsList = Objects.requireNonNull(task.getResult()).getDocuments();
                        mutableSavedTransactionsList.setValue(savedTransactionsList);
                    }
                });
    }
}