package io.mpos.ui.paybutton.controller;

import io.mpos.accessories.parameters.AccessoryParameters;
import io.mpos.transactionprovider.CardProcess;
import io.mpos.transactionprovider.CardProcessDetails;
import io.mpos.transactionprovider.ReadCardProcessListener;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.transactions.CardDetails;
import io.mpos.ui.shared.MposUi;

public class StatefulReadCardProcessProxy {

    public interface Callback {

        void onCompleted(CardProcessDetails processDetails, CardDetails cardDetails);

        void onStatusChanged(CardProcessDetails processDetails);

    }

    private boolean mOngoing = false;
    private CardProcess mProcess;
    private CardProcessDetails mProcessDetails;
    private CardDetails mCardDetails;
    private Callback mCallback;

    private final static StatefulReadCardProcessProxy INSTANCE = new StatefulReadCardProcessProxy();

    public static StatefulReadCardProcessProxy getInstance() {
        return INSTANCE;
    }

    public void readCard(AccessoryParameters accessoryParameters) {
        cleanupForNewCardRead();
        TransactionProvider transactionProvider = MposUi.getInitializedInstance().getTransactionProvider();
        transactionProvider.readCard(accessoryParameters, mCardProcessListener);
        mOngoing = true;
    }

    public boolean requestAbort() {
        return mProcess != null && mProcess.requestAbort();
    }

    public boolean isAbortable() {
        return mProcess != null && mProcess.canBeAborted();
    }

    public boolean isOngoing() {
        return mOngoing;
    }

    private ReadCardProcessListener mCardProcessListener = new ReadCardProcessListener() {
        @Override
        public void onCompleted(CardProcess cardProcess, CardProcessDetails processDetails) {
            mProcess = cardProcess;
            mProcessDetails = processDetails;
            mCardDetails = cardProcess.getCardDetails();
            mOngoing = false;
            if (mCallback != null) {
                mCallback.onCompleted(processDetails, cardProcess.getCardDetails());
            }
        }

        @Override
        public void onStatusChanged(CardProcess cardProcess, CardProcessDetails processDetails) {
            mProcess = cardProcess;
            mProcessDetails = processDetails;
            if (mCallback != null) {
                mCallback.onStatusChanged(processDetails);
            }
        }
    };

    public void attachCallback(Callback callback) {
        mCallback = callback;
        if (mCallback != null && mProcessDetails != null) {
            if (mOngoing) {
                mCallback.onStatusChanged(mProcessDetails);
            } else {
                mCallback.onCompleted(mProcessDetails, mCardDetails);
            }
        }
    }

    public void teardown() {
        if (!mOngoing) {
            mProcess = null;
            mProcessDetails = null;
            mOngoing = false;
        }
    }

    public void cleanupForNewCardRead() {
        mCardDetails = null;
    }

    public CardDetails getCardDetails() {
        return mCardDetails;
    }

}

