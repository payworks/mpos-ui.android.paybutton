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
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import java.math.BigDecimal;
import java.util.List;

import io.mpos.Mpos;
import io.mpos.accessories.Accessory;
import io.mpos.accessories.AccessoryConnectionState;
import io.mpos.accessories.AccessoryFamily;
import io.mpos.errors.ErrorType;
import io.mpos.errors.MposError;
import io.mpos.paymentdetails.ApplicationInformation;
import io.mpos.provider.ProviderMode;
import io.mpos.shared.errors.DefaultMposError;
import io.mpos.transactionprovider.PaymentProcess;
import io.mpos.transactionprovider.PaymentProcessDetails;
import io.mpos.transactionprovider.PaymentProcessDetailsState;
import io.mpos.transactionprovider.PaymentProcessWithRegistrationListener;
import io.mpos.transactionprovider.SendReceiptListener;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.transactions.Currency;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.TransactionTemplate;
import io.mpos.transactions.receipts.Receipt;

/**
 * StatefulTransactionProviderProxy keeps the state of the ongoing transaction independent of the Fragment/Activity's lifecycle.
 * It also reissues the last event when a callback is attached. (during FragmentTransaction / orientation change)
 */
public class StatefulTransactionProviderProxy implements PaymentProcessWithRegistrationListener {

    private final static String TAG = "TxProviderProxy";

    public interface Callback {

        void onApplicationSelectionRequired(List<ApplicationInformation> applicationInformations);

        void onCustomerSignatureRequired();

        void onStatusChanged(PaymentProcessDetails details, Transaction transaction);

        void onCompleted(Transaction transaction, MposError error);
    }

    public interface SendReceiptCallback {

        void onSendingStarted();

        void onCompleted(MposError error);
    }

    private final static StatefulTransactionProviderProxy INSTANCE = new StatefulTransactionProviderProxy();

    private Transaction mCurrentTransaction;
    private PaymentProcess mCurrentPaymentProcess;

    private Callback mCallback;
    private SendReceiptCallback mSendReceiptCallback;

    private boolean mAwaitingSignature;
    private boolean mAwaitingApplicationSelection;
    private boolean mTransactionSessionLookup;

    private Context mContext;

    private boolean mPaymentIsOnGoing;
    private boolean mSendReceiptOnGoing;
    private PaymentProcessDetails mLastPaymentProcessDetails;
    private TransactionProvider mTransactionProvider;

    private boolean mCustomerVerificationRequired = false;

    private List<ApplicationInformation> mApplicationInformationList;


    private StatefulTransactionProviderProxy() {
        //singleton
    }

    public static StatefulTransactionProviderProxy getInstance() {
        return INSTANCE;
    }


    public void startPayment(Context context, String merchantIdentifier, String merchantSecret, ProviderMode providerMode, AccessoryFamily accessoryFamily, BigDecimal amount, Currency currency, String subject, String customIdentifier, boolean isCharge) {
        if(isPaymentOnGoing()) {
            //TODO: throw exception
        }

        reset();
        mContext = context;

        mTransactionProvider = Mpos.createTransactionProvider(context, providerMode, merchantIdentifier, merchantSecret);

        TransactionTemplate template = null;
        if(isCharge) {
            template = mTransactionProvider.createChargeTransactionTemplate(amount, currency, subject, null);
        } else {
            //template = transactionProvider.createRefundTransactionTemplate(mTransactionParameters.getTransactionIdentifier(), amount, currency, subject, null);
        }

        mCurrentPaymentProcess = mTransactionProvider.startPayment(template, accessoryFamily, this);
        mPaymentIsOnGoing = true;
    }

    public void startPayment(Context context, String sessionIdentifier, ProviderMode providerMode, String merchantIdentifier, String merchantSecret, AccessoryFamily accessoryFamily) {
        if(isPaymentOnGoing()) {
            //TODO: throw exception
        }

        reset();
        mContext = context;
        mTransactionProvider = Mpos.createTransactionProvider(context, providerMode, merchantIdentifier, merchantSecret);

        mCurrentPaymentProcess = mTransactionProvider.startPayment(sessionIdentifier, accessoryFamily, this);
        mPaymentIsOnGoing = true;
        mTransactionSessionLookup = true;
    }

    @Override
    public void onRegistered(PaymentProcess paymentProcess, Transaction transaction) {
        Log.d(TAG, "onRegistered");
        mCurrentTransaction = transaction;
    }

    @Override
    public void onCompleted(PaymentProcess paymentProcess, Transaction transaction, PaymentProcessDetails paymentProcessDetails) {
        Log.d(TAG, "onCompleted details=" + paymentProcessDetails);
        mLastPaymentProcessDetails = paymentProcessDetails;
        mPaymentIsOnGoing = false;
        mAwaitingApplicationSelection = false;
        mAwaitingSignature = false;

        if(mCallback != null) {
            mCallback.onCompleted(transaction, paymentProcessDetails.getError());
        }
    }

    @Override
    public void onStatusChanged(PaymentProcess paymentProcess, Transaction transaction, PaymentProcessDetails paymentProcessDetails) {
        Log.d(TAG, "onStatusChanged details=" + paymentProcessDetails);
        mLastPaymentProcessDetails = paymentProcessDetails;
        if(mCurrentTransaction == null && transaction != null) {
            mCurrentTransaction = transaction;
        }

        if(mCallback != null) {

            //signature required callback
            if(!mAwaitingSignature && !mAwaitingApplicationSelection) {
                mCallback.onStatusChanged(paymentProcessDetails, transaction);
            }
        }
    }

    @Override
    public void onCustomerSignatureRequired(PaymentProcess paymentProcess, Transaction transaction) {
        mAwaitingSignature = true;
        if(mCallback != null) {
            mCallback.onCustomerSignatureRequired();
        }
    }

    @Override
    public void onCustomerVerificationRequired(PaymentProcess paymentProcess, Transaction transaction) {
        mCurrentPaymentProcess.continueWithCustomerIdentityVerified(false);
    }

    @Override
    public void onApplicationSelectionRequired(PaymentProcess paymentProcess, Transaction transaction, List<ApplicationInformation> applicationInformations) {
        mAwaitingApplicationSelection = true;
        mApplicationInformationList = applicationInformations;
        if(mCallback != null) {
            mCallback.onApplicationSelectionRequired(applicationInformations);
        }
    }

    public void continueWithSignature(Bitmap signature, boolean verified) {
        mAwaitingSignature = false;
        mCurrentPaymentProcess.continueWithCustomerSignature(signature, verified);
    }

    public void continueWithApplicationSelection(ApplicationInformation selectedApplication) {
        mAwaitingApplicationSelection = false;
        mCurrentPaymentProcess.continueWithSelectedApplication(selectedApplication);
    }

    public void attachCallback(Callback callback) {
        mCallback = callback;

        if(callback != null && mLastPaymentProcessDetails != null && mPaymentIsOnGoing) {
            if(mAwaitingSignature) {
                callback.onCustomerSignatureRequired();
            } else if(mAwaitingApplicationSelection) {
                callback.onApplicationSelectionRequired(mApplicationInformationList);
            } else {
                callback.onStatusChanged(mLastPaymentProcessDetails, mCurrentTransaction);
            }
        }
    }

    public Receipt getMerchantReceipt() {
        if(mCurrentTransaction == null)
            return null;

        return mCurrentPaymentProcess.getReceiptFactory().createMerchantReceipt(mCurrentTransaction);
    }


    public boolean isPaymentOnGoing() {
        return mPaymentIsOnGoing;
    }

    public boolean abortPayment() {
        return mCurrentPaymentProcess.requestAbort();
    }

    public boolean paymentCanBeAborted() {
        boolean stateIsAbortable = mLastPaymentProcessDetails.getState().equals(PaymentProcessDetailsState.CONNECTING_TO_ACCESSORY);
        boolean accessoryConnected = true;
        if(mCurrentPaymentProcess != null && mCurrentPaymentProcess.getAccessory() != null) {
            Accessory accessory = mCurrentPaymentProcess.getAccessory();
            if(accessory.getConnectionState().equals(AccessoryConnectionState.UNKNOWN) || accessory.getConnectionState().equals(AccessoryConnectionState.DISCONNECTED)) {
                accessoryConnected = false;
            }
        }

        if(stateIsAbortable && !accessoryConnected) {
            return true;
        }

        return mCurrentTransaction != null && mCurrentTransaction.canBeAborted();
    }

    public void sendReceipt(final String email) {
        mSendReceiptOnGoing = true;
        if (mSendReceiptCallback != null) {
            mSendReceiptCallback.onSendingStarted();
        }

        mCurrentPaymentProcess.setSendReceiptListener(new SendReceiptListener() {
            @Override
            public void onCompleted(PaymentProcess paymentProcess, MposError error) {
                mSendReceiptOnGoing = false;
                if (mSendReceiptCallback != null) {
                    mSendReceiptCallback.onCompleted(error);
                }
            }
        });
        mCurrentPaymentProcess.sendReceiptForTransaction(email);
    }

    public void attachSendReceiptCallback(SendReceiptCallback callback) {
        mSendReceiptCallback = callback;

        if (callback != null && mSendReceiptOnGoing) {
            callback.onSendingStarted();
        }
    }

    public void teardown() {
        //keep transaction reference
        boolean completed = mLastPaymentProcessDetails.getState().equals(PaymentProcessDetailsState.APPROVED) ||
                mLastPaymentProcessDetails.getState().equals(PaymentProcessDetailsState.DECLINED) ||
                mLastPaymentProcessDetails.getState().equals(PaymentProcessDetailsState.ABORTED) ||
                mLastPaymentProcessDetails.getState().equals(PaymentProcessDetailsState.FAILED);

        if(completed) {
            mCurrentPaymentProcess = null;
            mLastPaymentProcessDetails = null;
            mPaymentIsOnGoing = false;
            mApplicationInformationList = null;
            mAwaitingSignature = false;
            mAwaitingApplicationSelection = false;
        }
    }


    public Transaction getCurrentTransaction() {
        return mCurrentTransaction;
    }

    public PaymentProcessDetails getLastPaymentProcessDetails() {
        return mLastPaymentProcessDetails;
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

    void reset() {
        mTransactionProvider = null;
        mLastPaymentProcessDetails = null;
        mCurrentPaymentProcess = null;
        mCurrentTransaction = null;
        mPaymentIsOnGoing = false;
        mSendReceiptOnGoing = false;
        mApplicationInformationList = null;
        mAwaitingApplicationSelection = false;
        mAwaitingSignature = false;
        mTransactionSessionLookup = false;
    }
}
