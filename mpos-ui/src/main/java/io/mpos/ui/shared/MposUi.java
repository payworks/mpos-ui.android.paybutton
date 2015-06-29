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
package io.mpos.ui.shared;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.math.BigDecimal;

import io.mpos.provider.ProviderMode;
import io.mpos.transactionprovider.TransactionProcessDetails;
import io.mpos.transactions.Currency;
import io.mpos.transactions.Transaction;
import io.mpos.ui.BuildConfig;
import io.mpos.ui.paybutton.controller.StatefulTransactionProviderProxy;
import io.mpos.ui.paybutton.view.TransactionActivity;
import io.mpos.ui.printbutton.view.PrintReceiptActivity;
import io.mpos.ui.shared.model.MposUiConfiguration;
import io.mpos.ui.summarybutton.view.TransactionSummaryActivity;

/**
 * Entry point for the Payworks SDK and paybutton UI.
 *
 * Used to create intents to start activities for:
 * <ul>
 *     <li>creating a charge/refund transaction,</li>
 *     <li>showing a summary of a transaction,</li>
 *     <li>printing a receipt of a transaction.</li>
 * </ul>
 *
 * Can be also used for getting the information about the last processed transaction.
 *
 * Implemented as a singleton, make sure to initialize it using {@link #initialize(android.content.Context, io.mpos.provider.ProviderMode, String, String)}.
 */
public final class MposUi {

    public static final int REQUEST_CODE_PAYMENT = 1001;
    public static final int REQUEST_CODE_PRINT_RECEIPT = 1004;

    public static final int RESULT_CODE_APPROVED = 2001;
    public static final int RESULT_CODE_FAILED = 2004;

    public static final int RESULT_CODE_PRINT_SUCCESS = 3001;
    public static final int RESULT_CODE_PRINT_FAILED = 3004;

    public static final String RESULT_EXTRA_TRANSACTION_IDENTIFIER = "io.mpos.ui.shared.MposUiController.TRANSACTION_IDENTIFIER";

    private static MposUi INSTANCE;

    private Context mContext;
    private ProviderMode mProviderMode;
    private String mMerchantIdentifier;
    private String mMerchantSecret;

    private MposUiConfiguration mConfiguration = new MposUiConfiguration();

    /**
     * Initialization method for this singleton class.
     * @param context Android context of your application.
     * @param mode Enum value specifying which backend environment to use.
     * @param merchantIdentifier Identifier of the merchant which should be used for transactions.
     * @param merchantSecret Secret (authentication token) of the merchant which should be used for transactions.
     * @return Initialized singleton object.
     */
    public static MposUi initialize(Context context, ProviderMode mode, String merchantIdentifier, String merchantSecret) {
        INSTANCE = new MposUi(context.getApplicationContext(), mode, merchantIdentifier, merchantSecret);
        return INSTANCE;
    }

    /**
     * Initialization method for this singleton class.
     * Deprecated alias for {@link #initialize(android.content.Context, io.mpos.provider.ProviderMode, String, String)}, use that method instead.
     * @param context Android context of your application.
     * @param mode Enum value specifying which backend environment to use.
     * @param merchantIdentifier Identifier of the merchant which should be used for transactions.
     * @param merchantSecret Secret (authentication token) of the merchant which should be used for transactions.
     * @return Initialized singleton object.
     */
    @Deprecated
    public static MposUi initializeController(Context context, ProviderMode mode, String merchantIdentifier, String merchantSecret) {
        return initialize(context, mode, merchantIdentifier, merchantSecret);
    }

    /**
     * Gets the singleton instance of this class.
     * @return Initialized MposUi object.
     */
    public static MposUi getInitializedInstance() {
        return INSTANCE;
    }

    /**
     * Gets the SDK and MposUi version you are using.
     * @return Version code.
     */
    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Gets the configuration holder for the MposUi.
     * @return Configuration holder.
     */
    public MposUiConfiguration getConfiguration() {
        return mConfiguration;
    }

    /**
     * Sets the configuration for the MposUi.
     * @param configuration Configuration holder.
     */
    public void setConfiguration(MposUiConfiguration configuration) {
        mConfiguration = configuration;
    }

    /**
     * Returns if a transaction is ongoing.
     * @return Whether is transaction ongoing.
     */
    public boolean isTransactionOngoing() {
        return StatefulTransactionProviderProxy.getInstance().isTransactionOnGoing();
    }

    /**
     * Returns the current transaction which is ongoing or finished in the MposUi.
     * @return The transaction object.
     */
    public Transaction getTransaction() {
        return StatefulTransactionProviderProxy.getInstance().getCurrentTransaction();
    }

    /**
     * Returns the current transaction process which is ongoing or finished in the MposUi.
     * @return The transaction process object.
     */
    public TransactionProcessDetails getTransactionProcessDetails() {
        return StatefulTransactionProviderProxy.getInstance().getLastTransactionProcessDetails();
    }

    /**
     * Creates an intent for a new transaction from a session identifier
     * (this identifier is created after registering the transaction on the backend).
     *
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
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_ACCESSORY_FAMILY, mConfiguration.getAccessoryFamily());
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_SESSION_IDENTIFIER, sessionIdentifier);
        return intent;
    }

    /**
     * Creates an intent for a new charge transaction from the transaction data.
     *
     * You should use the returned intent with {@code startActivityForResult()} using request code {@link #REQUEST_CODE_PAYMENT}.
     * The result code will be either {@link #RESULT_CODE_APPROVED} if the transaction was successfully processed and approved
     * or {@link #RESULT_CODE_FAILED} otherwise. The identifier of the transaction can be retrieved from the resulting intent
     * using {@link #RESULT_EXTRA_TRANSACTION_IDENTIFIER} key.
     *
     * @param amount The transaction amount.
     * @param currency The transaction currency.
     * @param subject The subject of the transaction.
     * @param customIdentifier The subject of the transaction.
     * @return The intent which can be used to start a new activity for result.
     */
    public Intent createChargeTransactionIntent(BigDecimal amount, Currency currency, @Nullable String subject, @Nullable String customIdentifier) {
        Intent intent = new Intent(mContext, TransactionActivity.class);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_ACCESSORY_FAMILY, mConfiguration.getAccessoryFamily());
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_AMOUNT, amount);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_CURRENCY, currency);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_SUBJECT, subject);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_CUSTOM_IDENTIFIER, customIdentifier);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_IS_REFUND, false);
        return intent;
    }

    /**
     * Creates an intent for a new refund transaction from the identifier of the transaction
     * which is to be refunded.
     *
     * You should use the returned intent with {@code startActivityForResult()} using request code {@link #REQUEST_CODE_PAYMENT}.
     * The result code will be either {@link #RESULT_CODE_APPROVED} if the transaction was successfully processed and approved
     * or {@link #RESULT_CODE_FAILED} otherwise. The identifier of the transaction can be retrieved from the resulting intent
     * using {@link #RESULT_EXTRA_TRANSACTION_IDENTIFIER} key.
     *
     * @param transactionIdentifier The identifier of the old transaction which is to be refunded.
     * @param subject The subject of the new transaction.
     * @param customIdentifier The subject of the new transaction.
     * @return The intent which can be used to start a new activity.
     */
    public Intent createRefundTransactionIntent(String transactionIdentifier, @Nullable String subject, @Nullable String customIdentifier) {
        Intent intent = new Intent(mContext, TransactionActivity.class);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_ACCESSORY_FAMILY, mConfiguration.getAccessoryFamily());
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_TRANSACTION_IDENTIFIER, transactionIdentifier);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_SUBJECT, subject);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_CUSTOM_IDENTIFIER, customIdentifier);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_IS_REFUND, true);
        return intent;
    }

    /**
     * Creates an intent for showing the summary screen of a transaction.
     *
     * You should use the returned intent with {@code startActivity()}.
     *
     * @param transactionIdentifier The identifier of the transaction to show the summary.
     * @return The intent which can be used to start a new activity.
     */
    public Intent createTransactionSummaryIntent(String transactionIdentifier) {
        Intent intent = new Intent(mContext, TransactionSummaryActivity.class);
        intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_TRANSACTION_IDENTIFIER, transactionIdentifier);
        return intent;
    }

    /**
     * Creates an intent for printing a receipt of a transaction.
     *
     * You should use the returned intent with {@code startActivityForResult()} using request code {@link #REQUEST_CODE_PRINT_RECEIPT}.
     * The result code will be either {@link #RESULT_CODE_PRINT_SUCCESS} if the receipt data was successfully sent to the printer
     * or {@link #RESULT_CODE_PRINT_FAILED} otherwise.
     *
     * @param transactionIdentifier The transaction identifier for the receipt to be printed.
     * @return The intent which can be used to start a new activity.
     */
    public Intent createPrintReceiptIntent(String transactionIdentifier) {
        Intent intent = new Intent(mContext, PrintReceiptActivity.class);
        intent.putExtra(PrintReceiptActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(PrintReceiptActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(PrintReceiptActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(PrintReceiptActivity.BUNDLE_EXTRA_TRANSACTION_IDENTIFIER, transactionIdentifier);
        return intent;
    }

    private MposUi(Context context, ProviderMode providerMode, String merchantIdentifier, String merchantSecret) {
        mContext = context;
        mProviderMode = providerMode;
        mMerchantIdentifier = merchantIdentifier;
        mMerchantSecret = merchantSecret;
    }
}
