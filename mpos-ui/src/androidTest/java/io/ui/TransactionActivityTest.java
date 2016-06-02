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

import android.content.Context;
import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.provider.ProviderMode;
import io.mpos.transactions.Currency;
import io.mpos.ui.R;
import io.mpos.ui.paybutton.view.TransactionActivity;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.model.MposUiConfiguration;
import io.ui.helper.StatefulTransactionProviderProxyIdlingResource;

@LargeTest
public class TransactionActivityTest extends ActivityInstrumentationTestCase2<TransactionActivity> {

    public TransactionActivityTest() {
        super(TransactionActivity.class);
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

    public void testBackKeyShowsToast() {
        initWithAmount(106);
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withText(R.string.MPUBackButtonDisabled)).inRoot(RootMatchers.withDecorView(CoreMatchers.not(CoreMatchers.is(getActivity().getWindow().getDecorView())))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    public void testReceiptButtonOnSummaryPage() {
        initWithAmount(106);
        StatefulTransactionProviderProxyIdlingResource idlingResource = new StatefulTransactionProviderProxyIdlingResource();
        Espresso.registerIdlingResources(idlingResource);
        Espresso.onView(ViewMatchers.withId(R.id.mpu_send_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.unregisterIdlingResources(idlingResource);
    }

    public void testNoReceiptButtonOnSummaryPageWithoutFeature() {
        initWithAmount(106);
        MposUi.getInitializedInstance().getConfiguration().setSummaryFeatures(EnumSet.of(MposUiConfiguration.SummaryFeature.REFUND_TRANSACTION));
        StatefulTransactionProviderProxyIdlingResource idlingResource = new StatefulTransactionProviderProxyIdlingResource();
        Espresso.registerIdlingResources(idlingResource);
        Espresso.onView(ViewMatchers.withId(R.id.mpu_send_button))
                .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())));
        Espresso.unregisterIdlingResources(idlingResource);
    }

    public void testSendReceipt() {
        initWithAmount(106);
        StatefulTransactionProviderProxyIdlingResource idlingResource = new StatefulTransactionProviderProxyIdlingResource();
        Espresso.registerIdlingResources(idlingResource);
        Espresso.onView(ViewMatchers.withId(R.id.mpu_send_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.mpu_email_address_view))
                .perform(ViewActions.typeText("a@example.com"));
        Espresso.onView(ViewMatchers.withId(R.id.mpu_send_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.mpu_send_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withText(R.string.MPUReceiptSent)).inRoot(RootMatchers.withDecorView(CoreMatchers.not(CoreMatchers.is(getActivity().getWindow().getDecorView())))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.unregisterIdlingResources(idlingResource);
    }

    public void testSendReceiptWhenInvalidEmail() {
        initWithAmount(106);
        StatefulTransactionProviderProxyIdlingResource idlingResource = new StatefulTransactionProviderProxyIdlingResource();
        Espresso.registerIdlingResources(idlingResource);
        Espresso.onView(ViewMatchers.withId(R.id.mpu_send_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.mpu_email_address_view))
                .perform(ViewActions.typeText("aexample.com"));
        Espresso.onView(ViewMatchers.withId(R.id.mpu_send_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(R.string.MPUInvalidEmailAddress)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.unregisterIdlingResources(idlingResource);
    }

    public void testApplicationSelection() {
        initWithAmount(113.73);
        StatefulTransactionProviderProxyIdlingResource idlingResource = new StatefulTransactionProviderProxyIdlingResource(false, true);
        Espresso.registerIdlingResources(idlingResource);
        Espresso.onData(Matchers.hasToString(Matchers.equalToIgnoringWhiteSpace("Mocked VISA")))
                .inAdapterView(ViewMatchers.withId(R.id.mpu_application_list_view))
                .perform(ViewActions.click());

        Espresso.unregisterIdlingResources(idlingResource);

        idlingResource = new StatefulTransactionProviderProxyIdlingResource();
        Espresso.registerIdlingResources(idlingResource);


        Espresso.onView(ViewMatchers.withId(R.id.mpu_summary_account_number_view))
                .check(ViewAssertions.matches(ViewMatchers.withText("************0119")));

        Espresso.unregisterIdlingResources(idlingResource);
    }

    public void testSignatureActivityIsShown() {
        initWithAmount(108.20);
        StatefulTransactionProviderProxyIdlingResource idlingResource = new StatefulTransactionProviderProxyIdlingResource(true, false);
        Espresso.registerIdlingResources(idlingResource);

        //fake a signature by triggering a touch event
        Espresso.onView(ViewMatchers.withId(R.id.mpu_signature_view))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.mpu_continue_button))
                .perform(ViewActions.click());

        Espresso.unregisterIdlingResources(idlingResource);

        idlingResource = new StatefulTransactionProviderProxyIdlingResource();
        Espresso.registerIdlingResources(idlingResource);
        Espresso.onView(ViewMatchers.withId(R.id.mpu_summary_scheme_view))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.unregisterIdlingResources(idlingResource);
    }


    private void initWithAmount(double amount) {
        MposUi controller = MposUi.initialize(getInstrumentation().getContext(), ProviderMode.MOCK, "mock", "mock");
        MposUiConfiguration config = controller.getConfiguration();
        config.setAccessoryFamily(AccessoryFamily.MOCK);
        config.setSummaryFeatures(EnumSet.of(MposUiConfiguration.SummaryFeature.SEND_RECEIPT_VIA_EMAIL));
        Intent transactionIntent = controller.createChargeTransactionIntent(BigDecimal.valueOf(amount), Currency.EUR, "subject", null);
        setActivityIntent(transactionIntent);
        //Espresso.registerIdlingResources(new TransactionProviderControllerIdlingResource());
        getActivity();
    }

    private void hideSoftKeyboard(int editTextId) {
        EditText editText = (EditText) getActivity().findViewById(editTextId);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
