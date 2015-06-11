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
package io.mpos.ui.summarybutton.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import io.mpos.Mpos;
import io.mpos.errors.MposError;
import io.mpos.provider.ProviderMode;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.TransactionType;
import io.mpos.transactions.receipts.ReceiptFactory;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.util.UiHelper;
import io.mpos.ui.shared.view.ErrorFragment;
import io.mpos.ui.shared.view.SendReceiptFragment;
import io.mpos.ui.shared.view.SummaryFragment;

public class TransactionSummaryActivity extends AppCompatActivity
        implements LoadTransactionSummaryFragment.Interaction, SummaryFragment.Interaction, ErrorFragment.Interaction, SendReceiptFragment.Interaction {

    private final static String TAG = "TransactionSummaryActivity";

    public final static String BUNDLE_EXTRA_TRANSACTION_IDENTIFIER = "io.mpos.ui.paybutton.TransactionSummaryActivity.TRANSACTION_IDENTIFIER";
    public final static String BUNDLE_EXTRA_MERCHANT_ID = "io.mpos.ui.paybutton.TransactionSummaryActivity.MERCHANT_ID";
    public final static String BUNDLE_EXTRA_MERCHANT_SECRET = "io.mpos.ui.paybutton.TransactionSummaryActivity.MERCHANT_SECRET";
    public final static String BUNDLE_EXTRA_PROVIDER_MODE = "io.mpos.ui.paybutton.TransactionSummaryActivity.PROVIDER_MODE";

    private TransactionProvider mTransactionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_summary);

        UiHelper.setActionbarWithCustomColors(this, (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar), true);
        setTitle(R.string.MPUSummary);

        createMposProvider();

        if (savedInstanceState == null) {
            showLoadingFragment();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBack();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void createMposProvider() {
        String merchantId = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_ID);
        String merchantSecret = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_SECRET);
        ProviderMode providerMode = (ProviderMode) getIntent().getSerializableExtra(BUNDLE_EXTRA_PROVIDER_MODE);

        mTransactionProvider = Mpos.createTransactionProvider(getApplicationContext(), providerMode, merchantId, merchantSecret);
    }

    @Override
    public TransactionProvider getTransactionProvider() {
        return mTransactionProvider;
    }

    @Override
    public void onTransactionLoaded(Transaction transaction) {
        showSummaryFragment(transaction);
    }

    @Override
    public void onLoadingError(MposError error) {
        showErrorFragment(error);
    }

    @Override
    public void onErrorRetryButtonClicked() {
        showLoadingFragment();
    }

    @Override
    public void onErrorCancelButtonClicked() {
        finish();
    }

    @Override
    public void onSendReceiptButtonClicked(Transaction transaction) {
        showSendReceiptFragment(transaction);
    }

    @Override
    public void onSummaryRetryButtonClicked() {
        // noop
    }

    @Override
    public void onSummaryRefundButtonClicked(Transaction transaction) {
        Intent intent = MposUi.getInitializedInstance().createRefundTransactionIntent(transaction.getIdentifier(), null, null);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSummaryClosed(Transaction transaction, MposError error) {
        finish();
    }

    @Override
    public void onReceiptSent(Transaction transaction) {
        showSummaryFragment(transaction);
    }

    private void showLoadingFragment() {
        String transactionIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_TRANSACTION_IDENTIFIER);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, LoadTransactionSummaryFragment.newInstance(transactionIdentifier), LoadTransactionSummaryFragment.TAG)
                .commit();
    }

    private void showErrorFragment(MposError error) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, ErrorFragment.newInstance(false, error), ErrorFragment.TAG)
                .commit();
    }

    private void showSummaryFragment(Transaction transaction) {
        ReceiptFactory receiptFactory = mTransactionProvider.getReceiptFactory();
        SummaryFragment fragment = SummaryFragment.newInstance(false, true, constructSummaryTitle(transaction), transaction, receiptFactory.createMerchantReceipt(transaction), null);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment, SummaryFragment.TAG)
                .commit();
    }

    private void showSendReceiptFragment(Transaction transaction) {
        SendReceiptFragment fragment = SendReceiptFragment.newInstance(transaction, constructSummaryTitle(transaction));
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment, SendReceiptFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    private void navigateBack() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    private String constructSummaryTitle(Transaction transaction) {
        String type = (transaction.getType() == TransactionType.CHARGE ? getString(R.string.MPUSale) : getString(R.string.MPURefund) );
        String amount = UiHelper.formatAmountWithSymbol(transaction.getCurrency(), transaction.getAmount());
        return type + ": " + amount;
    }
}
