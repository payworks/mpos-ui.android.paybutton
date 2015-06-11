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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import io.mpos.transactions.RefundDetailsStatus;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.TransactionStatus;
import io.mpos.transactions.TransactionType;
import io.mpos.transactions.receipts.Receipt;
import io.mpos.transactions.receipts.ReceiptLineItemKey;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.model.MposUiConfiguration;
import io.mpos.ui.shared.util.UiHelper;

public class SummaryFragment extends Fragment {

    public interface Interaction {

        void onSendReceiptButtonClicked(Transaction transaction);

        void onSummaryRetryButtonClicked();

        void onSummaryRefundButtonClicked(Transaction transaction);

        void onSummaryClosed(Transaction transaction, MposError error);
    }

    public final static String TAG = "SummaryFragment";
    private Transaction mTransaction;
    private MposError mError;
    private boolean mRetryEnabled;
    private Receipt mMerchantReceipt;
    private String mActivityDefaultTitle;
    private boolean mRefundEnabled;

    public static SummaryFragment newInstance(boolean retryEnabled, boolean refundEnabled, String title, Transaction transaction, Receipt merchantReceipt, MposError error) {
        SummaryFragment fragment = new SummaryFragment();
        fragment.setTransaction(transaction);
        fragment.setMerchantReceipt(merchantReceipt);
        fragment.setError(error);
        fragment.setActivityDefaultTitle(title);
        fragment.setRetryEnabled(retryEnabled);
        fragment.setRefundEnabled(refundEnabled);
        return fragment;
    }

    public SummaryFragment() {
        // Required empty public constructor
    }

    private Interaction mInteractionActivity;

    private TextView mTransactionStatusView;
    private TextView mAmountView;
    private TextView mTransactionTypeView;
    private TextView mSubjectView;
    private TextView mAccountNumberView;
    private TextView mSchemeView;
    private TextView mDateTimeView;
    private Button mActionButton;
    private Button mRefundButton;
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
        mRefundButton = (Button) view.findViewById(R.id.summary_refund_button);
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mInteractionActivity = (Interaction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SummaryInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().setTitle(mActivityDefaultTitle);
        mInteractionActivity = null;
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
                mInteractionActivity.onSummaryClosed(mTransaction, mError);
            }
        });

        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransactionStatus status = mTransaction.getStatus();
                switch (status) {
                    case APPROVED:
                        mInteractionActivity.onSendReceiptButtonClicked(mTransaction);
                        break;
                    case DECLINED:
                        mInteractionActivity.onSummaryRetryButtonClicked();
                        break;
                    case ABORTED:
                        mInteractionActivity.onSummaryRetryButtonClicked();
                        break;
                    default:
                        mInteractionActivity.onSummaryRetryButtonClicked();
                        break;
                }
            }
        });

        if (showRefundButton()) {
            mRefundButton.setVisibility(View.VISIBLE);
            mRefundButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.MPURefundPayment)
                            .setMessage(R.string.MPURefundPrompt)
                            .setPositiveButton(R.string.MPURefund, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mInteractionActivity.onSummaryRefundButtonClicked(mTransaction);
                                }
                            })
                            .setNegativeButton(R.string.MPUAbort, null)
                            .show();
                }
            });
        } else {
            mRefundButton.setVisibility(View.GONE);
        }

        boolean hideReceiptButton = (MposUi.getInitializedInstance().getConfiguration().getReceiptMethod() == MposUiConfiguration.ReceiptMethod.OWN_IMPLEMENTATION);
        if(TransactionStatus.APPROVED == mTransaction.getStatus()) {
            if(hideReceiptButton) {
                mActionButton.setVisibility(View.GONE);
            } else {
                mActionButton.setVisibility(View.VISIBLE);
            }
        } else if(TransactionStatus.DECLINED == mTransaction.getStatus() || TransactionStatus.ABORTED == mTransaction.getStatus()) {
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
        mTransactionStatusView.setTextColor(getResources().getColor(getTransactionStatusColor(mTransaction)));
        mTransactionStatusView.setText(getTransactionStatusText(mTransaction));
        mActionButton.setText(getActionButtonText(mTransaction));
    }

    private void showTransactionAmountAndType() {
        mAmountView.setText(UiHelper.formatAmountWithSymbol(mTransaction.getCurrency(), mTransaction.getAmount()));

        if (mTransaction.getType() == TransactionType.CHARGE) {
            mTransactionTypeView.setText(R.string.MPUSale);
        } else if (mTransaction.getType() == TransactionType.REFUND) {
            mTransactionTypeView.setText(R.string.MPURefund);
        } else {
            mTransactionTypeView.setText(R.string.MPUUnknown);
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
            if (UiHelper.getDrawableIdImageForCreditCard(scheme) != -1) {
                mSchemeView.setCompoundDrawablesWithIntrinsicBounds(UiHelper.getDrawableIdImageForCreditCard(scheme), 0, 0, 0);
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

    public void setRefundEnabled(boolean refundEnabled) {
        mRefundEnabled = refundEnabled;
    }

    private boolean showRefundButton() {
        return mRefundEnabled && (mTransaction.getStatus() == TransactionStatus.APPROVED) && (mTransaction.getRefundDetails().getStatus() != RefundDetailsStatus.REFUNDED);
    }

    private int getTransactionStatusText(Transaction transaction) {
        switch (transaction.getStatus()) {
            case APPROVED:
                if (transaction.getType() == TransactionType.CHARGE) {
                    if (transaction.getRefundDetails().getStatus() == RefundDetailsStatus.REFUNDED) {
                        return R.string.MPUPaymentRefunded;
                    } else {
                        return R.string.MPUPaymentSuccessful;
                    }
                } else {
                    return R.string.MPURefundApproved;
                }

            case DECLINED:
                if (transaction.getType() == TransactionType.CHARGE) {
                    return R.string.MPUPaymentDeclined;
                } else {
                    return R.string.MPURefundDeclined;
                }

            case ABORTED:
                if (transaction.getType() == TransactionType.CHARGE) {
                    return R.string.MPUPaymentAborted;
                } else {
                    return R.string.MPURefundAborted;
                }
        }
        return R.string.MPUUnknown;
    }

    private int getTransactionStatusColor(Transaction transaction) {
        if (transaction.getStatus() == TransactionStatus.APPROVED) {
            return R.color.transaction_state_approved;
        } else {
            return R.color.transaction_state_declined_aborted;
        }
    }

    private int getActionButtonText(Transaction transaction) {
        if (transaction.getStatus() == TransactionStatus.APPROVED) {
            return R.string.MPUSendReceipt;
        } else {
            return R.string.MPURetry;
        }
    }
}
