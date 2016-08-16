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

import java.util.EnumSet;

import io.mpos.accessories.parameters.AccessoryParameters;

/**
 * Configuration class used for tweaking appearance and features.
 */
public class MposUiConfiguration {

    public static final int RESULT_DISPLAY_BEHAVIOUR_TIMEOUT = 5000; //ms

    /**
     * Methods of capturing a signature from the customer (if needed).
     */
    public enum SignatureCapture {
        /**
         * Have customer sign on the screen of the device.
         */
        ON_SCREEN,

        /**
         * Have customer sign elsewhere, e.g. on printed receipt.
         */
        ON_RECEIPT
    }

    public enum ResultDisplayBehavior {

        DISPLAY_INDEFINITELY,

        CLOSE_AFTER_TIMEOUT
    }

    /**
     * Features which can be enabled or disabled on the transaction summary screen.
     */
    public enum SummaryFeature {
        /**
         * Send receipt to the customer via email.
         */
        SEND_RECEIPT_VIA_EMAIL,

        /**
         * Print receipt using a hardware printer.
         */
        PRINT_RECEIPT,

        /**
         * Refund transaction right away.
         */
        REFUND_TRANSACTION,

        /**
         * Capture the transaction.
         */
        CAPTURE_TRANSACTION
    }


    private MposUiAppearance mAppearance = new MposUiAppearance();
    private EnumSet<SummaryFeature> mSummaryFeatures = EnumSet.noneOf(SummaryFeature.class);
    private SignatureCapture mSignatureCapture = SignatureCapture.ON_SCREEN;

    private AccessoryParameters mTerminalParameters = null;
    private AccessoryParameters mPrinterParameters = null;
    private String mApplicationIdentifier;

    private ResultDisplayBehavior mDisplayResultBehavior = ResultDisplayBehavior.DISPLAY_INDEFINITELY;

    /**
     * Set appearance of the MposUi.
     *
     * @param appearance Object carrying appearance details.
     * @return Self, to allow chaining of calls.
     */
    public MposUiConfiguration setAppearance(MposUiAppearance appearance) {
        mAppearance = appearance;
        return this;
    }

    /**
     * Get the appearance of the MposUi.
     *
     * @return Object carrying appearance details
     */
    public MposUiAppearance getAppearance() {
        return mAppearance;
    }

    /**
     * Set signature capture method used in the MposUi
     *
     * @param signatureCapture Enum value representing the capture method.
     * @return Self, to allow chaining of calls.
     */
    public MposUiConfiguration setSignatureCapture(SignatureCapture signatureCapture) {
        mSignatureCapture = signatureCapture;
        return this;
    }

    /**
     * Get the signature capture method used in the MposUi.
     *
     * @return Enum value representing the capture method.
     */
    public SignatureCapture getSignatureCapture() {
        return mSignatureCapture;
    }

    /**
     * Set the features which will be enabled on the summary screen.
     *
     * @param summaryFeatures Enum set of values representing enabled features.
     * @return Self, to allow chaining of calls.
     */
    public MposUiConfiguration setSummaryFeatures(EnumSet<SummaryFeature> summaryFeatures) {
        mSummaryFeatures = summaryFeatures;
        return this;
    }

    /**
     * Get the features which will be enabled on the summary screen.
     *
     * @return Enum set of values representing Enum set of values representing wanted features features
     */
    public EnumSet<SummaryFeature> getSummaryFeatures() {
        return mSummaryFeatures;
    }

    /**
     * Set the accessory parameters which will be used for transactions.
     *
     * @param accessoryParameters AccessoryParameters for the transaction.
     * @return Self, to allow chaining of calls.
     */
    public MposUiConfiguration setTerminalParameters(AccessoryParameters accessoryParameters) {
        mTerminalParameters = accessoryParameters;
        return this;
    }

    /**
     * Get the accessory parameters which will be used for transactions.
     *
     * @return Enum value representing the accessory family.
     */
    public AccessoryParameters getTerminalParameters() {
        return mTerminalParameters;
    }

    /**
     * Set the accessory parameters which will be used for printing.
     *
     * @param printerAccessoryParameters AccessoryParameters for printing.
     * @return Self, to allow chaining of calls.
     */
    public MposUiConfiguration setPrinterParameters(AccessoryParameters printerAccessoryParameters) {
        mPrinterParameters = printerAccessoryParameters;
        return this;
    }

    /**
     * Get the accessory parameters which will be used for printing.
     *
     * @return Enum value representing the accessory family.
     */
    public AccessoryParameters getPrinterParameters() {
        return mPrinterParameters;
    }

    /**
     * Get the behavior of displaying the result
     *
     * @return Enum value representing the display result behavior
     */
    public ResultDisplayBehavior getDisplayResultBehavior() {
        return mDisplayResultBehavior;
    }

    /**
     * Set the behaviour for displaying the result.
     *
     * @param displayResultBehavior Enum value representing the display result behavior
     */
    public void setDisplayResultBehavior(ResultDisplayBehavior displayResultBehavior) {
        mDisplayResultBehavior = displayResultBehavior;
    }
}
