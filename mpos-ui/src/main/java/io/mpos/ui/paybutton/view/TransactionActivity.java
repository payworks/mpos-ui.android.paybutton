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
package io.mpos.ui.paybutton.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.List;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.errors.ErrorType;
import io.mpos.errors.MposError;
import io.mpos.paymentdetails.ApplicationInformation;
import io.mpos.paymentdetails.PaymentDetailsScheme;
import io.mpos.provider.ProviderMode;
import io.mpos.shared.errors.DefaultMposError;
import io.mpos.transactionprovider.TransactionProcessDetails;
import io.mpos.transactionprovider.TransactionProcessDetailsState;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.transactions.Currency;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.TransactionStatus;
import io.mpos.transactions.TransactionType;
import io.mpos.ui.R;
import io.mpos.ui.paybutton.controller.StatefulTransactionProviderProxy;
import io.mpos.ui.shared.view.PrintReceiptFragment;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.controller.StatefulPrintingProcessProxy;
import io.mpos.ui.shared.model.MposUiConfiguration;
import io.mpos.ui.shared.model.TransactionDataHolder;
import io.mpos.ui.shared.util.UiHelper;
import io.mpos.ui.shared.util.UiState;
import io.mpos.ui.shared.view.ErrorFragment;
import io.mpos.ui.shared.view.SendReceiptFragment;
import io.mpos.ui.shared.view.SummaryFragment;

public class TransactionActivity extends AbstractTransactionActivity
        implements StatefulTransactionProviderProxy.Callback, AbstractTransactionFragment.Interaction, ErrorFragment.Interaction, SummaryFragment.Interaction, SendReceiptFragment.Interaction, PrintReceiptFragment.Interaction {

    private final static String TAG = "TransactionActivity";

    public final static String BUNDLE_EXTRA_MERCHANT_ID = "io.mpos.ui.paybutton.TransactionActivity.MERCHANT_ID";
    public final static String BUNDLE_EXTRA_MERCHANT_SECRET = "io.mpos.ui.paybuttonTransactionActivity.MERCHANT_SECRET";
    public final static String BUNDLE_EXTRA_PROVIDER_MODE = "io.mpos.ui.TransactionActivity.PROVIDER_MODE";
    public final static String BUNDLE_EXTRA_ACCESSORY_FAMILY = "io.mpos.ui.paybutton.TransactionActivity.ACCESSORY_FAMILY";
    public final static String BUNDLE_EXTRA_AMOUNT = "io.mpos.ui.paybutton.TransactionActivity.AMOUNT";
    public final static String BUNDLE_EXTRA_CURRENCY = "io.mpos.ui.paybutton.TransactionActivity.CURRENCY";
    public final static String BUNDLE_EXTRA_SUBJECT = "io.mpos.ui.paybutton.TransactionActivity.SUBJECT";
    public final static String BUNDLE_EXTRA_TRANSACTION_TYPE = "io.mpos.ui.paybutton.TransactionActivity.TRANSACTION_TYPE";
    public final static String BUNDLE_EXTRA_CUSTOM_IDENTIFIER = "io.mpos.ui.paybutton.TransactionActivity.CUSTOM_IDENTIFIER";
    public final static String BUNDLE_EXTRA_SESSION_IDENTIFIER = "io.mpos.ui.paybutton.TransactionActivity.SESSION_IDENTIFIER";
    public final static String BUNDLE_EXTRA_TRANSACTION_IDENTIFIER = "io.mpos.ui.paybutton.TransactionActivity.TRANSACTION_IDENTIFIER";
    public final static String BUNDLE_EXTRA_IS_REFUND = "io.mpos.ui.paybutton.TransactionActivity.IS_REFUND";

    private static final int REQUEST_CODE_SIGNATURE = 1;
    private static final String BUNDLE_FORMATTED_AMOUNT = "io.mpos.ui.paybutton.TransactionActivity.FORMATTED_AMOUNT";
    private static final String BUNDLE_TITLE_TRANSACTION_TYPE = "io.mpos.ui.paybutton.TransactionActivity.TITLE_TRANSACTION_TYPE";

    public final static String SAVED_INSTANCE_STATE_UI_STATE = "io.mpos.ui.io.UI_STATE";

    private String mFormattedAmount;
    private String mTitleTransactionType;

    private StatefulTransactionProviderProxy mStatefulTransactionProviderProxy = StatefulTransactionProviderProxy.getInstance();
    private UiState mUiState = UiState.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (getCallingActivity() == null) {
            Log.w(TAG, "The transaction activity was started without startActivityForResult() and will not return a result code.");
        }

        UiHelper.setActionbarWithCustomColors(this, (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar), false);

        if (getIntent().hasExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER)) {
            if (savedInstanceState == null) {
                setTitle("");
            } else {
                mFormattedAmount = savedInstanceState.getString(BUNDLE_FORMATTED_AMOUNT);
                mTitleTransactionType = savedInstanceState.getString(BUNDLE_TITLE_TRANSACTION_TYPE);
                setTitle(constructTitle());
            }

        } else {
            Currency currency = (Currency) getIntent().getSerializableExtra(BUNDLE_EXTRA_CURRENCY);
            BigDecimal amount = (BigDecimal) getIntent().getSerializableExtra(BUNDLE_EXTRA_AMOUNT);
            constructTransactionTypeTitle();

            mFormattedAmount = UiHelper.formatAmountWithSymbol(currency, amount);

            setTitle(constructTitle());
        }

        if (!mStatefulTransactionProviderProxy.isTransactionOnGoing() && savedInstanceState == null) {
            startTransaction();
        }
    }

    private void constructTransactionTypeTitle() {
        TransactionType transactionType = (TransactionType) getIntent().getSerializableExtra(BUNDLE_EXTRA_TRANSACTION_TYPE);
        mTitleTransactionType = getString(R.string.MPUSale);
        if (transactionType != null && transactionType == TransactionType.REFUND) {
            mTitleTransactionType = getString(R.string.MPURefund);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStatefulTransactionProviderProxy.attachCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStatefulTransactionProviderProxy.attachCallback(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(BUNDLE_FORMATTED_AMOUNT, mFormattedAmount);
        outState.putString(BUNDLE_TITLE_TRANSACTION_TYPE, mTitleTransactionType);
        outState.putSerializable(SAVED_INSTANCE_STATE_UI_STATE, mUiState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SIGNATURE) {
            if (resultCode == RESULT_CANCELED) {
                mStatefulTransactionProviderProxy.continueWithSignature(null, false);
            } else {
                byte[] byteArraySignature = data.getByteArrayExtra(SignatureActivity.BUNDLE_KEY_SIGNATURE_IMAGE);
                Bitmap signature = BitmapFactory.decodeByteArray(byteArraySignature, 0, byteArraySignature.length);
                mStatefulTransactionProviderProxy.continueWithSignature(signature, true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().findFragmentByTag(SendReceiptFragment.TAG) != null) {
            showSummaryFragment(mStatefulTransactionProviderProxy.getCurrentTransaction(), null);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onApplicationSelectionRequired(List<ApplicationInformation> applicationInformations) {
        Log.d(TAG, "onApplicationSelectionRequired");
        mUiState = UiState.TRANSACTION_WAITING_APPLICATION_SELECTION;
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.container, ApplicationSelectionFragment.newInstance(applicationInformations), ApplicationSelectionFragment.TAG)
                .commit();
    }

    @Override
    public void onCustomerSignatureRequired() {
        Log.d(TAG, "onCustomerSignatureRequired");

        if (MposUi.getInitializedInstance().getConfiguration().getSignatureCapture() == MposUiConfiguration.SignatureCapture.ON_SCREEN) {
            showSignatureActivity();
        } else {
            throw new IllegalStateException("Signature on paper isn't supported, right now!");
        }
    }


    @Override
    public void onStatusChanged(TransactionProcessDetails details, Transaction transaction) {
        Log.d(TAG, "onStatusChanged=" + details);
        if (transaction != null && getIntent().hasExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER)) {
            mFormattedAmount = UiHelper.formatAmountWithSymbol(transaction.getCurrency(), transaction.getAmount());
            constructTransactionTypeTitle();
            setTitle(constructTitle());
        }

        TransactionFragment paymentFragment = (TransactionFragment) getFragmentManager().findFragmentByTag(TransactionFragment.TAG);
        if (paymentFragment == null) {
            paymentFragment = TransactionFragment.newInstance();
            getFragmentManager().beginTransaction().replace(R.id.container, paymentFragment, TransactionFragment.TAG).commit();
        }

        paymentFragment.updateStatus(details, transaction);

    }

    @Override
    public void onCompleted(Transaction transaction, MposError error) {
        if (transaction == null && error == null) {
            mUiState = UiState.TRANSACTION_ERROR;
            MposError e = new DefaultMposError(ErrorType.TRANSACTION_ABORTED);
            showErrorFragment(false, e);
        } else if (error == null) {
            showSummaryFragment(transaction, null);
        } else {
            mUiState = UiState.TRANSACTION_ERROR;
            if(mStatefulTransactionProviderProxy.getLastTransactionProcessDetails().getState() == TransactionProcessDetailsState.NOT_REFUNDABLE) {
                showErrorFragment(false, error);
            } else {
                showErrorFragment(!getIntent().hasExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER), error);
            }
        }
    }

    @Override
    public void onSummaryClosed(TransactionDataHolder transaction, MposError error) {
        boolean approved = (transaction != null && transaction.getTransactionStatus().equals(TransactionStatus.APPROVED.name()));
        String identifier = (transaction != null ? transaction.getTransactionIdentifier() : null);
        finish(approved, identifier);
    }

    @Override
    public void onSummaryPrintReceiptButtonClicked(String transactionIdentifier) {
        showPrintReceiptFragment(transactionIdentifier);
    }

    @Override
    public void onAbortTransactionButtonClicked() {
        boolean result = mStatefulTransactionProviderProxy.abortTransaction();
        if (!result) {
            Toast.makeText(this, R.string.MPUBackButtonDisabled, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onApplicationSelected(ApplicationInformation applicationInformation) {
        mStatefulTransactionProviderProxy.continueWithApplicationSelection(applicationInformation);
    }

    @Override
    public void onSummaryRetryButtonClicked() {
        startTransaction();
    }

    @Override
    public void onSummaryRefundButtonClicked(String transactionIdentifier) {
        // noop
    }

    @Override
    public void onErrorRetryButtonClicked() {
        if (mUiState == UiState.TRANSACTION_ERROR) {
            startTransaction();
        } else if (mUiState == UiState.RECEIPT_PRINTING_ERROR) {
            showPrintReceiptFragment(mStatefulTransactionProviderProxy.getCurrentTransaction().getIdentifier());
        }
    }

    @Override
    public void onErrorCancelButtonClicked() {
        Transaction transaction = mStatefulTransactionProviderProxy.getCurrentTransaction();
        String identifier = (transaction != null ? transaction.getIdentifier() : null);
        finish(false, identifier);
    }

    @Override
    public void onSendReceiptButtonClicked(String transactionIdentifier) {
        showSendReceiptFragment(transactionIdentifier);
    }

    @Override
    public void onReceiptSent() {
        showSummaryFragment(mStatefulTransactionProviderProxy.getCurrentTransaction(), null);
    }

    @Override
    public void onReceiptPrintCompleted(MposError error) {
        StatefulPrintingProcessProxy.getInstance().teardown();
        if (error != null) {
            mUiState = UiState.RECEIPT_PRINTING_ERROR;
            showErrorFragment(true, error);
        } else {
            showSummaryFragment(mStatefulTransactionProviderProxy.getCurrentTransaction(), null);
        }
    }

    @Override
    public void onAbortPrintingClicked() {
        StatefulPrintingProcessProxy.getInstance().requestAbort();
    }

    @Override
    public TransactionProvider getTransactionProvider() {
        return mStatefulTransactionProviderProxy.getTransactionProvider();
    }

    private void startTransaction() {
        boolean isRefund = getIntent().getBooleanExtra(BUNDLE_EXTRA_IS_REFUND, false);
        String sessionIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER);
        String merchantId = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_ID);
        String merchantSecret = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_SECRET);
        String subject = getIntent().getStringExtra(BUNDLE_EXTRA_SUBJECT);
        String customIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_CUSTOM_IDENTIFIER);
        String transactionIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_TRANSACTION_IDENTIFIER);
        ProviderMode providerMode = (ProviderMode) getIntent().getSerializableExtra(BUNDLE_EXTRA_PROVIDER_MODE);
        AccessoryFamily accessoryFamily = (AccessoryFamily) getIntent().getSerializableExtra(BUNDLE_EXTRA_ACCESSORY_FAMILY);
        BigDecimal amount = (BigDecimal) getIntent().getSerializableExtra(BUNDLE_EXTRA_AMOUNT);
        Currency currency = (Currency) getIntent().getSerializableExtra(BUNDLE_EXTRA_CURRENCY);

        if (mStatefulTransactionProviderProxy.isTransactionOnGoing()) {
            setResult(MposUi.RESULT_CODE_FAILED);
            finish();
        }

        if (sessionIdentifier == null) {
            if (isRefund) {
                mStatefulTransactionProviderProxy.startRefundTransaction(getApplicationContext(), providerMode, merchantId, merchantSecret, accessoryFamily, transactionIdentifier, subject, customIdentifier);
            } else {
                mStatefulTransactionProviderProxy.startChargeTransaction(getApplicationContext(), providerMode, merchantId, merchantSecret, accessoryFamily, amount, currency, subject, customIdentifier);
            }
        } else {
            mStatefulTransactionProviderProxy.startTransactionWithSessionIdentifier(getApplicationContext(), providerMode, merchantId, merchantSecret, accessoryFamily, sessionIdentifier);
        }
    }

    private void finish(boolean approved, String transactionIdentifier) {
        mStatefulTransactionProviderProxy.teardown();

        int resultCode = (approved) ? MposUi.RESULT_CODE_APPROVED : MposUi.RESULT_CODE_FAILED;

        Intent resultIntent = new Intent();
        resultIntent.putExtra(MposUi.RESULT_EXTRA_TRANSACTION_IDENTIFIER, transactionIdentifier);

        setResult(resultCode, resultIntent);
        finish();
    }

    private void showSignatureActivity() {
        Intent intent = new Intent(this, SignatureActivity.class);
        intent.putExtra(SignatureActivity.BUNDLE_KEY_AMOUNT, mFormattedAmount);
        PaymentDetailsScheme scheme = mStatefulTransactionProviderProxy.getCurrentTransaction().getPaymentDetails().getScheme();
        if (scheme != null) {
            int resId = UiHelper.getDrawableIdImageForCreditCard(scheme);
            intent.putExtra(SignatureActivity.BUNDLE_KEY_CARD_SCHEME_ID, resId);
        }
        startActivityForResult(intent, REQUEST_CODE_SIGNATURE);
    }

    private void showSendReceiptFragment(String transactionIdentifier) {
        SendReceiptFragment fragment = SendReceiptFragment.newInstance(transactionIdentifier);
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.container, fragment, SendReceiptFragment.TAG)
                .commit();
    }

    private void showSummaryFragment(Transaction transaction, MposError error) {
        mUiState = UiState.SUMMARY_DISPLAYING;
        TransactionDataHolder dataHolder = TransactionDataHolder.createTransactionDataHolder(transaction);
        SummaryFragment summaryFragment = SummaryFragment.newInstance(!getIntent().hasExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER), false, dataHolder, error);
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.container, summaryFragment, SummaryFragment.TAG)
                .commit();
    }

    private void showErrorFragment(boolean retryEnabled, MposError error) {
        ErrorFragment fragment = ErrorFragment.newInstance(retryEnabled, error, mStatefulTransactionProviderProxy.getLastTransactionProcessDetails());
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.container, fragment, ErrorFragment.TAG)
                .commit();

    }

    private void showPrintReceiptFragment(String transactionIdentifier) {
        mUiState = UiState.RECEIPT_PRINTING;
        PrintReceiptFragment fragment = PrintReceiptFragment.newInstance(transactionIdentifier);
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.container, fragment, PrintReceiptFragment.TAG)
                .commit();
    }

    private String constructTitle() {
        if (mFormattedAmount == null) {
            return mTitleTransactionType;
        } else {
            return mTitleTransactionType + ": " + mFormattedAmount;
        }
    }
}
