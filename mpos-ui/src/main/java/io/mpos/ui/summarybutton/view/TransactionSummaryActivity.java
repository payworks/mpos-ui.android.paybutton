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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import io.mpos.Mpos;
import io.mpos.errors.MposError;
import io.mpos.provider.ProviderMode;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.transactions.Transaction;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.controller.StatefulPrintingProcessProxy;
import io.mpos.ui.shared.model.TransactionDataHolder;
import io.mpos.ui.shared.util.ErrorHolder;
import io.mpos.ui.shared.util.UiHelper;
import io.mpos.ui.shared.util.UiState;
import io.mpos.ui.shared.view.AbstractBaseActivity;
import io.mpos.ui.shared.view.ErrorFragment;
import io.mpos.ui.shared.view.PrintReceiptFragment;
import io.mpos.ui.shared.view.SendReceiptFragment;
import io.mpos.ui.shared.view.SummaryFragment;

public class TransactionSummaryActivity extends AbstractBaseActivity
        implements LoadTransactionSummaryFragment.Interaction, SummaryFragment.Interaction, ErrorFragment.Interaction, SendReceiptFragment.Interaction, PrintReceiptFragment.Interaction {

    private final static String TAG = "TransactionSummaryActivity";

    public final static String BUNDLE_EXTRA_TRANSACTION_IDENTIFIER = "io.mpos.ui.summarybutton.TransactionSummaryActivity.TRANSACTION_IDENTIFIER";
    public final static String BUNDLE_EXTRA_MERCHANT_ID = "io.mpos.ui.summarybutton.TransactionSummaryActivity.MERCHANT_ID";
    public final static String BUNDLE_EXTRA_MERCHANT_SECRET = "io.mpos.ui.summarybutton.TransactionSummaryActivity.MERCHANT_SECRET";
    public final static String BUNDLE_EXTRA_PROVIDER_MODE = "io.mpos.ui.summarybutton.TransactionSummaryActivity.PROVIDER_MODE";

    public final static String SAVED_INSTANCE_STATE_TRANSACTION_DATA_HOLDER = "io.mpos.ui.TRANSACTION_DATA_HOLDER";

    private TransactionProvider mTransactionProvider;
    private Transaction mTransaction;
    private TransactionDataHolder mTransactionDataHolder;
    private ViewGroup mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mpu_activity_transaction_summary);

        UiHelper.setActionbarWithCustomColors(this, (android.support.v7.widget.Toolbar) findViewById(R.id.mpu_toolbar));
        setTitle(R.string.MPUSummary);
        mContainer = (ViewGroup) findViewById(R.id.mpu_fragment_container);

        ErrorHolder.getInstance().clear();
        createMposProvider();

        if (savedInstanceState == null) {
            showLoadingFragment();
        } else {
            mTransactionDataHolder = savedInstanceState.getParcelable(SAVED_INSTANCE_STATE_TRANSACTION_DATA_HOLDER);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SAVED_INSTANCE_STATE_TRANSACTION_DATA_HOLDER, mTransactionDataHolder);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        navigateBack();
    }

    @Override
    public void navigateBack() {
        hideSoftKeyboard();

        if (getUiState() == UiState.RECEIPT_PRINTING) {
            Toast.makeText(this, R.string.MPUBackButtonDisabled, Toast.LENGTH_LONG).show();
        } else if (getUiState() == UiState.RECEIPT_SENDING || getUiState() == UiState.RECEIPT_PRINTING_ERROR) {
            showSummaryFragment();
        } else {
            finishWithResult();
        }
    }

    @Override
    public TransactionProvider getTransactionProvider() {
        return mTransactionProvider;
    }

    @Override
    public void onTransactionLoaded(Transaction transaction) {
        mTransaction = transaction;
        mTransactionDataHolder = TransactionDataHolder.createTransactionDataHolder(transaction);
        showSummaryFragment();
    }

    @Override
    public void onLoadingError(MposError error) {
        ErrorHolder.getInstance().setError(error);
        showErrorFragment(UiState.SUMMARY_ERROR, false, error);
    }

    @Override
    public void onErrorRetryButtonClicked() {
        if (getUiState() == UiState.SUMMARY_ERROR) {
            showLoadingFragment();
        } else if (getUiState() == UiState.RECEIPT_PRINTING_ERROR) {
            showPrintReceiptFragment(mTransaction.getIdentifier());
        }
    }

    @Override
    public void onSendReceiptButtonClicked(String transactionIdentifier) {
        showSendReceiptFragment(transactionIdentifier);
    }

    @Override
    public void onSummaryRetryButtonClicked() {
        // noop
    }

    @Override
    public void onSummaryRefundButtonClicked(String transactionIdentifier) {
        Intent intent = MposUi.getInitializedInstance().createRefundTransactionIntent(transactionIdentifier, null, null);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSummaryPrintReceiptButtonClicked(String transactionIdentifier) {
        showPrintReceiptFragment(transactionIdentifier);
    }

    @Override
    public void onReceiptSent() {
        showSummaryFragment();
    }

    @Override
    public void onReceiptPrintCompleted(MposError error) {
        StatefulPrintingProcessProxy.getInstance().teardown();

        if (error != null) {
            ErrorHolder.getInstance().setError(error);
            showErrorFragment(UiState.RECEIPT_PRINTING_ERROR, true, error);
        } else {
            showSummaryFragment();
        }
    }

    @Override
    public void onAbortPrintingClicked() {
        StatefulPrintingProcessProxy.getInstance().requestAbort();
    }

    private void showLoadingFragment() {
        String transactionIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_TRANSACTION_IDENTIFIER);
        LoadTransactionSummaryFragment fragment = LoadTransactionSummaryFragment.newInstance(transactionIdentifier);

        showFragment(fragment, LoadTransactionSummaryFragment.TAG, UiState.SUMMARY_LOADING, true);
    }

    private void showErrorFragment(UiState state, boolean retryEnabled, MposError error) {
        ErrorFragment fragment = ErrorFragment.newInstance(retryEnabled, error, null);
        showFragment(fragment, ErrorFragment.TAG, state, true);
    }

    private void showSummaryFragment() {
        SummaryFragment fragment = SummaryFragment.newInstance(false, true, mTransactionDataHolder, null);
        showFragment(fragment, SummaryFragment.TAG, UiState.SUMMARY_DISPLAYING, true);
    }

    private void showSendReceiptFragment(String transactionIdentifier) {
        SendReceiptFragment fragment = SendReceiptFragment.newInstance(transactionIdentifier);
        showFragment(fragment, SendReceiptFragment.TAG, UiState.RECEIPT_SENDING, true);
    }

    private void showPrintReceiptFragment(String transactionIdentifier) {
        PrintReceiptFragment fragment = PrintReceiptFragment.newInstance(transactionIdentifier);
        showFragment(fragment, PrintReceiptFragment.TAG, UiState.RECEIPT_PRINTING, false);
    }

    private void createMposProvider() {
        String merchantId = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_ID);
        String merchantSecret = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_SECRET);
        ProviderMode providerMode = (ProviderMode) getIntent().getSerializableExtra(BUNDLE_EXTRA_PROVIDER_MODE);

        mTransactionProvider = Mpos.createTransactionProvider(getApplicationContext(), providerMode, merchantId, merchantSecret);
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mContainer.getWindowToken(), 0);
    }

    private void finishWithResult() {
        setResult(MposUi.RESULT_CODE_SUMMARY_CLOSED);
        finish();
    }
}
