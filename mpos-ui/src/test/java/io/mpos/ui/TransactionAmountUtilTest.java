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
package io.mpos.ui;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.mpos.transactions.Currency;
import io.mpos.transactions.RefundTransactionCode;
import io.mpos.transactions.TransactionStatus;
import io.mpos.ui.shared.model.RefundTransactionDataHolder;
import io.mpos.ui.shared.model.TransactionDataHolder;
import io.mpos.ui.shared.util.TransactionAmountUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionAmountUtilTest {

    @Mock
    TransactionDataHolder mMockTransaction;

    @Before
    public void setup() {
        //setup mock transaction
        when(mMockTransaction.getAmount()).thenReturn(new BigDecimal("14.00"));
        when(mMockTransaction.getCurrency()).thenReturn(Currency.EUR);
        when(mMockTransaction.getCreatedTimestamp()).thenReturn(System.currentTimeMillis());
        when(mMockTransaction.isCaptured()).thenReturn(true);
    }

    @Test
    public void effectiveTotalAmount_withNoRefunds() throws Exception {
        when(mMockTransaction.getRefundTransactions()).thenReturn(null);

        BigDecimal victim = TransactionAmountUtil.calculateEffectiveTotalAmount(mMockTransaction);

        assertThat(victim).isEqualTo(new BigDecimal("14.00"));
    }

    @Test
    public void effectiveTotalAmount_refund() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        BigDecimal victim = TransactionAmountUtil.calculateEffectiveTotalAmount(mMockTransaction);

        assertThat(victim).isEqualTo(new BigDecimal("12.00"));
    }

    @Test
    public void effectiveTotalAmount_partiallyCapturedRefund() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getPartiallyCapturedRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        BigDecimal victim = TransactionAmountUtil.calculateEffectiveTotalAmount(mMockTransaction);

        assertThat(victim).isEqualTo(new BigDecimal("12.00"));
    }

    @Test
    public void effectiveTotalAmount_refund_declined() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getDeclinedRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);
        
        BigDecimal victim = TransactionAmountUtil.calculateEffectiveTotalAmount(mMockTransaction);

        assertThat(victim).isEqualTo(new BigDecimal("14.00"));
    }

    @Test
    public void effectiveTotalAmount_multipleRefunds() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getRefundTransaction());
        mockRefundTransactions.add(getRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        BigDecimal victim = TransactionAmountUtil.calculateEffectiveTotalAmount(mMockTransaction);

        assertThat(victim).isEqualTo(new BigDecimal("10.00"));
    }


    @Test
    public void effectiveTotalAmount_multipleRefunds_withDeclined() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getDeclinedRefundTransaction());
        mockRefundTransactions.add(getRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        BigDecimal victim = TransactionAmountUtil.calculateEffectiveTotalAmount(mMockTransaction);

        assertThat(victim).isEqualTo(new BigDecimal("12.00"));
    }

    @Test
    public void partiallyCapturedAmount() throws Exception {
        RefundTransactionDataHolder partiallyCapturedTx = getPartiallyCapturedRefundTransaction();

        BigDecimal victim = TransactionAmountUtil.calculatePartiallyCapturedAmount(mMockTransaction, partiallyCapturedTx);

        assertThat(victim).isEqualTo(new BigDecimal("12.00"));
    }


    private RefundTransactionDataHolder getPartiallyCapturedRefundTransaction() {
        RefundTransactionDataHolder partiallyCapturedRefund = Mockito.mock(RefundTransactionDataHolder.class);
        when(partiallyCapturedRefund.getAmount()).thenReturn(new BigDecimal("2.00"));
        when(partiallyCapturedRefund.getCurrency()).thenReturn(Currency.EUR);
        when(partiallyCapturedRefund.getCreatedTimestamp()).thenReturn(System.currentTimeMillis());
        when(partiallyCapturedRefund.getStatus()).thenReturn(TransactionStatus.APPROVED);
        when(partiallyCapturedRefund.getRefundTransactionCode()).thenReturn(RefundTransactionCode.PARTIAL_CAPTURE);
        return partiallyCapturedRefund;
    }

    private RefundTransactionDataHolder getRefundTransaction() {
        RefundTransactionDataHolder refundTransaction = Mockito.mock(RefundTransactionDataHolder.class);
        when(refundTransaction.getAmount()).thenReturn(new BigDecimal("2.00"));
        when(refundTransaction.getCurrency()).thenReturn(Currency.EUR);
        when(refundTransaction.getStatus()).thenReturn(TransactionStatus.APPROVED);
        when(refundTransaction.getCreatedTimestamp()).thenReturn(System.currentTimeMillis());
        when(refundTransaction.getRefundTransactionCode()).thenReturn(RefundTransactionCode.REFUND_BEFORE_CLEARING);
        return refundTransaction;
    }

    private RefundTransactionDataHolder getDeclinedRefundTransaction() {
        RefundTransactionDataHolder refundTransaction = Mockito.mock(RefundTransactionDataHolder.class);
        when(refundTransaction.getAmount()).thenReturn(new BigDecimal("2.00"));
        when(refundTransaction.getCurrency()).thenReturn(Currency.EUR);
        when(refundTransaction.getCreatedTimestamp()).thenReturn(System.currentTimeMillis());
        when(refundTransaction.getStatus()).thenReturn(TransactionStatus.DECLINED);
        when(refundTransaction.getRefundTransactionCode()).thenReturn(RefundTransactionCode.REFUND_BEFORE_CLEARING);
        return refundTransaction;
    }
}
