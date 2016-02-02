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
package io.mpos.ui.paybutton.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.List;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.accessories.parameters.AccessoryParameters;
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
import io.mpos.transactions.parameters.TransactionParameters;
import io.mpos.ui.R;
import io.mpos.ui.acquirer.MposUiAccountManager;
import io.mpos.ui.acquirer.view.LoginFragment;
import io.mpos.ui.paybutton.controller.StatefulTransactionProviderProxy;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.controller.StatefulPrintingProcessProxy;
import io.mpos.ui.shared.model.MposUiConfiguration;
import io.mpos.ui.shared.model.TransactionDataHolder;
import io.mpos.ui.shared.util.ErrorHolder;
import io.mpos.ui.shared.util.UiHelper;
import io.mpos.ui.shared.util.UiState;
import io.mpos.ui.shared.view.ErrorFragment;
import io.mpos.ui.shared.view.PrintReceiptFragment;
import io.mpos.ui.shared.view.SendReceiptFragment;
import io.mpos.ui.shared.view.SummaryFragment;

public class TransactionActivity extends AbstractTransactionActivity implements StatefulTransactionProviderProxy.Callback,
        AbstractTransactionFragment.Interaction,
        ErrorFragment.Interaction,
        SummaryFragment.Interaction,
        SendReceiptFragment.Interaction,
        PrintReceiptFragment.Interaction,
        LoginFragment.Interaction {

    private final static String TAG = "TransactionActivity";

    public final static String BUNDLE_EXTRA_MERCHANT_ID = "io.mpos.ui.paybutton.view.TransactionActivity.MERCHANT_ID";
    public final static String BUNDLE_EXTRA_MERCHANT_SECRET = "io.mpos.ui.paybutton.view.TransactionActivity.MERCHANT_SECRET";
    public final static String BUNDLE_EXTRA_PROVIDER_MODE = "io.mpos.ui.paybutton.view.TransactionActivity.PROVIDER_MODE";
    public final static String BUNDLE_EXTRA_AMOUNT = "io.mpos.ui.paybutton.view.TransactionActivity.AMOUNT";
    public final static String BUNDLE_EXTRA_CURRENCY = "io.mpos.ui.paybutton.view.TransactionActivity.CURRENCY";
    public final static String BUNDLE_EXTRA_SUBJECT = "io.mpos.ui.paybutton.view.TransactionActivity.SUBJECT";
    public final static String BUNDLE_EXTRA_TRANSACTION_TYPE = "io.mpos.ui.paybutton.view.TransactionActivity.TRANSACTION_TYPE";
    public final static String BUNDLE_EXTRA_CUSTOM_IDENTIFIER = "io.mpos.ui.paybutton.view.TransactionActivity.CUSTOM_IDENTIFIER";
    public final static String BUNDLE_EXTRA_SESSION_IDENTIFIER = "io.mpos.ui.paybutton.view.TransactionActivity.SESSION_IDENTIFIER";
    public final static String BUNDLE_EXTRA_TRANSACTION_IDENTIFIER = "io.mpos.ui.paybutton.view.TransactionActivity.TRANSACTION_IDENTIFIER";
    public final static String BUNDLE_EXTRA_IS_REFUND = "io.mpos.ui.paybutton.view.TransactionActivity.IS_REFUND";

    public final static String BUNDLE_EXTRA_TRANSACTION_PARAMETERS = "io.mpos.ui.paybutton.view.TransactionActivity.TRANSACTION_PARAMETERS";

    public final static String BUNDLE_EXTRA_ACQUIRER_LOGIN = "io.mpos.ui.paybutton.TransactionActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN";
    public final static String BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID = "io.mpos.ui.paybutton.TransactionActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN";

    private static final int REQUEST_CODE_SIGNATURE = 1;

    private static final String BUNDLE_FORMATTED_AMOUNT = "io.mpos.ui.paybutton.view.TransactionActivity.FORMATTED_AMOUNT";
    private static final String BUNDLE_TITLE_TRANSACTION_TYPE = "io.mpos.ui.paybutton.view.TransactionActivity.TITLE_TRANSACTION_TYPE";

    public final static String SAVED_INSTANCE_STATE_UI_STATE = "io.mpos.ui.paybutton.view.TransactionActivity.UI_STATE";

    private String mFormattedAmount;
    private String mTitleTransactionType;

    private MposUiAccountManager mMposUiAccountManager;
    private String mMerchantIdentifier;
    private String mMerchantSecretKey;
    private ProviderMode mProviderMode;
    private TransactionParameters mTransactionParameters;
    private AccessoryParameters mAccessoryParameters;
    private String mSessionIdentifier;

    private String mApplicationIdentifier;
    private boolean mIsAcquirerMode;
    private boolean mIsRefund;
    private boolean mHasSessionIdentifier;
    private boolean mHasTransactionParameters;

    private String mSubject;
    private String mCustomIdentifier;
    private String mTransactionIdentifier;
    private BigDecimal mAmount;
    private Currency mCurrency;
    private TransactionType mTransactionType;
    private AccessoryFamily mAccessoryFamily;

    private StatefulTransactionProviderProxy mStatefulTransactionProviderProxy = StatefulTransactionProviderProxy.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mpu_activity_payment);

        if (getCallingActivity() == null) {
            Log.w(TAG, "The transaction activity was started without startActivityForResult() and will not return a result code.");
        }

        UiHelper.setActionbarWithCustomColors(this, (android.support.v7.widget.Toolbar) findViewById(R.id.mpu_toolbar));

        ErrorHolder.getInstance().clear();

        parseExtras();

        if (savedInstanceState == null) {
            if (mHasSessionIdentifier) {
                setTitle("");
            } else if (mHasTransactionParameters) {
                mTransactionParameters = (TransactionParameters) getIntent().getSerializableExtra(BUNDLE_EXTRA_TRANSACTION_PARAMETERS);
                mFormattedAmount = UiHelper.formatAmountWithSymbol(mTransactionParameters.getCurrency(), mTransactionParameters.getAmount());
                constructTransactionTypeTitle();
                setTitle(constructTitle());
            } else {
                constructTransactionTypeTitle();
                mFormattedAmount = UiHelper.formatAmountWithSymbol(mCurrency, mAmount);
                setTitle(constructTitle());
            }
        } else {
            mFormattedAmount = savedInstanceState.getString(BUNDLE_FORMATTED_AMOUNT);
            mTitleTransactionType = savedInstanceState.getString(BUNDLE_TITLE_TRANSACTION_TYPE);
            setUiState((UiState) savedInstanceState.getSerializable(SAVED_INSTANCE_STATE_UI_STATE));
            setTitle(constructTitle());
        }

        if (!mStatefulTransactionProviderProxy.isTransactionOnGoing() && savedInstanceState == null) {
            if (mIsAcquirerMode) {
                mMposUiAccountManager = MposUiAccountManager.getInitializedInstance();
                mProviderMode = mMposUiAccountManager.getProviderMode();
                if (mMposUiAccountManager.isLoggedIn()) {
                    // Already logged in. Proceed with the transaction.
                    mMerchantIdentifier = mMposUiAccountManager.getMerchantIdentifier();
                    mMerchantSecretKey = mMposUiAccountManager.getMerchantSecretKey();
                    setTitle(constructTitle());
                    startTransaction();
                } else {
                    // Show the acquirer UI and continue only if logged in.
                    mStatefulTransactionProviderProxy.clearForNewTransaction(); // This needs to be done!
                    showLoginFragment(mApplicationIdentifier);
                }
            } else {
                mMerchantSecretKey = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_SECRET);
                mMerchantIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_ID);
                mProviderMode = (ProviderMode) getIntent().getSerializableExtra(BUNDLE_EXTRA_PROVIDER_MODE);
                startTransaction();
            }
        }
    }

    private String modifyCustomIdentifier(String integratorIdentifier, String customIdentifier) {
        if (!TextUtils.isEmpty(customIdentifier)) {
            return integratorIdentifier + "-" + customIdentifier;
        }
        return integratorIdentifier;
    }

    private void parseExtras() {

        if (getIntent().hasExtra(BUNDLE_EXTRA_ACQUIRER_LOGIN)) {
            mIsAcquirerMode = getIntent().hasExtra(BUNDLE_EXTRA_ACQUIRER_LOGIN);
            mApplicationIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID);
        }

        mAccessoryFamily = MposUi.getInitializedInstance().getConfiguration().getAccessoryFamily();
        mAccessoryParameters = MposUi.getInitializedInstance().getConfiguration().getTerminalParameters();

        if (getIntent().hasExtra(BUNDLE_EXTRA_TRANSACTION_PARAMETERS)) {
            mHasTransactionParameters = true;
            mTransactionParameters = (TransactionParameters) getIntent().getSerializableExtra(BUNDLE_EXTRA_TRANSACTION_PARAMETERS);
            mFormattedAmount = UiHelper.formatAmountWithSymbol(mTransactionParameters.getCurrency(), mTransactionParameters.getAmount());
            mIsRefund = mTransactionParameters.getType() == TransactionType.REFUND;
            mTransactionType = mTransactionParameters.getType();
        } else if (getIntent().hasExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER)) {
            mHasSessionIdentifier = true;
            mSessionIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER);
        } else {
            mIsRefund = getIntent().getBooleanExtra(BUNDLE_EXTRA_IS_REFUND, false);
            mSubject = getIntent().getStringExtra(BUNDLE_EXTRA_SUBJECT);
            mCustomIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_CUSTOM_IDENTIFIER);
            mTransactionIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_TRANSACTION_IDENTIFIER);
            mAmount = (BigDecimal) getIntent().getSerializableExtra(BUNDLE_EXTRA_AMOUNT);
            mCurrency = (Currency) getIntent().getSerializableExtra(BUNDLE_EXTRA_CURRENCY);
            mTransactionType = (TransactionType) getIntent().getSerializableExtra(BUNDLE_EXTRA_TRANSACTION_TYPE);
        }
    }

    private void startTransaction() {

        if (mIsAcquirerMode) {
            String integratorIdentifier = MposUiAccountManager.getInitializedInstance().getIntegratorIdentifier();
            if (mHasTransactionParameters) {
                String modifiedCustomIdentifier = modifyCustomIdentifier(integratorIdentifier, mTransactionParameters.getCustomIdentifier());
                mTransactionParameters = io.mpos.ui.shared.util.TransactionParametersHelper.getTransactionParametersWithNewCustomerIdentifier(mTransactionParameters, modifiedCustomIdentifier);
            } else {
                mCustomIdentifier = modifyCustomIdentifier(integratorIdentifier, mCustomIdentifier);
            }
        }

        if (mHasSessionIdentifier) {
            if (mAccessoryParameters != null) {
                mStatefulTransactionProviderProxy.startTransactionWithSessionIdentifier(getApplicationContext(), mProviderMode, mMerchantIdentifier, mMerchantSecretKey, mAccessoryParameters, mSessionIdentifier);
            } else {
                mStatefulTransactionProviderProxy.startTransactionWithSessionIdentifier(getApplicationContext(), mProviderMode, mMerchantIdentifier, mMerchantSecretKey, mAccessoryFamily, mSessionIdentifier);
            }
        } else if (mHasTransactionParameters) {
            mStatefulTransactionProviderProxy.startTransaction(getApplicationContext(), mProviderMode, mMerchantIdentifier, mMerchantSecretKey, mAccessoryParameters, mTransactionParameters);
        } else {
            if (mIsRefund) {
                mStatefulTransactionProviderProxy.startRefundTransaction(getApplicationContext(), mProviderMode, mMerchantIdentifier, mMerchantSecretKey, mAccessoryFamily, mTransactionIdentifier, mSubject, mCustomIdentifier);
            } else {
                mStatefulTransactionProviderProxy.startChargeTransaction(getApplicationContext(), mProviderMode, mMerchantIdentifier, mMerchantSecretKey, mAccessoryFamily, mAmount, mCurrency, mSubject, mCustomIdentifier);
            }
        }
    }

    private void constructTransactionTypeTitle() {
        mTitleTransactionType = getString(R.string.MPUSale);
        if (mTransactionType != null && mTransactionType == TransactionType.REFUND) {
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
        outState.putSerializable(SAVED_INSTANCE_STATE_UI_STATE, getUiState());
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
        navigateBack();
    }

    @Override
    public void navigateBack() {
        hideSoftKeyboard();

        if (getUiState() == UiState.RECEIPT_SENDING || getUiState() == UiState.RECEIPT_PRINTING_ERROR) {
            showSummaryFragment(mStatefulTransactionProviderProxy.getCurrentTransaction());
        } else if (getUiState() == UiState.TRANSACTION_ERROR) {
            processErrorState();
        } else if (getUiState() == UiState.SUMMARY_DISPLAYING || getUiState() == UiState.LOGIN_DISPLAYING) {
            finishWithResult();
        } else if (getUiState() == UiState.FORGOT_PASSWORD_DISPLAYING) {
            LoginFragment loginFragment = (LoginFragment) getFragmentManager().findFragmentByTag(LoginFragment.TAG);
            if (loginFragment != null) {
                loginFragment.setLoginMode(true);
            }
        } else {
            super.navigateBack();
        }
    }

    private void processErrorState() {
        if (mIsAcquirerMode &&
                MposUi.getInitializedInstance().getError() != null &&
                MposUi.getInitializedInstance().getError().getErrorType() == ErrorType.SERVER_AUTHENTICATION_FAILED) {
            showLoginFragment(MposUiAccountManager.getInitializedInstance().getApplicationData().getIdentifier());
        } else {
            finishWithResult();
        }
    }

    @Override
    public void onApplicationSelectionRequired(List<ApplicationInformation> applicationInformations) {
        Log.d(TAG, "onApplicationSelectionRequired");
        showApplicationSelectionFragment(applicationInformations);
    }

    @Override
    public void onCustomerSignatureRequired() {
        Log.d(TAG, "onCustomerSignatureRequired");

        if (MposUi.getInitializedInstance().getConfiguration().getSignatureCapture() == MposUiConfiguration.SignatureCapture.ON_SCREEN) {
            showSignatureActivity();
        } else {
            mStatefulTransactionProviderProxy.continueWithCustomerSignatureOnReceipt();
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
            getFragmentManager().beginTransaction().replace(R.id.mpu_fragment_container, paymentFragment, TransactionFragment.TAG).commit();
        }
        showFragment(paymentFragment, TransactionFragment.TAG, UiState.TRANSACTION_ONGOING, false);
        paymentFragment.updateStatus(details, transaction);
    }

    @Override
    public void onCompleted(Transaction transaction, MposError error) {
        TransactionProcessDetails details = mStatefulTransactionProviderProxy.getLastTransactionProcessDetails();

        if (transaction == null && error == null) {
            MposError e = new DefaultMposError(ErrorType.TRANSACTION_ABORTED);
            showErrorFragment(UiState.TRANSACTION_ERROR, false, e, details);
        } else if (error == null) {
            showSummaryFragment(transaction);
        } else {
            ErrorHolder.getInstance().setError(error);
            if (mIsAcquirerMode && error.getErrorType() == ErrorType.SERVER_AUTHENTICATION_FAILED) {
                mMposUiAccountManager.logout(false);
            }
            if (mStatefulTransactionProviderProxy.getLastTransactionProcessDetails().getState() == TransactionProcessDetailsState.NOT_REFUNDABLE) {
                showErrorFragment(UiState.TRANSACTION_ERROR, false, error, details);
            } else if (error.getErrorType() == ErrorType.ACCESSORY_BUSY) {
                showErrorFragment(UiState.TRANSACTION_ERROR, true, error, details);
            } else {
                showErrorFragment(UiState.TRANSACTION_ERROR, !getIntent().hasExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER), error, details);
            }
        }
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
        setTitle(constructTitle());
        startTransaction();
    }

    @Override
    public void onSummaryRefundButtonClicked(String transactionIdentifier) {
        // noop
    }

    @Override
    public void onErrorRetryButtonClicked() {
        if (getUiState() == UiState.TRANSACTION_ERROR) {
            startTransaction();
        } else if (getUiState() == UiState.RECEIPT_PRINTING_ERROR) {
            showPrintReceiptFragment(mStatefulTransactionProviderProxy.getCurrentTransaction().getIdentifier());
        }
    }

    @Override
    public void onSendReceiptButtonClicked(String transactionIdentifier) {
        showSendReceiptFragment(transactionIdentifier);
    }

    @Override
    public void onReceiptSent() {
        showSummaryFragment(mStatefulTransactionProviderProxy.getCurrentTransaction());
    }

    @Override
    public void onReceiptPrintCompleted(MposError error) {
        StatefulPrintingProcessProxy.getInstance().teardown();
        if (error != null) {
            ErrorHolder.getInstance().setError(error);
            showErrorFragment(UiState.RECEIPT_PRINTING_ERROR, true, error, null);
        } else {
            showSummaryFragment(mStatefulTransactionProviderProxy.getCurrentTransaction());
        }
    }

    @Override
    public void onAbortPrintingClicked() {
        StatefulPrintingProcessProxy.getInstance().requestAbort();
    }

    // Login Interaction

    @Override
    public void onLoginCompleted() {
        mMerchantIdentifier = mMposUiAccountManager.getMerchantIdentifier();
        mMerchantSecretKey = mMposUiAccountManager.getMerchantSecretKey();
        setTitle(constructTitle());
        startTransaction();
    }

    @Override
    public void onLoginModeChanged(boolean loginMode) {
        // handle back button behaviour
        if (loginMode) {
            setUiState(UiState.LOGIN_DISPLAYING);
        } else {
            setUiState(UiState.FORGOT_PASSWORD_DISPLAYING);

        }
    }

    @Override
    public TransactionProvider getTransactionProvider() {
        return mStatefulTransactionProviderProxy.getTransactionProvider();
    }

    private void finishWithResult() {
        Transaction transaction = mStatefulTransactionProviderProxy.getCurrentTransaction();

        int resultCode;
        String transactionIdentifier = null;

        if (transaction != null) {
            boolean approved = (transaction.getStatus() == TransactionStatus.APPROVED);
            resultCode = approved ? MposUi.RESULT_CODE_APPROVED : MposUi.RESULT_CODE_FAILED;
            transactionIdentifier = transaction.getIdentifier();
        } else {
            resultCode = MposUi.RESULT_CODE_FAILED;
        }

        mStatefulTransactionProviderProxy.teardown();

        Intent resultIntent = new Intent();
        resultIntent.putExtra(MposUi.RESULT_EXTRA_TRANSACTION_IDENTIFIER, transactionIdentifier);

        setResult(resultCode, resultIntent);
        finish();
    }

    private void showApplicationSelectionFragment(List<ApplicationInformation> applicationInformations) {
        ApplicationSelectionFragment fragment = ApplicationSelectionFragment.newInstance(applicationInformations);
        showFragment(fragment, ApplicationSelectionFragment.TAG, UiState.TRANSACTION_WAITING_APPLICATION_SELECTION, false);
    }

    private void showSignatureActivity() {
        setUiState(UiState.TRANSACTION_WAITING_SIGNATURE);
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
        showFragment(fragment, SendReceiptFragment.TAG, UiState.RECEIPT_SENDING, true);
    }

    private void showSummaryFragment(Transaction transaction) {
        TransactionDataHolder dataHolder = TransactionDataHolder.createTransactionDataHolder(transaction);
        SummaryFragment summaryFragment = SummaryFragment.newInstance(!getIntent().hasExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER), false, dataHolder);

        showFragment(summaryFragment, SummaryFragment.TAG, UiState.SUMMARY_DISPLAYING, true);
    }

    private void showErrorFragment(UiState uiState, boolean retryEnabled, MposError error, TransactionProcessDetails transactionProcessDetails) {
        ErrorFragment fragment = ErrorFragment.newInstance(retryEnabled, error, transactionProcessDetails);
        showFragment(fragment, ErrorFragment.TAG, uiState, true);
    }

    private void showPrintReceiptFragment(String transactionIdentifier) {
        PrintReceiptFragment fragment = PrintReceiptFragment.newInstance(transactionIdentifier);
        showFragment(fragment, PrintReceiptFragment.TAG, UiState.RECEIPT_PRINTING, false);
    }

    private void showLoginFragment(String applicationIdentifier) {
        LoginFragment fragment = LoginFragment.newInstance(applicationIdentifier);
        showFragment(fragment, LoginFragment.TAG, UiState.LOGIN_DISPLAYING, true);
    }

    private String constructTitle() {
        if (mFormattedAmount == null && mTitleTransactionType == null) { // Initialized with Session identifier;
            return "";
        }
        if (mFormattedAmount == null) {
            return mTitleTransactionType;
        } else {
            return mTitleTransactionType + ": " + mFormattedAmount;
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.mpu_fragment_container).getWindowToken(), 0);
    }
}
