/*
 * mpos-ui : http://www.payworksmobile.com
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

import io.mpos.transactions.Transaction;

/**
 * Created by Abhijith Srivatsav<abhijith.srivatsav@payworksmobile.com> on 18/06/15.
 */
public class TransactionDataHolder implements Parcelable {

    private String mTransactionIdentifier;
    private String mReferencedTransactionIdentifier;
    private String mTransactionStatus;
    private String mCurrency;
    private String mSubject;
    private String mAmount;
    private String mTransactionType;
    private String mPaymentDetailsScheme;
    private String mPaymentDetailsSource;
    private String mMaskedAccountNumber;
    private String mRefundDetailsStatus;
    private String mErrorType;
    private long mCreatedTimestamp;


    public TransactionDataHolder() {
    }

    public static TransactionDataHolder createTransactionDataHolder(Transaction transaction){
        TransactionDataHolder holder = new TransactionDataHolder();
        holder.setTransactionIdentifier(transaction.getIdentifier());
        holder.setReferencedTransactionIdentifier(transaction.getReferencedTransactionIdentifier());
        holder.setTransactionStatus(transaction.getStatus().name());
        holder.setCurrency(transaction.getCurrency().name());
        holder.setSubject(transaction.getSubject());
        holder.setAmount(transaction.getAmount().toString());
        holder.setTransactionType(transaction.getType().name());

        if (transaction.getPaymentDetails() != null) {
            if (transaction.getPaymentDetails().getScheme() != null) {
                holder.setPaymentDetailsScheme(transaction.getPaymentDetails().getScheme().name());
            }
            if (transaction.getPaymentDetails().getSource() != null) {
                holder.setPaymentDetailsSource(transaction.getPaymentDetails().getSource().name());
            }
            holder.setMaskedAccountNumber(transaction.getPaymentDetails().getMaskedAccountNumber());
        }

        if (transaction.getRefundDetails() != null && transaction.getRefundDetails().getStatus() != null) {
            holder.setRefundDetailsStatus(transaction.getRefundDetails().getStatus().name());
        }

        holder.setErrorType(transaction.getError().getErrorType().name());
        holder.setCreatedTimestamp(transaction.getCreatedTimestamp());

        return holder;
    }

    public String getTransactionIdentifier() {
        return mTransactionIdentifier;
    }

    public void setTransactionIdentifier(String transactionIdentifier) {
        mTransactionIdentifier = transactionIdentifier;
    }

    public String getReferencedTransactionIdentifier() {
        return mReferencedTransactionIdentifier;
    }

    public void setReferencedTransactionIdentifier(String referencedTransactionIdentifier) {
        mReferencedTransactionIdentifier = referencedTransactionIdentifier;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public void setCurrency(String currency) {
        mCurrency = currency;
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        mSubject = subject;
    }

    public String getTransactionType() {
        return mTransactionType;
    }

    public void setTransactionType(String transactionType) {
        mTransactionType = transactionType;
    }

    public String getPaymentDetailsScheme() {
        return mPaymentDetailsScheme;
    }

    public void setPaymentDetailsScheme(String paymentDetailsScheme) {
        mPaymentDetailsScheme = paymentDetailsScheme;
    }

    public String getPaymentDetailsSource() {
        return mPaymentDetailsSource;
    }

    public void setPaymentDetailsSource(String paymentDetailsSource) {
        mPaymentDetailsSource = paymentDetailsSource;
    }

    public String getMaskedAccountNumber() {
        return mMaskedAccountNumber;
    }

    public void setMaskedAccountNumber(String maskedAccountNumber) {
        mMaskedAccountNumber = maskedAccountNumber;
    }

    public String getRefundDetailsStatus() {
        return mRefundDetailsStatus;
    }

    public void setRefundDetailsStatus(String refundDetailsStatus) {
        mRefundDetailsStatus = refundDetailsStatus;
    }

    public String getErrorType() {
        return mErrorType;
    }

    public void setErrorType(String errorType) {
        mErrorType = errorType;
    }

    public long getCreatedTimestamp() {
        return mCreatedTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        mCreatedTimestamp = createdTimestamp;
    }

    public String getAmount() {
        return mAmount;
    }

    public void setAmount(String amount) {
        mAmount = amount;
    }

    public String getTransactionStatus() {
        return mTransactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        mTransactionStatus = transactionStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected TransactionDataHolder(String transactionIdentifier, String referencedTransactionIdentifier, String transactionStatus, String currency, String subject, String amount, String transactionType, String paymentDetailsScheme, String paymentDetailsSource, String customerVerification, String maskedAccountNumber, String refundDetailsStatus, String errorType, long createdTimestamp) {
        mTransactionIdentifier = transactionIdentifier;
        mReferencedTransactionIdentifier = referencedTransactionIdentifier;
        mTransactionStatus = transactionStatus;
        mCurrency = currency;
        mSubject = subject;
        mAmount = amount;
        mTransactionType = transactionType;
        mPaymentDetailsScheme = paymentDetailsScheme;
        mPaymentDetailsSource = paymentDetailsSource;
        mMaskedAccountNumber = maskedAccountNumber;
        mRefundDetailsStatus = refundDetailsStatus;
        mErrorType = errorType;
        mCreatedTimestamp = createdTimestamp;
    }

    protected TransactionDataHolder(Parcel in) {
        mTransactionIdentifier = in.readString();
        mReferencedTransactionIdentifier = in.readString();
        mTransactionStatus = in.readString();
        mCurrency = in.readString();
        mSubject = in.readString();
        mAmount = in.readString();
        mTransactionType = in.readString();
        mPaymentDetailsScheme = in.readString();
        mPaymentDetailsSource = in.readString();
        mMaskedAccountNumber = in.readString();
        mRefundDetailsStatus = in.readString();
        mErrorType = in.readString();
        mCreatedTimestamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTransactionIdentifier);
        dest.writeString(mReferencedTransactionIdentifier);
        dest.writeString(mTransactionStatus);
        dest.writeString(mCurrency);
        dest.writeString(mSubject);
        dest.writeString(mAmount);
        dest.writeString(mTransactionType);
        dest.writeString(mPaymentDetailsScheme);
        dest.writeString(mPaymentDetailsSource);
        dest.writeString(mMaskedAccountNumber);
        dest.writeString(mRefundDetailsStatus);
        dest.writeString(mErrorType);
        dest.writeLong(mCreatedTimestamp);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TransactionDataHolder> CREATOR = new Parcelable.Creator<TransactionDataHolder>() {
        @Override
        public TransactionDataHolder createFromParcel(Parcel in) {
            return new TransactionDataHolder(in);
        }

        @Override
        public TransactionDataHolder[] newArray(int size) {
            return new TransactionDataHolder[size];
        }
    };

}
