/*
 * mpos-ui : http://www.payworksmobile.com
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 payworks GmbH
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
package io.mpos.ui.shared.util;

import android.content.Context;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.mpos.transactions.RefundTransactionCode;
import io.mpos.transactions.TransactionStatus;
import io.mpos.ui.R;
import io.mpos.ui.shared.model.RefundTransactionDataHolder;
import io.mpos.ui.shared.model.TransactionDataHolder;
import io.mpos.ui.shared.model.TransactionHistoryItem;

public class TransactionHistoryHelper {


    private TransactionDataHolder mTransaction;
    private List<RefundTransactionDataHolder> mRefundTransactions;

    public TransactionHistoryHelper(TransactionDataHolder transaction) {
        mTransaction = transaction;
        mRefundTransactions = transaction.getRefundTransactions();
    }

    public List<TransactionHistoryItem> createTransactionHistoryItems(Context context) {
        if (mTransaction == null || mTransaction.getRefundTransactions() == null) {
            return null;
        }
        List<TransactionHistoryItem> transactionHistoryItems = new ArrayList<>();

        TransactionHistoryItem partialCaptureItem = generatePartialCaptureItem(context);
        if (partialCaptureItem != null) {
            transactionHistoryItems.add(partialCaptureItem);
        } else if (hasApprovedRefundTransaction()) {
            transactionHistoryItems.add(generateSaleItem(context));
        } else {
            return null; // No partial captures or approved refunds.
        }

        transactionHistoryItems.addAll(generateRefundTransactionItems(context));

        return transactionHistoryItems;
    }

    private TransactionHistoryItem generatePartialCaptureItem(Context context) {
        RefundTransactionDataHolder partiallyCapturedRefundTx = getPartiallyCapturedRefundTransaction();
        if (partiallyCapturedRefundTx != null) {
            BigDecimal partiallyCapturedAmount = TransactionAmountUtil.calculatePartiallyCapturedAmount(mTransaction, partiallyCapturedRefundTx);

            return TransactionHistoryItem.createPartialCaptureItem(context,
                    R.string.MPUTransactionTypeSale,
                    partiallyCapturedAmount,
                    partiallyCapturedRefundTx.getCurrency(),
                    partiallyCapturedRefundTx.getCreatedTimestamp(),
                    mTransaction.getAmount(),
                    partiallyCapturedRefundTx.getCurrency());
        }

        return null;
    }

    private TransactionHistoryItem generateSaleItem(Context context) {
        if (mTransaction.isCaptured()) {
            return TransactionHistoryItem.createItem(context,
                    TransactionHistoryItem.Type.CHARGE,
                    R.string.MPUTransactionTypeSale,
                    mTransaction.getAmount(),
                    mTransaction.getCurrency(),
                    mTransaction.getCreatedTimestamp());
        } else {
            return TransactionHistoryItem.createItem(context,
                    TransactionHistoryItem.Type.PREAUTHORIZED,
                    R.string.MPUTransactionTypePreauthorization,
                    mTransaction.getAmount(),
                    mTransaction.getCurrency(),
                    mTransaction.getCreatedTimestamp());
        }
    }

    private List<TransactionHistoryItem> generateRefundTransactionItems(Context context) {
        List<TransactionHistoryItem> refundItems = new ArrayList<>();
        for (RefundTransactionDataHolder refundTransaction : mTransaction.getRefundTransactions()) {
            if (refundTransaction.getStatus() == TransactionStatus.APPROVED  // Approved
                    && refundTransaction.getRefundTransactionCode() != RefundTransactionCode.PARTIAL_CAPTURE) { // Not partial capture
                TransactionHistoryItem refundItem = TransactionHistoryItem.createRefundItem(context,
                        R.string.MPUTransactionTypeRefund,
                        refundTransaction.getAmount(),
                        refundTransaction.getCurrency(),
                        refundTransaction.getCreatedTimestamp());
                refundItems.add(refundItem);
            }
        }
        return refundItems;
    }

    public RefundTransactionDataHolder getPartiallyCapturedRefundTransaction() {
        if (mRefundTransactions != null) {
            for (RefundTransactionDataHolder refundTransaction : mRefundTransactions) {
                if (refundTransaction.getRefundTransactionCode() == RefundTransactionCode.PARTIAL_CAPTURE) {
                    return refundTransaction;
                }
            }
        }
        return null;
    }

    private boolean hasApprovedRefundTransaction() {
        if (mRefundTransactions != null) {
            for (RefundTransactionDataHolder refundTransaction : mTransaction.getRefundTransactions()) {
                if (refundTransaction.getStatus() == TransactionStatus.APPROVED) {
                    return true;
                }
            }
        }
        return false;
    }

    public RefundTransactionDataHolder getLatestApprovedRefundTransaction() {
        // The RefundTransactions are sorted in the ascending order.
        // We find the latest approved by iterating from the end.
        if (mRefundTransactions != null) {
            int size = mRefundTransactions.size();
            for (int i = size - 1; i >= 0; i--) {
                RefundTransactionDataHolder refundTransaction = mRefundTransactions.get(i);
                if (refundTransaction.getRefundTransactionCode() != RefundTransactionCode.PARTIAL_CAPTURE
                        && refundTransaction.getStatus() == TransactionStatus.APPROVED) {
                    return refundTransaction;
                }
            }
        }
        return null;
    }
}
