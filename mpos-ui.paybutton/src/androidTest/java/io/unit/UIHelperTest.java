/**
 * PAYWORKS GMBH ("COMPANY") CONFIDENTIAL
 * Copyright (c) 2015 payworks GmbH, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of COMPANY. The intellectual and technical concepts contained
 * herein are proprietary to COMPANY and may be covered by European or foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from COMPANY.  Access to the source code contained herein is hereby forbidden to anyone except current COMPANY employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of COMPANY.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF COMPANY IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package io.unit;

import android.test.ActivityUnitTestCase;

import io.mpos.paymentdetails.PaymentDetailsScheme;
import io.mpos.ui.R;
import io.mpos.ui.paybutton.util.UIHelper;
import io.mpos.ui.paybutton.view.PaymentActivity;

/**
 * Created by abhijith on 22/01/15.
 */
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
