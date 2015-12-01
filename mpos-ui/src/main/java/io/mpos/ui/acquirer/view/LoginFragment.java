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
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.mpos.Mpos;
import io.mpos.accounts.listeners.LoginListener;
import io.mpos.accounts.listeners.PasswordResetRequestListener;
import io.mpos.errors.ErrorType;
import io.mpos.errors.MposError;
import io.mpos.provider.ProviderMode;
import io.mpos.ui.R;
import io.mpos.ui.acquirer.MposUiAccountManager;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.util.UiHelper;

public class LoginFragment extends Fragment {

    public static final String TAG = "LoginFragment";

    public interface Interaction {

        void onLoginCompleted();

        void onLoginModeChanged(boolean loginMode);
    }

    private Interaction mInteractionActivity;
    private String mApplicationIdentifier;

    private ImageView mLogoImageView;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mActionButton;
    private Button mForgotPasswordButton;

    private ProgressBar mProgressBar;

    private String mEmailText;
    private String mPasswordText;
    private boolean loginMode = true;

    private MposUiAccountManager mMposUiAccountManager;

    public static LoginFragment newInstance(String applicationIdentifier) {
        LoginFragment fragment = new LoginFragment();
        fragment.setApplicationIdentifier(applicationIdentifier);
        return fragment;
    }

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMposUiAccountManager = MposUiAccountManager.getInitializedInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.mpu_fragment_login, container, false);
        mEmailEditText = (EditText) rootView.findViewById(R.id.mpu_email_view);
        mPasswordEditText = (EditText) rootView.findViewById(R.id.mpu_password_view);
        mActionButton = (Button) rootView.findViewById(R.id.mpu_action_button);
        mForgotPasswordButton = (Button) rootView.findViewById(R.id.mpu_forgot_password_button);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.mpu_login_progress);
        mLogoImageView = (ImageView) rootView.findViewById(R.id.mpu_logo_image);

        setupUI();

        mForgotPasswordButton.setOnClickListener(mForgotPasswordClickListener);
        mActionButton.setOnClickListener(mActionButtonClickListener);
        mEmailEditText.addTextChangedListener(mEmailTextWatcher);
        mPasswordEditText.addTextChangedListener(mPasswordTextWatcher);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.MPULogin);
    }

    public void setLoginMode(boolean login) {
        if (login) {
            loginMode = true;
            mActionButton.setEnabled(false);
            mPasswordEditText.setText(null);
            mPasswordEditText.setVisibility(View.VISIBLE);
            mForgotPasswordButton.setText(R.string.MPUForgot);
            mActionButton.setText(R.string.MPULogin);
        } else {
            loginMode = false;
            mPasswordEditText.setVisibility(View.GONE);
            mForgotPasswordButton.setText(R.string.MPUBack);
            mActionButton.setText(R.string.MPURequestPassword);
        }
        mInteractionActivity.onLoginModeChanged(login);
    }

    public void setApplicationIdentifier(String applicationIdentifier) {
        this.mApplicationIdentifier = applicationIdentifier;
    }

    View.OnClickListener mForgotPasswordClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            evaluateEmailField();
            setLoginMode(!loginMode);
        }
    };
    View.OnClickListener mActionButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mEmailText = mEmailEditText.getText().toString();
            mPasswordText = mPasswordEditText.getText().toString();
            ProviderMode providerMode = mMposUiAccountManager.getProviderMode();
            if (loginMode) {
                enableUi(false);
                Mpos.loginWithApplication(getActivity().getApplicationContext(), providerMode, mApplicationIdentifier, mEmailText, mPasswordText, new LoginListener() {
                    @Override
                    public void onLoginSuccessful(final String username, final String merchantId, final String merchantSecret) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enableUi(true);
                                mMposUiAccountManager.loginWithCredentials(username, merchantId, merchantSecret);
                                mInteractionActivity.onLoginCompleted();
                            }
                        });
                    }

                    @Override
                    public void onLoginFailure(String s, final MposError mposError) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enableUi(true);
                                if (mposError.getErrorType() == ErrorType.SERVER_AUTHENTICATION_FAILED) {
                                    showErrorDialog(getString(R.string.MPUWrongCredentials));
                                } else {
                                    showErrorDialog(mposError.getInfo());
                                }
                            }
                        });
                    }
                });
            } else {
                enableUi(false);
                Mpos.requestPasswordResetForApplication(getActivity(), providerMode, mApplicationIdentifier, mEmailText, new PasswordResetRequestListener() {
                    @Override
                    public void onPasswordResetRequestSuccessful(String username) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enableUi(true);
                                Toast.makeText(getActivity(), R.string.MPUPasswordReset, Toast.LENGTH_SHORT).show();
                                setLoginMode(true);
                            }
                        });
                    }

                    @Override
                    public void onPasswordResetRequestFailure(String s, final MposError mposError) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enableUi(true);
                                if (mposError.getErrorType() == ErrorType.SERVER_UNKNOWN_USERNAME) {
                                    showErrorDialog(getString(R.string.MPUUnknownUsername));
                                } else {
                                    showErrorDialog(mposError.getInfo());
                                }
                            }
                        });
                    }
                });
            }
        }
    };

    private void showErrorDialog(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.MPUError));
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(getString(R.string.MPUOK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.create().show();
    }

    TextWatcher mEmailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!loginMode) {
                evaluateEmailField();
            } else {
                if (!TextUtils.isEmpty(mPasswordEditText.getText().toString())) {
                    if (!isValidEmail(editable.toString())) {
                        enableAction(false);
                    } else {
                        enableAction(true);
                    }
                }
            }
        }
    };

    TextWatcher mPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() > 0 && !TextUtils.isEmpty(mEmailEditText.getText().toString())) {
                if (isValidEmail(mEmailEditText.getText().toString())) {
                    enableAction(true);
                } else {
                    mEmailEditText.setError(getResources().getString(R.string.MPUInvalidEmailAddress));
                }
            } else {
                enableAction(false);
            }
        }
    };

    private void setupUI() {
        UiHelper.tintView(mEmailEditText, MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary());
        UiHelper.tintView(mPasswordEditText, MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary());
        UiHelper.tintButton(mActionButton, MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary());
        mLogoImageView.setImageResource(mMposUiAccountManager.getApplicationData().getImageResourceId());
        mEmailEditText.setText(mMposUiAccountManager.getUsername());
    }

    private void evaluateEmailField() {
        // Improving UX
        // 1. If empty disable the action button
        if (TextUtils.isEmpty(mEmailEditText.getText().toString())) {
            enableAction(false);
        } else if (!isValidEmail(mEmailEditText.getText().toString())) { //2. If invalid email: show error and disable action button
            enableAction(false);
        } else { // 3. Seems to be valid email and not empty. Enable action button.
            enableAction(true);
        }
    }

    private void enableAction(boolean enable) {
        mActionButton.setEnabled(enable);
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mpu_menu_login, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Drawable drawable = menu.findItem(R.id.mpu_action_help).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, MposUi.getInitializedInstance().getConfiguration().getAppearance().getTextColorPrimary());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.mpu_action_help) {
            // Take me away from the app!
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mMposUiAccountManager.getApplicationData().getHelpUrl()));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void enableUi(boolean enabled) {
        mEmailEditText.setEnabled(enabled);
        mPasswordEditText.setEnabled(enabled);
        mActionButton.setEnabled(enabled);
        mForgotPasswordButton.setEnabled(enabled);

        if (enabled) {
            mProgressBar.setVisibility(View.GONE);

        } else {
            mProgressBar.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            activity.setTitle(R.string.MPUPrinting);
            mInteractionActivity = (Interaction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LoginFragment.Interaction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInteractionActivity = null;
    }

}
