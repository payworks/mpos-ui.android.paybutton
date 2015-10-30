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
package io.mpos.ui.paybutton.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import io.mpos.ui.R;
import io.mpos.ui.shared.util.UiHelper;


public class SignatureFragment extends Fragment implements SignatureView.SignatureViewListener {

    public final static String TAG = "SignatureFragment";

    private final static String BUNDLE_KEY_AMOUNT = "io.mpos.ui.SignatureFragment.AMOUNT";
    private final static String BUNDLE_KEY_CARD_SCHEME = "io.mpos.ui.SignatureFragment.CARD_SCHEME";

    public interface Listener {
        void onContinueWithSignatureButtonClicked(Bitmap bitmap, boolean verified);
        void onAbortPaymentButtonClicked();
    }

    public static SignatureFragment newInstance(String amount, int cardSchemeResId) {
        SignatureFragment fragment = new SignatureFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_AMOUNT, amount);
        bundle.putInt(BUNDLE_KEY_CARD_SCHEME, cardSchemeResId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public SignatureFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.mpu_fragment_signature, container, false);

        TextView amountView = (TextView) view.findViewById(R.id.mpu_amount_view);
        String amount = getArguments().getString(BUNDLE_KEY_AMOUNT);
        amountView.setText(amount);

        int resId = getArguments().getInt(BUNDLE_KEY_CARD_SCHEME);
        ImageView schemeView = (ImageView) view.findViewById(R.id.mpu_scheme_view);
        schemeView.setImageResource(resId);

        TextView authorizeView = (TextView) view.findViewById(R.id.mpu_authorize_amount_view);
        String txt = String.format(getString(R.string.MPUSignatureStatusLine), amount);
        authorizeView.setText(txt);

        view.findViewById(R.id.mpu_continue_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignatureView signatureView = (SignatureView) view.findViewById(R.id.mpu_signature_view);
                Bitmap bitmap = signatureView.getImage();
                ((Listener) getActivity()).onContinueWithSignatureButtonClicked(bitmap, true);
            }
        });

        Button button = (Button) view.findViewById(R.id.mpu_clear_button);
        button.setTypeface(UiHelper.createAwesomeFontTypeface(view.getContext()));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignatureView signatureView = (SignatureView) view.findViewById(R.id.mpu_signature_view);
                signatureView.clearSignature();
            }
        });

        view.findViewById(R.id.mpu_abort_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Listener) getActivity()).onAbortPaymentButtonClicked();
            }
        });

        SignatureView signatureView = (SignatureView) view.findViewById(R.id.mpu_signature_view);
        signatureView.setListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    @Override
    public void signatureViewDidDrawSignature(SignatureView signatureView) {
        if(getView() != null) {
            getView().findViewById(R.id.mpu_continue_button).setEnabled(true);
            getView().findViewById(R.id.mpu_clear_button).setEnabled(true);
        }
    }

    @Override
    public void signatureViewDidClearSignature(SignatureView signatureView) {
        if(getView() != null) {
            getView().findViewById(R.id.mpu_continue_button).setEnabled(false);
            getView().findViewById(R.id.mpu_clear_button).setEnabled(false);
        }
    }
}
