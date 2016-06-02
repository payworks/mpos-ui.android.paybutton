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
package io.mpos.ui;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.mpos.paymentdetails.PaymentDetailsScheme;
import io.mpos.ui.shared.util.UiHelper;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UiHelperTest {

    @Test
    public void drawableIdForScheme() {
        assertThat(UiHelper.getDrawableIdForCardScheme(PaymentDetailsScheme.VISA)).isEqualTo(R.drawable.mpu_visacard_image);
        assertThat(UiHelper.getDrawableIdForCardScheme(PaymentDetailsScheme.VISA_ELECTRON)).isEqualTo(R.drawable.mpu_visacard_image);
        assertThat(UiHelper.getDrawableIdForCardScheme(PaymentDetailsScheme.MAESTRO)).isEqualTo(R.drawable.mpu_maestro_image);
        assertThat(UiHelper.getDrawableIdForCardScheme(PaymentDetailsScheme.MASTERCARD)).isEqualTo(R.drawable.mpu_mastercard_image);
        assertThat(UiHelper.getDrawableIdForCardScheme(PaymentDetailsScheme.AMERICAN_EXPRESS)).isEqualTo(R.drawable.mpu_american_express_image);
        assertThat(UiHelper.getDrawableIdForCardScheme(PaymentDetailsScheme.DINERS)).isEqualTo(R.drawable.mpu_diners_image);
        assertThat(UiHelper.getDrawableIdForCardScheme(PaymentDetailsScheme.DISCOVER)).isEqualTo(R.drawable.mpu_discover_image);
        assertThat(UiHelper.getDrawableIdForCardScheme(PaymentDetailsScheme.JCB)).isEqualTo(R.drawable.mpu_jcb_image);
        assertThat(UiHelper.getDrawableIdForCardScheme(PaymentDetailsScheme.UNION_PAY)).isEqualTo(R.drawable.mpu_unionpay_image);
        assertThat(UiHelper.getDrawableIdForCardScheme(PaymentDetailsScheme.UNKNOWN)).isEqualTo(-1);
    }

    @Test
    public void formattedAccountNumber_lengthDivisibleByFour() {
        String accountNumber = "************1234";
        String victim = UiHelper.formatAccountNumber(accountNumber);
        assertThat(victim).isEqualTo("•••• •••• •••• 1234");
    }

    @Test
    public void formattedAccountNumber_lengthNotDivisibleByFour() {
        String accountNumber = "******1234";
        String victim = UiHelper.formatAccountNumber(accountNumber);
        assertThat(victim).isEqualTo("•• •••• 1234");
    }

    @Test
    public void formattedAccountNumber_lengthIsOdd() {
        String accountNumber = "*****1234";
        String victim = UiHelper.formatAccountNumber(accountNumber);
        assertThat(victim).isEqualTo("• •••• 1234");
    }

    @Test
    public void formattedAccountNumber_lessThanFourNumbers_evenInLength() {
        String accountNumber = "34";
        String victim = UiHelper.formatAccountNumber(accountNumber);
        assertThat(victim).isEqualTo("34");
    }

    @Test
    public void formattedAccountNumber_lessThanFourNumbers_oddInLength() {
        String accountNumber = "123";
        String victim = UiHelper.formatAccountNumber(accountNumber);
        assertThat(victim).isEqualTo("123");
    }

    @Test
    public void formattedAccountNumber_emptyString() {
        String accountNumber = "";
        String victim = UiHelper.formatAccountNumber(accountNumber);
        assertThat(victim).isEmpty();
    }

    @Test
    public void formattedAccountNumber_null() {
        String victim = UiHelper.formatAccountNumber(null);
        assertThat(victim).isEmpty();
    }

    @Test
    public void formattedAccountNumber_dashes() {
        String accountNumber = "123-4567-8123";
        String victim = UiHelper.formatAccountNumber(accountNumber);
        assertThat(victim).isEqualTo("123 4567 8123");
    }

    @Test
    public void formattedAccountNumber_oddlySpacedDashes() {
        String accountNumber = "123-567-123";
        String victim = UiHelper.formatAccountNumber(accountNumber);
        assertThat(victim).isEqualTo("1 2356 7123");
    }

    @Test
    public void formattedAccountNumber_dashAtTheEnd() {
        String accountNumber = "-5678-1234";
        String victim = UiHelper.formatAccountNumber(accountNumber);
        assertThat(victim).isEqualTo("5678 1234");
    }
}
