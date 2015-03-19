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
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;

import java.math.BigDecimal;
import java.util.List;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.provider.ProviderMode;
import io.mpos.transactions.Currency;
import io.mpos.ui.R;
import io.mpos.ui.paybutton.controller.PaymentController;
import io.mpos.ui.paybutton.model.PaymentControllerConfiguration;
import io.mpos.ui.paybutton.view.PaymentActivity;

@LargeTest
public class PaymentActivityTest extends ActivityInstrumentationTestCase2<PaymentActivity> {

    public PaymentActivityTest() {
        super(PaymentActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        List<IdlingResource> idlingResources = Espresso.getIdlingResources();
        for(IdlingResource resource : idlingResources) {
            Espresso.unregisterIdlingResources(resource);
        }
    }

    /*
    public void testToolbarIsVisible() {
        initWithAmount(99.00);
        Espresso.registerIdlingResources(new TransactionProviderControllerIdlingResource());
        Espresso.onView(ViewMatchers.withId(R.id.toolbar))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

    }
    */

    public void testErrorFragmentIsVisibleInErrorState() {
        initWithAmount(115.00);
        getActivity();
        TransactionProviderControllerIdlingResource idlingResource = new TransactionProviderControllerIdlingResource();
        Espresso.registerIdlingResources(idlingResource);

        Espresso.onView(ViewMatchers.withId(R.id.payment_fragment_error))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.fa_times_circle))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.unregisterIdlingResources(idlingResource);
    }

    public void testBackKeyShowsToast() {
        initWithAmount(106);
        TransactionProviderControllerIdlingResource idlingResource = new TransactionProviderControllerIdlingResource();
        Espresso.registerIdlingResources(idlingResource);
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withText(R.string.back_button_hint)).inRoot(RootMatchers.withDecorView(CoreMatchers.not(CoreMatchers.is(getActivity().getWindow().getDecorView())))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.unregisterIdlingResources(idlingResource);
    }

    public void testNoReceiptButtonOnSummaryPage() {
        initWithAmount(106);
        PaymentController.getInitializedInstance().getConfiguration().setReceiptMethod(PaymentControllerConfiguration.ReceiptMethod.OWN_IMPLEMENTATION);
        TransactionProviderControllerIdlingResource idlingResource = new TransactionProviderControllerIdlingResource();
        Espresso.registerIdlingResources(idlingResource);
        Espresso.onView(ViewMatchers.withId(R.id.summary_action_button))
                .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())));
        Espresso.unregisterIdlingResources(idlingResource);
    }

    public void testApplicationSelection() {
        initWithAmount(113.73);
        TransactionProviderControllerIdlingResource idlingResource = new TransactionProviderControllerIdlingResource(false, true);
        Espresso.registerIdlingResources(idlingResource);
        Espresso.onData(Matchers.hasToString(Matchers.equalToIgnoringWhiteSpace("Mocked VISA")))
                .inAdapterView(ViewMatchers.withId(R.id.application_list_view))
                .perform(ViewActions.click());

        Espresso.unregisterIdlingResources(idlingResource);

        idlingResource = new TransactionProviderControllerIdlingResource();
        Espresso.registerIdlingResources(idlingResource);


        Espresso.onView(ViewMatchers.withId(R.id.summary_account_number_view))
                .check(ViewAssertions.matches(ViewMatchers.withText("************0119")));

        Espresso.unregisterIdlingResources(idlingResource);
    }

    public void testSignatureActivityIsShown() {
        initWithAmount(108.20);
        TransactionProviderControllerIdlingResource idlingResource = new TransactionProviderControllerIdlingResource(true, false);
        Espresso.registerIdlingResources(idlingResource);

        //fake a signature by triggering a touch event
        Espresso.onView(ViewMatchers.withId(R.id.signature_view))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.continue_button))
                .perform(ViewActions.click());

        Espresso.unregisterIdlingResources(idlingResource);

        idlingResource = new TransactionProviderControllerIdlingResource();
        Espresso.registerIdlingResources(idlingResource);
        Espresso.onView(ViewMatchers.withId(R.id.summary_scheme_view))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.unregisterIdlingResources(idlingResource);
    }


    void initWithAmount(double amount) {
        PaymentController controller = PaymentController.initializePaymentController(getInstrumentation().getContext(), ProviderMode.MOCK, "mock", "mock");
        PaymentControllerConfiguration config = controller.getConfiguration();
        config.setAccessoryFamily(AccessoryFamily.MOCK);
        Intent paymentIntent = controller.createPaymentIntent(BigDecimal.valueOf(amount), Currency.EUR, "subject", null);
        setActivityIntent(paymentIntent);
        //Espresso.registerIdlingResources(new TransactionProviderControllerIdlingResource());
        getActivity();
    }
}
