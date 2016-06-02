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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import io.mpos.transactions.TransactionStatus;
import io.mpos.ui.shared.model.RefundTransactionDataHolder;
import io.mpos.ui.shared.model.TransactionDataHolder;

public class TransactionAmountUtil {

    private TransactionAmountUtil() {
    }

    public static BigDecimal calculateEffectiveTotalAmount(TransactionDataHolder transaction) {
        BigDecimal transactionAmount = transaction.getAmount();
        BigDecimal refundedAmount = new BigDecimal(BigInteger.ZERO);
        List<RefundTransactionDataHolder> refundTransactions = transaction.getRefundTransactions();
        if (refundTransactions != null) {
            for (RefundTransactionDataHolder refundTransaction : refundTransactions) {
                if (refundTransaction.getStatus() == TransactionStatus.APPROVED) {
                    refundedAmount = refundedAmount.add(refundTransaction.getAmount());
                }
            }
        }
        BigDecimal effectiveAmount = transactionAmount.subtract(refundedAmount);
        return effectiveAmount;
    }

    public static BigDecimal calculatePartiallyCapturedAmount(TransactionDataHolder transaction, RefundTransactionDataHolder partiallyCapturedRefundTx) {
        BigDecimal reservedAmount = transaction.getAmount();
        BigDecimal refundedAmount = partiallyCapturedRefundTx.getAmount();
        BigDecimal partiallyCapturedAmount = reservedAmount.subtract(refundedAmount);
        return partiallyCapturedAmount;
    }
}
