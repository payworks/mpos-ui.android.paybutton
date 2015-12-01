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
package io.mpos.ui.shared.controller;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.accessories.parameters.AccessoryParameters;
import io.mpos.transactionprovider.PrintingProcess;
import io.mpos.transactionprovider.PrintingProcessDetails;
import io.mpos.transactionprovider.PrintingProcessListener;
import io.mpos.transactionprovider.TransactionProvider;

public class StatefulPrintingProcessProxy {

    private static final String TAG = "StatefulPrinterController";

    public interface Callback {

        void onCompleted(PrintingProcess printingProcess, PrintingProcessDetails printingProcessDetails);

        void onStatusChanged(PrintingProcess printingProcess, PrintingProcessDetails printingProcessDetails);
    }

    private static StatefulPrintingProcessProxy INSTANCE;

    public static StatefulPrintingProcessProxy getInstance() {
        if (INSTANCE == null)
            INSTANCE = new StatefulPrintingProcessProxy();
        return INSTANCE;
    }

    private StatefulPrintingProcessProxy() {
    }

    private TransactionProvider mTransactionProvider;
    private String mTransactionIdentifier;
    private PrintingProcess mPrintingProcess;
    private Callback mCallback;
    private PrintingProcessDetails mlastPrintingProcessDetails;
    private boolean mPrintingProcessOngoing;

    @Deprecated
    public void printReceipt(String transactionIdentifier, TransactionProvider transactionProvider, AccessoryFamily accessoryFamily) {
        mTransactionProvider = transactionProvider;
        mTransactionIdentifier = transactionIdentifier;
        mPrintingProcessOngoing = true;
        mPrintingProcess = mTransactionProvider.printCustomerReceiptForTransaction(transactionIdentifier, accessoryFamily, mPrintingProcessListenerProxy);
    }


    public void printReceipt(String transactionIdentifier, TransactionProvider transactionProvider, AccessoryParameters accessoryParameters) {
        mTransactionProvider = transactionProvider;
        mTransactionIdentifier = transactionIdentifier;
        mPrintingProcessOngoing = true;
        mPrintingProcess = mTransactionProvider.printCustomerReceiptForTransaction(transactionIdentifier, accessoryParameters, mPrintingProcessListenerProxy);
    }

    private PrintingProcessListener mPrintingProcessListenerProxy = new PrintingProcessListener() {
        @Override
        public void onCompleted(PrintingProcess printingProcess, PrintingProcessDetails printingProcessDetails) {
            mPrintingProcess = printingProcess;
            mlastPrintingProcessDetails = printingProcessDetails;
            mPrintingProcessOngoing = false;
            if (mCallback != null) {
                mCallback.onCompleted(printingProcess, printingProcessDetails);
            }
        }

        @Override
        public void onStatusChanged(PrintingProcess printingProcess, PrintingProcessDetails printingProcessDetails) {
            mPrintingProcess = printingProcess;
            mlastPrintingProcessDetails = printingProcessDetails;
            if (mCallback != null) {
                mCallback.onStatusChanged(printingProcess, printingProcessDetails);
            }
        }
    };

    public void attachCallback(Callback callback) {
        mCallback = callback;
        if (callback != null && mlastPrintingProcessDetails != null) {
            if (mPrintingProcessOngoing)
                callback.onStatusChanged(mPrintingProcess, mlastPrintingProcessDetails);
            else
                callback.onCompleted(mPrintingProcess, mlastPrintingProcessDetails);
        }
    }
    public void requestAbort() {
        mPrintingProcess.requestAbort();
    }

    public boolean canAbort() {
        return mPrintingProcess.canAbort();
    }

    public void teardown(){
        if(!mPrintingProcessOngoing){
            mPrintingProcess = null;
            mlastPrintingProcessDetails = null;
            mTransactionIdentifier = null;
            mTransactionProvider = null;
            mPrintingProcessOngoing = false;
            mCallback = null;
        }
    }
}
