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
package io.mpos.ui.acquirer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.ArrayRes;
import android.support.annotation.DrawableRes;

import java.util.EnumSet;
import java.util.Locale;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.provider.ProviderMode;
import io.mpos.ui.R;
import io.mpos.ui.shared.model.MposUiAppearance;
import io.mpos.ui.shared.model.MposUiConfiguration;
import io.mpos.ui.shared.util.ParametersHelper;

public class MposUiAccountManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int INDEX_APPLICATION_ID = 0;
    private static final int INDEX_TERMINAL_FAMILY = 1;
    private static final int INDEX_PRINTER_FAMILY = 2;
    private static final int INDEX_FEATURE_PRINT_RECEIPT = 3;
    private static final int INDEX_FEATURE_SEND_RECEIPT = 4;
    private static final int INDEX_FEATURE_REFUND = 5;
    private static final int INDEX_COLOR_PRIMARY = 6;
    private static final int INDEX_COLOR_PRIMARY_DARK = 7;
    private static final int INDEX_TEXT_COLOR_PRIMARY = 8;
    private static final int INDEX_BACKGROUND_COLOR = 9;


    private static final String SHARED_PREFERENCE_FILE_NAME_KEY = "io.mpos.ui.account.credentials";
    private static final String PREFERENCE_KEY_MERCHANT_IDENTIFIER = "io.mpos.ui.account.merchant.identifier";
    private static final String PREFERENCE_KEY_MERCHANT_SECRET_KEY = "io.mpos.ui..account.merchant.secretkey";
    private static final String PREFERENCE_KEY_USERNAME = "io.mpos.ui.account.merchant.username";
    private static final String PREFERENCE_KEY_APPLICATION_ID = "io.mpos.ui.account.applicationid";

    private ProviderMode mProviderMode;
    private ApplicationData mApplicationData;
    private String mMerchantIdentifier;
    private String mApplicationId;
    private String mMerchantSecretKey;
    private String mUsername;
    private String mIntegratorIdentifier;

    private SharedPreferences mSharedPrefs;

    private static MposUiAccountManager INSTANCE;

    public static MposUiAccountManager initialize(Context context, ProviderMode providerMode, ApplicationName applicationName, String integratorIdentifier) {
        if (INSTANCE != null && applicationName == INSTANCE.getApplicationData().getApplicationName()) {
            return INSTANCE;
        }
        INSTANCE = new MposUiAccountManager(context, providerMode, applicationName, integratorIdentifier);
        return INSTANCE;
    }

    public static MposUiAccountManager getInitializedInstance() {
        return INSTANCE;
    }

    private MposUiAccountManager(Context context, ProviderMode providerMode, ApplicationName applicationName, String integratorIdentifier) {
        mProviderMode = providerMode;
        mApplicationData = new ApplicationData();
        mApplicationData.setApplicationName(applicationName);
        mIntegratorIdentifier = integratorIdentifier;
        switch (applicationName) {
            case MCASHIER:
                init(context, R.array.mpu_acquirer_mcashier, R.drawable.mpu_mcashier_logo);
                break;
            case CONCARDIS:
                init(context, R.array.mpu_acquirer_concardis, R.drawable.mpu_concardis_logo);
                break;
            case SECURE_RETAIL:
                init(context, R.array.mpu_acquirer_secure_retail, R.drawable.mpu_secure_retail_logo);
                break;
            case YOURBRAND:
                init(context, R.array.mpu_acquirer_yourbrand, R.drawable.mpu_yourbrand_logo);
                break;
        }
        mSharedPrefs = context.getSharedPreferences(SHARED_PREFERENCE_FILE_NAME_KEY, Context.MODE_PRIVATE);
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
        resolveCredentials();
        if (mApplicationId != null && !mApplicationId.equals(mApplicationData.getIdentifier())) {
            // Initialized with new application without logging out from the older one.
            clearCredentials(true);
        }
    }

    private void resolveCredentials() {
        mMerchantIdentifier = mSharedPrefs.getString(PREFERENCE_KEY_MERCHANT_IDENTIFIER, null);
        mMerchantSecretKey = mSharedPrefs.getString(PREFERENCE_KEY_MERCHANT_SECRET_KEY, null);
        mUsername = mSharedPrefs.getString(PREFERENCE_KEY_USERNAME, null);
        mApplicationId = mSharedPrefs.getString(PREFERENCE_KEY_APPLICATION_ID, null);
    }

    private void clearCredentials(boolean clearUsername) {
        mMerchantIdentifier = null;
        mMerchantSecretKey = null;
        mIntegratorIdentifier = null;
        mApplicationId = null;
        SharedPreferences.Editor sharedPrefsEditor = mSharedPrefs.edit();
        sharedPrefsEditor.remove(PREFERENCE_KEY_MERCHANT_IDENTIFIER);
        sharedPrefsEditor.remove(PREFERENCE_KEY_MERCHANT_SECRET_KEY);
        sharedPrefsEditor.remove(PREFERENCE_KEY_APPLICATION_ID);
        if (clearUsername) {
            mUsername = null;
            sharedPrefsEditor.remove(PREFERENCE_KEY_USERNAME);
        }
        sharedPrefsEditor.apply();
    }

    public boolean isLoggedIn() {
        return mMerchantIdentifier != null && mMerchantSecretKey != null;
    }

    public void logout(boolean clearUsername) {
        clearCredentials(clearUsername);
    }

    public void loginWithCredentials(String username, String merchantId, String merchantSecret) {
        storeCredentials(username, merchantId, merchantSecret, mApplicationData.getIdentifier());
    }

    private void storeCredentials(String username, String merchantId, String merchantSecret, String applicationId) {
        SharedPreferences.Editor sharedPrefsEditor = mSharedPrefs.edit();
        sharedPrefsEditor.putString(PREFERENCE_KEY_MERCHANT_IDENTIFIER, merchantId);
        sharedPrefsEditor.putString(PREFERENCE_KEY_MERCHANT_SECRET_KEY, merchantSecret);
        sharedPrefsEditor.putString(PREFERENCE_KEY_USERNAME, username);
        sharedPrefsEditor.putString(PREFERENCE_KEY_APPLICATION_ID, applicationId);
        sharedPrefsEditor.apply();
    }

    private void init(Context context, @ArrayRes int acquirerArrayResourceId, @DrawableRes int acquirerImageResourceId) {

        String[] applicationData = context.getResources().getStringArray(acquirerArrayResourceId);
        if (applicationData == null) {
            return;
        }

        AccessoryFamily accessoryFamily = resolveAccessoryFamily(applicationData[INDEX_TERMINAL_FAMILY]);
        AccessoryFamily printerAccessoryFamily = resolveAccessoryFamily(applicationData[INDEX_PRINTER_FAMILY]);

        MposUiConfiguration mposUiConfiguration = new MposUiConfiguration();
        mposUiConfiguration.setAccessoryFamily(accessoryFamily);
        mposUiConfiguration.setPrinterAccessoryFamily(printerAccessoryFamily);

        mposUiConfiguration.setTerminalParameters(ParametersHelper.getAccessoryParametersForAccessoryFamily(accessoryFamily));
        mposUiConfiguration.setPrinterParameters(ParametersHelper.getAccessoryParametersForAccessoryFamily(printerAccessoryFamily));

        mposUiConfiguration.setSignatureCapture(MposUiConfiguration.SignatureCapture.ON_SCREEN);
        mposUiConfiguration.setSummaryFeatures(resolveSummaryFeatures(applicationData[INDEX_FEATURE_PRINT_RECEIPT], applicationData[INDEX_FEATURE_SEND_RECEIPT], applicationData[INDEX_FEATURE_REFUND]));

        MposUiAppearance mposUiAppearance = new MposUiAppearance();
        mposUiAppearance.setColorPrimary(Color.parseColor(applicationData[INDEX_COLOR_PRIMARY]));
        mposUiAppearance.setColorPrimaryDark(Color.parseColor(applicationData[INDEX_COLOR_PRIMARY_DARK]));
        mposUiAppearance.setTextColorPrimary(Color.parseColor(applicationData[INDEX_TEXT_COLOR_PRIMARY]));
        mposUiAppearance.setBackgroundColor(Color.parseColor(applicationData[INDEX_BACKGROUND_COLOR]));

        mposUiConfiguration.setAppearance(mposUiAppearance);

        String applicationId = applicationData[INDEX_APPLICATION_ID];
        mApplicationData.setMposUiConfiguration(mposUiConfiguration);
        mApplicationData.setImageResourceId(acquirerImageResourceId);
        mApplicationData.setIdentifier(applicationId);
        mApplicationData.setHelpUrl(resolveHelpUrl(applicationId));
    }


    private String resolveHelpUrl(String applicationIdentifier) {
        String liveUrl = "https://services.pwtx.info";
        String redirect = String.format("applications/%s/redirects", applicationIdentifier);
        String helpEndpoint = "help";
        String localizationParameter = String.format("language=%s", Locale.getDefault().getLanguage());
        String helpUrl = String.format("%s/%s/%s?%s", liveUrl, redirect, helpEndpoint, localizationParameter);

        return helpUrl;
    }

    private EnumSet<MposUiConfiguration.SummaryFeature> resolveSummaryFeatures(String featurePrint, String featureSend, String featureRefund) {
        EnumSet<MposUiConfiguration.SummaryFeature> summaryFeatures = EnumSet.noneOf(MposUiConfiguration.SummaryFeature.class);
        if (Boolean.parseBoolean(featurePrint)) {
            summaryFeatures.add(MposUiConfiguration.SummaryFeature.PRINT_RECEIPT);
        }
        if (Boolean.parseBoolean(featureSend)) {
            summaryFeatures.add(MposUiConfiguration.SummaryFeature.SEND_RECEIPT_VIA_EMAIL);
        }
        if (Boolean.parseBoolean(featureRefund)) {
            summaryFeatures.add(MposUiConfiguration.SummaryFeature.REFUND_TRANSACTION);
        }
        return summaryFeatures;
    }

    private AccessoryFamily resolveAccessoryFamily(String accessoryFamilyString) {

        if(mProviderMode == ProviderMode.MOCK) {
            return AccessoryFamily.MOCK;
        }

        if (accessoryFamilyString.equalsIgnoreCase("Miura")) {
            return AccessoryFamily.MIURA_MPI;
        } else if (accessoryFamilyString.equalsIgnoreCase("Verifone")) {
            return AccessoryFamily.VERIFONE_E105;
        } else if (accessoryFamilyString.equalsIgnoreCase("Sewoo")) {
            return AccessoryFamily.SEWOO;
        }
        return AccessoryFamily.MOCK;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        resolveCredentials();
    }

    public ApplicationData getApplicationData() {
        return mApplicationData;
    }

    public String getMerchantIdentifier() {
        return mMerchantIdentifier;
    }

    public String getMerchantSecretKey() {
        return mMerchantSecretKey;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getIntegratorIdentifier() {
        return mIntegratorIdentifier;
    }

    public ProviderMode getProviderMode() {
        return mProviderMode;
    }

}
