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
package io.mpos.ui.acquirer.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import io.mpos.ui.R;
import io.mpos.ui.acquirer.MposUiAccountManager;
import io.mpos.ui.shared.MposUi;

/**
 * Created by Abhijith Srivatsav<abhijith.srivatsav@payworksmobile.com> on 13/10/15.
 */

public class SettingsFragment extends PreferenceFragment {

    public interface Interaction {

        void onLogoutCompleted();
    }

    public static final String TAG = "SettingsFragment";

    private static final String PREFERENCE_KEY_LOGGED_IN_AS = "mpu_key_logged_in_as";
    private static final String PREFERENCE_KEY_VERSION = "mpu_key_version";
    private static final String PREFERENCE_KEY_HELP = "mpu_key_help";
    private static final String PREFERENCE_KEY_LOGOUT = "mpu_key_logout";

    private SettingsActivity mInteractionActivity;

    private Preference mLoggedInAsPreference;
    private Preference mVersionPreference;

    private Preference mHelpPreference;
    private Preference mLogoutPreference;

    private String mApplicationIdentifier;
    private PreferenceCategory mPreferenceCategory;
    private MposUiAccountManager mMposUiAccountManager;

    public static SettingsFragment newInstance(String applicationIdentifier) {
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setApplicationIdentifier(applicationIdentifier);
        return settingsFragment;
    }

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.mpu_settings_prefs);
        mMposUiAccountManager = MposUiAccountManager.getInitializedInstance();

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        mLoggedInAsPreference = preferenceScreen.findPreference(PREFERENCE_KEY_LOGGED_IN_AS);
        mVersionPreference = preferenceScreen.findPreference(PREFERENCE_KEY_VERSION);
        mHelpPreference = preferenceScreen.findPreference(PREFERENCE_KEY_HELP);
        mLogoutPreference = preferenceScreen.findPreference(PREFERENCE_KEY_LOGOUT);

        mLoggedInAsPreference.setSummary(mMposUiAccountManager.getUsername());
        mVersionPreference.setSummary(MposUi.getVersion());
        mLogoutPreference.setOnPreferenceClickListener(logoutClickListener);
        mHelpPreference.setOnPreferenceClickListener(helpClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.MPUSettings);
    }

    private Preference.OnPreferenceClickListener helpClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mMposUiAccountManager.getApplicationData().getHelpUrl()));
            startActivity(intent);
            return true;
        }
    };

    private Preference.OnPreferenceClickListener logoutClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            mMposUiAccountManager.logout(false);
            mInteractionActivity.onLogoutCompleted();
            return true;
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mInteractionActivity = (SettingsActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SettingsActivity.Interaction");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInteractionActivity = null;
    }

    public SettingsActivity getInteractionActivity() {
        return mInteractionActivity;
    }

    public void setApplicationIdentifier(String applicationIdentifier) {
        mApplicationIdentifier = applicationIdentifier;
    }
}
