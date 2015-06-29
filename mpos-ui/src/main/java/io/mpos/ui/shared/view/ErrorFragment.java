/*
 * mpos-ui : http://www.payworksmobile.com
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
package io.mpos.ui.shared.view;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.mpos.errors.ErrorType;
import io.mpos.errors.MposError;
import io.mpos.transactionprovider.TransactionProcessDetails;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.util.UiHelper;

public class ErrorFragment extends Fragment {

    public interface Interaction {

        void onErrorRetryButtonClicked();

        void onErrorCancelButtonClicked();

    }

    public static final String TAG = "ErrorFragment";

    private Interaction mInteractionActivity;
    private MposError mError;
    private TransactionProcessDetails mTransactionProcessDetails;
    boolean mRetryEnabled;

    public static ErrorFragment newInstance(boolean retryEnabled, MposError error, TransactionProcessDetails details) {
        ErrorFragment fragment = new ErrorFragment();
        fragment.setError(error);
        fragment.setRetryEnabled(retryEnabled);
        fragment.setTransactionProcessDetails(details);
        return fragment;
    }

    public ErrorFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_error, container, false);

        Button retryButton = (Button) view.findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionActivity.onErrorRetryButtonClicked();
            }
        });
        if (mRetryEnabled) {
            retryButton.setVisibility(View.VISIBLE);
        } else {
            retryButton.setVisibility(View.GONE);
        }

        if (mError.getErrorType() == ErrorType.SERVER_AUTHENTICATION_FAILED) {
            retryButton.setVisibility(View.GONE);
        }

        view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionActivity.onErrorCancelButtonClicked();
            }
        });

        TextView iconView = (TextView) view.findViewById(R.id.status_icon_view);
        iconView.setTypeface(UiHelper.createAwesomeFontTypeface(getActivity()));
        iconView.setTextColor(MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary());

        TextView errorView = (TextView) view.findViewById(R.id.status_view);

        if (mTransactionProcessDetails != null && mTransactionProcessDetails.getInformation() != null && mTransactionProcessDetails.getInformation().length == 2) {
            String message = UiHelper.joinAndTrimStatusInformation(mTransactionProcessDetails.getInformation());
            errorView.setText(message);
        } else {
            errorView.setText(mError.getInfo().trim());
        }
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mInteractionActivity = (Interaction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ErrorFragment.Interaction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInteractionActivity = null;
    }

    public void setError(MposError error) {
        mError = error;
    }

    public void setRetryEnabled(boolean retryEnabled) {
        mRetryEnabled = retryEnabled;
    }

    public void setTransactionProcessDetails(TransactionProcessDetails transactionProcessDetails) {
        mTransactionProcessDetails = transactionProcessDetails;
    }

}