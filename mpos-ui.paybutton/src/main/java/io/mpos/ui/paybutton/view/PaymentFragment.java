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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import io.mpos.transactionprovider.PaymentProcessDetails;
import io.mpos.transactionprovider.PaymentProcessDetailsState;
import io.mpos.transactionprovider.PaymentProcessDetailsStateDetails;
import io.mpos.transactions.Transaction;
import io.mpos.ui.paybutton.R;
import io.mpos.ui.paybutton.controller.PaymentController;
import io.mpos.ui.paybutton.controller.StatefulTransactionProviderProxy;
import io.mpos.ui.paybutton.util.UIHelper;

public class PaymentFragment extends AbstractPaymentFragment {

    public static String TAG = "PaymentFragment";

    private ImageView mProgressView;
    private TextView mStatusView;
    private TextView mIconView;
    private Button mAbortButton;

    private PaymentProcessDetails mProcessDetails;
    private boolean mAbortable;

    private PaymentInteractionListener mListener;

    public static PaymentFragment newInstance() {
        PaymentFragment fragment = new PaymentFragment();
        return fragment;
    }

    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);
        mStatusView = (TextView) view.findViewById(R.id.status_view);
        mProgressView = (ImageView)view.findViewById(R.id.progress_view);

        int color = PaymentController.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary();
        int secondaryColor = PaymentController.getInitializedInstance().getConfiguration().getAppearance().getColorPrimaryDark();

        mIconView = (TextView) view.findViewById(R.id.status_icon_view);
        mIconView.setTypeface(UIHelper.createAwesomeFontTypeface(view.getContext()));
        mIconView.setTextColor(color);

        mAbortButton = (Button) view.findViewById(R.id.abort_button);
        mAbortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPaymentInteractionListener().onAbortPaymentButtonClicked();
            }
        });

        updateViews();

        return view;
    }

    public void updateStatus(PaymentProcessDetails details, Transaction transaction) {
        mProcessDetails = details;

        mAbortable = StatefulTransactionProviderProxy.getInstance().paymentCanBeAborted();
        updateViews();
    }

    private void updateViews() {
        if(mStatusView == null || mProcessDetails == null)
            return;

        int visibility = mAbortable ? View.VISIBLE : View.INVISIBLE;
        mAbortButton.setVisibility(visibility);

        String msg = UIHelper.joinAndTrimStatusInformation(mProcessDetails.getInformation());
        mStatusView.setText(msg);
        mIconView.setText(statusIcon(mProcessDetails.getState(), mProcessDetails.getStateDetails()));
        mIconView.setPadding(statusIconPadding(mIconView.getText().toString()), 0, 0, 0);

        if(showProgressView(mProcessDetails.getStateDetails())) {
            if(mProgressView.getAnimation() == null) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotation);
                mProgressView.startAnimation(animation);
            }
            mProgressView.setVisibility(View.VISIBLE);
        } else {
            mProgressView.setAnimation(null);
            mProgressView.setVisibility(View.INVISIBLE);
        }
    }

    private boolean showProgressView(PaymentProcessDetailsStateDetails state)  {
        switch(state) {
            case PROCESSING_WAITING_FOR_PIN:
            case PROCESSING_WAITING_FOR_CARD_PRESENTATION:
            case PROCESSING_WAITING_FOR_CARD_REMOVAL:
            case APPROVED:
            case DECLINED:
            case ABORTED:
                return false;
            default:
                return true;

        }
    }

    private String statusIcon(PaymentProcessDetailsState state, PaymentProcessDetailsStateDetails stateDetails) {
        switch (stateDetails) {
            case CONNECTING_TO_ACCESSORY:
            case CONNECTING_TO_ACCESSORY_CHECKING_FOR_UPDATE:
            case CONNECTING_TO_ACCESSORY_UPDATING:
            case CONNECTING_TO_ACCESSORY_WAITING_FOR_READER:
                return getString(R.string.fa_lock);
            case PROCESSING_WAITING_FOR_PIN:
                return getString(R.string.fa_th);
        }

        switch (state) {
            case CREATED:
            case INITIALIZING_TRANSACTION:
                return getString(R.string.fa_lock);
            case PROCESSING:
                return getString(R.string.fa_bank);
            case WAITING_FOR_CARD_PRESENTATION:
            case WAITING_FOR_CARD_REMOVAL:
                return getString(R.string.fa_credit_card);
            case APPROVED:
            case DECLINED:
            case ABORTED:
                return getString(R.string.fa_bank);
            case FAILED:
                return getString(R.string.fa_times_circle);
        }

        return "";
    }

    //The Awesome Font character 'fa_bank' has some trailing whitespace and
    //isn't centered correctly
    private int statusIconPadding(String statusIcon) {
        if(getString(R.string.fa_bank).equals(statusIcon)) {
            return UIHelper.dpToPx(getActivity(), 4);
        }

        return 0;
    }
}
