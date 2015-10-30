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
package io.mpos.ui.shared.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;

import io.mpos.errors.MposError;
import io.mpos.paymentdetails.PaymentDetailsScheme;
import io.mpos.transactions.Currency;
import io.mpos.transactions.RefundDetailsStatus;
import io.mpos.transactions.TransactionStatus;
import io.mpos.transactions.TransactionType;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.model.MposUiConfiguration;
import io.mpos.ui.shared.model.TransactionDataHolder;
import io.mpos.ui.shared.util.UiHelper;

public class SummaryFragment extends Fragment {

    public interface Interaction {

        void onSendReceiptButtonClicked(String transactionIdentifier);

        void onSummaryRetryButtonClicked();

        void onSummaryRefundButtonClicked(String transactionIdentifier);

        void onSummaryPrintReceiptButtonClicked(String transactionIdentifier);
    }

    public final static String TAG = "SummaryFragment";
    private TransactionDataHolder mTransactionDataHolder;
    private MposError mError;
    private boolean mRetryEnabled;
    private boolean mRefundEnabled;

    public static SummaryFragment newInstance(boolean retryEnabled, boolean refundEnabled, TransactionDataHolder transactionDataHolder, MposError error) {
        SummaryFragment fragment = new SummaryFragment();
        fragment.setTransactionDataHolder(transactionDataHolder);
        fragment.setError(error);
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
    private Button mRetryButton;
    private Button mSendReceiptButton;
    private Button mRefundButton;
    private Button mCloseButton;
    private Button mPrintReceiptButton;


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
        View view = inflater.inflate(R.layout.mpu_fragment_summary, container, false);

        mTransactionStatusView = (TextView) view.findViewById(R.id.mpu_summary_tx_status_view);
        mAmountView = (TextView) view.findViewById(R.id.mpu_summary_amount_view);
        mTransactionTypeView = (TextView) view.findViewById(R.id.mpu_summary_tx_type_view);
        mSubjectView = (TextView) view.findViewById(R.id.mpu_summary_subject_view);
        mAccountNumberView = (TextView) view.findViewById(R.id.mpu_summary_account_number_view);
        mSchemeView = (TextView) view.findViewById(R.id.mpu_summary_scheme_view);
        mDateTimeView = (TextView) view.findViewById(R.id.mpu_summary_datetime_view);
        mRetryButton = (Button) view.findViewById(R.id.mpu_summary_retry_button);
        mSendReceiptButton = (Button) view.findViewById(R.id.mpu_summary_send_receipt_button);
        mRefundButton = (Button) view.findViewById(R.id.mpu_summary_refund_button);
        mPrintReceiptButton = (Button) view.findViewById(R.id.mpu_summary_print_receipt_button);

        mSubjectViewDivider = view.findViewById(R.id.mpu_summary_divider_subject_view);
        mSchemeAccNoViewDivider = view.findViewById(R.id.mpu_summary_divider_scheme_accno_view);

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

        setupButtons();

        TransactionStatus transactionStatus = TransactionStatus.valueOf(mTransactionDataHolder.getTransactionStatus());

        if (TransactionStatus.APPROVED == transactionStatus) {
            mRetryButton.setVisibility(View.GONE);

            if (showRefundButton()) {
                mRefundButton.setVisibility(View.VISIBLE);
            } else {
                mRefundButton.setVisibility(View.GONE);
            }

            if (showSendReceiptButton()) {
                mSendReceiptButton.setVisibility(View.VISIBLE);
            } else {
                mSendReceiptButton.setVisibility(View.GONE);
            }

            if (showPrintReceiptButton()) {
                mPrintReceiptButton.setVisibility(View.VISIBLE);
            } else {
                mPrintReceiptButton.setVisibility(View.GONE);
            }

        } else if (TransactionStatus.DECLINED == transactionStatus || TransactionStatus.ABORTED == transactionStatus) {
            mRefundButton.setVisibility(View.GONE);

            if (mRetryEnabled) {
                mRetryButton.setVisibility(View.VISIBLE);
            } else {
                mRetryButton.setVisibility(View.GONE);
            }

            if (showSendReceiptButton()) {
                mSendReceiptButton.setVisibility(View.VISIBLE);
            } else {
                mSendReceiptButton.setVisibility(View.GONE);
            }

            if (showPrintReceiptButton()) {
                mPrintReceiptButton.setVisibility(View.VISIBLE);
            } else {
                mPrintReceiptButton.setVisibility(View.GONE);
            }

        } else {
            if (mRetryEnabled) {
                mRetryButton.setVisibility(View.VISIBLE);
            }
            mRefundButton.setVisibility(View.GONE);
            mSendReceiptButton.setVisibility(View.GONE);
            mPrintReceiptButton.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        mSendReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTransactionDataHolder.getRefundTransactionIdentifier() == null) {
                    Log.e(TAG, "RefundTxIdentifier is null !");
                    mInteractionActivity.onSendReceiptButtonClicked(mTransactionDataHolder.getTransactionIdentifier());
                } else {
                    Log.e(TAG, "RefundTxIdentifier is : " + mTransactionDataHolder.getRefundTransactionIdentifier());
                    mInteractionActivity.onSendReceiptButtonClicked(mTransactionDataHolder.getRefundTransactionIdentifier());
                }
            }
        });

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionActivity.onSummaryRetryButtonClicked();
            }
        });

        mRefundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.MPURefundPayment)
                        .setMessage(R.string.MPURefundPrompt)
                        .setPositiveButton(R.string.MPURefund, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mInteractionActivity.onSummaryRefundButtonClicked(mTransactionDataHolder.getTransactionIdentifier());
                            }
                        })
                        .setNegativeButton(R.string.MPUAbort, null)
                        .show();
            }
        });

        mPrintReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTransactionDataHolder.getRefundTransactionIdentifier() == null) {
                    mInteractionActivity.onSummaryPrintReceiptButtonClicked(mTransactionDataHolder.getTransactionIdentifier());
                } else {
                    mInteractionActivity.onSummaryPrintReceiptButtonClicked(mTransactionDataHolder.getRefundTransactionIdentifier());
                }
            }
        });
    }

    private void showTransactionStatus() {
        mTransactionStatusView.setTextColor(getResources().getColor(getTransactionStatusColor()));
        mTransactionStatusView.setText(getTransactionStatusText());
    }

    private void showTransactionAmountAndType() {
        mAmountView.setText(UiHelper.formatAmountWithSymbol(Currency.valueOf(mTransactionDataHolder.getCurrency()), new BigDecimal(mTransactionDataHolder.getAmount())));
        TransactionType transactionType = TransactionType.valueOf(mTransactionDataHolder.getTransactionType());
        if (transactionType == TransactionType.CHARGE) {
            mTransactionTypeView.setText(R.string.MPUSale);
        } else if (transactionType == TransactionType.REFUND) {
            mTransactionTypeView.setText(R.string.MPURefund);
        } else {
            mTransactionTypeView.setText(R.string.MPUUnknown);
        }
    }

    private void showSchemeAndAccountNumber() {
        //Set scheme and masked account number

        if (mTransactionDataHolder.getMaskedAccountNumber() == null) {
            mAccountNumberView.setVisibility(View.GONE);
            mSchemeAccNoViewDivider.setVisibility(View.GONE);
            mSchemeView.setVisibility(View.GONE);
        } else {
            String maskedAccountNumber = mTransactionDataHolder.getMaskedAccountNumber();
            if (maskedAccountNumber != null) {
                maskedAccountNumber = maskedAccountNumber.replaceAll("[^0-9]", "*");
                mAccountNumberView.setText(maskedAccountNumber);
            } else { //account number is null! WHAT!
                mAccountNumberView.setVisibility(View.GONE);
                mSchemeAccNoViewDivider.setVisibility(View.GONE);
                mSchemeView.setVisibility(View.GONE);
                return;
            }

            PaymentDetailsScheme scheme = PaymentDetailsScheme.UNKNOWN;
            if (mTransactionDataHolder.getPaymentDetailsScheme() != null)
                scheme = PaymentDetailsScheme.valueOf(mTransactionDataHolder.getPaymentDetailsScheme());
            if (UiHelper.getDrawableIdImageForCreditCard(scheme) != -1) {
                mSchemeView.setCompoundDrawablesWithIntrinsicBounds(UiHelper.getDrawableIdImageForCreditCard(scheme), 0, 0, 0);
            } else if (scheme != null) {
                mSchemeView.setText(scheme.toString());
                //The compound drawable padding is 8dp(in the image) + padding from layout is 8dp = 16dp
                //We change padding from 8dp tp 16dp because we dont set the drawable. Only text is visible.
                mSchemeView.setPadding(getResources().getDimensionPixelSize(R.dimen.mpu_content_padding), 0, 0, 0);
            } else { //scheme is null! WHAAT!
                mAccountNumberView.setVisibility(View.GONE);
                mSchemeAccNoViewDivider.setVisibility(View.GONE);
                mSchemeView.setVisibility(View.GONE);
                return;
            }
        }

    }

    private void showSubject() {
        //Set the subject
        if (mTransactionDataHolder.getSubject() != null) {
            String subject = mTransactionDataHolder.getSubject();
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
        if (mTransactionDataHolder.getCreatedTimestamp() == 0) {
            mDateTimeView.setVisibility(View.GONE);
        } else {
            mDateTimeView.setText(DateUtils.formatDateTime(this.getActivity().getApplicationContext(), mTransactionDataHolder.getCreatedTimestamp(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
        }

    }

    public void setTransactionDataHolder(TransactionDataHolder transactionDataHolder) {
        mTransactionDataHolder = transactionDataHolder;
    }

    public void setError(MposError error) {
        mError = error;
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
        return mRefundEnabled &&
                MposUi.getInitializedInstance().getConfiguration().getSummaryFeatures().contains(MposUiConfiguration.SummaryFeature.REFUND_TRANSACTION) &&
                mTransactionDataHolder.getTransactionStatus().equals(TransactionStatus.APPROVED.name()) &&
                !mTransactionDataHolder.getRefundDetailsStatus().equals(RefundDetailsStatus.REFUNDED.name()) &&
                isTransactionRefundable();
    }

    private boolean isTransactionRefundable() {
        if (mTransactionDataHolder.getRefundDetailsStatus() == null) {
            return false;
        }
        RefundDetailsStatus status = RefundDetailsStatus.valueOf(mTransactionDataHolder.getRefundDetailsStatus());
        if (status == null) {
            return false;
        } else {
            return (status == RefundDetailsStatus.REFUNDABLE_PARTIAL_AND_FULL || status == RefundDetailsStatus.REFUNDABLE_FULL_ONLY);
        }
    }

    private boolean showPrintReceiptButton() {
        return MposUi.getInitializedInstance().getConfiguration().getSummaryFeatures().contains(MposUiConfiguration.SummaryFeature.PRINT_RECEIPT);
    }

    private boolean showSendReceiptButton() {
        return MposUi.getInitializedInstance().getConfiguration().getSummaryFeatures().contains(MposUiConfiguration.SummaryFeature.SEND_RECEIPT_VIA_EMAIL);
    }

    private int getTransactionStatusText() {
        switch (TransactionStatus.valueOf(mTransactionDataHolder.getTransactionStatus())) {
            case APPROVED:
                if (mTransactionDataHolder.getTransactionType().equals(TransactionType.CHARGE.name())) {

                    if (mTransactionDataHolder.getTransactionStatus() != null && mTransactionDataHolder.getRefundDetailsStatus().equals(RefundDetailsStatus.REFUNDED.name())) {
                        return R.string.MPUPaymentRefunded;
                    } else {
                        return R.string.MPUPaymentSuccessful;
                    }
                } else {
                    return R.string.MPURefundApproved;
                }

            case DECLINED:
                if (mTransactionDataHolder.getTransactionType().equals(TransactionType.CHARGE.name())) {
                    return R.string.MPUPaymentDeclined;
                } else {
                    return R.string.MPURefundDeclined;
                }

            case ABORTED:
                if (mTransactionDataHolder.getTransactionType().equals(TransactionType.CHARGE.name())) {
                    return R.string.MPUPaymentAborted;
                } else {
                    return R.string.MPURefundAborted;
                }
        }
        return R.string.MPUUnknown;
    }

    private int getTransactionStatusColor() {
        if (mTransactionDataHolder.getTransactionStatus().equals(TransactionStatus.APPROVED.name())) {
            return R.color.mpu_transaction_state_approved;
        } else {
            return R.color.mpu_transaction_state_declined_aborted;
        }
    }
}
