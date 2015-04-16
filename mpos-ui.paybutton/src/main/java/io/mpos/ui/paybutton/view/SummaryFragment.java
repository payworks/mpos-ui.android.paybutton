/**
 * mpos-ui.paybutton : http://www.payworksmobile.com
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
 *
 */
package io.mpos.ui.paybutton.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

import io.mpos.errors.MposError;
import io.mpos.paymentdetails.PaymentDetailsScheme;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.TransactionStatus;
import io.mpos.transactions.TransactionType;
import io.mpos.transactions.receipts.Receipt;
import io.mpos.transactions.receipts.ReceiptLineItemKey;
import io.mpos.ui.paybutton.R;
import io.mpos.ui.paybutton.controller.PaymentController;
import io.mpos.ui.paybutton.model.PaymentControllerConfiguration;
import io.mpos.ui.paybutton.util.UIHelper;

public class SummaryFragment extends AbstractPaymentFragment {

    public final static String TAG = "SummaryFragment";
    private Transaction mTransaction;
    private MposError mError;
    private boolean mRetryEnabled;
    private Receipt mMerchantReceipt;
    private String mActivityDefaultTitle;

    public static SummaryFragment newInstance(boolean retryEnabled, String title, Transaction transaction, Receipt merchantReceipt, MposError error) {
        SummaryFragment fragment = new SummaryFragment();
        fragment.setTransaction(transaction);
        fragment.setMerchantReceipt(merchantReceipt);
        fragment.setError(error);
        fragment.setActivityDefaultTitle(title);
        fragment.setRetryEnabled(retryEnabled);
        return fragment;
    }

    public SummaryFragment() {
        // Required empty public constructor
    }

    private TextView mTransactionStatusView;
    private TextView mAmountView;
    private TextView mTransactionTypeView;
    private TextView mSubjectView;
    private TextView mAccountNumberView;
    private TextView mSchemeView;
    private TextView mDateTimeView;
    private Button mActionButton;
    private Button mCloseButton;

    //Dividers
    private View mSubjectViewDivider;
    private View mSchemeAccNoViewDivider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        mTransactionStatusView = (TextView) view.findViewById(R.id.summary_tx_status_view);
        mAmountView = (TextView) view.findViewById(R.id.summary_amount_view);
        mTransactionTypeView = (TextView) view.findViewById(R.id.summary_tx_type_view);
        mSubjectView = (TextView) view.findViewById(R.id.summary_subject_view);
        mAccountNumberView = (TextView) view.findViewById(R.id.summary_account_number_view);
        mSchemeView = (TextView) view.findViewById(R.id.summary_scheme_view);
        mDateTimeView = (TextView) view.findViewById(R.id.summary_datetime_view);
        mActionButton = (Button) view.findViewById(R.id.summary_action_button);
        mCloseButton = (Button) view.findViewById(R.id.summary_close_button);

        mSubjectViewDivider = view.findViewById(R.id.summary_divider_subject_view);
        mSchemeAccNoViewDivider = view.findViewById(R.id.summary_divider_scheme_accno_view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.MPUSummary);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().setTitle(mActivityDefaultTitle);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showTransactionStatus();
        showTransactionAmountAndType();
        showSchemeAndAccountNumber();
        showSubject();
        showTransactionDateTime();

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPaymentInteractionListener().onSummaryClosed(mTransaction, mError);
            }
        });

        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransactionStatus status = mTransaction.getStatus();
                if (status.equals(TransactionStatus.APPROVED)) {
                    getPaymentInteractionListener().onSendReceiptButtonClicked(mTransaction);
                } else if (status.equals(TransactionStatus.DECLINED)) {
                    getPaymentInteractionListener().onRetryPaymentButtonClicked();
                } else if (status.equals(TransactionStatus.ABORTED)) {
                    getPaymentInteractionListener().onRetryPaymentButtonClicked();
                } else {
                    getPaymentInteractionListener().onRetryPaymentButtonClicked();
                }

            }
        });

        boolean hideReceiptButton = PaymentController.getInitializedInstance().getConfiguration().getReceiptMethod().equals(PaymentControllerConfiguration.ReceiptMethod.OWN_IMPLEMENTATION);
        if(TransactionStatus.APPROVED.equals(mTransaction.getStatus())) {
            if(hideReceiptButton) {
                mActionButton.setVisibility(View.GONE);
            } else {
                mActionButton.setVisibility(View.VISIBLE);
            }
        } else if(TransactionStatus.DECLINED.equals(mTransaction.getStatus()) || TransactionStatus.ABORTED.equals(mTransaction.getStatus())) {
            if(mRetryEnabled) {
                mActionButton.setVisibility(View.VISIBLE);
            } else {
                mActionButton.setVisibility(View.GONE);
            }
        } else {
            mActionButton.setVisibility(View.VISIBLE);
        }
    }

    private void showTransactionStatus() {
        TransactionStatus status = mTransaction.getStatus();
        if (status.equals(TransactionStatus.APPROVED)) {
            mTransactionStatusView.setTextColor(getResources().getColor(R.color.transaction_state_approved));
            mTransactionStatusView.setText(R.string.MPUPaymentSuccessful);
            mActionButton.setText(R.string.MPUSendReceipt);
        } else if (status.equals(TransactionStatus.DECLINED)) {
            mTransactionStatusView.setText(R.string.MPUPaymentDeclined);
            mTransactionStatusView.setTextColor(getResources().getColor(R.color.transaction_state_declined_aborted));
            mActionButton.setText(R.string.MPURetry);
        } else if (status.equals(TransactionStatus.ABORTED)) {
            mTransactionStatusView.setText(R.string.MPUPaymentAborted);
            mTransactionStatusView.setTextColor(getResources().getColor(R.color.transaction_state_declined_aborted));
            mActionButton.setText(R.string.MPURetry);
        } else {
            mTransactionStatusView.setTextColor(getResources().getColor(R.color.transaction_state_declined_aborted));
            mActionButton.setText(R.string.MPURetry);
        }
    }

    private void showTransactionAmountAndType() {
        mAmountView.setText(UIHelper.formatAmountWithSymbol(mTransaction.getCurrency(), mTransaction.getAmount()));

        if (mTransaction.getType().equals(TransactionType.CHARGE)) {
            mTransactionTypeView.setText(R.string.MPUSale);
        } else if (mTransaction.getType().equals(TransactionType.REFUND)) {
            mTransactionTypeView.setText(R.string.tx_type_refund);
        } else {
            mTransactionTypeView.setText(R.string.tx_type_unknown);
        }
    }

    private void showSchemeAndAccountNumber() {
        //Set scheme and masked account number

        if (mMerchantReceipt.getReceiptLineItem(ReceiptLineItemKey.PAYMENT_DETAILS_MASKED_ACCOUNT) == null) {
            mAccountNumberView.setVisibility(View.GONE);
            mSchemeAccNoViewDivider.setVisibility(View.GONE);
            mSchemeView.setVisibility(View.GONE);
        } else {
            String maskedAccountNumber = mMerchantReceipt.getReceiptLineItem(ReceiptLineItemKey.PAYMENT_DETAILS_MASKED_ACCOUNT).getValue();
            maskedAccountNumber = maskedAccountNumber.replaceAll("[^0-9]", "*");
            mAccountNumberView.setText(maskedAccountNumber);

            PaymentDetailsScheme scheme = mTransaction.getPaymentDetails().getScheme();
            if (UIHelper.getDrawableIdImageForCreditCard(scheme) != -1) {
                mSchemeView.setCompoundDrawablesWithIntrinsicBounds(UIHelper.getDrawableIdImageForCreditCard(scheme), 0, 0, 0);
            } else {
                mSchemeView.setText(scheme.toString());
                //The compound drawable padding is 8dp(in the image) + padding from layout is 8dp = 16dp
                //We change padding from 8dp tp 16dp because we dont set the drawable. Only text is visible.
                mSchemeView.setPadding(getResources().getDimensionPixelSize(R.dimen.content_padding), 0, 0, 0);
            }
        }


    }

    private void showSubject() {
        //Set the subject
        if (mMerchantReceipt.getReceiptLineItem(ReceiptLineItemKey.SUBJECT) != null) {
            String subject = mMerchantReceipt.getReceiptLineItem(ReceiptLineItemKey.SUBJECT).getValue();
            if (!TextUtils.isEmpty(subject)) {
                mSubjectView.setText(subject);
            } else {
                mSubjectView.setVisibility(View.GONE);
                mSubjectViewDivider.setVisibility(View.GONE);
            }
        } else {
            mSubjectView.setVisibility(View.GONE);
            mSubjectViewDivider.setVisibility(View.GONE);
        }
    }

    private void showTransactionDateTime() {
        //Set the date and time
        if (mMerchantReceipt.getReceiptLineItem(ReceiptLineItemKey.DATE) == null && mMerchantReceipt.getReceiptLineItem(ReceiptLineItemKey.TIME) == null) {
            mDateTimeView.setVisibility(View.GONE);
        } else {
            mDateTimeView.setText(DateUtils.formatDateTime(this.getActivity().getApplicationContext(), mTransaction.getCreatedTimestamp(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
        }
    }


    public void setMerchantReceipt(Receipt merchantReceipt) {
        mMerchantReceipt = merchantReceipt;
    }

    public void setTransaction(Transaction transaction) {
        mTransaction = transaction;
    }

    public void setError(MposError error) {
        mError = error;
    }

    public void setActivityDefaultTitle(String activityDefaultTitle) {
        mActivityDefaultTitle = activityDefaultTitle;
    }

    public String getActivityDefaultTitle() {
        return mActivityDefaultTitle;
    }

    public void setRetryEnabled(boolean retryEnabled) {
        mRetryEnabled = retryEnabled;
    }

    public boolean isRetryEnabled() {
        return mRetryEnabled;
    }
}
