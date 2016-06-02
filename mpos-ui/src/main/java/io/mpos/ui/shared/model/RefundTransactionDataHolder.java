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
package io.mpos.ui.shared.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

import io.mpos.transactions.Currency;
import io.mpos.transactions.RefundTransaction;
import io.mpos.transactions.RefundTransactionCode;
import io.mpos.transactions.TransactionStatus;
import io.mpos.transactions.TransactionType;

public class RefundTransactionDataHolder implements Parcelable {
    private BigDecimal mAmount;
    private Currency mCurrency;
    private long mCreatedTimestamp;
    private RefundTransactionCode mRefundTransactionCode;
    private TransactionType mTransactionType;
    private TransactionStatus mStatus;
    private String mTransactionIdentifier;

    public RefundTransactionDataHolder(RefundTransaction refundTransaction) {
        mAmount = refundTransaction.getAmount();
        mCurrency = (refundTransaction.getCurrency() != null) ? refundTransaction.getCurrency() : Currency.UNKNOWN;
        mCreatedTimestamp = refundTransaction.getCreatedTimestamp();
        mRefundTransactionCode = (refundTransaction.getCode() != null) ? refundTransaction.getCode() : RefundTransactionCode.UNKNOWN;
        mTransactionType = (refundTransaction.getType() != null) ? refundTransaction.getType() : TransactionType.UNKNOWN;
        mStatus = (refundTransaction.getStatus() != null) ? refundTransaction.getStatus() : TransactionStatus.UNKNOWN;
        mTransactionIdentifier = refundTransaction.getIdentifier();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.mAmount);
        dest.writeInt(this.mCurrency == null ? -1 : this.mCurrency.ordinal());
        dest.writeLong(this.mCreatedTimestamp);
        dest.writeInt(this.mRefundTransactionCode == null ? -1 : this.mRefundTransactionCode.ordinal());
        dest.writeInt(this.mTransactionType == null ? -1 : this.mTransactionType.ordinal());
        dest.writeInt(this.mStatus == null ? -1 : this.mStatus.ordinal());
        dest.writeString(this.mTransactionIdentifier);
    }

    public RefundTransactionDataHolder() {
    }

    protected RefundTransactionDataHolder(Parcel in) {
        this.mAmount = (BigDecimal) in.readSerializable();
        int tmpCurrency = in.readInt();
        this.mCurrency = (tmpCurrency == -1) ? Currency.UNKNOWN : Currency.values()[tmpCurrency];
        this.mCreatedTimestamp = in.readLong();
        int tmpRefundTransactionCode = in.readInt();
        this.mRefundTransactionCode = (tmpRefundTransactionCode == -1) ? RefundTransactionCode.UNKNOWN : RefundTransactionCode.values()[tmpRefundTransactionCode];
        int tmpTransactionType = in.readInt();
        this.mTransactionType = (tmpTransactionType == -1) ? TransactionType.UNKNOWN : TransactionType.values()[tmpTransactionType];
        int tmpStatus = in.readInt();
        this.mStatus = (tmpStatus == -1) ? TransactionStatus.UNKNOWN : TransactionStatus.values()[tmpStatus];
        this.mTransactionIdentifier = in.readString();
    }

    public static final Parcelable.Creator<RefundTransactionDataHolder> CREATOR = new Parcelable.Creator<RefundTransactionDataHolder>() {
        @Override
        public RefundTransactionDataHolder createFromParcel(Parcel source) {
            return new RefundTransactionDataHolder(source);
        }

        @Override
        public RefundTransactionDataHolder[] newArray(int size) {
            return new RefundTransactionDataHolder[size];
        }
    };

    public BigDecimal getAmount() {
        return mAmount;
    }

    public Currency getCurrency() {
        return mCurrency;
    }

    public long getCreatedTimestamp() {
        return mCreatedTimestamp;
    }

    public RefundTransactionCode getRefundTransactionCode() {
        return mRefundTransactionCode;
    }

    public TransactionType getTransactionType() {
        return mTransactionType;
    }

    public TransactionStatus getStatus() {
        return mStatus;
    }

    public String getTransactionIdentifier() {
        return mTransactionIdentifier;
    }
}