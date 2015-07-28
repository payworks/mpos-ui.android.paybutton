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
package io.unit;

import android.test.ActivityUnitTestCase;

import io.mpos.paymentdetails.PaymentDetailsScheme;
import io.mpos.ui.R;
import io.mpos.ui.paybutton.view.TransactionActivity;
import io.mpos.ui.shared.util.UiHelper;


public class UiHelperTest extends ActivityUnitTestCase<TransactionActivity> {


    public UiHelperTest() {
        super(TransactionActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetDrawableIdImageForCreditCard() {
        assertEquals(R.drawable.mpu_visacard_image, UiHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.VISA));
        assertEquals(R.drawable.mpu_visacard_image, UiHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.VISA_ELECTRON));
        assertEquals(R.drawable.mpu_maestro_image, UiHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.MAESTRO));
        assertEquals(R.drawable.mpu_mastercard_image, UiHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.MASTERCARD));
        assertEquals(R.drawable.mpu_american_express_image, UiHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.AMERICAN_EXPRESS));
        assertEquals(-1, UiHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.DINERS));
        assertEquals(-1, UiHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.DISCOVER));
        assertEquals(-1, UiHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.JCB));
        assertEquals(-1, UiHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.UNION_PAY));
        assertEquals(-1, UiHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.UNKNOWN));
    }
}
