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
package io.ui;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import org.hamcrest.CoreMatchers;

import io.mpos.ui.R;
import io.mpos.ui.paybutton.view.SignatureActivity;


@LargeTest
public class SignatureActivityTest extends ActivityInstrumentationTestCase2<SignatureActivity> {

    public SignatureActivityTest() {
        super(SignatureActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Intent intent = new Intent();
        intent.putExtra(SignatureActivity.BUNDLE_KEY_AMOUNT, "12.34$");
        setActivityIntent(intent);
        getActivity();
    }

    public void testRequiredButtonsPresent() {
        Espresso.onView(ViewMatchers.withId(R.id.clear_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.abort_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.continue_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    public void testAmountViewShowsCorrectAmount() {
        Espresso.onView(ViewMatchers.withId(R.id.amount_view))
                .check(ViewAssertions.matches(ViewMatchers.withText("12.34$")));
    }

    public void testToolbarInvisible() {
            Espresso.onView(ViewMatchers.withId(R.id.toolbar))
                    .check(ViewAssertions.doesNotExist());
    }

    public void testBackKeyShowsToast() {
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withText(R.string.back_button_hint)).inRoot(RootMatchers.withDecorView(CoreMatchers.not(CoreMatchers.is(getActivity().getWindow().getDecorView())))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
