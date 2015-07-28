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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import io.mpos.Mpos;
import io.mpos.errors.MposError;
import io.mpos.provider.ProviderMode;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.controller.StatefulPrintingProcessProxy;
import io.mpos.ui.shared.util.ErrorHolder;
import io.mpos.ui.shared.util.UiHelper;
import io.mpos.ui.shared.util.UiState;
import io.mpos.ui.shared.view.AbstractBaseActivity;
import io.mpos.ui.shared.view.ErrorFragment;
import io.mpos.ui.shared.view.PrintReceiptFragment;

public class PrintReceiptActivity extends AbstractBaseActivity
        implements PrintReceiptFragment.Interaction, ErrorFragment.Interaction {

    private final static String TAG = "PrintReceiptActivity";

    public final static String BUNDLE_EXTRA_TRANSACTION_IDENTIFIER = "io.mpos.ui.printbutton.view.PrintReceiptActivity.TRANSACTION_IDENTIFIER";
    public final static String BUNDLE_EXTRA_MERCHANT_ID = "io.mpos.ui.printbutton.view.PrintReceiptActivity.MERCHANT_ID";
    public final static String BUNDLE_EXTRA_MERCHANT_SECRET = "io.mpos.ui.printbutton.view.PrintReceiptActivity.MERCHANT_SECRET";
    public final static String BUNDLE_EXTRA_PROVIDER_MODE = "io.mpos.ui.printbutton.view.PrintReceiptActivity.PROVIDER_MODE";

    private TransactionProvider mTransactionProvider;
    private String mTransactionIdentifier;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mpu_activity_print_receipt);

        if (getCallingActivity() == null) {
            Log.w(TAG, "The printing activity was started without startActivityForResult() and will not return a result code.");
        }

        UiHelper.setActionbarWithCustomColors(this, (android.support.v7.widget.Toolbar) findViewById(R.id.mpu_toolbar));
        setTitle(R.string.MPUPrinting);

        ErrorHolder.getInstance().clear();
        createMposProvider();

        mTransactionIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_TRANSACTION_IDENTIFIER);

        if (savedInstanceState == null) {
            showPrintReceiptFragment();
        }
    }

    private void showPrintReceiptFragment() {
        PrintReceiptFragment fragment = PrintReceiptFragment.newInstance(mTransactionIdentifier);
        showFragment(fragment, PrintReceiptFragment.TAG, UiState.RECEIPT_PRINTING, false);
    }

    private void createMposProvider() {
        String merchantId = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_ID);
        String merchantSecret = getIntent().getStringExtra(BUNDLE_EXTRA_MERCHANT_SECRET);
        ProviderMode providerMode = (ProviderMode) getIntent().getSerializableExtra(BUNDLE_EXTRA_PROVIDER_MODE);

        mTransactionProvider = Mpos.createTransactionProvider(getApplicationContext(), providerMode, merchantId, merchantSecret);
    }

    @Override
    public void onErrorRetryButtonClicked() {
        showPrintReceiptFragment();
    }

    @Override
    public void onReceiptPrintCompleted(MposError error) {
        if (error != null) {
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
    public TransactionProvider getTransactionProvider() {
        return mTransactionProvider;
    }

    @Override
    public void onBackPressed() {
        navigateBack();
    }

    @Override
    public void navigateBack() {
        if (getUiState() == UiState.RECEIPT_PRINTING_ERROR) {
            finishWithResult(false);
        } else {
            Toast.makeText(this, R.string.MPUBackButtonDisabled, Toast.LENGTH_LONG).show();
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
}
