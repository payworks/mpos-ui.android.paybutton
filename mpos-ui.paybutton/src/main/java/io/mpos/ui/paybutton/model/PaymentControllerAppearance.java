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
package io.mpos.ui.paybutton.model;

import android.graphics.Color;

public class PaymentControllerAppearance {

    private int mColorPrimary = Color.parseColor("#03a9f4");
    private int mColorPrimaryDark = Color.parseColor("#039be5");
    private int mTextColorPrimary = Color.WHITE;

    public int getColorPrimary() {
        return mColorPrimary;
    }

    public PaymentControllerAppearance setColorPrimary(int colorPrimary) {
        mColorPrimary = colorPrimary;
        return this;
    }

    public int getColorPrimaryDark() {
        return mColorPrimaryDark;
    }

    public PaymentControllerAppearance setColorPrimaryDark(int colorPrimaryDark) {
        mColorPrimaryDark = colorPrimaryDark;
        return this;
    }

    public int getTextColorPrimary() {
        return mTextColorPrimary;
    }

    public PaymentControllerAppearance setTextColorPrimary(int textColorPrimary) {
        mTextColorPrimary = textColorPrimary;
        return this;
    }
}
