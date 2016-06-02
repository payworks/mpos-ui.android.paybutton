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
package io.mpos.ui.shared.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.mpos.errors.ErrorType;
import io.mpos.errors.MposError;
import io.mpos.paymentdetails.PaymentDetails;
import io.mpos.paymentdetails.PaymentDetailsScheme;
import io.mpos.paymentdetails.PaymentDetailsSource;
import io.mpos.transactions.Currency;
import io.mpos.transactions.RefundDetails;
import io.mpos.transactions.RefundDetailsStatus;
import io.mpos.transactions.RefundTransaction;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.TransactionStatus;
import io.mpos.transactions.TransactionType;

public class TransactionDataHolder implements Parcelable {

    private String mTransactionIdentifier;
    private String mReferencedTransactionIdentifier;
    private TransactionStatus mTransactionStatus;
    private Currency mCurrency;
    private String mSubject;
    private BigDecimal mAmount;
    private TransactionType mTransactionType;
    private PaymentDetailsScheme mPaymentDetailsScheme;
    private PaymentDetailsSource mPaymentDetailsSource;
    private String mMaskedAccountNumber;
    private RefundDetailsStatus mRefundDetailsStatus;
    private ErrorType mErrorType;
    private long mCreatedTimestamp;
    private boolean mCaptured;
    private List<RefundTransactionDataHolder> mRefundTransactions;

    public TransactionDataHolder(Transaction transaction) {
        mTransactionIdentifier = transaction.getIdentifier();
        mReferencedTransactionIdentifier = transaction.getReferencedTransactionIdentifier();
        mTransactionStatus = (transaction.getStatus() != null) ? transaction.getStatus() : TransactionStatus.UNKNOWN;
        mCurrency = (transaction.getCurrency() != null) ? transaction.getCurrency() : Currency.UNKNOWN;
        mSubject = transaction.getSubject();
        mAmount = transaction.getAmount();
        mTransactionType = transaction.getType() != null ? transaction.getType() : TransactionType.UNKNOWN;
        PaymentDetails tmpPaymentDetails = transaction.getPaymentDetails();
        if (tmpPaymentDetails != null) {
            mPaymentDetailsScheme = (tmpPaymentDetails.getScheme() != null) ? tmpPaymentDetails.getScheme() : PaymentDetailsScheme.UNKNOWN;
            mPaymentDetailsSource = (tmpPaymentDetails.getSource() != null) ? tmpPaymentDetails.getSource() : PaymentDetailsSource.UNKNOWN;
            mMaskedAccountNumber = transaction.getPaymentDetails().getMaskedAccountNumber();
        }

        RefundDetails tmpRefundDetails = transaction.getRefundDetails();
        if (tmpRefundDetails != null) {
            if (tmpRefundDetails.getRefundTransactions() != null && tmpRefundDetails.getRefundTransactions().size() > 0) {
                List<RefundTransactionDataHolder> refundTransactions = new ArrayList<>();
                for (RefundTransaction refundTransaction : transaction.getRefundDetails().getRefundTransactions()) {
                    RefundTransactionDataHolder refundHolder = new RefundTransactionDataHolder(refundTransaction);
                    refundTransactions.add(refundHolder);
                }
                mRefundTransactions = refundTransactions;
            }
            mRefundDetailsStatus = (tmpRefundDetails.getStatus() != null) ? tmpRefundDetails.getStatus() : RefundDetailsStatus.UNKNOWN;
        }

        mCaptured = transaction.isCaptured();
        MposError tmpError = transaction.getError();
        if (tmpError != null) {
            mErrorType = (tmpError.getErrorType() != null) ? tmpError.getErrorType() : ErrorType.UNKNOWN;
        }
        mCreatedTimestamp = transaction.getCreatedTimestamp();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTransactionIdentifier);
        dest.writeString(this.mReferencedTransactionIdentifier);
        dest.writeInt(this.mTransactionStatus == null ? -1 : this.mTransactionStatus.ordinal());
        dest.writeInt(this.mCurrency == null ? -1 : this.mCurrency.ordinal());
        dest.writeString(this.mSubject);
        dest.writeSerializable(this.mAmount);
        dest.writeInt(this.mTransactionType == null ? -1 : this.mTransactionType.ordinal());
        dest.writeInt(this.mPaymentDetailsScheme == null ? -1 : this.mPaymentDetailsScheme.ordinal());
        dest.writeInt(this.mPaymentDetailsSource == null ? -1 : this.mPaymentDetailsSource.ordinal());
        dest.writeString(this.mMaskedAccountNumber);
        dest.writeInt(this.mRefundDetailsStatus == null ? -1 : this.mRefundDetailsStatus.ordinal());
        dest.writeInt(this.mErrorType == null ? -1 : this.mErrorType.ordinal());
        dest.writeLong(this.mCreatedTimestamp);
        dest.writeByte(mCaptured ? (byte) 1 : (byte) 0);
        dest.writeTypedList(mRefundTransactions);
    }

    public TransactionDataHolder() {
    }

    protected TransactionDataHolder(Parcel in) {
        this.mTransactionIdentifier = in.readString();
        this.mReferencedTransactionIdentifier = in.readString();
        int tmpTransactionStatus = in.readInt();
        this.mTransactionStatus = (tmpTransactionStatus == -1) ? TransactionStatus.UNKNOWN : TransactionStatus.values()[tmpTransactionStatus];
        int tmpCurrency = in.readInt();
        this.mCurrency = (tmpCurrency == -1) ? Currency.UNKNOWN : Currency.values()[tmpCurrency];
        this.mSubject = in.readString();
        this.mAmount = (BigDecimal) in.readSerializable();
        int tmpTransactionType = in.readInt();
        this.mTransactionType = (tmpTransactionType == -1) ? TransactionType.UNKNOWN : TransactionType.values()[tmpTransactionType];
        int tmpPaymentDetailsScheme = in.readInt();
        this.mPaymentDetailsScheme = (tmpPaymentDetailsScheme == -1) ? PaymentDetailsScheme.UNKNOWN : PaymentDetailsScheme.values()[tmpPaymentDetailsScheme];
        int tmpPaymentDetailsSource = in.readInt();
        this.mPaymentDetailsSource = (tmpPaymentDetailsSource == -1) ? PaymentDetailsSource.UNKNOWN : PaymentDetailsSource.values()[tmpPaymentDetailsSource];
        this.mMaskedAccountNumber = in.readString();
        int tmpRefundDetailsStatus = in.readInt();
        this.mRefundDetailsStatus = (tmpRefundDetailsStatus == -1) ? RefundDetailsStatus.UNKNOWN : RefundDetailsStatus.values()[tmpRefundDetailsStatus];
        int tmpErrorType = in.readInt();
        this.mErrorType = (tmpErrorType == -1) ? ErrorType.UNKNOWN : ErrorType.values()[tmpErrorType];
        this.mCreatedTimestamp = in.readLong();
        this.mCaptured = in.readByte() != 0;
        this.mRefundTransactions = in.createTypedArrayList(RefundTransactionDataHolder.CREATOR);
    }

    public static final Parcelable.Creator<TransactionDataHolder> CREATOR = new Parcelable.Creator<TransactionDataHolder>() {
        @Override
        public TransactionDataHolder createFromParcel(Parcel source) {
            return new TransactionDataHolder(source);
        }

        @Override
        public TransactionDataHolder[] newArray(int size) {
            return new TransactionDataHolder[size];
        }
    };

    public String getTransactionIdentifier() {
        return mTransactionIdentifier;
    }

    public String getReferencedTransactionIdentifier() {
        return mReferencedTransactionIdentifier;
    }

    public TransactionStatus getTransactionStatus() {
        return mTransactionStatus;
    }

    public Currency getCurrency() {
        return mCurrency;
    }

    public String getSubject() {
        return mSubject;
    }

    public BigDecimal getAmount() {
        return mAmount;
    }

    public TransactionType getTransactionType() {
        return mTransactionType;
    }

    public PaymentDetailsScheme getPaymentDetailsScheme() {
        return mPaymentDetailsScheme;
    }

    public PaymentDetailsSource getPaymentDetailsSource() {
        return mPaymentDetailsSource;
    }

    public String getMaskedAccountNumber() {
        return mMaskedAccountNumber;
    }

    public RefundDetailsStatus getRefundDetailsStatus() {
        return mRefundDetailsStatus;
    }

    public ErrorType getErrorType() {
        return mErrorType;
    }

    public long getCreatedTimestamp() {
        return mCreatedTimestamp;
    }

    public boolean isCaptured() {
        return mCaptured;
    }

    public List<RefundTransactionDataHolder> getRefundTransactions() {
        return mRefundTransactions;
    }
}
