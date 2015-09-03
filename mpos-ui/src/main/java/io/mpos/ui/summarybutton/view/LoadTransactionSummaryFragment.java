/*
 * mpos-ui : http://www.payworksmobile.com
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 payworks GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.mpos.ui.summarybutton.view;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import io.mpos.errors.MposError;
import io.mpos.transactionprovider.LookupTransactionListener;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.transactions.Transaction;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.util.UiHelper;

public class LoadTransactionSummaryFragment extends Fragment {

    public interface Interaction {

        TransactionProvider getTransactionProvider();

        void onTransactionLoaded(Transaction transaction);

        void onLoadingError(MposError error);
    }

    public static String TAG = "LoadTransactionSummaryFragment";

    private Interaction mInteractionActivity;
    private String mTransactionIdentifier;

    public static LoadTransactionSummaryFragment newInstance(String identifier) {
        LoadTransactionSummaryFragment fragment = new LoadTransactionSummaryFragment();
        fragment.setTransactionIdentifier(identifier);
        return fragment;
    }

    public LoadTransactionSummaryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        startLoading();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mpu_fragment_load_transaction_summary, container, false);

        int color = MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary();

        ImageView progressView = (ImageView)view.findViewById(R.id.mpu_progress_view);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.mpu_rotation);
        progressView.startAnimation(animation);

        TextView iconView = (TextView) view.findViewById(R.id.mpu_status_icon_view);
        iconView.setTypeface(UiHelper.createAwesomeFontTypeface(view.getContext()));
        iconView.setTextColor(color);
        iconView.setText(R.string.mpu_fa_history);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mInteractionActivity = (Interaction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LoadTransactionSummaryFragment.Interaction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInteractionActivity = null;
    }

    private void setTransactionIdentifier(String identifier) {
        mTransactionIdentifier = identifier;
    }

    private void startLoading() {
        TransactionProvider transactionProvider = mInteractionActivity.getTransactionProvider();
        transactionProvider.lookupTransaction(mTransactionIdentifier, new LookupTransactionListener() {
            @Override
            public void onCompleted(String identifier, Transaction transaction, MposError error) {
                if (mInteractionActivity != null) {
                    if (error == null) {
                        mInteractionActivity.onTransactionLoaded(transaction);
                    } else {
                        mInteractionActivity.onLoadingError(error);
                    }
                }
            }
        });
    }

}
