package com.crystal.hello;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
    public final ConstraintLayout   transactionConstraintLayout;
    public final ImageView          transactionLogoImageView;
    public final ImageView          transactionArrowImageView;
    public final TextView           transactionTitleTextView;
    public final TextView           transactionSubtitleTextView;
    public final TextView           transactionAmountTextView;
    public final View               transactionDividerView;

    public ViewHolder(@NonNull final View itemView) {
        super(itemView);
        transactionConstraintLayout = itemView.findViewById(R.id.transactionConstraintLayout);
        transactionLogoImageView    = itemView.findViewById(R.id.transactionLogoImageView);
        transactionArrowImageView   = itemView.findViewById(R.id.transactionArrowImageView);
        transactionTitleTextView    = itemView.findViewById(R.id.transactionTitleTextView);
        transactionSubtitleTextView = itemView.findViewById(R.id.transactionSubtitleTextView);
        transactionAmountTextView   = itemView.findViewById(R.id.transactionAmountTextView);
        transactionDividerView      = itemView.findViewById(R.id.transactionDividerView);
    }
}
