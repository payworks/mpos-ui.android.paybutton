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
package io.unit;

import android.test.ActivityUnitTestCase;

import io.mpos.paymentdetails.PaymentDetailsScheme;
import io.mpos.ui.paybutton.R;
import io.mpos.ui.paybutton.util.UIHelper;
import io.mpos.ui.paybutton.view.PaymentActivity;


public class UIHelperTest extends ActivityUnitTestCase<PaymentActivity> {


    public UIHelperTest() {
        super(PaymentActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetDrawableIdImageForCreditCard() {
        assertEquals(R.drawable.visacard_image, UIHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.VISA));
        assertEquals(R.drawable.visacard_image, UIHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.VISA_ELECTRON));
        assertEquals(R.drawable.maestro_image, UIHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.MAESTRO));
        assertEquals(R.drawable.mastercard_image, UIHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.MASTERCARD));
        assertEquals(-1, UIHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.AMERICAN_EXPRESS));
        assertEquals(-1, UIHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.DINERS));
        assertEquals(-1, UIHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.DISCOVER));
        assertEquals(-1, UIHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.JCB));
        assertEquals(-1, UIHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.UNION_PAY));
        assertEquals(-1, UIHelper.getDrawableIdImageForCreditCard(PaymentDetailsScheme.UNKNOWN));
    }
}
