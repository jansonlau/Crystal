package com.crystal.hello;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TransactionMonthlyActivityFragment extends Fragment {

    private TransactionMonthlyActivityViewModel mViewModel;

    public static TransactionMonthlyActivityFragment newInstance() {
        return new TransactionMonthlyActivityFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_monthly_activity, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(TransactionMonthlyActivityViewModel.class);
        // TODO: Use the ViewModel
    }








    public class ScreenSlidePagerActivity extends FragmentActivity {
        /**
         * The number of pages (wizard steps) to show in this demo.
         */
        private static final int NUM_PAGES = 5;

        /**
         * The pager widget, which handles animation and allows swiping horizontally to access previous
         * and next wizard steps.
         */
        private ViewPager2 viewPager;

        /**
         * The pager adapter, which provides the pages to the view pager widget.
         */
        private FragmentStateAdapter pagerAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.fragment_transaction_monthly_activity);

            // Instantiate a ViewPager2 and a PagerAdapter.
            viewPager = findViewById(R.id.pager);
            pagerAdapter = new ScreenSlidePagerAdapter(this);
            viewPager.setAdapter(pagerAdapter);
        }









        /**
         * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
         * sequence.
         */
        private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
            public ScreenSlidePagerAdapter(FragmentActivity fa) {
                super(fa);
            }

            @Override
            public Fragment createFragment(int position) {
                return new TransactionMonthlyActivityFragment();
            }

            @Override
            public int getItemCount() {
                return NUM_PAGES;
            }
        }
    }
}