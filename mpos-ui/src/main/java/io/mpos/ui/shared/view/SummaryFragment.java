/*
 * Copyright (C) 2016 Payworks GmbH (http://www.payworks.com)
 *
 * The MIT License (MIT)
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
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

import io.mpos.paymentdetails.PaymentDetailsScheme;
import io.mpos.platform.LocalizationToolbox;
import io.mpos.transactions.RefundDetailsStatus;
import io.mpos.transactions.TransactionStatus;
import io.mpos.transactions.TransactionStatusDetailsCodes;
import io.mpos.transactions.TransactionType;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.model.MposUiAppearance;
import io.mpos.ui.shared.model.MposUiConfiguration;
import io.mpos.ui.shared.model.RefundTransactionDataHolder;
import io.mpos.ui.shared.model.TransactionDataHolder;
import io.mpos.ui.shared.model.TransactionHistoryItem;
import io.mpos.ui.shared.util.TransactionAmountUtil;
import io.mpos.ui.shared.util.TransactionHistoryHelper;
import io.mpos.ui.shared.util.UiHelper;

public class SummaryFragment extends Fragment {

    public interface Interaction {

        void onSummaryCaptureButtonClicked(String transactionIdentifier);

        void onSummarySendReceiptButtonClicked(String transactionIdentifier);

        void onSummaryRetryButtonClicked();

        void onSummaryRefundButtonClicked(String transactionIdentifier);

        void onSummaryPrintReceiptButtonClicked(String transactionIdentifier);

        void onSummaryCloseButtonClicked();
    }

    public final static String TAG = "SummaryFragment";

    private final static String SAVED_INSTANCE_STATE_REFUND_ENABLED = "io.mpos.ui.SummaryFragment.REFUND_ENABLED";
    private final static String SAVED_INSTANCE_STATE_RETRY_ENABLED = "io.mpos.ui.SummaryFragment.RETRY_ENABLED";
    private final static String SAVED_INSTANCE_STATE_CAPTURE_ENABLED = "io.mpos.ui.SummaryFragment.CAPTURE_ENABLED";
    private final static String SAVED_INSTANCE_STATE_TRANSACTION_DATA_HOLDER = "io.mpos.ui.SummaryFragment.TRANSACTION_DATA_HOLDER";

    private TransactionDataHolder mTransactionDataHolder;
    private TransactionHistoryHelper mTransactionHistoryHelper;
    private LocalizationToolbox mLocalizationToolbox;
    private MposUiAppearance mMposUiAppearance;
    private boolean mRetryEnabled;
    private boolean mRefundEnabled;
    private boolean mCaptureEnabled;
    private boolean mSendEnabled = true;

    public static SummaryFragment newInstance(boolean retryEnabled, boolean refundEnabled, boolean captureEnabled, TransactionDataHolder transactionDataHolder) {
        SummaryFragment fragment = new SummaryFragment();
        fragment.setTransactionDataHolder(transactionDataHolder);
        fragment.setRetryEnabled(retryEnabled);
        fragment.setRefundEnabled(refundEnabled);
        fragment.setCaptureEnabled(captureEnabled);
        return fragment;
    }

    public SummaryFragment() {
        // Required empty public constructor
    }

    private Interaction mInteractionActivity;

    private TextView mTransactionStatusView;
    private TextView mTransactionStatusInformationView;
    private TextView mAmountView;
    private TextView mSubjectView;
    private TextView mAccountNumberView;
    private TextView mSchemeView;
    private TextView mDateTimeView;
    private Button mRetryButton;
    private Button mRefundButton;
    private Button mCaptureButton;
    private Button mPrintReceiptButton;
    private Button mSendReceiptButton;
    private Button mCloseButton;
    private View mHeaderViewContainer;

    private LinearLayout mTransactionHistoryContainer;
    //Dividers
    private View mSchemeAccNoViewDivider;
    private View mHeaderViewDivider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mMposUiAppearance = MposUi.getInitializedInstance().getConfiguration().getAppearance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mpu_fragment_summary, container, false);

        mTransactionStatusView = (TextView) view.findViewById(R.id.mpu_summary_status_view);
        mTransactionStatusInformationView = (TextView) view.findViewById(R.id.mpu_summary_status_information_view);
        mAmountView = (TextView) view.findViewById(R.id.mpu_summary_amount_view);
        mSubjectView = (TextView) view.findViewById(R.id.mpu_summary_subject_view);
        mAccountNumberView = (TextView) view.findViewById(R.id.mpu_summary_account_number_view);
        mSchemeView = (TextView) view.findViewById(R.id.mpu_summary_scheme_view);
        mDateTimeView = (TextView) view.findViewById(R.id.mpu_summary_datetime_view);
        mCaptureButton = (Button) view.findViewById(R.id.mpu_summary_capture_button);
        mRefundButton = (Button) view.findViewById(R.id.mpu_summary_refund_button);
        mRetryButton = (Button) view.findViewById(R.id.mpu_summary_retry_button);
        mPrintReceiptButton = (Button) view.findViewById(R.id.mpu_summary_print_receipt_button);
        mSendReceiptButton = (Button) view.findViewById(R.id.mpu_summary_send_receipt_button);
        mCloseButton = (Button) view.findViewById(R.id.mpu_summary_close_button);
        mTransactionHistoryContainer = (LinearLayout) view.findViewById(R.id.mpu_summary_tx_history_container);
        mSchemeAccNoViewDivider = view.findViewById(R.id.mpu_summary_divider_scheme_accno_view);
        mHeaderViewDivider = view.findViewById(R.id.mpu_summary_divider_header_view);
        mHeaderViewContainer = view.findViewById(R.id.mpu_summary_header_container_view);
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
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SAVED_INSTANCE_STATE_TRANSACTION_DATA_HOLDER, mTransactionDataHolder);
        outState.putBoolean(SAVED_INSTANCE_STATE_REFUND_ENABLED, mRefundEnabled);
        outState.putBoolean(SAVED_INSTANCE_STATE_RETRY_ENABLED, mRetryEnabled);
        outState.putBoolean(SAVED_INSTANCE_STATE_CAPTURE_ENABLED, mCaptureEnabled);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            mTransactionDataHolder = savedInstanceState.getParcelable(SAVED_INSTANCE_STATE_TRANSACTION_DATA_HOLDER);
            mRefundEnabled = savedInstanceState.getBoolean(SAVED_INSTANCE_STATE_REFUND_ENABLED);
            mRetryEnabled = savedInstanceState.getBoolean(SAVED_INSTANCE_STATE_RETRY_ENABLED);
            mCaptureEnabled = savedInstanceState.getBoolean(SAVED_INSTANCE_STATE_CAPTURE_ENABLED);
        }

        mLocalizationToolbox = MposUi.getInitializedInstance().getTransactionProvider().getLocalizationToolbox();
        mTransactionHistoryHelper = new TransactionHistoryHelper(mTransactionDataHolder, mLocalizationToolbox);
        showTransactionStatusAndAmount();
        showSchemeAndAccountNumber();
        showSubject();
        showTransactionDateTime();
        showTransactionHistory();
        setupButtons(mTransactionDataHolder.getTransactionStatus());
        setupClickListeners();
        setupDividers();
    }

    private void setupButtons(TransactionStatus transactionStatus) {
        if (TransactionStatus.APPROVED == transactionStatus) {
            mRetryButton.setVisibility(View.GONE);

            mCaptureButton.setVisibility(showCaptureButton() ? View.VISIBLE : View.GONE);
            mRefundButton.setVisibility(showRefundButton() ? View.VISIBLE : View.GONE);
            mPrintReceiptButton.setVisibility(showPrintReceiptButton() ? View.VISIBLE : View.GONE);
            mSendReceiptButton.setVisibility(showSendReceiptButton() ? View.VISIBLE : View.GONE);

        } else if (TransactionStatus.DECLINED == transactionStatus || TransactionStatus.ABORTED == transactionStatus) {
            mCaptureButton.setVisibility(View.GONE);
            mRefundButton.setVisibility(View.GONE);

            mRetryButton.setVisibility(mRetryEnabled ? View.VISIBLE : View.GONE);
            mPrintReceiptButton.setVisibility(showPrintReceiptButton() ? View.VISIBLE : View.GONE);
            mSendReceiptButton.setVisibility(showSendReceiptButton() ? View.VISIBLE : View.GONE);

        } else {
            mRetryButton.setVisibility(mRetryEnabled ? View.VISIBLE : View.GONE);
            mCaptureButton.setVisibility(View.GONE);
            mRefundButton.setVisibility(View.GONE);
            mPrintReceiptButton.setVisibility(View.GONE);
            mPrintReceiptButton.setVisibility(View.GONE);
            mSendReceiptButton.setVisibility(View.GONE);

            mSendEnabled = false;
        }
    }

    private void setupClickListeners() {

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionActivity.onSummaryRetryButtonClicked();
            }
        });

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.MPUCaptureTransaction)
                        .setMessage(R.string.MPUCapturePrompt)
                        .setPositiveButton(R.string.MPUCapture, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mInteractionActivity.onSummaryCaptureButtonClicked(mTransactionDataHolder.getTransactionIdentifier());
                            }
                        })
                        .setNegativeButton(R.string.MPUAbort, null)
                        .show();
            }
        });

        mRefundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.MPURefundTransaction)
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

        mSendReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionActivity.onSummarySendReceiptButtonClicked(getTransactionIdentifierForSendingAndPrinting());
            }
        });

        mPrintReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInteractionActivity.onSummaryPrintReceiptButtonClicked(getTransactionIdentifierForSendingAndPrinting());
            }
        });

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInteractionActivity.onSummaryCloseButtonClicked();
            }
        });
    }

    private void showTransactionStatusAndAmount() {
        setTransactionStatusText();
        setEffectiveTotalAmountText();
    }

    private void setEffectiveTotalAmountText() {
        BigDecimal effectiveTotalAmount = TransactionAmountUtil.calculateEffectiveTotalAmount(mTransactionDataHolder);
        mAmountView.setText(mLocalizationToolbox.formatAmount(effectiveTotalAmount, mTransactionDataHolder.getCurrency()));
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
                mAccountNumberView.setText(UiHelper.formatAccountNumber(maskedAccountNumber));
            } else { //account number is null! WHAT!
                mAccountNumberView.setVisibility(View.GONE);
                mSchemeAccNoViewDivider.setVisibility(View.GONE);
                mSchemeView.setVisibility(View.GONE);
                return;
            }

            PaymentDetailsScheme scheme = PaymentDetailsScheme.UNKNOWN;
            if (mTransactionDataHolder.getPaymentDetailsScheme() != null)
                scheme = mTransactionDataHolder.getPaymentDetailsScheme();
            if (UiHelper.getDrawableIdForCardScheme(scheme) != -1) {
                mSchemeView.setCompoundDrawablesWithIntrinsicBounds(UiHelper.getDrawableIdForCardScheme(scheme), 0, 0, 0);
            } else if (scheme != null) {
                mSchemeView.setText(scheme.toString());
                //The compound drawable padding is 8dp(in the image) + padding from layout is 8dp = 16dp
                //We change padding from 8dp tp 16dp because we dont set the drawable. Only text is visible.
                mSchemeView.setPadding(getResources().getDimensionPixelSize(R.dimen.mpu_content_padding), 0, 0, 0);
            } else { //scheme is null! WHAAT!
                mAccountNumberView.setVisibility(View.GONE);
                mSchemeAccNoViewDivider.setVisibility(View.GONE);
                mSchemeView.setVisibility(View.GONE);
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
            }
        } else {
            mSubjectView.setVisibility(View.GONE);
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

    private void showTransactionHistory() {
        if (mTransactionDataHolder.getRefundTransactions() == null) {
            mTransactionHistoryContainer.setVisibility(View.GONE);
            mHeaderViewDivider.setVisibility(View.GONE);
            return;
        }
        mTransactionHistoryContainer.setVisibility(View.VISIBLE);
        mHeaderViewDivider.setVisibility(View.VISIBLE);
        List<TransactionHistoryItem> transactionHistoryItems = mTransactionHistoryHelper.createTransactionHistoryItems(getActivity().getApplicationContext());
        for (TransactionHistoryItem transactionHistoryItem : transactionHistoryItems) {
            mTransactionHistoryContainer.addView(createTransactionHistoryItemView(transactionHistoryItem));
        }
    }

    private View createTransactionHistoryItemView(TransactionHistoryItem item) {

        View historyItemView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.mpu_listitem_transaction_history, mTransactionHistoryContainer, false);

        TextView statusView = (TextView) historyItemView.findViewById(R.id.mpu_summary_tx_history_list_status_view);
        TextView timestampView = (TextView) historyItemView.findViewById(R.id.mpu_summary_tx_history_list_timestamp_view);
        TextView partialCaptureView = (TextView) historyItemView.findViewById(R.id.mpu_summary_tx_history_list_partial_cp_view);
        TextView amountView = (TextView) historyItemView.findViewById(R.id.mpu_summary_tx_history_list_amount_view);

        statusView.setText(item.getStatusText());
        amountView.setText(item.getAmountText());
        timestampView.setText(item.getTimestampText());

        switch (item.getType()) {
            case CHARGE:
                partialCaptureView.setVisibility(View.GONE);
                amountView.setTextColor(mMposUiAppearance.getApprovedBackgroundColor());
                break;
            case PREAUTHORIZED:
                partialCaptureView.setVisibility(View.GONE);
                amountView.setTextColor(mMposUiAppearance.getPreAuthorizedBackgroundColor());
                break;
            case REFUND:
                partialCaptureView.setVisibility(View.GONE);
                amountView.setTextColor(mMposUiAppearance.getRefundedBackgroundColor());
                break;
            case PARTIAL_CAPTURE:
                partialCaptureView.setVisibility(View.VISIBLE);
                partialCaptureView.setText(item.getPartialCaptureHintText());
                amountView.setTextColor(mMposUiAppearance.getApprovedBackgroundColor());
                break;
        }

        return historyItemView;
    }

    public void setTransactionDataHolder(TransactionDataHolder transactionDataHolder) {
        mTransactionDataHolder = transactionDataHolder;
    }

    public void setRetryEnabled(boolean retryEnabled) {
        mRetryEnabled = retryEnabled;
    }

    public void setCaptureEnabled(boolean captureEnabled) {
        mCaptureEnabled = captureEnabled;
    }

    public void setRefundEnabled(boolean refundEnabled) {
        mRefundEnabled = refundEnabled;
    }

    private boolean showRefundButton() {
        return mRefundEnabled // Allowed?
                && MposUi.getInitializedInstance().getConfiguration().getSummaryFeatures().contains(MposUiConfiguration.SummaryFeature.REFUND_TRANSACTION) // Feature enabled?
                && mTransactionDataHolder.getTransactionType() == TransactionType.CHARGE // Charge
                && mTransactionDataHolder.getTransactionStatus() == TransactionStatus.APPROVED // Approved?
                && mTransactionDataHolder.getRefundDetailsStatus() != RefundDetailsStatus.REFUNDED // Not refunded?
                && isTransactionRefundable();
    }

    private boolean showCaptureButton() {
        return mCaptureEnabled  // Allowed?
                && MposUi.getInitializedInstance().getConfiguration().getSummaryFeatures().contains(MposUiConfiguration.SummaryFeature.CAPTURE_TRANSACTION)  // Feature enabled?
                && mTransactionDataHolder.getTransactionType() == TransactionType.CHARGE // Charge
                && mTransactionDataHolder.getTransactionStatus() == TransactionStatus.APPROVED // Approved?
                && mTransactionDataHolder.getRefundDetailsStatus() != RefundDetailsStatus.REFUNDED // Not refunded?
                && !mTransactionDataHolder.isCaptured(); // Not captured?
    }

    private boolean isTransactionRefundable() {
        if (mTransactionDataHolder.getRefundDetailsStatus() == null) {
            return false;
        }
        RefundDetailsStatus status = mTransactionDataHolder.getRefundDetailsStatus();
        return status != null && (status == RefundDetailsStatus.REFUNDABLE_PARTIAL_AND_FULL || status == RefundDetailsStatus.REFUNDABLE_FULL_ONLY);
    }

    private boolean showPrintReceiptButton() {
        return MposUi.getInitializedInstance().getConfiguration().getSummaryFeatures().contains(MposUiConfiguration.SummaryFeature.PRINT_RECEIPT);
    }

    private boolean showSendReceiptButton() {
        return mSendEnabled && MposUi.getInitializedInstance().getConfiguration().getSummaryFeatures().contains(MposUiConfiguration.SummaryFeature.SEND_RECEIPT_VIA_EMAIL);
    }

    private void setTransactionStatusText() {
        switch (mTransactionDataHolder.getTransactionStatus()) {
            case APPROVED:
                if (mTransactionDataHolder.getRefundTransactions() != null) {
                    setHeaderStatusText(R.string.MPUTotal, mMposUiAppearance.getApprovedBackgroundColor(), mMposUiAppearance.getApprovedTextColor(), false);
                } else if (mTransactionDataHolder.isCaptured()) {
                    setHeaderStatusText(R.string.MPUApproved, mMposUiAppearance.getApprovedBackgroundColor(), mMposUiAppearance.getApprovedTextColor(), false);
                } else {
                    setHeaderStatusText(R.string.MPUPreauthorized, mMposUiAppearance.getPreAuthorizedBackgroundColor(), mMposUiAppearance.getPreAuthorizedTextColor(), false);
                }

                if (mTransactionDataHolder.getTransactionType() == TransactionType.REFUND) {
                    setHeaderStatusText(R.string.MPURefunded, mMposUiAppearance.getRefundedBackgroundColor(), mMposUiAppearance.getRefundedTextColor(), false);
                }
                break;
            case DECLINED:
                setHeaderStatusText(R.string.MPUDeclined, mMposUiAppearance.getDeclinedBackgroundColor(), mMposUiAppearance.getDeclinedTextColor(), true);
                break;
            case ABORTED:
                setHeaderStatusText(R.string.MPUAborted, mMposUiAppearance.getDeclinedBackgroundColor(), mMposUiAppearance.getDeclinedTextColor(), false);
                break;
            case ERROR:
                setHeaderStatusText(R.string.MPUError, mMposUiAppearance.getDeclinedBackgroundColor(), mMposUiAppearance.getDeclinedTextColor(), false);
                break;
            case INCONCLUSIVE:
                setHeaderStatusText(R.string.MPUInconclusive, mMposUiAppearance.getDeclinedBackgroundColor(), mMposUiAppearance.getDeclinedTextColor(), false);
                break;
            case INITIALIZED:
            case PENDING:
                setHeaderStatusText(R.string.MPUUnknown, mMposUiAppearance.getDeclinedBackgroundColor(), mMposUiAppearance.getDeclinedTextColor(), false);
                break;
        }
    }

    private void setHeaderStatusText(@StringRes int statusTextResource, int backgroundColor, int textColor, boolean showInformationText) {
        mTransactionStatusView.setText(statusTextResource);
        mHeaderViewContainer.setBackgroundColor(backgroundColor);
        mTransactionStatusView.setTextColor(textColor);
        mTransactionStatusInformationView.setTextColor(textColor);
        mAmountView.setTextColor(textColor);
        if (showInformationText) {
            showTransactionStatusInformation();
        }
    }


    private void showTransactionStatusInformation() {
        TransactionStatusDetailsCodes code = mTransactionDataHolder.getTransactionStatusDetailsCode();
        String information = mLocalizationToolbox.informationForTransactionStatusDetailsCode(code);
        if (information == null) {
            return;
        }
        mTransactionStatusInformationView.setVisibility(View.VISIBLE);
        mTransactionStatusInformationView.setText(information);
    }

    private void setupDividers() {

        if (mSubjectView.getVisibility() == View.GONE) {
            mSchemeAccNoViewDivider.setVisibility(View.GONE);
        }

        if (mSchemeView.getVisibility() == View.GONE && mSubjectView.getVisibility() == View.GONE) {
            mHeaderViewDivider.setVisibility(View.GONE);
        }
    }

    private String getTransactionIdentifierForSendingAndPrinting() {
        RefundTransactionDataHolder refundTransaction = mTransactionHistoryHelper.getLatestApprovedRefundTransaction();
        if (refundTransaction != null) {
            return refundTransaction.getTransactionIdentifier();
        } else {
            return mTransactionDataHolder.getTransactionIdentifier();
        }
    }

}
