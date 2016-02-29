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
package io.mpos.ui.shared;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.math.BigDecimal;

import io.mpos.errors.MposError;
import io.mpos.provider.ProviderMode;
import io.mpos.transactionprovider.FetchReceiptListener;
import io.mpos.transactionprovider.TransactionProcessDetails;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.transactionprovider.processparameters.TransactionProcessParameters;
import io.mpos.transactions.Currency;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.parameters.TransactionParameters;
import io.mpos.ui.BuildConfig;
import io.mpos.ui.acquirer.ApplicationName;
import io.mpos.ui.acquirer.MposUiAccountManager;
import io.mpos.ui.acquirer.view.LoginActivity;
import io.mpos.ui.acquirer.view.SettingsActivity;
import io.mpos.ui.paybutton.controller.StatefulTransactionProviderProxy;
import io.mpos.ui.paybutton.view.TransactionActivity;
import io.mpos.ui.printbutton.view.PrintReceiptActivity;
import io.mpos.ui.shared.model.MposUiConfiguration;
import io.mpos.ui.shared.util.ErrorHolder;
import io.mpos.ui.summarybutton.view.TransactionSummaryActivity;

/**
 * Entry point for the Payworks SDK and paybutton UI.
 * <p>
 * Used to create intents to start activities for:
 * <ul>
 * <li>creating a charge/refund transaction,</li>
 * <li>showing a summary of a transaction,</li>
 * <li>printing a receipt of a transaction.</li>
 * </ul>
 * <p>
 * Can be also used for getting the information about the last processed transaction.
 * <p>
 * Implemented as a singleton, make sure to initialize it using {@link #initialize(android.content.Context, io.mpos.provider.ProviderMode, String, String)}.
 */
public final class MposUi {

    public static final int REQUEST_CODE_PAYMENT = 1001;
    public static final int REQUEST_CODE_PRINT_RECEIPT = 1003;
    public static final int REQUEST_CODE_SHOW_SUMMARY = 1005;
    public static final int REQUEST_CODE_LOGIN = 1007;
    public static final int REQUEST_CODE_SETTINGS = 1009;


    public static final int RESULT_CODE_APPROVED = 2001;
    public static final int RESULT_CODE_FAILED = 2004;

    public static final int RESULT_CODE_PRINT_SUCCESS = 3001;
    public static final int RESULT_CODE_PRINT_FAILED = 3004;

    public static final int RESULT_CODE_SUMMARY_CLOSED = 4001;

    public static final int RESULT_CODE_LOGIN_SUCCESS = 5001;
    public static final int RESULT_CODE_LOGIN_FAILED = 5002;

    public static final int RESULT_CODE_SETTINGS_CLOSED = 6001;


    public static final String RESULT_EXTRA_TRANSACTION_IDENTIFIER = "io.mpos.ui.shared.MposUiController.TRANSACTION_IDENTIFIER";

    private static MposUi INSTANCE;

    private Context mContext;
    private ProviderMode mProviderMode;
    private String mMerchantIdentifier;
    private String mMerchantSecret;
    private TransactionProvider mTransactionProvider;

    private MposUiMode mMposUiMode = MposUiMode.PROVIDER;
    private MposUiAccountManager mMposUiAccountManager;

    private enum MposUiMode {
        PROVIDER,
        ACQUIRER
    }

    private MposUiConfiguration mConfiguration = new MposUiConfiguration();

    /**
     * Initialization method for this singleton class.
     *
     * @param context            Android context of your application.
     * @param mode               Enum value specifying which backend environment to use.
     * @param merchantIdentifier Identifier of the merchant which should be used for transactions.
     * @param merchantSecret     Secret (authentication token) of the merchant which should be used for transactions.
     * @return Initialized singleton object.
     */
    public static MposUi initialize(Context context, ProviderMode mode, String merchantIdentifier, String merchantSecret) {
        INSTANCE = new MposUi(context.getApplicationContext(), mode, merchantIdentifier, merchantSecret);
        return INSTANCE;
    }

    /**
     * Initialization method for this singleton class.
     * Deprecated alias for {@link #initialize(android.content.Context, io.mpos.provider.ProviderMode, String, String)}, use that method instead.
     *
     * @param context            Android context of your application.
     * @param mode               Enum value specifying which backend environment to use.
     * @param merchantIdentifier Identifier of the merchant which should be used for transactions.
     * @param merchantSecret     Secret (authentication token) of the merchant which should be used for transactions.
     * @return Initialized singleton object.
     */
    @Deprecated
    public static MposUi initializeController(Context context, ProviderMode mode, String merchantIdentifier, String merchantSecret) {
        return initialize(context, mode, merchantIdentifier, merchantSecret);
    }

    /**
     * Initialization using an application for this singleton class.
     * ProviderMode is set to LIVE by default
     *
     * @param context              Android context of your application.
     * @param applicationName      Enum value specifying which Application to use.
     * @param integratorIdentifier Identifier of the integrator.
     * @return Initialized singleton object.
     * @deprecated 2.6.0
     */
    @Deprecated
    public static MposUi initialize(Context context, ApplicationName applicationName, String integratorIdentifier) {
        return initialize(context.getApplicationContext(), ProviderMode.LIVE, applicationName, integratorIdentifier);
    }

    /**
     * Initialization method for this singleton class.
     *
     * @param context              Android context of your application.
     * @param providerMode         Enum value specifying which backend environment to use.
     * @param applicationName      Enum value specifying which Application to use.
     * @param integratorIdentifier Identifier of the integrator.
     * @return Initialized singleton object.
     */
    public static MposUi initialize(Context context, ProviderMode providerMode, ApplicationName applicationName, String integratorIdentifier) {
        INSTANCE = new MposUi(context.getApplicationContext(), providerMode, applicationName, integratorIdentifier);
        return INSTANCE;
    }

    /**
     * Gets the singleton instance of this class.
     *
     * @return Initialized MposUi object.
     */
    public static MposUi getInitializedInstance() {
        return INSTANCE;
    }

    /**
     * Gets the SDK and MposUi version you are using.
     *
     * @return Version code.
     */
    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Gets the configuration holder for the MposUi.
     *
     * @return Configuration holder.
     */
    public MposUiConfiguration getConfiguration() {
        return mConfiguration;
    }

    /**
     * Sets the configuration for the MposUi.
     *
     * @param configuration Configuration holder.
     */
    public void setConfiguration(MposUiConfiguration configuration) {
        mConfiguration = configuration;
    }

    /**
     * Returns if a transaction is ongoing.
     *
     * @return Whether is transaction ongoing.
     */
    public boolean isTransactionOngoing() {
        return StatefulTransactionProviderProxy.getInstance().isTransactionOnGoing();
    }

    /**
     * Returns the current transaction which is ongoing or finished in the MposUi.
     *
     * @return The transaction object.
     */
    public Transaction getTransaction() {
        return StatefulTransactionProviderProxy.getInstance().getCurrentTransaction();
    }

    /**
     * Returns the current transaction process which is ongoing or finished in the MposUi.
     *
     * @return The transaction process object.
     */
    public TransactionProcessDetails getTransactionProcessDetails() {
        return StatefulTransactionProviderProxy.getInstance().getLastTransactionProcessDetails();
    }

    /**
     * Returns the latest error which might have occurred in the MposUi.
     *
     * @return The last error object.
     */
    public MposError getError() {
        return ErrorHolder.getInstance().getError();
    }

    /**
     * Creates an intent for a new transaction from a session identifier
     * (this identifier is created after registering the transaction on the backend).
     * <p>
     * You should use the returned intent with {@code startActivityForResult()} using request code {@link #REQUEST_CODE_PAYMENT}.
     * The result code will be either {@link #RESULT_CODE_APPROVED} if the transaction was successfully processed and approved
     * or {@link #RESULT_CODE_FAILED} otherwise. The identifier of the transaction can be retrieved from the resulting intent
     * using {@link #RESULT_EXTRA_TRANSACTION_IDENTIFIER} key.
     *
     * @param sessionIdentifier The session identifier which should be used for the transaction.
     * @return The intent which can be used to start a new activity.
     */
    public Intent createTransactionIntent(String sessionIdentifier) {
        Intent intent = new Intent(mContext, TransactionActivity.class);

        if (mMposUiMode == MposUiMode.ACQUIRER) {
            intent.putExtra(TransactionActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN, true);
            intent.putExtra(TransactionActivity.BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID, mMposUiAccountManager.getApplicationData().getIdentifier());
        }

        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_SESSION_IDENTIFIER, sessionIdentifier);
        return intent;
    }

    /**
     * Creates an intent for a new transaction from a session identifier
     * (this identifier is created after registering the transaction on the backend).
     * Using {@param transactionProcessParameters} you can set additional parameters for the process, e.g. additional steps for the process like asking for tip.
     * <p/>
     * You should use the returned intent with {@code startActivityForResult()} using request code {@link #REQUEST_CODE_PAYMENT}.
     * The result code will be either {@link #RESULT_CODE_APPROVED} if the transaction was successfully processed and approved
     * or {@link #RESULT_CODE_FAILED} otherwise. The identifier of the transaction can be retrieved from the resulting intent
     * using {@link #RESULT_EXTRA_TRANSACTION_IDENTIFIER} key.
     *
     * @param sessionIdentifier The session identifier which should be used for the transaction.
     * @return The intent which can be used to start a new activity.
     */
    public Intent createTransactionIntent(String sessionIdentifier, TransactionProcessParameters transactionProcessParameters) {
        Intent intent = new Intent(mContext, TransactionActivity.class);

        if (mMposUiMode == MposUiMode.ACQUIRER) {
            intent.putExtra(TransactionActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN, true);
            intent.putExtra(TransactionActivity.BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID, mMposUiAccountManager.getApplicationData().getIdentifier());
        }

        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_SESSION_IDENTIFIER, sessionIdentifier);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_TRANSACTION_PROCESS_PARAMETERS, transactionProcessParameters);
        return intent;
    }

    /**
     * Creates an intent for a new transaction from the supplied transaction parameters
     * <p>
     * You should use the returned intent with {@code startActivityForResult()} using request code {@link #REQUEST_CODE_PAYMENT}.
     * The result code will be either {@link #RESULT_CODE_APPROVED} if the transaction was successfully processed and approved
     * or {@link #RESULT_CODE_FAILED} otherwise. The identifier of the transaction can be retrieved from the resulting intent
     * using {@link #RESULT_EXTRA_TRANSACTION_IDENTIFIER} key.
     *
     * @param transactionParameters Transaction parameters for the transaction.
     * @return The intent which can be used to start a new activity.
     */
    public Intent createTransactionIntent(TransactionParameters transactionParameters) {
        Intent intent = new Intent(mContext, TransactionActivity.class);

        if (mMposUiMode == MposUiMode.ACQUIRER) {
            intent.putExtra(TransactionActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN, true);
            intent.putExtra(TransactionActivity.BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID, mMposUiAccountManager.getApplicationData().getIdentifier());
        }

        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_TRANSACTION_PARAMETERS, transactionParameters);
        return intent;
    }

    /**
     * Creates an intent for a new transaction from the supplied transaction parameters.
     * Using {@param transactionProcessParameters} you can set additional parameters for the process, e.g. additional steps for the process like asking for tip.
     * <p/>
     * You should use the returned intent with {@code startActivityForResult()} using request code {@link #REQUEST_CODE_PAYMENT}.
     * The result code will be either {@link #RESULT_CODE_APPROVED} if the transaction was successfully processed and approved
     * or {@link #RESULT_CODE_FAILED} otherwise. The identifier of the transaction can be retrieved from the resulting intent
     * using {@link #RESULT_EXTRA_TRANSACTION_IDENTIFIER} key.
     *
     * @param transactionParameters Transaction parameters for the transaction.
     * @param transactionProcessParameters Transaction process parameters for the transaction process.
     * @return The intent which can be used to start a new activity.
     * @since 2.7.0
     */
    public Intent createTransactionIntent(TransactionParameters transactionParameters, TransactionProcessParameters transactionProcessParameters) {
        Intent intent = new Intent(mContext, TransactionActivity.class);

        if (mMposUiMode == MposUiMode.ACQUIRER) {
            intent.putExtra(TransactionActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN, true);
            intent.putExtra(TransactionActivity.BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID, mMposUiAccountManager.getApplicationData().getIdentifier());
        }

        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_TRANSACTION_PARAMETERS, transactionParameters);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_TRANSACTION_PROCESS_PARAMETERS, transactionProcessParameters);
        return intent;
    }

    /**
     * Creates an intent for a new charge transaction from the transaction data.
     * <p>
     * You should use the returned intent with {@code startActivityForResult()} using request code {@link #REQUEST_CODE_PAYMENT}.
     * The result code will be either {@link #RESULT_CODE_APPROVED} if the transaction was successfully processed and approved
     * or {@link #RESULT_CODE_FAILED} otherwise. The identifier of the transaction can be retrieved from the resulting intent
     * using {@link #RESULT_EXTRA_TRANSACTION_IDENTIFIER} key.
     *
     * @param amount           The transaction amount.
     * @param currency         The transaction currency.
     * @param subject          The subject of the transaction.
     * @param customIdentifier The subject of the transaction.
     * @return The intent which can be used to start a new activity for result.
     * @deprecated 2.5.0
     */
    @Deprecated
    public Intent createChargeTransactionIntent(BigDecimal amount, Currency currency, @Nullable String subject, @Nullable String customIdentifier) {

        TransactionParameters transactionParameters = new TransactionParameters.Builder().charge(amount, currency).
                subject(subject).
                customIdentifier(customIdentifier).
                build();

        return createTransactionIntent(transactionParameters);
    }

    /**
     * Creates an intent for a new refund transaction from the identifier of the transaction
     * which is to be refunded.
     * <p>
     * You should use the returned intent with {@code startActivityForResult()} using request code {@link #REQUEST_CODE_PAYMENT}.
     * The result code will be either {@link #RESULT_CODE_APPROVED} if the transaction was successfully processed and approved
     * or {@link #RESULT_CODE_FAILED} otherwise. The identifier of the transaction can be retrieved from the resulting intent
     * using {@link #RESULT_EXTRA_TRANSACTION_IDENTIFIER} key.
     *
     * @param transactionIdentifier The identifier of the old transaction which is to be refunded.
     * @param subject               The subject of the new transaction.
     * @param customIdentifier      The subject of the new transaction.
     * @return The intent which can be used to start a new activity.
     * @deprecated 2.5.0
     */
    @Deprecated
    public Intent createRefundTransactionIntent(String transactionIdentifier, @Nullable String subject, @Nullable String customIdentifier) {

        TransactionParameters transactionParameters = new TransactionParameters.Builder().refund(transactionIdentifier).
                subject(subject).
                customIdentifier(customIdentifier).
                build();

        return createTransactionIntent(transactionParameters);
    }

    /**
     * Creates an intent for showing the summary screen of a transaction.
     * <p>
     * You should use the returned intent with {@code startActivity()} or {@code startActivityForResult()} using request code {@link #REQUEST_CODE_SHOW_SUMMARY}
     * if you want to be notified when the transaction summary screen is closed. The result code will always be {@link #RESULT_CODE_SUMMARY_CLOSED}.
     *
     * @param transactionIdentifier The identifier of the transaction to show the summary.
     * @return The intent which can be used to start a new activity.
     */
    public Intent createTransactionSummaryIntent(String transactionIdentifier) {
        Intent intent = new Intent(mContext, TransactionSummaryActivity.class);

        if (mMposUiMode == MposUiMode.ACQUIRER) {
            intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN, true);
            intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID, mMposUiAccountManager.getApplicationData().getIdentifier());
        }

        intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_TRANSACTION_IDENTIFIER, transactionIdentifier);
        return intent;
    }

    /**
     * Creates an intent for printing a receipt of a transaction.
     * <p>
     * Use only when MposUi is initialized with an Application.
     * You should use the returned intent with {@code startActivityForResult()} using request code {@link #REQUEST_CODE_PRINT_RECEIPT}.
     * The result code will be either {@link #RESULT_CODE_PRINT_SUCCESS} if the receipt data was successfully sent to the printer
     * or {@link #RESULT_CODE_PRINT_FAILED} otherwise.
     *
     * @param transactionIdentifier The transaction identifier for the receipt to be printed.
     * @return The intent which can be used to start a new activity.
     */
    public Intent createPrintReceiptIntent(String transactionIdentifier) {
        Intent intent = new Intent(mContext, PrintReceiptActivity.class);

        if (mMposUiMode == MposUiMode.ACQUIRER) {
            intent.putExtra(PrintReceiptActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN, true);
            intent.putExtra(PrintReceiptActivity.BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID, mMposUiAccountManager.getApplicationData().getIdentifier());
        }

        intent.putExtra(PrintReceiptActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(PrintReceiptActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(PrintReceiptActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(PrintReceiptActivity.BUNDLE_EXTRA_TRANSACTION_IDENTIFIER, transactionIdentifier);
        return intent;
    }

    /**
     * Creates an intent for the login screen
     * <p>
     * Use only when MposUi is initialized with an Application.
     * The user is logged out forcefully before showing the login screen
     * <p>
     * You should use the returned intent with {@code startActivityForResult()} using request code {@link #REQUEST_CODE_LOGIN}.
     * The result code will be either {@link #RESULT_CODE_LOGIN_SUCCESS} if the login was successful or {@link #RESULT_CODE_LOGIN_FAILED} otherwise.
     *
     * @return The intent which can be used to start a new activity.
     * @throws IllegalStateException if the MposUi in not initialized with an Application
     */
    public Intent createLoginIntent() throws IllegalStateException {
        if (mMposUiMode != MposUiMode.ACQUIRER) {
            throwExceptionForWrongMode(MposUiMode.ACQUIRER);
        }
        // Forcing login
        mMposUiAccountManager.logout(false);
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(LoginActivity.BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID, mMposUiAccountManager.getApplicationData().getIdentifier());
        return intent;
    }

    /**
     * Creates an intent for the settings screen
     * <p>
     * Use only when MposUi is initialized with an Application.
     * You should use the returned intent with {@code startActivity()} or {@code startActivityForResult()} using request code {@link #REQUEST_CODE_SETTINGS}
     * if you want to be notified when the settings screen is closed. The result code will always be {@link #RESULT_CODE_SETTINGS_CLOSED}.
     *
     * @return The intent which can be used to start a new activity.
     * @throws IllegalStateException if the MposUi in not initialized with an Application
     */
    public Intent createSettingsIntent() throws IllegalStateException {
        if (mMposUiMode != MposUiMode.ACQUIRER) {
            throwExceptionForWrongMode(MposUiMode.ACQUIRER);
        }
        Intent intent = new Intent(mContext, SettingsActivity.class);
        intent.putExtra(SettingsActivity.BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID, mMposUiAccountManager.getApplicationData().getIdentifier());
        return intent;
    }

    /**
     * Logs the user out of the application.
     * Use only when MposUi is initialized with an Application.
     *
     * @throws IllegalStateException if the MposUi in not initialized with an application
     */
    public void logout() throws IllegalStateException {
        if (mMposUiMode != MposUiMode.ACQUIRER) {
            throwExceptionForWrongMode(MposUiMode.ACQUIRER);
        }
        mMposUiAccountManager.logout(false);
    }

    /**
     * Checks if the user is logged in with the Application.
     * Use only when MposUi is initialized with an Application.
     *
     * @return boolean indicating whether logged in or not.
     * @throws IllegalStateException if the MposUi in not initialized with an Application
     */
    public boolean isLoggedIn() throws IllegalStateException {
        if (mMposUiMode != MposUiMode.ACQUIRER) {
            throwExceptionForWrongMode(MposUiMode.ACQUIRER);
        }
        return mMposUiAccountManager.isLoggedIn();
    }

    /**
     * Returns the transaction provider used for the previous transaction.
     * Returns null if the transaction hasn't been processed before.
     * This can be used to query receipts, see {@link io.mpos.transactionprovider.TransactionProvider#fetchCustomerReceiptForTransaction(String, FetchReceiptListener)}
     *
     * @return TransactionProvider
     */
    public TransactionProvider getTransactionProvider() {
        TransactionProvider provider = StatefulTransactionProviderProxy.getInstance().getTransactionProvider();
        return provider;
    }

    private MposUi(Context context, ProviderMode providerMode, String merchantIdentifier, String merchantSecret) {
        mContext = context;
        mProviderMode = providerMode;
        mMerchantIdentifier = merchantIdentifier;
        mMerchantSecret = merchantSecret;
        mMposUiMode = MposUiMode.PROVIDER;
    }

    private MposUi(Context context, ProviderMode providerMode, ApplicationName applicationName, String integratorIdentifier) {

        if (TextUtils.isEmpty(integratorIdentifier)) {
            throw new IllegalArgumentException("Integrator Identifier cannot be null / empty.");
        }

        mMposUiMode = MposUiMode.ACQUIRER;
        mContext = context;
        mMposUiAccountManager = MposUiAccountManager.initialize(context, providerMode, applicationName, integratorIdentifier);
        mConfiguration = mMposUiAccountManager.getApplicationData().getMposUiConfiguration();
        if (mMposUiAccountManager.isLoggedIn()) {
            mMerchantIdentifier = mMposUiAccountManager.getMerchantIdentifier();
            mMerchantSecret = mMposUiAccountManager.getMerchantSecretKey();
            mProviderMode = mMposUiAccountManager.getProviderMode();
        }
    }

    private void throwExceptionForWrongMode(MposUiMode mode) throws IllegalStateException {
        if (mode == MposUiMode.ACQUIRER) {
            throw new IllegalStateException("MposUi needs to be initialized with an Application to use this method");
        } else {
            throw new IllegalStateException("MposUi needs to be initialized with a Provider to use this method");
        }
    }
}
