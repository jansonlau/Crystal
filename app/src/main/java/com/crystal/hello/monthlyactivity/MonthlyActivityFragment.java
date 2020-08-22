package com.crystal.hello.monthlyactivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.crystal.hello.R;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthlyActivityFragment extends Fragment {
    private MonthlyActivityViewModel viewModel;
    private View root;
    private boolean isSetToLastItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MonthlyActivityViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_monthly_activity, container, false);
        observeInitializePagerBoolean();
        return root;
    }

    private void observeInitializePagerBoolean() {
        viewModel.getMutableInitializePagerBoolean().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean && getActivity() != null) {
                ViewPager2 viewPager = root.findViewById(R.id.pager);
                FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(getActivity());
                viewPager.setAdapter(pagerAdapter);

                if (!isSetToLastItem) {
                    viewPager.setCurrentItem(viewModel.getMonths() - 1, false);
                    isSetToLastItem = true;
                }
            }
        });
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap;
        List<DocumentSnapshot> oneMonthNegativeAmountPaymentsList;
        List<DocumentSnapshot> oneMonthNegativeAmountRefundsList;

        public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NotNull
        @Override
        public Fragment createFragment(int position) {
            // Reinitialize containers for each month
            oneMonthPositiveAmountTransactionsByCategoryMap = new HashMap<>();
            oneMonthNegativeAmountPaymentsList = new ArrayList<>();
            oneMonthNegativeAmountRefundsList = new ArrayList<>();

            final Map<String, List<DocumentSnapshot>> oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap = viewModel.getAllTransactionsByCategoryList().get(position);
            final List<Map.Entry<String, Double>> sortedPositiveAmountByCategoryList = viewModel.getSortedListOfAmountsByCategories(oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap
                    , oneMonthPositiveAmountTransactionsByCategoryMap
                    , oneMonthNegativeAmountPaymentsList
                    , oneMonthNegativeAmountRefundsList);

            final Bundle bundle = new Bundle();
            bundle.putString("com.crystal.hello.MONTH_YEAR", viewModel.getMonthAndYearList().get(position));
            bundle.putSerializable("com.crystal.hello.POSITIVE_TRANSACTIONS_MAP", (Serializable) oneMonthPositiveAmountTransactionsByCategoryMap);
            bundle.putSerializable("com.crystal.hello.NEGATIVE_PAYMENTS_LIST", (Serializable) oneMonthNegativeAmountPaymentsList);
            bundle.putSerializable("com.crystal.hello.NEGATIVE_REFUNDS_LIST", (Serializable) oneMonthNegativeAmountRefundsList);
            bundle.putSerializable("com.crystal.hello.SORTED_POSITIVE_AMOUNTS_LIST", (Serializable) sortedPositiveAmountByCategoryList);

            final Fragment transactionMonthlyActivityItemFragment = new MonthlyActivityItemFragment();
            transactionMonthlyActivityItemFragment.setArguments(bundle);
            return transactionMonthlyActivityItemFragment;
        }

        @Override
        public int getItemCount() {
            return viewModel.getMonths();
        }
    }
}