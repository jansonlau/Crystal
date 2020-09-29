package com.crystal.hello.monthlyactivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.TransactionRecyclerAdapter;
import com.crystal.hello.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Objects;

public class MonthlyActivityItemDetailFragment extends Fragment {
    private List<DocumentSnapshot> oneMonthPositiveAmountTransactionsByCategoryMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        oneMonthPositiveAmountTransactionsByCategoryMap =
                (List<DocumentSnapshot>) Objects.requireNonNull(getArguments()).getSerializable("com.crystal.hello.TRANSACTIONS_LIST");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_extended_transactions, container, false);
        final TransactionRecyclerAdapter recyclerAdapter = new TransactionRecyclerAdapter(getActivity(), oneMonthPositiveAmountTransactionsByCategoryMap);
        final RecyclerView recyclerView = root.findViewById(R.id.extendedTransactionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerAdapter);
        return root;
    }
}