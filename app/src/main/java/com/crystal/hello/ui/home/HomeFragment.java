package com.crystal.hello.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.HomeRecyclerAdapter;
import com.crystal.hello.R;
import com.plaid.client.response.TransactionsGetResponse;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;
import com.robinhood.spark.animation.LineSparkAnimator;

import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private View root;
    private SparkView sparkView;
    public static TransactionSparkAdapter sparkAdapter;

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    // https://guides.codepath.com/android/Creating-and-Using-Fragments
    // https://developer.android.com/guide/components/fragments
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);

        initializeSparkGraph();
        observeTransactionList();
        return root;
    }

    private void observeTransactionList() {
        homeViewModel.getList().observe(getViewLifecycleOwner(), new Observer<List<TransactionsGetResponse.Transaction>>() {
            @Override
            public void onChanged(List<TransactionsGetResponse.Transaction> list) {
                final HomeRecyclerAdapter recyclerAdapter = new HomeRecyclerAdapter(getActivity(), list);
                RecyclerView recyclerView = root.findViewById(R.id.recyclerHome);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setAdapter(recyclerAdapter);
            }
        });
    }

    private void initializeSparkGraph() {
        sparkView = root.findViewById(R.id.sparkView);
        sparkView.setSparkAnimator(new LineSparkAnimator());
        sparkAdapter = new TransactionSparkAdapter();
        sparkView.setAdapter(sparkAdapter);
        sparkAdapter.randomize();
    }

    public static class TransactionSparkAdapter extends SparkAdapter {
        private final float[] yData;
        private final Random random;

        public TransactionSparkAdapter() {
            random = new Random();
            yData = new float[10];
            randomize();
        }

        public void randomize() {
            for (int i = 0, count = yData.length; i < count; i++) {
                yData[i] = random.nextFloat();
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return yData.length;
        }

        @NonNull
        @Override
        public Object getItem(int index) {
            return yData[index];
        }

        @Override
        public float getY(int index) {
            return yData[index];
        }
    }
}