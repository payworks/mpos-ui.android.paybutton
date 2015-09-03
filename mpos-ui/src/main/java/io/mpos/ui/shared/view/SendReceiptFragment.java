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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.mpos.errors.MposError;
import io.mpos.transactionprovider.SendReceiptListener;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.util.UiHelper;

public class SendReceiptFragment extends Fragment {

    public interface Interaction {
        void onReceiptSent();
        TransactionProvider getTransactionProvider();
    }

    public static final String TAG = "SendReceiptFragment";

    private Interaction mInteractionActivity;
    private ImageView mProgressView;
    private Button mSendButton;
    private EditText mEmailView;
    private String mTransactionIdentifier;


    public static SendReceiptFragment newInstance(String transactionIdentifier) {
        SendReceiptFragment fragment = new SendReceiptFragment();
        fragment.setTransactionIdentifier(transactionIdentifier);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mpu_fragment_send_receipt, container, false);
        mProgressView = (ImageView) view.findViewById(R.id.mpu_progress_view);

        int color = MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary();

        TextView iconView = (TextView) view.findViewById(R.id.mpu_status_icon_view);
        iconView.setTypeface(UiHelper.createAwesomeFontTypeface(view.getContext()));
        iconView.setTextColor(color);
        iconView.setText(getString(R.string.mpu_fa_email));

        mSendButton = (Button) view.findViewById(R.id.mpu_send_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailView.getText().toString();
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    hideSoftKeyboard();
                    mSendButton.setEnabled(false);

                    sendReceipt(mTransactionIdentifier, email);
                } else {
                    showErrorDialog(getString(R.string.MPUInvalidEmailAddress), getString(R.string.MPUEnterValidEmailAddress));
                }
            }
        });

        mProgressView.setAnimation(null);
        mProgressView.setVisibility(View.INVISIBLE);

        mEmailView = (EditText) view.findViewById(R.id.mpu_email_address_view);
        mEmailView.requestFocus();
        int primaryDarkColor = MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary();
        mEmailView.getBackground().setColorFilter(primaryDarkColor, PorterDuff.Mode.SRC_ATOP);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().setTitle(R.string.MPUSendReceipt);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        try {
            mInteractionActivity = (Interaction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SendReceipt.Interaction");
        }
    }

    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.MPUSendReceipt);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInteractionActivity = null;
    }

    private void setTransactionIdentifier(String transactionIdentifier) {
        mTransactionIdentifier = transactionIdentifier;
    }

    private void sendReceipt(String transactionIdentifier, String email) {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.mpu_rotation);
        mProgressView.setAnimation(animation);
        mProgressView.setVisibility(View.VISIBLE);

        mInteractionActivity.getTransactionProvider().sendCustomerReceiptForTransaction(transactionIdentifier, email, new SendReceiptListener() {
            @Override
            public void onCompleted(String transactionIdentifier, MposError error) {
                sendReceiptCompleted(error);
            }
        });
    }

    private void sendReceiptCompleted(MposError error) {
        mSendButton.setEnabled(true);
        mProgressView.setAnimation(null);
        mProgressView.setVisibility(View.INVISIBLE);

        if (mInteractionActivity != null) {
            if (error == null) {
                Toast.makeText(getActivity(), R.string.MPUReceiptSent, Toast.LENGTH_LONG).show();
                mInteractionActivity.onReceiptSent();
            } else {
                showErrorDialog(error);
            }
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEmailView.getWindowToken(), 0);
    }

    private void showErrorDialog(MposError error) {
        showErrorDialog(error.getInfo());
    }

    private void showErrorDialog(String message) {
        showErrorDialog(getString(R.string.MPUError), message);
    }

    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.MPUClose, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}
