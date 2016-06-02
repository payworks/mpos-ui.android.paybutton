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
package io.mpos.ui.shared.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import java.math.BigDecimal;

import io.mpos.paymentdetails.PaymentDetailsScheme;
import io.mpos.transactions.Currency;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;

public class UiHelper {

    private static Typeface awesomeFont;

    public static String joinAndTrimStatusInformation(String[] information) {
        if (information == null) {
            return "";
        }

        String retVal = "";
        for (String line : information) {
            retVal += line.trim() + "\n";
        }

        return retVal.trim();
    }

    public static String formatAmountWithSymbol(Currency currency, BigDecimal amount) {
        if (currency == null) {
            return null;
        } else {
            return currency.formatAmountWithSymbol(amount);
        }
    }

    public static Typeface createAwesomeFontTypeface(Context context) {
        if (awesomeFont == null) {
            awesomeFont = Typeface.createFromAsset(context.getAssets(), "font/fontawesome-webfont.ttf");
        }
        return awesomeFont;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void setActionbarWithCustomColors(AppCompatActivity activity, Toolbar toolbar) {
        if (toolbar != null) {
            setupUpNavigation(activity, toolbar);
            activity.setSupportActionBar(toolbar);

            int color = MposUi.getInitializedInstance().getConfiguration().getAppearance().getTextColorPrimary();
            toolbar.setTitleTextColor(color);

            final Drawable navigationDrawable = toolbar.getNavigationIcon();
            if (navigationDrawable != null) {
                navigationDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                toolbar.setNavigationIcon(navigationDrawable);
            }

            color = MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary();
            toolbar.setBackgroundColor(color);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            int color = MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimaryDark();
            activity.getWindow().setStatusBarColor(color);
        }
    }


    public static void tintButton(Button button, int color) {
        button.setTextColor(getColorsStateListForTint(color));
    }

    public static int getDrawableIdForCardScheme(PaymentDetailsScheme cardScheme) {
        //if the given scheme is something totally unknown, we catch the exception and send back a -1
        try {
            switch (cardScheme) {
                case MASTERCARD:
                    return R.drawable.mpu_mastercard_image;
                case MAESTRO:
                    return R.drawable.mpu_maestro_image;
                case VISA:
                case VISA_ELECTRON:
                    return R.drawable.mpu_visacard_image;
                case AMERICAN_EXPRESS:
                    return R.drawable.mpu_american_express_image;
                case JCB:
                    return R.drawable.mpu_jcb_image;
                case DINERS:
                    return R.drawable.mpu_diners_image;
                case DISCOVER:
                    return R.drawable.mpu_discover_image;
                case UNION_PAY:
                    return R.drawable.mpu_unionpay_image;
                default:
                    return -1;
            }
        } catch (Exception ex) {
            return -1;
        }
    }

    public static void tintView(View view, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(view.getBackground());
        DrawableCompat.setTint(wrappedDrawable, color);
        view.setBackgroundDrawable(wrappedDrawable);
    }

    public static String getPartialCaptureHintText(Context context, BigDecimal amount, Currency currency) {
        String amountText = formatAmountWithSymbol(currency, amount);
        return context.getString(R.string.MPUPartiallyCaptured, amountText);
    }

    public static String formatAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            return "";
        }
        accountNumber = accountNumber.replaceAll(" ", ""); // Removing spaces
        accountNumber = accountNumber.replaceAll("-", ""); // Removing dashes(-)
        accountNumber = accountNumber.replaceAll("[^0-9]", "\u2022"); // Making everything except numbers X's

        int initialLength = accountNumber.length();
        int numberOfSpaces = (initialLength - 1) / 4;

        StringBuilder sb = new StringBuilder(accountNumber);
        for (int i = 1; i <= numberOfSpaces; i++) {
            sb.insert(initialLength - 4 * i, " "); // Inserting spaces in the right spot.
        }

        return sb.toString();
    }

    private static void setupUpNavigation(final AppCompatActivity activity, Toolbar toolbar) {
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(activity);
            }
        });
    }

    private static ColorStateList getColorsStateListForTint(int color) {

        int[][] colorStates = new int[][]{
                new int[]{-android.R.attr.state_enabled},  // disabled state (-)
                new int[]{}                                // enabled
        };

        int[] colors = new int[]{
                setAlphaForColor(color, 0.4f),
                color
        };

        return new ColorStateList(colorStates, colors);

    }

    private static int setAlphaForColor(int color, float alphaFactor) {
        int alpha = Math.round(Color.alpha(color) * alphaFactor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

}
