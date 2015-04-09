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
package io.mpos.ui.paybutton.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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
import io.mpos.transactionprovider.PaymentProcessDetails;
import io.mpos.transactions.Currency;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.TransactionStatus;
import io.mpos.transactions.TransactionType;
import io.mpos.transactions.receipts.Receipt;
import io.mpos.ui.paybutton.R;
import io.mpos.ui.paybutton.controller.PaymentController;
import io.mpos.ui.paybutton.controller.StatefulTransactionProviderProxy;
import io.mpos.ui.paybutton.model.PaymentControllerConfiguration;
import io.mpos.ui.paybutton.util.UIHelper;

public class PaymentActivity extends AbstractPaymentActivity implements StatefulTransactionProviderProxy.Callback, io.mpos.ui.paybutton.view.PaymentInteractionListener {

    private final static String TAG = "PaymentActivity";

    public final static String BUNDLE_EXTRA_MERCHANT_ID = "io.mpos.ui.paybutton.PaymentActivity.MERCHANT_ID";
    public final static String BUNDLE_EXTRA_MERCHANT_SECRET = "io.mpos.ui.paybuttonPaymentActivity.MERCHANT_SECRET";
    public final static String BUNDLE_EXTRA_PROVIDER_MODE = "io.mpos.ui.PaymentActivity.PROVIDER_MODE";
    public final static String BUNDLE_EXTRA_ACCESSORY_FAMILY = "io.mpos.ui.paybutton.PaymentActivity.ACCESSORY_FAMILY";
    public final static String BUNDLE_EXTRA_AMOUNT = "io.mpos.ui.paybutton.PaymentActivity.AMOUNT";
    public final static String BUNDLE_EXTRA_CURRENCY = "io.mpos.ui.paybutton.PaymentActivity.CURRENCY";
    public final static String BUNDLE_EXTRA_SUBJECT = "io.mpos.ui.paybutton.PaymentActivity.SUBJECT";
    public final static String BUNDLE_EXTRA_TRANSACTION_TYPE = "io.mpos.ui.paybutton.PaymentActivity.TRANSACTION_TYPE";
    public final static String BUNDLE_EXTRA_CUSTOM_IDENTIFIER = "io.mpos.ui.paybutton.PaymentActivity.CUSTOM_IDENTIFIER";
    public final static String BUNDLE_EXTRA_SESSION_IDENTIFIER = "io.mpos.ui.paybutton.PaymentActivity.SESSION_IDENTIFIER";

    private static final int REQUEST_CODE_SIGNATURE = 1;
    private static final String BUNDLE_FORMATTED_AMOUNT = "io.mpos.ui.paybutton.PaymentActivity.FORMATTED_AMOUNT";
    private static final String BUNDLE_TITLE_TRANSACTION_TYPE = "io.mpos.ui.paybutton.PaymentActivity.TITLE_TRANSACTION_TYPE";

    private String mFormattedAmount;
    private String mTitleTransactionType;


    private StatefulTransactionProviderProxy mStatefulPaymentProcess = StatefulTransactionProviderProxy.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        UIHelper.applyCustomColors(this, (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

        if(getIntent().hasExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER)) {
            if(savedInstanceState == null) {
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

            mFormattedAmount = UIHelper.formatAmountWithSymbol(currency, amount);

            setTitle(constructTitle());
        }


        if(!mStatefulPaymentProcess.isPaymentOnGoing() && savedInstanceState == null) {
            startPayment();
        }
    }

    private void constructTransactionTypeTitle() {
        TransactionType transactionType = (TransactionType) getIntent().getSerializableExtra(BUNDLE_EXTRA_TRANSACTION_TYPE);
        mTitleTransactionType = getString(R.string.tx_type_charge);
        if(transactionType != null && transactionType.equals(TransactionType.REFUND)) {
            mTitleTransactionType = getString(R.string.tx_type_refund);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStatefulPaymentProcess.attachCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStatefulPaymentProcess.attachCallback(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_FORMATTED_AMOUNT, mFormattedAmount);
        outState.putString(BUNDLE_TITLE_TRANSACTION_TYPE, mTitleTransactionType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_SIGNATURE) {
            if(resultCode == RESULT_CANCELED) {
                mStatefulPaymentProcess.continueWithSignature(null, false);
            } else {
                byte[] byteArraySignature = data.getByteArrayExtra(SignatureActivity.BUNDLE_KEY_SIGNATURE_IMAGE);
                Bitmap signature = BitmapFactory.decodeByteArray(byteArraySignature, 0, byteArraySignature.length);
                mStatefulPaymentProcess.continueWithSignature(signature, true);
            }
        }
    }

    @Override
    public void onApplicationSelectionRequired(List<ApplicationInformation> applicationInformations) {
        Log.d(TAG, "onApplicationSelectionRequired");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, ApplicationSelectionFragment.newInstance(applicationInformations), ApplicationSelectionFragment.TAG)
                .commit();
    }

    @Override
    public void onCustomerSignatureRequired() {
        Log.d(TAG, "onCustomerSignatureRequired");

        if(PaymentController.getInitializedInstance().getConfiguration().getSignatureMethod().equals(PaymentControllerConfiguration.SignatureMethod.ON_SCREEN)) {
            showSignatureActivity();
        } else {
            throw new IllegalStateException("Signature on paper isn't supported, right now!");
        }
    }


    @Override
    public void onStatusChanged(PaymentProcessDetails details, Transaction transaction) {
        Log.d(TAG, "onStatusChanged=" + details);
        if(transaction != null && getIntent().hasExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER)) {
            mFormattedAmount = UIHelper.formatAmountWithSymbol(transaction.getCurrency(), transaction.getAmount());
            constructTransactionTypeTitle();
            setTitle(constructTitle());
        }

        PaymentFragment paymentFragment = (PaymentFragment) getSupportFragmentManager().findFragmentByTag(PaymentFragment.TAG);
        if(paymentFragment == null) {
            paymentFragment = PaymentFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, paymentFragment, PaymentFragment.TAG).commit();
        }

        paymentFragment.updateStatus(details, transaction);

    }

    @Override
    public void onCompleted(Transaction transaction, MposError error) {
        if(transaction == null && error == null) {
            MposError e = new DefaultMposError(ErrorType.TRANSACTION_ABORTED);
            PaymentErrorFragment fragment = PaymentErrorFragment.newInstance(true, e);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, PaymentErrorFragment.TAG)
                    .commit();
        } else if(error == null) {
            Receipt merchantReceipt = mStatefulPaymentProcess.getMerchantReceipt();
            SummaryFragment summaryFragment = SummaryFragment.newInstance(!getIntent().hasExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER), constructTitle(), transaction, merchantReceipt, error);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, summaryFragment, SummaryFragment.TAG)
                    .commit();
        } else {
            PaymentErrorFragment fragment = PaymentErrorFragment.newInstance(!getIntent().hasExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER), error);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, PaymentErrorFragment.TAG)
                    .commit();
        }
    }

    @Override
    public void onSummaryClosed(Transaction transaction, MposError error) {
        boolean approved = transaction != null && transaction.getStatus().equals(TransactionStatus.APPROVED);
        finish(approved);
    }

    @Override
    public void onAbortPaymentButtonClicked() {
        boolean result = mStatefulPaymentProcess.abortPayment();
        if(!result) {
            Toast.makeText(this, R.string.request_abort_failure, Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public void onApplicationSelected(ApplicationInformation applicationInformation) {
        mStatefulPaymentProcess.continueWithApplicationSelection(applicationInformation);
    }

    @Override
    public void onCancelPaymentButtonClicked() {
        finish(false);
    }

    @Override
    public void onRetryPaymentButtonClicked() {
        startPayment();
    }

    private void startPayment() {
        String sessionIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_SESSION_IDENTIFIER);
        String merchantId = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_ID);
        String merchantSecret = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_SECRET);
        String subject = getIntent().getStringExtra(BUNDLE_EXTRA_SUBJECT);
        String customIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_CUSTOM_IDENTIFIER);
        ProviderMode providerMode = (ProviderMode) getIntent().getSerializableExtra(BUNDLE_EXTRA_PROVIDER_MODE);
        AccessoryFamily accessoryFamily = (AccessoryFamily) getIntent().getSerializableExtra(BUNDLE_EXTRA_ACCESSORY_FAMILY);
        BigDecimal amount = (BigDecimal) getIntent().getSerializableExtra(BUNDLE_EXTRA_AMOUNT);
        Currency currency = (Currency) getIntent().getSerializableExtra(BUNDLE_EXTRA_CURRENCY);

        if(mStatefulPaymentProcess.isPaymentOnGoing()) {
            setResult(PaymentController.RESULT_CODE_FAILED);
            finish();
        }

        if(sessionIdentifier == null) {
            mStatefulPaymentProcess.startPayment(getApplicationContext(), merchantId,
                    merchantSecret, providerMode, accessoryFamily, amount, currency, subject, customIdentifier, true);
        } else {
            mStatefulPaymentProcess.startPayment(getApplicationContext(), sessionIdentifier, providerMode, merchantId, merchantSecret, accessoryFamily);
        }
    }

    private void finish(boolean approved) {
        mStatefulPaymentProcess.teardown();
        int result = (approved) ? PaymentController.RESULT_CODE_APPROVED  : PaymentController.RESULT_CODE_FAILED;
        setResult(result);
        finish();
    }

    private void showSignatureActivity() {
        Intent intent = new Intent(this, SignatureActivity.class);
        intent.putExtra(SignatureActivity.BUNDLE_KEY_AMOUNT, mFormattedAmount);
        PaymentDetailsScheme scheme = mStatefulPaymentProcess.getCurrentTransaction().getPaymentDetails().getScheme();
        if(scheme != null) {
            int resId = UIHelper.getDrawableIdImageForCreditCard(scheme);
            intent.putExtra(SignatureActivity.BUNDLE_KEY_CARD_SCHEME_ID, resId);
        }
        startActivityForResult(intent, REQUEST_CODE_SIGNATURE);
    }

    private String constructTitle() {
        return mTitleTransactionType + ": " + mFormattedAmount;
    }
}
