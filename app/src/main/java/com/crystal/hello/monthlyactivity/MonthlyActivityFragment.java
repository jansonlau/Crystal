package com.crystal.hello.monthlyactivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.crystal.hello.R;
import com.crystal.hello.ui.home.HomeViewModel;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthlyActivityFragment extends Fragment {
//    private MonthlyActivityViewModel viewModel;
    private View root;
    private boolean isSetToLastItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        viewModel = new ViewModelProvider(this).get(MonthlyActivityViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_monthly_activity, container, false);
        observeInitializePagerBoolean();
        return root;
    }

    private void observeInitializePagerBoolean() {
        HomeViewModel.getMonthlyActivityViewModel().getMutableInitializePagerBoolean().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean && getActivity() != null) {
                final ViewPager2 viewPager = root.findViewById(R.id.pager);
                final FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(getActivity());
                viewPager.setAdapter(pagerAdapter);

                if (!isSetToLastItem) {
                    viewPager.setCurrentItem(pagerAdapter.getItemCount() - 1, false);
                    isSetToLastItem = true;
                }
            }
        });
    }

    private static class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NotNull
        @Override
        public Fragment createFragment(int position) {
            // Reinitialize containers for each month
            final Map<String, List<DocumentSnapshot>> oneMonthPositiveAmountTransactionsByCategoryMap = new HashMap<>(); // Key category. Value list of positive amount transactions
            final Map<String, List<DocumentSnapshot>> oneMonthNegativeAmountTransactionsByCategoryMap = new HashMap<>(); // Key category. Value list of credit transactions
            final Map<String, List<DocumentSnapshot>> oneMonthMerchantTransactionsMap                 = new HashMap<>();
            final List<Map<String, Double>> oneMonthNegativeAmountByCategoryList                      = new ArrayList<>();
            final List<Map<String, Double>> oneMonthAmountByMerchantNameList                          = new ArrayList<>();

            final Map<String, List<DocumentSnapshot>> oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap = HomeViewModel.getMonthlyActivityViewModel().getAllTransactionsByCategoryList().get(position);
            final List<Map<String, Double>> oneMonthSortedPositiveAmountByCategoryList = HomeViewModel.getMonthlyActivityViewModel().getSortedListOfAmountsByCategories(oneMonthPositiveAndNegativeAmountTransactionsByCategoryMap
                    , oneMonthPositiveAmountTransactionsByCategoryMap
                    , oneMonthNegativeAmountTransactionsByCategoryMap
                    , oneMonthNegativeAmountByCategoryList
                    , oneMonthMerchantTransactionsMap
                    , oneMonthAmountByMerchantNameList);

            final Fragment transactionMonthlyActivityItemFragment = new MonthlyActivityItemFragment();
            final Bundle bundle = new Bundle();
            bundle.putString("com.crystal.hello.MONTH_YEAR", HomeViewModel.getMonthlyActivityViewModel().getMonthAndYearList().get(position));

            // Budgets and categories
            if (!oneMonthPositiveAmountTransactionsByCategoryMap.isEmpty()) {
                bundle.putSerializable("com.crystal.hello.POSITIVE_TRANSACTIONS_MAP"    , (Serializable) oneMonthPositiveAmountTransactionsByCategoryMap);
                bundle.putSerializable("com.crystal.hello.SORTED_POSITIVE_AMOUNTS_LIST" , (Serializable) oneMonthSortedPositiveAmountByCategoryList);
                bundle.putSerializable("com.crystal.hello.BUDGETS_MAP"                  , (Serializable) HomeViewModel.getMonthlyActivityViewModel().getBudgetsMap());
            }

            // Merchants
            if (!oneMonthMerchantTransactionsMap.isEmpty()) {
                bundle.putSerializable("com.crystal.hello.MERCHANT_TRANSACTIONS_MAP"    , (Serializable) oneMonthMerchantTransactionsMap);
                bundle.putSerializable("com.crystal.hello.MERCHANT_AMOUNTS_LIST"        , (Serializable) oneMonthAmountByMerchantNameList);
            }

            // Payments and refunds
            if (!oneMonthNegativeAmountTransactionsByCategoryMap.isEmpty()) {
                bundle.putSerializable("com.crystal.hello.NEGATIVE_TRANSACTIONS_MAP"    , (Serializable) oneMonthNegativeAmountTransactionsByCategoryMap);
                bundle.putSerializable("com.crystal.hello.NEGATIVE_AMOUNTS_LIST"        , (Serializable) oneMonthNegativeAmountByCategoryList);
            }

            transactionMonthlyActivityItemFragment.setArguments(bundle);
            return transactionMonthlyActivityItemFragment;
        }

        @Override
        public int getItemCount() {
            return HomeViewModel.getMonthlyActivityViewModel().getMonths();
        }
    }
}