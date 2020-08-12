package com.crystal.hello.ui.home;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.HomeLatestTransactionsRecyclerAdapter;
import com.crystal.hello.R;
import com.crystal.hello.TransactionMonthlyActivityFragment;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;
import com.robinhood.spark.animation.LineSparkAnimator;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private View root;
    private SparkView sparkView;
    private TransactionSparkAdapter sparkAdapter;
    private static double[] yData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // New user from create account
        if (getArguments() != null && getArguments().getBoolean("com.crystal.hello.CREATE_USER_BOOLEAN")) {
            String publicToken = getArguments().getString("com.crystal.hello.PUBLIC_TOKEN_STRING");
            homeViewModel.buildPlaidClient();
            homeViewModel.exchangeAccessToken(publicToken);
        } else { // Log in
            homeViewModel.getSubsetTransactionsFromDatabase();
            homeViewModel.getBalancesFromDatabase();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        setMonthlyActivityOnClickListener();
        observeTransactionList();
        observeCurrentBalance();
        return root;
    }

    private void setMonthlyActivityOnClickListener() {
        FrameLayout monthlyActivityFrameLayout = root.findViewById(R.id.frameLayoutMonthlyActivity);
        monthlyActivityFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayoutFragmentContainer, new TransactionMonthlyActivityFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void observeTransactionList() {
        homeViewModel.getMutableSubsetTransactionsList().observe(getViewLifecycleOwner(), new Observer<List<Map<String, Object>>>() {
            @Override
            public void onChanged(List<Map<String, Object>> list) {
                final HomeLatestTransactionsRecyclerAdapter recyclerAdapter = new HomeLatestTransactionsRecyclerAdapter(getActivity(), list);
                RecyclerView recyclerView = root.findViewById(R.id.recyclerHome);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setAdapter(recyclerAdapter);
            }
        });
    }

    private void observeCurrentBalance() {
        homeViewModel.getCurrentTotalBalance().observe(getViewLifecycleOwner(), new Observer<Double>() {
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
        sparkView.setSparkAnimator(new LineSparkAnimator());
        sparkAdapter = new TransactionSparkAdapter();
        sparkView.setAdapter(sparkAdapter);

        sparkView.setLineWidth(4);
        sparkView.setScrubLineWidth(3);

        // Add date when scrubbing
        sparkView.setScrubListener(new SparkView.OnScrubListener() {
            @Override
            public void onScrubbed(Object value) {
            }
        });

        // Create baseline
        float baseLineDashSpacing = 15;
        float baseLineDashLength = 3;
        DashPathEffect dashPathEffect = new DashPathEffect(new float[] {baseLineDashLength, baseLineDashSpacing}, 0);
        Paint baseLinePaint = sparkView.getBaseLinePaint();
        baseLinePaint.setPathEffect(dashPathEffect);

        initializeSparkTimePeriodListeners();
//        sparkAdapter.initializeTransactionAmount();
    }

    public void initializeSparkTimePeriodListeners() {
        // Set default time frame to 1 month
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        float monthSoFarRatio = (float) day / month; // convert to current day of month / # of days in month
        int paddingRight = sparkView.getWidth() - Math.round(sparkView.getWidth() * monthSoFarRatio);
        sparkView.setPadding(sparkView.getPaddingLeft(), sparkView.getPaddingTop(), paddingRight, sparkView.getPaddingBottom());

        TextView oneMonthTextView = root.findViewById(R.id.textViewOneMonth);
        TextView threeMonthTextView = root.findViewById(R.id.textViewThreeMonths);
        TextView oneYearTextView = root.findViewById(R.id.textViewOneYear);

        oneMonthTextView.setTextColor(Color.parseColor("#FFFFFFFF"));
        oneMonthTextView.setSelected(true);
        oneMonthTextView.setBackgroundResource(R.drawable.round_corner);

        oneMonthTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                sparkView.setPadding(sparkView.getPaddingLeft(), sparkView.getPaddingTop(), paddingRight, sparkView.getPaddingBottom());
                sparkAdapter.randomize();

                oneMonthTextView.setTextColor(Color.parseColor("#FFFFFFFF"));
                v.setSelected(true);
                v.setBackgroundResource(R.drawable.round_corner);

                threeMonthTextView.setSelected(false);
                threeMonthTextView.setTextColor(Color.parseColor("#000000"));
                threeMonthTextView.setBackgroundResource(R.color.colorOnBackground);

                oneYearTextView.setSelected(false);
                oneYearTextView.setTextColor(Color.parseColor("#000000"));
                oneYearTextView.setBackgroundResource(R.color.colorOnBackground);
            }
        });

        threeMonthTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                sparkView.setPadding(sparkView.getPaddingLeft(), sparkView.getPaddingTop(), 0, sparkView.getPaddingBottom());
                sparkAdapter.randomize();

                oneMonthTextView.setSelected(false);
                oneMonthTextView.setTextColor(Color.parseColor("#000000"));
                oneMonthTextView.setBackgroundResource(R.color.colorOnBackground);

                threeMonthTextView.setTextColor(Color.parseColor("#FFFFFFFF"));
                v.setSelected(true);
                v.setBackgroundResource(R.drawable.round_corner);

                oneYearTextView.setSelected(false);
                oneYearTextView.setTextColor(Color.parseColor("#000000"));
                oneYearTextView.setBackgroundResource(R.color.colorOnBackground);
            }
        });

        oneYearTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                sparkView.setPadding(sparkView.getPaddingLeft(), sparkView.getPaddingTop(), 0, sparkView.getPaddingBottom());
                sparkAdapter.randomize();

                oneMonthTextView.setSelected(false);
                oneMonthTextView.setTextColor(Color.parseColor("#000000"));
                oneMonthTextView.setBackgroundResource(R.color.colorOnBackground);

                threeMonthTextView.setSelected(false);
                threeMonthTextView.setTextColor(Color.parseColor("#000000"));
                threeMonthTextView.setBackgroundResource(R.color.colorOnBackground);

                oneYearTextView.setTextColor(Color.parseColor("#FFFFFFFF"));
                v.setSelected(true);
                v.setBackgroundResource(R.drawable.round_corner);
            }
        });
    }

    public static class TransactionSparkAdapter extends SparkAdapter {
//        double[] yData;
        private final Random random;

        public TransactionSparkAdapter() {
            random = new Random();
            yData = new double[31]; // this will be axis of balance later. not transaction amount.
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