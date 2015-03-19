/**
 * mpos-ui.paybutton : http://www.payworksmobile.com
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
 *
 */
package io.mpos.ui.paybutton.controller;

import android.content.Context;
import android.content.Intent;

import java.math.BigDecimal;

import io.mpos.provider.ProviderMode;
import io.mpos.transactionprovider.PaymentProcessDetails;
import io.mpos.transactions.Currency;
import io.mpos.transactions.Transaction;
import io.mpos.ui.paybutton.BuildConfig;
import io.mpos.ui.paybutton.model.PaymentControllerConfiguration;
import io.mpos.ui.paybutton.view.PaymentActivity;

public final class PaymentController {

    public static final int REQUEST_CODE_PAYMENT = 1001;

    public static final int RESULT_CODE_APPROVED = 2001;
    public static final int RESULT_CODE_FAILED = 2004;

    private static PaymentController INSTANCE;

    private Context mContext;
    private ProviderMode mProviderMode;
    private String mMerchantIdentifier;
    private String mMerchantSecret;

    private PaymentControllerConfiguration mConfiguration = new PaymentControllerConfiguration();

    public static PaymentController initializePaymentController(Context context, ProviderMode mode, String merchantIdentifier, String merchantSecret) {
        INSTANCE = new PaymentController(context.getApplicationContext(), mode, merchantIdentifier, merchantSecret);
        return INSTANCE;
    }

    public static PaymentController getInitializedInstance() {
        return INSTANCE;
    }

    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }


    public PaymentControllerConfiguration getConfiguration() {
        return mConfiguration;
    }

    public void setConfiguration(PaymentControllerConfiguration configuration) {
        mConfiguration = configuration;
    }

    public boolean isTransactionOngoing() {
        return StatefulTransactionProviderProxy.getInstance().isPaymentOnGoing();
    }

    public Transaction getTransaction() {
        return StatefulTransactionProviderProxy.getInstance().getCurrentTransaction();
    }

    public PaymentProcessDetails getPaymentProcessDetails() {
        return StatefulTransactionProviderProxy.getInstance().getLastPaymentProcessDetails();
    }

    public Intent createPaymentIntent(String sessionIdentifier) {
        Intent intent = new Intent(mContext, PaymentActivity.class);
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_ACCESSORY_FAMILY, mConfiguration.getAccessoryFamily());
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_SESSION_IDENTIFIER, sessionIdentifier);
        return intent;
    }

    public Intent createPaymentIntent(BigDecimal amount, Currency currency, String subject, String customIdentifier) {
        Intent intent = new Intent(mContext, PaymentActivity.class);
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_MERCHANT_ID, mMerchantIdentifier);
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_MERCHANT_SECRET, mMerchantSecret);
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_PROVIDER_MODE, mProviderMode);
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_ACCESSORY_FAMILY, mConfiguration.getAccessoryFamily());
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_AMOUNT, amount);
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_CURRENCY, currency);
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_SUBJECT, subject);
        intent.putExtra(PaymentActivity.BUNDLE_EXTRA_CUSTOM_IDENTIFIER, customIdentifier);
        return intent;
    }


    private PaymentController(Context context, ProviderMode providerMode, String merchantIdentifier, String merchantSecret) {
        mContext = context;
        mProviderMode = providerMode;
        mMerchantIdentifier = merchantIdentifier;
        mMerchantSecret = merchantSecret;
    }
}
