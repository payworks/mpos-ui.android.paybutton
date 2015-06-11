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

import java.math.BigDecimal;

import io.mpos.provider.ProviderMode;
import io.mpos.transactionprovider.TransactionProcessDetails;
import io.mpos.transactions.Currency;
import io.mpos.transactions.Transaction;
import io.mpos.ui.BuildConfig;
import io.mpos.ui.paybutton.controller.StatefulTransactionProviderProxy;
import io.mpos.ui.paybutton.view.TransactionActivity;
import io.mpos.ui.shared.model.MposUiConfiguration;
import io.mpos.ui.summarybutton.view.TransactionSummaryActivity;

public final class MposUi {

    public static final int REQUEST_CODE_PAYMENT = 1001;

    public static final int RESULT_CODE_APPROVED = 2001;
    public static final int RESULT_CODE_FAILED = 2004;

    public static final String RESULT_EXTRA_TRANSACTION_IDENTIFIER = "io.mpos.ui.shared.MposUiController.TRANSACTION_IDENTIFIER";

    private static MposUi INSTANCE;

    private Context mContext;
    private ProviderMode mProviderMode;
    private String mMerchantIdentifier;
    private String mMerchantSecret;

    private MposUiConfiguration mConfiguration = new MposUiConfiguration();

    public static MposUi initializeController(Context context, ProviderMode mode, String merchantIdentifier, String merchantSecret) {
        INSTANCE = new MposUi(context.getApplicationContext(), mode, merchantIdentifier, merchantSecret);
        return INSTANCE;
    }

    public static MposUi getInitializedInstance() {
        return INSTANCE;
    }

    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }


    public MposUiConfiguration getConfiguration() {
        return mConfiguration;
    }

    public void setConfiguration(MposUiConfiguration configuration) {
        mConfiguration = configuration;
    }

    public boolean isTransactionOngoing() {
        return StatefulTransactionProviderProxy.getInstance().isTransactionOnGoing();
    }

    public Transaction getTransaction() {
        return StatefulTransactionProviderProxy.getInstance().getCurrentTransaction();
    }

    public TransactionProcessDetails getTransactionProcessDetails() {
        return StatefulTransactionProviderProxy.getInstance().getLastTransactionProcessDetails();
    }

    public Intent createTransactionIntent(String sessionIdentifier) {
        Intent intent = new Intent(mContext, TransactionActivity.class);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_ACCESSORY_FAMILY, mConfiguration.getAccessoryFamily());
        intent.putExtra(TransactionActivity.BUNDLE_EXTRA_SESSION_IDENTIFIER, sessionIdentifier);
        return intent;
    }

    public Intent createChargeTransactionIntent(BigDecimal amount, Currency currency, String subject, String customIdentifier) {
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

    public Intent createRefundTransactionIntent(String transactionIdentifier, String subject, String customIdentifier) {
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

    public Intent createTransactionSummaryIntent(String transactionIdentifier) {
        Intent intent = new Intent(mContext, TransactionSummaryActivity.class);
        intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(TransactionSummaryActivity.BUNDLE_EXTRA_TRANSACTION_IDENTIFIER, transactionIdentifier);
        return intent;
    }


    private MposUi(Context context, ProviderMode providerMode, String merchantIdentifier, String merchantSecret) {
        mContext = context;
        mProviderMode = providerMode;
        mMerchantIdentifier = merchantIdentifier;
        mMerchantSecret = merchantSecret;
    }
}
