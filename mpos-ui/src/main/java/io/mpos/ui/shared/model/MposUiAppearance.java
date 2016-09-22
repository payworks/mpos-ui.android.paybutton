/*
 * Copyright (C) 2016 Payworks GmbH (http://www.payworks.com)
 *
 * The MIT License (MIT)
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

import android.graphics.Color;

/**
 * Configuration holder for setting theme colors for MposUi.
 * Base theme used is {@code @android:style/Theme.Material.Light}
 */
public class MposUiAppearance {

    private int mColorPrimary = Color.parseColor("#0D2048");
    private int mColorPrimaryDark = Color.parseColor("#071025");
    private int mTextColorPrimary = Color.parseColor("#FFFFFF");
    private int mBackgroundColor = Color.parseColor("#EEEEEE");

    private int mApprovedBackgroundColor = Color.parseColor("#638D31");
    private int mDeclinedBackgroundColor = Color.parseColor("#B03B3B");
    private int mPreAuthorizedBackgroundColor = Color.parseColor("#DCA54C");
    private int mRefundedBackgroundColor = Color.parseColor("#3F6CA1");
    private int mApprovedTextColor = Color.parseColor("#FFFFFF");
    private int mDeclinedTextColor = Color.parseColor("#FFFFFF");
    private int mPreAuthorizedTextColor = Color.parseColor("#FFFFFF");
    private int mRefundedTextColor = Color.parseColor("#FFFFFF");


    /**
     * Get the primary color used by MposUi. See <a href="https://developer.android.com/training/material/images/ThemeColors.png">the Material theme</a>.
     *
     * @return The primary color
     */
    public int getColorPrimary() {
        return mColorPrimary;
    }

    /**
     * Set the primary color used by MposUi.
     *
     * @param colorPrimary The color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setColorPrimary(int colorPrimary) {
        mColorPrimary = colorPrimary;
        return this;
    }

    /**
     * Get the primary dark color used by MposUi. See <a href="https://developer.android.com/training/material/images/ThemeColors.png">the Material theme</a>.
     *
     * @return The primary dark color.
     */
    public int getColorPrimaryDark() {
        return mColorPrimaryDark;
    }

    /**
     * Set the primary dark color used by MposUi.
     *
     * @param colorPrimaryDark The color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setColorPrimaryDark(int colorPrimaryDark) {
        mColorPrimaryDark = colorPrimaryDark;
        return this;
    }

    /**
     * Get the primary text color used by MposUi. See <a href="https://developer.android.com/training/material/images/ThemeColors.png">the Material theme</a>.
     *
     * @return The primary text color.
     */
    public int getTextColorPrimary() {
        return mTextColorPrimary;
    }

    /**
     * Set the primary text color used by MposUi.
     *
     * @param textColorPrimary The color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setTextColorPrimary(int textColorPrimary) {
        mTextColorPrimary = textColorPrimary;
        return this;
    }

    /**
     * Get the window background color used by MposUi.
     *
     * @return The primary text color.
     */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    /**
     * Set the window background color used by MposUi.
     *
     * @param backgroundColor The color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        return this;
    }

    /**
     * Get the color for approved transactions in the summary screen.
     *
     * @return The color of the approved transactions.
     */
    public int getApprovedBackgroundColor() {
        return mApprovedBackgroundColor;
    }

    /**
     * Set the color for approved transactions in the summary screen.
     *
     * @param approvedBackgroundColor the color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setApprovedBackgroundColor(int approvedBackgroundColor) {
        mApprovedBackgroundColor = approvedBackgroundColor;
        return this;
    }

    /**
     * Get the color for declined transactions in the summary screen.
     *
     * @return The color of the declined transactions.
     */
    public int getDeclinedBackgroundColor() {
        return mDeclinedBackgroundColor;
    }

    /**
     * Set the color for declined transactions in the summary screen.
     *
     * @param declinedBackgroundColor the color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setDeclinedBackgroundColor(int declinedBackgroundColor) {
        mDeclinedBackgroundColor = declinedBackgroundColor;
        return this;
    }

    /**
     * Get the color for pre-authorized transactions in the summary screen.
     *
     * @return The color of the pre-authorized transactions.
     */
    public int getPreAuthorizedBackgroundColor() {
        return mPreAuthorizedBackgroundColor;
    }

    /**
     * Set the color for pre-authorized transactions in the summary screen.
     *
     * @param preAuthorizedBackgroundColor the color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setPreAuthorizedBackgroundColor(int preAuthorizedBackgroundColor) {
        mPreAuthorizedBackgroundColor = preAuthorizedBackgroundColor;
        return this;
    }

    /**
     * Get the color for refunded transactions in the summary screen.
     *
     * @return The color of the refunded transactions.
     */
    public int getRefundedBackgroundColor() {
        return mRefundedBackgroundColor;
    }

    /**
     * Set the color for refunded transactions in the summary screen.
     *
     * @param refundedBackgroundColor the color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setRefundedBackgroundColor(int refundedBackgroundColor) {
        mRefundedBackgroundColor = refundedBackgroundColor;
        return this;
    }

    /**
     * Get the color for the header text for Approved transactions in the summary screen.
     *
     * @return The color of the header text.
     */
    public int getApprovedTextColor() {
        return mApprovedTextColor;
    }

    /**
     * Set the color of the header text for Approved transactions in the summary screen.
     *
     * @param approvedTextColor the color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setApprovedTextColor(int approvedTextColor) {
        mApprovedTextColor = approvedTextColor;
        return this;
    }

    /**
     * Get the color for the header text for Declined transactions in the summary screen.
     *
     * @return The color of the header text.
     */
    public int getDeclinedTextColor() {
        return mDeclinedTextColor;
    }

    /**
     * Set the color of the header text for Declined transactions in the summary screen.
     *
     * @param declinedTextColor the color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setDeclinedTextColor(int declinedTextColor) {
        mDeclinedTextColor = declinedTextColor;
        return this;
    }

    /**
     * Get the color for the header text for PreAuthorized transactions in the summary screen.
     *
     * @return The color of the header text.
     */
    public int getPreAuthorizedTextColor() {
        return mPreAuthorizedTextColor;
    }

    /**
     * Set the color of the header text for PreAuthorized transactions in the summary screen.
     *
     * @param preAuthorizedTextColor the color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setPreAuthorizedTextColor(int preAuthorizedTextColor) {
        mPreAuthorizedTextColor = preAuthorizedTextColor;
        return this;
    }

    /**
     * Get the color for the header text for Refunded transactions in the summary screen.
     *
     * @return The color of the header text.
     */
    public int getRefundedTextColor() {
        return mRefundedTextColor;
    }

    /**
     * Set the color of the header text for Refunded transactions in the summary screen.
     *
     * @param refundedTextColor the color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setRefundedTextColor(int refundedTextColor) {
        mRefundedTextColor = refundedTextColor;
        return this;
    }
}
