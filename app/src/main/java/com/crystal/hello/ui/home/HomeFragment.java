package com.crystal.hello.ui.home;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.HomeRecyclerAdapter;
import com.crystal.hello.R;
import com.plaid.client.response.TransactionsGetResponse;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;
import com.robinhood.spark.animation.LineSparkAnimator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment {
    private static HomeViewModel homeViewModel;
    private View root;
    private SparkView sparkView;
    private static TransactionSparkAdapter sparkAdapter;
    private static double[] yData;

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    // https://guides.codepath.com/android/Creating-and-Using-Fragments
    // https://developer.android.com/guide/components/fragments
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);

        observeTransactionList();
        observeCurrentBalance();
        return root;
    }

    private void observeTransactionList() {
        homeViewModel.getTransactionList().observe(getViewLifecycleOwner(), new Observer<List<TransactionsGetResponse.Transaction>>() {
            @Override
            public void onChanged(List<TransactionsGetResponse.Transaction> list) {
                final HomeRecyclerAdapter recyclerAdapter = new HomeRecyclerAdapter(getActivity(), list);
                RecyclerView recyclerView = root.findViewById(R.id.recyclerHome);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setAdapter(recyclerAdapter);
            }
        });
    }

    private void observeCurrentBalance() {
        homeViewModel.getCurrentBalanceAmount().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                String currentBalanceString = String.format(Locale.US,"%.2f", aDouble);
                TextView currentBalanceTextView = root.findViewById(R.id.textViewCurrentBalanceAmount);

                if (aDouble >= 0.0) {
                    currentBalanceString = "$" + currentBalanceString;
                } else { // Negative transactions
                    currentBalanceString = new StringBuilder(currentBalanceString).insert(1, "$").toString();
                }
                currentBalanceTextView.setText(currentBalanceString);

                initializeSparkGraph();
//                for (int i = 0, count = yData.length; i < count; i++) {
//                    yData[i] = aDouble;
//                }
//                sparkAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initializeSparkGraph() {
        sparkView = root.findViewById(R.id.sparkView);
        sparkAdapter = new TransactionSparkAdapter();

        Paint baseLinePaint = sparkView.getBaseLinePaint();
        float baseLineDashSpacing = 15;
        float baseLineDashLength = 5;
        DashPathEffect dashPathEffect = new DashPathEffect(new float[] {baseLineDashLength, baseLineDashSpacing}, 0);
        baseLinePaint.setPathEffect(dashPathEffect);

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        float monthSoFarRatio = (float) day / month; // convert to current day of month / # of days in month
        int paddingRight = sparkView.getWidth() - Math.round(sparkView.getWidth() * monthSoFarRatio);

        sparkView.setPadding(sparkView.getPaddingLeft(), sparkView.getPaddingTop(), paddingRight, sparkView.getPaddingBottom());
        sparkView.setSparkAnimator(new LineSparkAnimator());
        sparkView.setAdapter(sparkAdapter);
//        sparkAdapter.initializeTransactionAmount();
    }

    public static class TransactionSparkAdapter extends SparkAdapter {
//        double[] yData;
        private final Random random;

        public TransactionSparkAdapter() {
            random = new Random();
            yData = new double[50]; // this will be axis of balance later. not transaction amount.
//            initializeTransactionAmount();
            randomize();
        }

//        public void initializeTransactionAmount() {
//            for (int i = 0, count = yData.length; i < count; i++) {
//                yData[i] = homeViewModel.getCurrentBalanceAmount();
//            }
//            notifyDataSetChanged();
//        }

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
            return (float) yData[index];
        }

        @Override
        public boolean hasBaseLine() {
            return true;
        }
    }
}