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

import android.os.Bundle;
import android.view.MenuItem;

import io.mpos.ui.R;
import io.mpos.ui.acquirer.MposUiAccountManager;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.util.UiHelper;
import io.mpos.ui.shared.util.UiState;
import io.mpos.ui.shared.view.AbstractBaseActivity;

/**
 * Created by Abhijith Srivatsav<abhijith.srivatsav@payworksmobile.com> on 07/10/15.
 */
public class SettingsActivity extends AbstractBaseActivity implements
        LoginFragment.Interaction,
        SettingsFragment.Interaction {

    public static final String TAG = "SettingsActivity";

    public final static String BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID = "io.mpos.ui.acquirer.SettingsActivity.BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID";

    private MposUiAccountManager mMposUiAccountManager;
    private String mApplicationIdentifier;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mpu_activity_settings);
        UiHelper.setActionbarWithCustomColors(this, (android.support.v7.widget.Toolbar) findViewById(R.id.mpu_toolbar));
        mApplicationIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID);

        if (savedInstanceState == null) {
            mMposUiAccountManager = MposUiAccountManager.getInitializedInstance();
            if (mMposUiAccountManager.isLoggedIn()) {
                showSettingsFragment(mApplicationIdentifier);
            } else {
                showLoginFragment(mApplicationIdentifier);
            }
        }
    }

    private void showSettingsFragment(String applicationIdentifier) {
        SettingsFragment settingsFragment = SettingsFragment.newInstance(applicationIdentifier);
        showFragment(settingsFragment, SettingsFragment.TAG, UiState.SETTINGS_DISPLAYING, true);
    }

    private void showLoginFragment(String applicationIdentifier) {
        LoginFragment loginFragment = LoginFragment.newInstance(applicationIdentifier);
        showFragment(loginFragment, LoginFragment.TAG, UiState.LOGIN_DISPLAYING, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        navigateBack();
    }

    @Override
    public void navigateBack() {
        if (getUiState() == UiState.FORGOT_PASSWORD_DISPLAYING) {
            LoginFragment loginFragment = (LoginFragment) getFragmentManager().findFragmentByTag(LoginFragment.TAG);
            if (loginFragment != null) {
                loginFragment.setLoginMode(true);
            }
        } else {
            finishWithResult();
        }
    }

    @Override
    public void onLoginCompleted() {
        showSettingsFragment(mApplicationIdentifier);
    }

    private void finishWithResult() {
        setResult(MposUi.RESULT_CODE_SETTINGS_CLOSED);
        finish();
    }

    @Override
    public void onLoginModeChanged(boolean loginMode) {
        if (loginMode) {
            setUiState(UiState.LOGIN_DISPLAYING);
        } else {
            setUiState(UiState.FORGOT_PASSWORD_DISPLAYING);
        }
    }

    @Override
    public void onLogoutCompleted() {
        showLoginFragment(mApplicationIdentifier);
    }
}
