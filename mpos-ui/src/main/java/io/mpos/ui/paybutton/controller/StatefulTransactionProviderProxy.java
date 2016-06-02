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

package io.mpos.ui.paybutton.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.Log;

import java.math.BigDecimal;
import java.util.List;

import io.mpos.Mpos;
import io.mpos.accessories.Accessory;
import io.mpos.accessories.AccessoryConnectionState;
import io.mpos.accessories.AccessoryFamily;
import io.mpos.accessories.parameters.AccessoryParameters;
import io.mpos.errors.MposError;
import io.mpos.paymentdetails.ApplicationInformation;
import io.mpos.provider.ProviderMode;
import io.mpos.transactionprovider.TransactionProcess;
import io.mpos.transactionprovider.TransactionProcessDetails;
import io.mpos.transactionprovider.TransactionProcessDetailsState;
import io.mpos.transactionprovider.TransactionProcessWithRegistrationListener;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.transactionprovider.processparameters.TransactionProcessParameters;
import io.mpos.transactions.Currency;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.TransactionTemplate;
import io.mpos.transactions.parameters.TransactionParameters;

/**
 * StatefulTransactionProviderProxy keeps the state of the ongoing transaction independent of the Fragment/Activity's lifecycle.
 * It also reissues the last event when a callback is attached. (during FragmentTransaction / orientation change)
 */
public class StatefulTransactionProviderProxy implements TransactionProcessWithRegistrationListener {

    private final static String TAG = "TxProviderProxy";

    public interface Callback {

        void onApplicationSelectionRequired(List<ApplicationInformation> applicationInformations);

        void onCustomerSignatureRequired();

        void onStatusChanged(TransactionProcessDetails details, Transaction transaction);

        void onCompleted(Transaction transaction, MposError error);
    }

    private final static StatefulTransactionProviderProxy INSTANCE = new StatefulTransactionProviderProxy();

    private Transaction mCurrentTransaction;
    private TransactionProcess mCurrentTransactionProcess;

    private Callback mCallback;

    private boolean mAwaitingSignature;
    private boolean mAwaitingApplicationSelection;
    private boolean mTransactionSessionLookup;

    private boolean mTransactionIsOnGoing;
    private TransactionProcessDetails mLastTransactionProcessDetails;
    private TransactionProvider mTransactionProvider;

    private boolean mCustomerVerificationRequired = false;

    private List<ApplicationInformation> mApplicationInformationList;


    private StatefulTransactionProviderProxy() {
        //singleton
    }

    public static StatefulTransactionProviderProxy getInstance() {
        return INSTANCE;
    }


    @Deprecated
    public void startChargeTransaction(Context context, ProviderMode providerMode, String merchantIdentifier, String merchantSecret, AccessoryFamily accessoryFamily, BigDecimal amount, Currency currency, String subject, String customIdentifier) {
        clearForNewTransaction();

        mTransactionProvider = Mpos.createTransactionProvider(context, providerMode, merchantIdentifier, merchantSecret);

        TransactionTemplate template = mTransactionProvider.createChargeTransactionTemplate(amount, currency, subject, customIdentifier);
        mCurrentTransactionProcess = mTransactionProvider.startTransaction(template, accessoryFamily, this);
        mTransactionIsOnGoing = true;
    }

    @Deprecated
    public void startRefundTransaction(Context context, ProviderMode providerMode, String merchantIdentifier, String merchantSecret, AccessoryFamily accessoryFamily, String transactionIdentifier, String subject, String customIdentifier) {
        clearForNewTransaction();
        mTransactionProvider = Mpos.createTransactionProvider(context, providerMode, merchantIdentifier, merchantSecret);

        TransactionTemplate template = mTransactionProvider.createRefundTransactionTemplate(transactionIdentifier, subject, customIdentifier);
        mCurrentTransactionProcess = mTransactionProvider.startTransaction(template, accessoryFamily, this);
        mTransactionIsOnGoing = true;
    }

    @Deprecated
    public void startTransactionWithSessionIdentifier(Context context, ProviderMode providerMode, String merchantIdentifier, String merchantSecret, AccessoryFamily accessoryFamily, String sessionIdentifier) {
        clearForNewTransaction();
        mTransactionProvider = Mpos.createTransactionProvider(context, providerMode, merchantIdentifier, merchantSecret);

        mCurrentTransactionProcess = mTransactionProvider.startTransaction(sessionIdentifier, accessoryFamily, this);
        mTransactionIsOnGoing = true;
        mTransactionSessionLookup = true;
    }

    public void startTransactionWithSessionIdentifier(Context context, ProviderMode providerMode, String merchantIdentifier, String merchantSecret, AccessoryParameters accessoryParameters, String sessionIdentifier, TransactionProcessParameters transactionProcessParameters) {
        clearForNewTransaction();
        mTransactionProvider = Mpos.createTransactionProvider(context, providerMode, merchantIdentifier, merchantSecret);
        mCurrentTransactionProcess = mTransactionProvider.startTransaction(sessionIdentifier, accessoryParameters, transactionProcessParameters, this);

        mTransactionIsOnGoing = true;
        mTransactionSessionLookup = true;
    }

    public void startTransaction(Context context, ProviderMode providerMode, String merchantIdentifier, String merchantSecret, AccessoryParameters accessoryParameters, TransactionParameters transactionParameters, TransactionProcessParameters transactionProcessParameters) {
        clearForNewTransaction();
        mTransactionProvider = Mpos.createTransactionProvider(context, providerMode, merchantIdentifier, merchantSecret);
        mCurrentTransactionProcess = mTransactionProvider.startTransaction(transactionParameters, accessoryParameters, transactionProcessParameters, this);

        mTransactionIsOnGoing = true;
    }

    public void amendTransaction(Context context, ProviderMode providerMode, String merchantIdentifier, String merchantSecret, TransactionParameters transactionParameters) {
        clearForNewTransaction();
        mTransactionProvider = Mpos.createTransactionProvider(context, providerMode, merchantIdentifier, merchantSecret);
        mTransactionProvider.amendTransaction(transactionParameters, this);
        mTransactionIsOnGoing = true;
    }

    @Override
    public void onRegistered(TransactionProcess paymentProcess, Transaction transaction) {
        Log.d(TAG, "onRegistered");
        mCurrentTransaction = transaction;
    }

    @Override
    public void onCompleted(TransactionProcess paymentProcess, @Nullable Transaction transaction, TransactionProcessDetails paymentProcessDetails) {
        Log.d(TAG, "onCompleted details=" + paymentProcessDetails);
        mLastTransactionProcessDetails = paymentProcessDetails;
        mTransactionIsOnGoing = false;
        mAwaitingApplicationSelection = false;
        mAwaitingSignature = false;

        if (mCallback != null) {
            mCallback.onCompleted(transaction, paymentProcessDetails.getError());
        }
    }

    @Override
    public void onStatusChanged(TransactionProcess paymentProcess, @Nullable Transaction transaction, TransactionProcessDetails paymentProcessDetails) {
        Log.d(TAG, "onStatusChanged details=" + paymentProcessDetails);
        mLastTransactionProcessDetails = paymentProcessDetails;
        if (mCurrentTransaction == null && transaction != null) {
            mCurrentTransaction = transaction;
        }

        if (mCallback != null) {
            //signature required callback
            if (!mAwaitingSignature && !mAwaitingApplicationSelection) {
                mCallback.onStatusChanged(paymentProcessDetails, transaction);
            }
        }
    }

    @Override
    public void onCustomerSignatureRequired(TransactionProcess paymentProcess, Transaction transaction) {
        mAwaitingSignature = true;
        if (mCallback != null) {
            mCallback.onCustomerSignatureRequired();
        }
    }

    @Override
    public void onCustomerVerificationRequired(TransactionProcess paymentProcess, Transaction transaction) {
        mCurrentTransactionProcess.continueWithCustomerIdentityVerified(false);
    }

    @Override
    public void onApplicationSelectionRequired(TransactionProcess paymentProcess, Transaction transaction, List<ApplicationInformation> applicationInformations) {
        mAwaitingApplicationSelection = true;
        mApplicationInformationList = applicationInformations;
        if (mCallback != null) {
            mCallback.onApplicationSelectionRequired(applicationInformations);
        }
    }

    public void continueWithSignature(Bitmap signature, boolean verified) {
        mAwaitingSignature = false;
        mCurrentTransactionProcess.continueWithCustomerSignature(signature, verified);
    }

    public void continueWithCustomerSignatureOnReceipt() {
        mAwaitingSignature = false;
        mCurrentTransactionProcess.continueWithCustomerSignatureOnReceipt();
    }

    public void continueWithApplicationSelection(ApplicationInformation selectedApplication) {
        mAwaitingApplicationSelection = false;
        mCurrentTransactionProcess.continueWithSelectedApplication(selectedApplication);
    }

    public void attachCallback(Callback callback) {
        mCallback = callback;
        if (callback != null && mLastTransactionProcessDetails != null) {

            if (mTransactionIsOnGoing) {
                if (mAwaitingSignature) {
                    callback.onCustomerSignatureRequired();
                } else if (mAwaitingApplicationSelection) {
                    callback.onApplicationSelectionRequired(mApplicationInformationList);
                } else {
                    callback.onStatusChanged(mLastTransactionProcessDetails, mCurrentTransaction);
                }
            } else {
                callback.onCompleted(mCurrentTransaction, mLastTransactionProcessDetails.getError());
            }
        }
    }

    public boolean isTransactionOnGoing() {
        return mTransactionIsOnGoing;
    }

    public boolean abortTransaction() {
        mAwaitingApplicationSelection = false;
        mAwaitingSignature = false;
        return mCurrentTransactionProcess.requestAbort();
    }

    public boolean paymentCanBeAborted() {
        return mCurrentTransactionProcess != null && mCurrentTransactionProcess.canBeAborted();
    }

    public void teardown() {
        //keep transaction reference
        boolean completed = (mLastTransactionProcessDetails == null || mLastTransactionProcessDetails.getState() == TransactionProcessDetailsState.APPROVED ||
                mLastTransactionProcessDetails.getState() == TransactionProcessDetailsState.DECLINED ||
                mLastTransactionProcessDetails.getState() == TransactionProcessDetailsState.ABORTED ||
                mLastTransactionProcessDetails.getState() == TransactionProcessDetailsState.FAILED);

        if (completed) {
            mCurrentTransactionProcess = null;
            mTransactionIsOnGoing = false;
            mApplicationInformationList = null;
            mAwaitingSignature = false;
            mAwaitingApplicationSelection = false;
        }
    }


    public Transaction getCurrentTransaction() {
        return mCurrentTransaction;
    }

    public TransactionProvider getTransactionProvider() {
        return mTransactionProvider;
    }

    public TransactionProcessDetails getLastTransactionProcessDetails() {
        return mLastTransactionProcessDetails;
    }

    public boolean isAwaitingSignature() {
        return mAwaitingSignature;
    }

    public boolean isAwaitingApplicationSelection() {
        return mAwaitingApplicationSelection;
    }

    public boolean isTransactionSessionLookup() {
        return mTransactionSessionLookup;
    }

    public void clearForNewTransaction() {
        mTransactionProvider = null;
        mLastTransactionProcessDetails = null;
        mCurrentTransactionProcess = null;
        mCurrentTransaction = null;
        mTransactionIsOnGoing = false;
        mApplicationInformationList = null;
        mAwaitingApplicationSelection = false;
        mAwaitingSignature = false;
        mTransactionSessionLookup = false;
    }
}
