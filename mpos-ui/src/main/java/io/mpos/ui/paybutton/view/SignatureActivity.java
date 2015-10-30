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
package io.mpos.ui.paybutton.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;

import io.mpos.ui.shared.util.UiHelper;


public class SignatureActivity extends AbstractTransactionActivity
        implements SignatureFragment.Listener {

    public final static String BUNDLE_KEY_AMOUNT = "io.mpos.ui.paybutton.view.SignatureActivity.AMOUNT";
    public final static String BUNDLE_KEY_CARD_SCHEME_ID = "io.mpos.ui.paybutton.view.SignatureActivity.CARD_SCHEME_ID";

    public final static String BUNDLE_KEY_SIGNATURE_IMAGE = "io.mpos.ui.paybutton.view.SignatureActivity.SIGNATURE_IMAGE";
    public final static String BUNDLE_KEY_SIGNATURE_VERIFIED = "io.mpos.ui.paybutton.view.SignatureActivity.SIGNATURE_VERIFIED";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWhiteBackground();
        UiHelper.setActionbarWithCustomColors(this, null);

        String amount = getIntent().getStringExtra(BUNDLE_KEY_AMOUNT);
        int resId = getIntent().getIntExtra(BUNDLE_KEY_CARD_SCHEME_ID, 0);

        if (savedInstanceState == null) {
            SignatureFragment signatureFragment = SignatureFragment.newInstance(amount, resId);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, signatureFragment, SignatureFragment.TAG)
                    .commit();
        }
    }

    private void setWhiteBackground() {
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onContinueWithSignatureButtonClicked(Bitmap bitmap, boolean verified) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(BUNDLE_KEY_SIGNATURE_IMAGE, byteArray);
        resultIntent.putExtra(BUNDLE_KEY_SIGNATURE_VERIFIED, verified);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onAbortPaymentButtonClicked() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
