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
package io.ui;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import org.hamcrest.CoreMatchers;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.provider.ProviderMode;
import io.mpos.ui.R;
import io.mpos.ui.paybutton.view.SignatureActivity;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.model.MposUiConfiguration;


@LargeTest
public class SignatureActivityTest extends ActivityInstrumentationTestCase2<SignatureActivity> {

    public SignatureActivityTest() {
        super(SignatureActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MposUi controller = MposUi.initialize(getInstrumentation().getContext(), ProviderMode.MOCK, "mock", "mock");
        MposUiConfiguration config = controller.getConfiguration();
        config.setAccessoryFamily(AccessoryFamily.MOCK);

        Intent intent = new Intent();
        intent.putExtra(SignatureActivity.BUNDLE_KEY_AMOUNT, "12.34$");
        setActivityIntent(intent);
        getActivity();
    }

    public void testRequiredButtonsPresent() {
        Espresso.onView(ViewMatchers.withId(R.id.mpu_clear_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.mpu_abort_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.mpu_continue_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    public void testAmountViewShowsCorrectAmount() {
        Espresso.onView(ViewMatchers.withId(R.id.mpu_amount_view))
                .check(ViewAssertions.matches(ViewMatchers.withText("12.34$")));
    }

    public void testToolbarInvisible() {
            Espresso.onView(ViewMatchers.withId(R.id.mpu_toolbar))
                    .check(ViewAssertions.doesNotExist());
    }

    public void testBackKeyShowsToast() {
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withText(R.string.MPUBackButtonDisabled)).inRoot(RootMatchers.withDecorView(CoreMatchers.not(CoreMatchers.is(getActivity().getWindow().getDecorView())))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
