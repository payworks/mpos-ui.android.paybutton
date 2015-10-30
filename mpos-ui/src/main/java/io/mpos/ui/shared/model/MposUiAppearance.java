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
package io.mpos.ui.shared.model;

import android.graphics.Color;

/**
 * Configuration holder for setting theme colors for MposUi.
 * Base theme used is {@code @android:style/Theme.Material.Light}
 */
public class MposUiAppearance {

    private int mColorPrimary = Color.parseColor("#03a9f4");
    private int mColorPrimaryDark = Color.parseColor("#039be5");
    private int mTextColorPrimary = Color.WHITE;
    private int mBackgroundColor = Color.parseColor("#eeeeee");

    /**
     * Get the primary color used by MposUi. See <a href="https://developer.android.com/training/material/images/ThemeColors.png">the Material theme</a>.
     * @return The primary color
     */
    public int getColorPrimary() {
        return mColorPrimary;
    }

    /**
     * Set the primary color used by MposUi.
     * @param colorPrimary The color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setColorPrimary(int colorPrimary) {
        mColorPrimary = colorPrimary;
        return this;
    }

    /**
     * Get the primary dark color used by MposUi. See <a href="https://developer.android.com/training/material/images/ThemeColors.png">the Material theme</a>.
     * @return The primary dark color.
     */
    public int getColorPrimaryDark() {
        return mColorPrimaryDark;
    }

    /**
     * Set the primary dark color used by MposUi.
     * @param colorPrimaryDark The color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setColorPrimaryDark(int colorPrimaryDark) {
        mColorPrimaryDark = colorPrimaryDark;
        return this;
    }

    /**
     * Get the primary text color used by MposUi. See <a href="https://developer.android.com/training/material/images/ThemeColors.png">the Material theme</a>.
     * @return The primary text color.
     */
    public int getTextColorPrimary() {
        return mTextColorPrimary;
    }

    /**
     * Set the primary text color used by MposUi.
     * @param textColorPrimary The color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setTextColorPrimary(int textColorPrimary) {
        mTextColorPrimary = textColorPrimary;
        return this;
    }

    /**
     * Get the window background color used by MposUi.
     * @return The primary text color.
     */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    /**
     * Set the window background color used by MposUi.
     * @param backgroundColor The color to be set.
     * @return Self, to allow chaining of calls.
     */
    public MposUiAppearance setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        return this;
    }
}
