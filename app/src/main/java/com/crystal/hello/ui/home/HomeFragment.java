package com.crystal.hello.ui.home;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.hello.R;
import com.crystal.hello.TransactionRecyclerAdapter;
import com.crystal.hello.monthlyactivity.MonthlyActivityFragment;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Locale;
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
        if (getArguments() != null && getArguments().getString("com.crystal.hello.PUBLIC_TOKEN_STRING") != null) {
            final String publicToken = Objects.requireNonNull(getArguments()).getString("com.crystal.hello.PUBLIC_TOKEN_STRING");
            homeViewModel.buildPlaidClient();
            homeViewModel.exchangeAccessToken(publicToken);
        } else {
            homeViewModel.getSubsetTransactionsFromDatabase();
            homeViewModel.getBalancesAndBankAccountsFromDatabase();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        setMonthlyActivityOnClickListener();
        observeTransactionList();
        return root;
    }

    private void setMonthlyActivityOnClickListener() {
        FrameLayout monthlyActivityFrameLayout = root.findViewById(R.id.frameLayoutMonthlyActivity);
        monthlyActivityFrameLayout.setOnClickListener(v -> Objects.requireNonNull(getActivity())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayoutFragmentContainer, new MonthlyActivityFragment())
                .addToBackStack(null)
                .commit());
    }

    private void observeTransactionList() {
        final RecyclerView homeRecyclerView = root.findViewById(R.id.homeRecyclerView);
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        homeViewModel.getMutableSubsetTransactionsList().observe(getViewLifecycleOwner(), list -> {
            final TransactionRecyclerAdapter recyclerAdapter = new TransactionRecyclerAdapter(getActivity(), list);
            homeRecyclerView.setAdapter(recyclerAdapter);
            observeCurrentBalance();
            root.findViewById(R.id.homeFragmentProgressBar).setVisibility(View.GONE);
        });
    }

    private void observeCurrentBalance() {
        final TextView currentBalanceTextView = root.findViewById(R.id.textViewCurrentBalanceAmount);

        homeViewModel.getCurrentTotalBalance().observe(getViewLifecycleOwner(), aDouble -> {
            initializeSparkGraph();
            String currentBalanceString = String.format(Locale.US,"%.2f", aDouble);

            if (aDouble >= 0) {
                currentBalanceString = "$" + currentBalanceString;
            } else { // Negative transactions
                currentBalanceString = new StringBuilder(currentBalanceString).insert(1, "$").toString();
            }
            currentBalanceTextView.setText(currentBalanceString);
        });
    }

    private void initializeSparkGraph() {
        sparkView = root.findViewById(R.id.sparkView);
//        sparkView.setSparkAnimator(new LineSparkAnimator());
        sparkView.setSparkAnimator(null);
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
        final int paddingRight = sparkView.getWidth() - Math.round(sparkView.getWidth() * monthSoFarRatio);
        sparkView.setPadding(sparkView.getPaddingLeft(), sparkView.getPaddingTop(), paddingRight, sparkView.getPaddingBottom());

        final Button oneMonthButton = root.findViewById(R.id.oneMonthButton);
        final Button threeMonthButton = root.findViewById(R.id.threeMonthButton);
        final Button oneYearButton = root.findViewById(R.id.oneYearButton);

        oneMonthButton.setSelected(true);
        oneMonthButton.setOnClickListener(v -> setButtonColors(oneMonthButton, threeMonthButton, oneYearButton));
        threeMonthButton.setOnClickListener(v -> setButtonColors(threeMonthButton, oneMonthButton, oneYearButton));
        oneYearButton.setOnClickListener(v -> setButtonColors(oneYearButton, oneMonthButton, threeMonthButton));
    }

    private void setButtonColors(@NotNull final Button selectedButton,
                                 @NotNull final Button firstUnselectedButton,
                                 @NotNull final Button secondUnselectedButton) {
        if (selectedButton.isSelected()) {
            return;
        }
        sparkView.setPadding(sparkView.getPaddingLeft(), sparkView.getPaddingTop(), 0, sparkView.getPaddingBottom());
        sparkAdapter.randomize();

        selectedButton.setTextColor(Color.parseColor("#FFFFFF"));
        selectedButton.setSelected(true);
        selectedButton.setBackgroundResource(R.drawable.round_corner);

        firstUnselectedButton.setSelected(false);
        firstUnselectedButton.setBackgroundResource(0);
        secondUnselectedButton.setSelected(false);
        secondUnselectedButton.setBackgroundResource(0);

        final int currentNightMode = Objects.requireNonNull(getContext()).getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                firstUnselectedButton.setTextColor(Color.BLACK);
                secondUnselectedButton.setTextColor(Color.BLACK);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                firstUnselectedButton.setTextColor(Color.WHITE);
                secondUnselectedButton.setTextColor(Color.WHITE);
                break;
        }
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