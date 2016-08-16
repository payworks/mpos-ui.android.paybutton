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
package io.mpos.ui.shared.util;

public enum UiState {
    /**
     * Initial idle state
     */
    IDLE,

    /**
     * Transaction is ongoing
     */
    TRANSACTION_ONGOING,

    /**
     * Transaction is waiting for signature
     */
    TRANSACTION_WAITING_SIGNATURE,

    /**
     * Transaction is waiting for application selection
     */
    TRANSACTION_WAITING_APPLICATION_SELECTION,

    /**
     * Error in transaction process
     */
    TRANSACTION_ERROR,

    /**
     * Loading summary from the backend
     */
    SUMMARY_LOADING,
    /**
     * Displaying summary
     */
    SUMMARY_DISPLAYING,

    /**
     * Error displaying summary
     */
    SUMMARY_ERROR,

    /**
     * Sending receipt via Email
     */
    RECEIPT_SENDING,

    /**
     * Printing receipt on printer
     */
    RECEIPT_PRINTING,

    /**
     * Error printing receipt
     */
    RECEIPT_PRINTING_ERROR,

    /**
     * Login screen is displayed
     */
    LOGIN_DISPLAYING,

    /**
     * Forgot password screen is displayed
     */
    FORGOT_PASSWORD_DISPLAYING,

    /**
     * Settings screen is displayed
     */
    SETTINGS_DISPLAYING

}
