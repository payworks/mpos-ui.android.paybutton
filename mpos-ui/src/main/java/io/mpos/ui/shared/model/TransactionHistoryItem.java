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

import android.content.Context;
import android.support.annotation.StringRes;
import android.text.format.DateUtils;

import java.math.BigDecimal;

import io.mpos.transactions.Currency;
import io.mpos.ui.shared.util.UiHelper;

public class TransactionHistoryItem {

    public enum Type {
        CHARGE,
        PREAUTHORIZED,
        REFUND,
        PARTIAL_CAPTURE
    }

    private Type mType;

    private String mStatusText;
    private String mAmountText;
    private String mTimestampText;
    private String mPartialCaptureHintText;

    private TransactionHistoryItem() {
    }

    public static TransactionHistoryItem createPartialCaptureItem(Context context, @StringRes int statusStringResource, BigDecimal amount, Currency currency, long timestamp, BigDecimal reservedAmount, Currency reservedCurrency) {
        TransactionHistoryItem item = TransactionHistoryItem.createItem(context, Type.PARTIAL_CAPTURE, statusStringResource, amount, currency, timestamp);
        item.setPartialCaptureHintText(UiHelper.getPartialCaptureHintText(context, reservedAmount, reservedCurrency));
        return item;
    }

    public static TransactionHistoryItem createRefundItem(Context context, @StringRes int statusStringResource, BigDecimal amount, Currency currency, long timestamp) {
        TransactionHistoryItem item = TransactionHistoryItem.createItem(context, Type.REFUND, statusStringResource, amount, currency, timestamp);
        item.setAmountText("-" + item.getAmountText());
        return item;
    }

    public static TransactionHistoryItem createItem(Context context, Type type, @StringRes int statusStringResource, BigDecimal amount, Currency currency, long timestamp) {
        TransactionHistoryItem item = new TransactionHistoryItem();
        item.setType(type);
        item.setStatusText(context.getString(statusStringResource));
        item.setTimestampText(DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
        item.setAmountText(UiHelper.formatAmountWithSymbol(currency, amount));
        return item;
    }

    public Type getType() {
        return mType;
    }

    private void setType(Type type) {
        mType = type;
    }

    public String getStatusText() {
        return mStatusText;
    }

    private void setStatusText(String statusText) {
        mStatusText = statusText;
    }

    public String getAmountText() {
        return mAmountText;
    }

    private void setAmountText(String amountText) {
        mAmountText = amountText;
    }

    public String getTimestampText() {
        return mTimestampText;
    }

    private void setTimestampText(String timestampText) {
        mTimestampText = timestampText;
    }

    public String getPartialCaptureHintText() {
        return mPartialCaptureHintText;
    }

    private void setPartialCaptureHintText(String partialCaptureHintText) {
        mPartialCaptureHintText = partialCaptureHintText;
    }
}
