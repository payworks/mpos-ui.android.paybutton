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
package io.mpos.ui.printbutton.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import io.mpos.Mpos;
import io.mpos.errors.ErrorType;
import io.mpos.errors.MposError;
import io.mpos.provider.ProviderMode;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.ui.R;
import io.mpos.ui.acquirer.MposUiAccountManager;
import io.mpos.ui.acquirer.view.LoginFragment;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.controller.StatefulPrintingProcessProxy;
import io.mpos.ui.shared.util.ErrorHolder;
import io.mpos.ui.shared.util.UiHelper;
import io.mpos.ui.shared.util.UiState;
import io.mpos.ui.shared.view.AbstractBaseActivity;
import io.mpos.ui.shared.view.ErrorFragment;
import io.mpos.ui.shared.view.PrintReceiptFragment;

public class PrintReceiptActivity extends AbstractBaseActivity implements
        PrintReceiptFragment.Interaction,
        ErrorFragment.Interaction,
        LoginFragment.Interaction {

    private final static String TAG = "PrintReceiptActivity";

    public final static String BUNDLE_EXTRA_TRANSACTION_IDENTIFIER = "io.mpos.ui.printbutton.view.PrintReceiptActivity.TRANSACTION_IDENTIFIER";
    public final static String BUNDLE_EXTRA_MERCHANT_ID = "io.mpos.ui.printbutton.view.PrintReceiptActivity.MERCHANT_ID";
    public final static String BUNDLE_EXTRA_MERCHANT_SECRET = "io.mpos.ui.printbutton.view.PrintReceiptActivity.MERCHANT_SECRET";
    public final static String BUNDLE_EXTRA_PROVIDER_MODE = "io.mpos.ui.printbutton.view.PrintReceiptActivity.PROVIDER_MODE";

    public final static String BUNDLE_EXTRA_ACQUIRER_LOGIN = "io.mpos.ui.printbutton.PrintReceiptActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN";
    public final static String BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID = "io.mpos.ui.printbutton.PrintReceiptActivity.BUNDLE_EXTRA_APPLICATION_ID";

    private final static String SAVED_INSTANCE_STATE_UI_STATE = "io.mpos.ui.UI_STATE";

    private TransactionProvider mTransactionProvider;
    private String mTransactionIdentifier;

    private String mMerchantIdentifier;
    private String mMerchantSecretKey;
    private ProviderMode mProviderMode;
    private MposUiAccountManager mMposUiAccountManager;
    private boolean isAcquirerMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mpu_activity_print_receipt);

        if (getCallingActivity() == null) {
            Log.w(TAG, "The printing activity was started without startActivityForResult() and will not return a result code.");
        }

        UiHelper.setActionbarWithCustomColors(this, (android.support.v7.widget.Toolbar) findViewById(R.id.mpu_toolbar));

        ErrorHolder.getInstance().clear();

        mTransactionIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_TRANSACTION_IDENTIFIER);

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
                    showPrintReceiptFragment();
                } else {
                    // Show the acquirer UI and continue only if logged in.
                    showLoginFragment(applicationIdentifier);
                }
            } else {
                mMerchantSecretKey = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_SECRET);
                mMerchantIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_ID);
                mProviderMode = (ProviderMode) getIntent().getSerializableExtra(BUNDLE_EXTRA_PROVIDER_MODE);

                createMposProvider();
                showPrintReceiptFragment();
            }
        } else {
            setUiState((UiState) savedInstanceState.getSerializable(SAVED_INSTANCE_STATE_UI_STATE));
        }
    }

    private void showPrintReceiptFragment() {
        PrintReceiptFragment fragment = PrintReceiptFragment.newInstance(mTransactionIdentifier);
        showFragment(fragment, PrintReceiptFragment.TAG, UiState.RECEIPT_PRINTING, false);
    }

    private void createMposProvider() {
        mTransactionProvider = Mpos.createTransactionProvider(getApplicationContext(), mProviderMode, mMerchantIdentifier, mMerchantSecretKey);
    }

    @Override
    public void onErrorRetryButtonClicked() {
        showPrintReceiptFragment();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SAVED_INSTANCE_STATE_UI_STATE, getUiState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onReceiptPrintCompleted(MposError error) {
        if (error != null) {
            if (isAcquirerMode && error.getErrorType() == ErrorType.SERVER_AUTHENTICATION_FAILED) {
                // Authentication failed. We need to logout.
                mMposUiAccountManager.logout(false);
            }
            ErrorHolder.getInstance().setError(error);
            showErrorFragment(error);
        } else {
            finishWithResult(true);
        }
    }

    @Override
    public void onAbortPrintingClicked() {
        finishWithResult(false);
    }

    @Override
    public void onLoginCompleted() {
        mMerchantIdentifier = mMposUiAccountManager.getMerchantIdentifier();
        mMerchantSecretKey = mMposUiAccountManager.getMerchantSecretKey();
        mProviderMode = ProviderMode.MOCK;
        createMposProvider();
        showPrintReceiptFragment();
    }

    @Override
    public void onLoginModeChanged(boolean loginMode) {
        if (loginMode) {
            setUiState(UiState.LOGIN_DISPLAYING);
        } else {
            setUiState(UiState.FORGOT_PASSWORD_DISPLAYING);
        }
    }

    @Override
    public TransactionProvider getTransactionProvider() {
        return mTransactionProvider;
    }

    @Override
    public void onBackPressed() {
        navigateBack();
    }

    @Override
    public void navigateBack() {
        if (getUiState() == UiState.LOGIN_DISPLAYING) {
            finishWithResult(false);
        } else if (getUiState() == UiState.RECEIPT_PRINTING_ERROR) {
            processErrorState();
        } else if (getUiState() == UiState.FORGOT_PASSWORD_DISPLAYING) {
            LoginFragment loginFragment = (LoginFragment) getFragmentManager().findFragmentByTag(LoginFragment.TAG);
            if (loginFragment != null) {
                loginFragment.setLoginMode(true);
            }
        } else {
            Toast.makeText(this, R.string.MPUBackButtonDisabled, Toast.LENGTH_LONG).show();
        }
    }

    private void processErrorState() {
        if (isAcquirerMode  &&
                MposUi.getInitializedInstance().getError()!= null &&
                MposUi.getInitializedInstance().getError().getErrorType() == ErrorType.SERVER_AUTHENTICATION_FAILED) {
            showLoginFragment(MposUiAccountManager.getInitializedInstance().getApplicationData().getIdentifier());
        } else {
            finishWithResult(false);
        }
    }

    private void finishWithResult(boolean success) {
        StatefulPrintingProcessProxy.getInstance().teardown();
        int resultCode = success ? MposUi.RESULT_CODE_PRINT_SUCCESS : MposUi.RESULT_CODE_PRINT_FAILED;
        setResult(resultCode);
        finish();
    }

    private void showErrorFragment(MposError error) {
        ErrorFragment fragment = ErrorFragment.newInstance(true, error, null);
        showFragment(fragment, ErrorFragment.TAG, UiState.RECEIPT_PRINTING_ERROR, true);
    }

    private void showLoginFragment(String applicationIdentifier) {
        LoginFragment fragment = LoginFragment.newInstance(applicationIdentifier);
        showFragment(fragment, PrintReceiptFragment.TAG, UiState.LOGIN_DISPLAYING, true);
    }
}
