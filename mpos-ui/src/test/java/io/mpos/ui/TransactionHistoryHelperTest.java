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

import android.content.Context;

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
import io.mpos.ui.shared.model.TransactionHistoryItem;
import io.mpos.ui.shared.util.TransactionHistoryHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionHistoryHelperTest {

    @Mock
    Context mMockContext;

    @Mock
    TransactionDataHolder mMockTransaction;

    @Before
    public void setup() {
        //setup mock context
        when(mMockContext.getString(R.string.MPUTransactionTypeSale)).thenReturn("Sale");
        when(mMockContext.getString(R.string.MPUTransactionTypePreauthorization)).thenReturn("Pre-Authorization");
        when(mMockContext.getString(R.string.MPUTransactionTypeRefund)).thenReturn("Refund");
        when(mMockContext.getString(eq(R.string.MPUPartiallyCaptured), any(String.class))).thenReturn("of Pre-Authorization for");

        //setup mock transaction
        when(mMockTransaction.getAmount()).thenReturn(new BigDecimal("14.00"));
        when(mMockTransaction.getCurrency()).thenReturn(Currency.EUR);
        when(mMockTransaction.getCreatedTimestamp()).thenReturn(System.currentTimeMillis());
        when(mMockTransaction.isCaptured()).thenReturn(true);
    }

    @Test
    public void transactionHistoryItems_partialCapture() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getPartiallyCapturedRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        List<TransactionHistoryItem> items = victim.createTransactionHistoryItems(mMockContext);

        assertThat(items.size()).isEqualTo(1);
        TransactionHistoryItem partialCaptureItem = items.get(0);
        assertThat(partialCaptureItem.getType()).isEqualTo(TransactionHistoryItem.Type.PARTIAL_CAPTURE);
        assertThat(partialCaptureItem.getAmountText()).isEqualTo("12.00€");
        assertThat(partialCaptureItem.getStatusText()).isEqualTo("Sale");
        assertThat(partialCaptureItem.getPartialCaptureHintText()).isNotNull();
    }

    @Test
    public void transactionHistoryItems_refund() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        List<TransactionHistoryItem> items = victim.createTransactionHistoryItems(mMockContext);

        assertThat(items.size()).isEqualTo(2);
        TransactionHistoryItem saleItem = items.get(0);
        assertThat(saleItem.getType()).isEqualTo(TransactionHistoryItem.Type.CHARGE);
        assertThat(saleItem.getAmountText()).isEqualTo("14.00€");
        assertThat(saleItem.getStatusText()).isEqualTo("Sale");
        assertThat(saleItem.getPartialCaptureHintText()).isNull();

        TransactionHistoryItem refundItem = items.get(1);
        assertThat(refundItem.getType()).isEqualTo(TransactionHistoryItem.Type.REFUND);
        assertThat(refundItem.getAmountText()).isEqualTo("-2.00€");
        assertThat(refundItem.getStatusText()).isEqualTo("Refund");
        assertThat(refundItem.getPartialCaptureHintText()).isNull();
    }

    @Test
    public void transactionHistoryItems_preAuthorizeAndRefund() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);
        when(mMockTransaction.isCaptured()).thenReturn(false);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        List<TransactionHistoryItem> items = victim.createTransactionHistoryItems(mMockContext);

        assertThat(items.size()).isEqualTo(2);
        TransactionHistoryItem saleItem = items.get(0);
        assertThat(saleItem.getType()).isEqualTo(TransactionHistoryItem.Type.PREAUTHORIZED);
        assertThat(saleItem.getAmountText()).isEqualTo("14.00€");
        assertThat(saleItem.getStatusText()).isEqualTo("Pre-Authorization");
        assertThat(saleItem.getPartialCaptureHintText()).isNull();

        TransactionHistoryItem refundItem = items.get(1);
        assertThat(refundItem.getType()).isEqualTo(TransactionHistoryItem.Type.REFUND);
        assertThat(refundItem.getAmountText()).isEqualTo("-2.00€");
        assertThat(refundItem.getStatusText()).isEqualTo("Refund");
        assertThat(refundItem.getPartialCaptureHintText()).isNull();
    }

    @Test
    public void transactionHistoryItems_partialCaptureAndRefund() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getPartiallyCapturedRefundTransaction());
        mockRefundTransactions.add(getRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        List<TransactionHistoryItem> items = victim.createTransactionHistoryItems(mMockContext);

        assertThat(items.size()).isEqualTo(2);
        TransactionHistoryItem saleItem = items.get(0);
        assertThat(saleItem.getType()).isEqualTo(TransactionHistoryItem.Type.PARTIAL_CAPTURE);
        assertThat(saleItem.getAmountText()).isEqualTo("12.00€");
        assertThat(saleItem.getStatusText()).isEqualTo("Sale");
        assertThat(saleItem.getPartialCaptureHintText()).isNotNull();

        TransactionHistoryItem refundItem = items.get(1);
        assertThat(refundItem.getType()).isEqualTo(TransactionHistoryItem.Type.REFUND);
        assertThat(refundItem.getAmountText()).isEqualTo("-2.00€");
        assertThat(refundItem.getStatusText()).isEqualTo("Refund");
        assertThat(refundItem.getPartialCaptureHintText()).isNull();
    }

    @Test
    public void transactionHistoryItems_multipleRefunds() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getRefundTransaction());
        mockRefundTransactions.add(getRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        List<TransactionHistoryItem> items = victim.createTransactionHistoryItems(mMockContext);

        assertThat(items.size()).isEqualTo(3);
        TransactionHistoryItem saleItem = items.get(0);
        assertThat(saleItem.getType()).isEqualTo(TransactionHistoryItem.Type.CHARGE);
        assertThat(saleItem.getAmountText()).isEqualTo("14.00€");
        assertThat(saleItem.getStatusText()).isEqualTo("Sale");
        assertThat(saleItem.getPartialCaptureHintText()).isNull();

        TransactionHistoryItem refundItem1 = items.get(1);
        assertThat(refundItem1.getType()).isEqualTo(TransactionHistoryItem.Type.REFUND);
        assertThat(refundItem1.getAmountText()).isEqualTo("-2.00€");
        assertThat(refundItem1.getStatusText()).isEqualTo("Refund");
        assertThat(refundItem1.getPartialCaptureHintText()).isNull();

        TransactionHistoryItem refundItem2 = items.get(2);
        assertThat(refundItem2.getType()).isEqualTo(TransactionHistoryItem.Type.REFUND);
        assertThat(refundItem2.getAmountText()).isEqualTo("-2.00€");
        assertThat(refundItem2.getStatusText()).isEqualTo("Refund");
        assertThat(refundItem2.getPartialCaptureHintText()).isNull();
    }

    @Test
    public void transactionHistoryItems_declinedRefund() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getDeclinedRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        List<TransactionHistoryItem> items = victim.createTransactionHistoryItems(mMockContext);

        assertThat(items).isNull();
    }

    @Test
    public void transactionHistoryItems_multipleRefunds_withDeclined() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getRefundTransaction());
        mockRefundTransactions.add(getRefundTransaction());
        mockRefundTransactions.add(getDeclinedRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        List<TransactionHistoryItem> items = victim.createTransactionHistoryItems(mMockContext);

        assertThat(items.size()).isEqualTo(3);
        TransactionHistoryItem saleItem = items.get(0);
        assertThat(saleItem.getType()).isEqualTo(TransactionHistoryItem.Type.CHARGE);
        assertThat(saleItem.getAmountText()).isEqualTo("14.00€");
        assertThat(saleItem.getStatusText()).isEqualTo("Sale");
        assertThat(saleItem.getPartialCaptureHintText()).isNull();

        TransactionHistoryItem refundItem1 = items.get(1);
        assertThat(refundItem1.getType()).isEqualTo(TransactionHistoryItem.Type.REFUND);
        assertThat(refundItem1.getAmountText()).isEqualTo("-2.00€");
        assertThat(refundItem1.getStatusText()).isEqualTo("Refund");
        assertThat(refundItem1.getPartialCaptureHintText()).isNull();

        TransactionHistoryItem refundItem2 = items.get(2);
        assertThat(refundItem2.getType()).isEqualTo(TransactionHistoryItem.Type.REFUND);
        assertThat(refundItem2.getAmountText()).isEqualTo("-2.00€");
        assertThat(refundItem2.getStatusText()).isEqualTo("Refund");
        assertThat(refundItem2.getPartialCaptureHintText()).isNull();
    }

    @Test
    public void transactionHistoryItems_nullRefundTransactions() throws Exception {
        when(mMockTransaction.getRefundTransactions()).thenReturn(null);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        List<TransactionHistoryItem> items = victim.createTransactionHistoryItems(mMockContext);

        assertThat(items).isNull();
    }

    @Test
    public void partiallyCapturedRefundTransaction_null() throws Exception {
        when(mMockTransaction.getRefundTransactions()).thenReturn(null);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        RefundTransactionDataHolder partiallyCapturedRefundTransaction = victim.getPartiallyCapturedRefundTransaction();

        assertThat(partiallyCapturedRefundTransaction).isNull();
    }

    @Test
    public void partiallyCapturedRefundTransaction_present() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getPartiallyCapturedRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        RefundTransactionDataHolder partiallyCapturedRefundTransaction = victim.getPartiallyCapturedRefundTransaction();

        assertThat(partiallyCapturedRefundTransaction).isNotNull();
    }

    @Test
    public void partiallyCapturedRefundTransaction_absent() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        mockRefundTransactions.add(getRefundTransaction());
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        RefundTransactionDataHolder partiallyCapturedRefundTransaction = victim.getPartiallyCapturedRefundTransaction();

        assertThat(partiallyCapturedRefundTransaction).isNull();
    }

    @Test
    public void latestApprovedRefund_withPartiallyCapturedRefund() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        RefundTransactionDataHolder refundTransaction1 = getRefundTransaction();
        when(refundTransaction1.getTransactionIdentifier()).thenReturn("tx-id1");
        RefundTransactionDataHolder refundTransaction2 = getRefundTransaction();
        when(refundTransaction2.getTransactionIdentifier()).thenReturn("tx-id2");
        RefundTransactionDataHolder refundTransaction3 = getPartiallyCapturedRefundTransaction();
        when(refundTransaction3.getTransactionIdentifier()).thenReturn("tx-partiallyCaptured");
        mockRefundTransactions.add(refundTransaction1);
        mockRefundTransactions.add(refundTransaction2);
        mockRefundTransactions.add(refundTransaction3);
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        RefundTransactionDataHolder latestRefund = victim.getLatestApprovedRefundTransaction();

        assertThat(latestRefund.getTransactionIdentifier()).isEqualTo("tx-id2");
    }

    @Test
    public void latestApprovedRefund_withoutPartiallyCapturedRefund() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        RefundTransactionDataHolder refundTransaction1 = getRefundTransaction();
        when(refundTransaction1.getTransactionIdentifier()).thenReturn("tx-id1");
        RefundTransactionDataHolder refundTransaction2 = getRefundTransaction();
        when(refundTransaction2.getTransactionIdentifier()).thenReturn("tx-id2");
        mockRefundTransactions.add(refundTransaction1);
        mockRefundTransactions.add(refundTransaction2);
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        RefundTransactionDataHolder latestRefund = victim.getLatestApprovedRefundTransaction();

        assertThat(latestRefund.getTransactionIdentifier()).isEqualTo("tx-id2");
    }

    @Test
    public void latestApprovedRefund_withApprovedAndDeclinedRefundTransactions() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        RefundTransactionDataHolder refundTransaction1 = getRefundTransaction();
        when(refundTransaction1.getTransactionIdentifier()).thenReturn("tx-id1");
        RefundTransactionDataHolder refundTransaction2 = getRefundTransaction();
        when(refundTransaction2.getTransactionIdentifier()).thenReturn("tx-id2");
        RefundTransactionDataHolder refundTransaction3 = getDeclinedRefundTransaction();
        when(refundTransaction3.getTransactionIdentifier()).thenReturn("tx-id3");
        mockRefundTransactions.add(refundTransaction1);
        mockRefundTransactions.add(refundTransaction2);
        mockRefundTransactions.add(refundTransaction3);
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        RefundTransactionDataHolder latestRefund = victim.getLatestApprovedRefundTransaction();

        assertThat(latestRefund.getTransactionIdentifier()).isEqualTo("tx-id2");
    }

    @Test
    public void latestApprovedRefund_withAllDeclinedRefundTransactions() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        RefundTransactionDataHolder refundTransaction1 = getDeclinedRefundTransaction();
        when(refundTransaction1.getTransactionIdentifier()).thenReturn("tx-id1");
        RefundTransactionDataHolder refundTransaction2 = getDeclinedRefundTransaction();
        when(refundTransaction2.getTransactionIdentifier()).thenReturn("tx-id2");
        RefundTransactionDataHolder refundTransaction3 = getDeclinedRefundTransaction();
        when(refundTransaction3.getTransactionIdentifier()).thenReturn("tx-id3");
        mockRefundTransactions.add(refundTransaction1);
        mockRefundTransactions.add(refundTransaction2);
        mockRefundTransactions.add(refundTransaction3);
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        RefundTransactionDataHolder latestRefund = victim.getLatestApprovedRefundTransaction();

        assertThat(latestRefund).isNull();
    }

    @Test
    public void latestApprovedRefund_withoutPartiallyCapturedAndDeclinedRefundTransactions() throws Exception {
        List<RefundTransactionDataHolder> mockRefundTransactions = new ArrayList<>();
        RefundTransactionDataHolder refundTransaction1 = getPartiallyCapturedRefundTransaction();
        when(refundTransaction1.getTransactionIdentifier()).thenReturn("tx-id1");
        RefundTransactionDataHolder refundTransaction2 = getDeclinedRefundTransaction();
        when(refundTransaction2.getTransactionIdentifier()).thenReturn("tx-id2");
        RefundTransactionDataHolder refundTransaction3 = getDeclinedRefundTransaction();
        when(refundTransaction3.getTransactionIdentifier()).thenReturn("tx-id3");
        mockRefundTransactions.add(refundTransaction1);
        mockRefundTransactions.add(refundTransaction2);
        mockRefundTransactions.add(refundTransaction3);
        when(mMockTransaction.getRefundTransactions()).thenReturn(mockRefundTransactions);

        TransactionHistoryHelper victim = new TransactionHistoryHelper(mMockTransaction);
        RefundTransactionDataHolder latestRefund = victim.getLatestApprovedRefundTransaction();

        assertThat(latestRefund).isNull();
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
