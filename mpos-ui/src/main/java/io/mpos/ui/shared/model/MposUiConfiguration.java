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

import io.mpos.accessories.AccessoryFamily;

public class MposUiConfiguration {

    public enum ReceiptMethod {
        READY_MADE,
        OWN_IMPLEMENTATION
    }

    public enum SignatureMethod {
        ON_SCREEN,
        ON_RECEIPT
    }

    private MposUiAppearance mAppearance = new MposUiAppearance();
    private ReceiptMethod mReceiptMethod = ReceiptMethod.READY_MADE;
    private SignatureMethod mSignatureMethod = SignatureMethod.ON_SCREEN;

    private AccessoryFamily mAccessoryFamily = AccessoryFamily.MOCK;

    public MposUiConfiguration setAppearance(MposUiAppearance appearance) {
        mAppearance = appearance;
        return this;
    }

    public MposUiAppearance getAppearance(){
        return mAppearance;
    }

    public MposUiConfiguration setSignatureMethod(SignatureMethod signatureMethod) {
        mSignatureMethod = signatureMethod;
        return this;
    }

    public SignatureMethod getSignatureMethod(){
        return mSignatureMethod;
    }

    public MposUiConfiguration setReceiptMethod(ReceiptMethod receiptMethod) {
        mReceiptMethod = receiptMethod;
        return this;
    }

    public ReceiptMethod getReceiptMethod() {
        return mReceiptMethod;
    }

    public MposUiConfiguration setAccessoryFamily(AccessoryFamily accessoryFamily) {
        mAccessoryFamily = accessoryFamily;
        return this;
    }

    public AccessoryFamily getAccessoryFamily() {
        return mAccessoryFamily;
    }
}
