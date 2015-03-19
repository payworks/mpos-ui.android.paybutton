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
package io.mpos.ui.paybutton.util;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;

import java.math.BigDecimal;

import io.mpos.paymentdetails.PaymentDetailsScheme;
import io.mpos.transactions.Currency;
import io.mpos.ui.paybutton.R;
import io.mpos.ui.paybutton.controller.PaymentController;

public class UIHelper {

    private static Typeface awesomeFont;

    public static String joinAndTrimStatusInformation(String[] information) {
        if(information == null) {
            return "";
        }

        String retVal = "";
        for(String line : information) {
            retVal += line.trim() + "\n";
        }

        return retVal.trim();
    }

    public static String formatAmountWithSymbol(Currency currency, BigDecimal amount) {
        return currency.formatAmountWithSymbol(amount);
    }

    public static Typeface createAwesomeFontTypeface(Context context) {
        if(awesomeFont == null) {
            awesomeFont = Typeface.createFromAsset(context.getAssets(), "font/fontawesome-webfont.ttf");
        }
        return awesomeFont;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void applyCustomColors(ActionBarActivity activity, Toolbar toolbar) {
        if(toolbar != null) {
            activity.setSupportActionBar(toolbar);
            int color = PaymentController.getInitializedInstance().getConfiguration().getAppearance().getTextColorPrimary();
            toolbar.setTitleTextColor(color);

            color = PaymentController.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary();
            toolbar.setBackgroundColor(color);
        }

        if(Build.VERSION.SDK_INT >= 21) {
            int color = PaymentController.getInitializedInstance().getConfiguration().getAppearance().getColorPrimaryDark();
            activity.getWindow().setStatusBarColor(color);
        }
    }

    public static int getDrawableIdImageForCreditCard(PaymentDetailsScheme cardScheme) {
        //if the given scheme is something totally unknown, we catch the exception and send back a -1
        try {
            switch (cardScheme) {
                case MASTERCARD:
                    return R.drawable.mastercard_image;
                case MAESTRO:
                    return R.drawable.maestro_image;
                case VISA:
                case VISA_ELECTRON:
                    return R.drawable.visacard_image;
                default:
                    return -1;
            }
        } catch (Exception ex) {
            return -1;
        }
    }
}
