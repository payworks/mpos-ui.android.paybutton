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
import io.mpos.errors.ErrorType;
import io.mpos.errors.MposError;
import io.mpos.provider.ProviderMode;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.parameters.TransactionParameters;
import io.mpos.ui.R;
import io.mpos.ui.acquirer.MposUiAccountManager;
import io.mpos.ui.acquirer.view.LoginFragment;
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

public class TransactionSummaryActivity extends AbstractBaseActivity implements
        LoadTransactionSummaryFragment.Interaction,
        SummaryFragment.Interaction,
        ErrorFragment.Interaction,
        SendReceiptFragment.Interaction,
        PrintReceiptFragment.Interaction,
        LoginFragment.Interaction {

    private final static String TAG = "TransactionSummaryActivity";

    public final static String BUNDLE_EXTRA_TRANSACTION_IDENTIFIER = "io.mpos.ui.summarybutton.TransactionSummaryActivity.TRANSACTION_IDENTIFIER";
    public final static String BUNDLE_EXTRA_MERCHANT_ID = "io.mpos.ui.summarybutton.TransactionSummaryActivity.MERCHANT_ID";
    public final static String BUNDLE_EXTRA_MERCHANT_SECRET = "io.mpos.ui.summarybutton.TransactionSummaryActivity.MERCHANT_SECRET";
    public final static String BUNDLE_EXTRA_PROVIDER_MODE = "io.mpos.ui.summarybutton.TransactionSummaryActivity.PROVIDER_MODE";

    public final static String BUNDLE_EXTRA_ACQUIRER_LOGIN = "io.mpos.ui.summarybutton.TransactionSummaryActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN";
    public final static String BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID = "io.mpos.ui.summarybutton.TransactionSummaryActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN";

    private final static String SAVED_INSTANCE_STATE_TRANSACTION_DATA_HOLDER = "io.mpos.ui.TRANSACTION_DATA_HOLDER";
    private final static String SAVED_INSTANCE_STATE_UI_STATE = "io.mpos.ui.UI_STATE";

    public final static String SAVED_INSTANCE_STATE_MERCHANT_ID = "io.mpos.ui.summarybutton.TransactionSummaryActivity.MERCHANT_ID";
    public final static String SAVED_INSTANCE_STATE_MERCHANT_SECRET = "io.mpos.ui.summarybutton.TransactionSummaryActivity.MERCHANT_SECRET";
    public final static String SAVED_INSTANCE_STATE_PROVIDER_MODE = "io.mpos.ui.summarybutton.TransactionSummaryActivity.PROVIDER_MODE";


    private TransactionProvider mTransactionProvider;
    private Transaction mTransaction;
    private TransactionDataHolder mTransactionDataHolder;
    private ViewGroup mContainer;

    private String mMerchantIdentifier;
    private String mMerchantSecretKey;
    private ProviderMode mProviderMode;
    private MposUiAccountManager mMposUiAccountManager;
    private boolean isAcquirerMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mpu_activity_transaction_summary);

        UiHelper.setActionbarWithCustomColors(this, (android.support.v7.widget.Toolbar) findViewById(R.id.mpu_toolbar));
        setTitle(R.string.MPUSummary);
        mContainer = (ViewGroup) findViewById(R.id.mpu_fragment_container);

        ErrorHolder.getInstance().clear();

        if (savedInstanceState == null) {
            if (getIntent().hasExtra(BUNDLE_EXTRA_ACQUIRER_LOGIN)) {
                isAcquirerMode = true;
                String applicationIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID);
                mMposUiAccountManager = MposUiAccountManager.getInitializedInstance();
                mProviderMode = mMposUiAccountManager.getProviderMode();

                if (mMposUiAccountManager.isLoggedIn()) {
                    mMerchantIdentifier = mMposUiAccountManager.getMerchantIdentifier();
                    mMerchantSecretKey = mMposUiAccountManager.getMerchantSecretKey();
                    createMposProvider();
                    showLoadingFragment();
                } else {
                    // Show the acquirer UI and continue only if logged in.
                    showLoginFragment(applicationIdentifier);
                }
            } else {
                mMerchantSecretKey = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_SECRET);
                mMerchantIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_ID);
                mProviderMode = (ProviderMode) getIntent().getSerializableExtra(BUNDLE_EXTRA_PROVIDER_MODE);

                createMposProvider();
                showLoadingFragment();
            }
        } else {
            mTransactionDataHolder = savedInstanceState.getParcelable(SAVED_INSTANCE_STATE_TRANSACTION_DATA_HOLDER);
            mMerchantIdentifier = savedInstanceState.getString(SAVED_INSTANCE_STATE_MERCHANT_ID);
            mMerchantSecretKey = savedInstanceState.getString(SAVED_INSTANCE_STATE_MERCHANT_SECRET);
            mProviderMode = (ProviderMode) savedInstanceState.getSerializable(SAVED_INSTANCE_STATE_PROVIDER_MODE);
            createMposProvider();
            setUiState((UiState) savedInstanceState.getSerializable(SAVED_INSTANCE_STATE_UI_STATE));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SAVED_INSTANCE_STATE_TRANSACTION_DATA_HOLDER, mTransactionDataHolder);
        outState.putSerializable(SAVED_INSTANCE_STATE_UI_STATE, getUiState());
        outState.putString(SAVED_INSTANCE_STATE_MERCHANT_ID, mMerchantIdentifier);
        outState.putString(SAVED_INSTANCE_STATE_MERCHANT_SECRET, mMerchantSecretKey);
        outState.putSerializable(SAVED_INSTANCE_STATE_PROVIDER_MODE, mProviderMode);
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
        } else if (getUiState() == UiState.FORGOT_PASSWORD_DISPLAYING) {
            LoginFragment loginFragment = (LoginFragment) getFragmentManager().findFragmentByTag(LoginFragment.TAG);
            if (loginFragment != null) {
                loginFragment.setLoginMode(true);
            }
        } else if (getUiState() == UiState.SUMMARY_ERROR || getUiState() == UiState.RECEIPT_PRINTING_ERROR) {
            processErrorState();
        } else if (getUiState() == UiState.RECEIPT_SENDING) {
            showSummaryFragment();
        } else {
            finishWithResult();
        }
    }

    private void processErrorState() {
        if (isAcquirerMode &&
                MposUi.getInitializedInstance().getError() != null &&
                MposUi.getInitializedInstance().getError().getErrorType() == ErrorType.SERVER_AUTHENTICATION_FAILED) {
            showLoginFragment(MposUiAccountManager.getInitializedInstance().getApplicationData().getIdentifier());
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
        mTransactionDataHolder = new TransactionDataHolder(transaction);
        showSummaryFragment();
    }

    @Override
    public void onLoadingError(MposError error) {
        if (isAcquirerMode && error.getErrorType() == ErrorType.SERVER_AUTHENTICATION_FAILED) {
            // Authentication failed. We need to logout.
            mMposUiAccountManager.logout(false);
        }
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
    public void onSummaryCaptureButtonClicked(String transactionIdentifier) {
        TransactionParameters transactionParameters = new TransactionParameters.Builder().capture(transactionIdentifier).build();
        Intent intent = MposUi.getInitializedInstance().createTransactionIntent(transactionParameters);
        startActivity(intent);
        finish();
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
        TransactionParameters transactionParameters = new TransactionParameters.Builder().refund(transactionIdentifier).build();
        Intent intent = MposUi.getInitializedInstance().createTransactionIntent(transactionParameters);
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

    @Override
    public void onLoginCompleted() {
        mMerchantIdentifier = mMposUiAccountManager.getMerchantIdentifier();
        mMerchantSecretKey = mMposUiAccountManager.getMerchantSecretKey();
        createMposProvider();
        showLoadingFragment();
    }

    @Override
    public void onLoginModeChanged(boolean loginMode) {
        if (loginMode) {
            setUiState(UiState.LOGIN_DISPLAYING);
        } else {
            setUiState(UiState.FORGOT_PASSWORD_DISPLAYING);
        }
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
        SummaryFragment fragment = SummaryFragment.newInstance(false, true, true, mTransactionDataHolder);
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

    private void showLoginFragment(String applicationIdentifier) {
        LoginFragment fragment = LoginFragment.newInstance(applicationIdentifier);
        showFragment(fragment, LoginFragment.TAG, UiState.LOGIN_DISPLAYING, true);
    }

    private void createMposProvider() {
        mTransactionProvider = Mpos.createTransactionProvider(getApplicationContext(), mProviderMode, mMerchantIdentifier, mMerchantSecretKey);
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
