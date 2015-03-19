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
package io.mpos.ui.paybutton.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.mpos.errors.MposError;
import io.mpos.ui.paybutton.R;
import io.mpos.ui.paybutton.controller.PaymentController;
import io.mpos.ui.paybutton.util.UIHelper;

public class PaymentErrorFragment extends AbstractPaymentFragment {

    public static final String TAG = "PaymentErrorFragment";
    
    private MposError mError;
    boolean mRetryEnabled;

    public static PaymentErrorFragment newInstance(boolean retryEnabled, MposError error) {
        PaymentErrorFragment fragment = new PaymentErrorFragment();
        fragment.setError(error);
        fragment.setRetryEnabled(retryEnabled);
        return fragment;
    }

    public PaymentErrorFragment() {  }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_error, container, false);

        Button retryButton = (Button) view.findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPaymentInteractionListener().onRetryPaymentButtonClicked();
            }
        });
        if(mRetryEnabled) {
            retryButton.setVisibility(View.VISIBLE);
        } else {
            retryButton.setVisibility(View.INVISIBLE);
        }

        view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPaymentInteractionListener().onCancelPaymentButtonClicked();
            }
        });

        TextView iconView = (TextView) view.findViewById(R.id.status_icon_view);
        iconView.setTypeface(UIHelper.createAwesomeFontTypeface(getActivity()));
        iconView.setTextColor(PaymentController.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary());

        TextView errorView = (TextView) view.findViewById(R.id.status_view);
        errorView.setText(mError.getInfo().trim());

        return view;
    }

    public void setError(MposError error) {
        mError = error;
    }

    public void setRetryEnabled(boolean retryEnabled) {
        mRetryEnabled = retryEnabled;
    }
}
