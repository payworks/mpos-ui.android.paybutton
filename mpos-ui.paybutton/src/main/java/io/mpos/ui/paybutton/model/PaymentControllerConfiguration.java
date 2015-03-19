/**
 * mpos-ui.paybutton : http://www.payworksmobile.com
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
 *
 */
package io.mpos.ui.paybutton.model;

import io.mpos.accessories.AccessoryFamily;

public class PaymentControllerConfiguration {

    public enum ReceiptMethod {
        READY_MADE,
        OWN_IMPLEMENTATION
    }

    public enum SignatureMethod {
        ON_SCREEN,
        ON_RECEIPT
    }

    private PaymentControllerAppearance mAppearance = new PaymentControllerAppearance();
    private ReceiptMethod mReceiptMethod = ReceiptMethod.OWN_IMPLEMENTATION;
    private SignatureMethod mSignatureMethod = SignatureMethod.ON_SCREEN;

    private AccessoryFamily mAccessoryFamily = AccessoryFamily.MOCK;

    public PaymentControllerConfiguration setAppearance(PaymentControllerAppearance appearance) {
        mAppearance = appearance;
        return this;
    }

    public PaymentControllerAppearance getAppearance(){
        return mAppearance;
    }

    public PaymentControllerConfiguration setSignatureMethod(SignatureMethod signatureMethod) {
        mSignatureMethod = signatureMethod;
        return this;
    }

    public SignatureMethod getSignatureMethod(){
        return mSignatureMethod;
    }

    public PaymentControllerConfiguration setReceiptMethod(ReceiptMethod receiptMethod) {
        mReceiptMethod = receiptMethod;
        return this;
    }

    public ReceiptMethod getReceiptMethod() {
        return mReceiptMethod;
    }

    public PaymentControllerConfiguration setAccessoryFamily(AccessoryFamily accessoryFamily) {
        mAccessoryFamily = accessoryFamily;
        return this;
    }

    public AccessoryFamily getAccessoryFamily() {
        return mAccessoryFamily;
    }
}
