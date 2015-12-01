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

package io.mpos.ui.shared.view;

import android.app.Fragment;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import io.mpos.Mpos;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.util.UiState;

public abstract class AbstractBaseActivity extends AppCompatActivity {

    private UiState mUiState = UiState.IDLE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int windowBackgroundColor = MposUi.getInitializedInstance().getConfiguration().getAppearance().getBackgroundColor();
        getWindow().getDecorView().setBackgroundColor(windowBackgroundColor);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBack();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public abstract void navigateBack();

    // We use this method to show the fragments so that we never forget to set the correct
    // UiState and whether the home arrow should be shown.
    public void showFragment(Fragment fragment, String tag, UiState uiState, boolean showDisplayHomeAsUp) {
        setUiState(uiState);

        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.mpu_fade_in, R.anim.mpu_fade_out)
                .replace(R.id.mpu_fragment_container, fragment, tag)
                .commitAllowingStateLoss();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showDisplayHomeAsUp);
        }
    }

    public UiState getUiState() {
        return mUiState;
    }

    public void setUiState(UiState mUiState) {
        this.mUiState = mUiState;
    }

}
