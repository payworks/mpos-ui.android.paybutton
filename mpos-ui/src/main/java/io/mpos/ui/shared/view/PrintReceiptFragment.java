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
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.accessories.parameters.AccessoryParameters;
import io.mpos.errors.MposError;
import io.mpos.transactionprovider.PrintingProcess;
import io.mpos.transactionprovider.PrintingProcessDetails;
import io.mpos.transactionprovider.PrintingProcessDetailsState;
import io.mpos.transactionprovider.TransactionProvider;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.controller.StatefulPrintingProcessProxy;
import io.mpos.ui.shared.util.ParametersHelper;
import io.mpos.ui.shared.util.UiHelper;

public class PrintReceiptFragment extends Fragment implements StatefulPrintingProcessProxy.Callback {

    public interface Interaction {
        void onReceiptPrintCompleted(MposError error);

        void onAbortPrintingClicked();

        TransactionProvider getTransactionProvider();
    }

    public static final String TAG = "PrintReceiptFragment";

    private Interaction mInteractionActivity;
    private String mTransactionIdentifier;
    private boolean mAbortable;

    private ImageView mProgressView;
    private TextView mIconView;
    private TextView mStatusView;
    private Button mAbortButton;

    private StatefulPrintingProcessProxy mStatefulPrintingProcess = StatefulPrintingProcessProxy.getInstance();
    private TransactionProvider mTransactionProvider;

    public static PrintReceiptFragment newInstance(String transactionIdentifier) {
        PrintReceiptFragment fragment = new PrintReceiptFragment();
        fragment.setTransactionIdentifier(transactionIdentifier);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mTransactionProvider = mInteractionActivity.getTransactionProvider();
        AccessoryParameters accessoryParameters = MposUi.getInitializedInstance().getConfiguration().getPrinterParameters();
        if (accessoryParameters == null) {
            AccessoryFamily accessoryFamily = MposUi.getInitializedInstance().getConfiguration().getPrinterAccessoryFamily();
            accessoryParameters = ParametersHelper.getAccessoryParametersForAccessoryFamily(accessoryFamily);
        }

        mStatefulPrintingProcess.printReceipt(mTransactionIdentifier, mTransactionProvider, accessoryParameters);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.mpu_fragment_print_receipt, container, false);
        mProgressView = (ImageView) rootView.findViewById(R.id.mpu_progress_view);
        mIconView = (TextView) rootView.findViewById(R.id.mpu_status_icon_view);
        mStatusView = (TextView) rootView.findViewById(R.id.mpu_status_view);
        mAbortButton = (Button) rootView.findViewById(R.id.mpu_abort_button);

        int color = MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary();
        mIconView.setTypeface(UiHelper.createAwesomeFontTypeface(rootView.getContext()));
        mIconView.setTextColor(color);

        mProgressView.setAnimation(null);

        mAbortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInteractionActivity.onAbortPrintingClicked();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mStatefulPrintingProcess.attachCallback(this);
        getActivity().setTitle(R.string.MPUPrinting);
    }

    @Override
    public void onPause() {
        super.onPause();
        mStatefulPrintingProcess.attachCallback(null);
    }

    @Override
    public void onCompleted(PrintingProcess printingProcess, PrintingProcessDetails printingProcessDetails) {
        updateView(printingProcessDetails);
        if (mInteractionActivity != null) {
            if (printingProcessDetails.getError() != null) { //THERE WAS AN ERROR!
                mInteractionActivity.onReceiptPrintCompleted(printingProcessDetails.getError());
            } else {
                mInteractionActivity.onReceiptPrintCompleted(null);
            }
        }
    }

    @Override
    public void onStatusChanged(PrintingProcess printingProcess, PrintingProcessDetails printingProcessDetails) {
        mAbortable = mStatefulPrintingProcess.canAbort();
        updateView(printingProcessDetails);
    }

    private void updateView(PrintingProcessDetails printingProcessDetails) {
        int visibility = mAbortable ? View.VISIBLE : View.INVISIBLE;
        mAbortButton.setVisibility(visibility);
        String msg = UiHelper.joinAndTrimStatusInformation(printingProcessDetails.getInformation());
        mStatusView.setText(msg);

        if (showProgressView(printingProcessDetails.getState())) {
            if (mProgressView.getAnimation() == null) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.mpu_rotation);
                mProgressView.startAnimation(animation);
            }
            mProgressView.setVisibility(View.VISIBLE);
        } else {
            mProgressView.setAnimation(null);
            mProgressView.setVisibility(View.INVISIBLE);
        }
    }

    private boolean showProgressView(PrintingProcessDetailsState state) {
        switch (state) {
            case CREATED:
            case FETCHING_TRANSACTION:
            case CONNECTING_TO_PRINTER:
            case SENDING_TO_PRINTER:
                return true;
            case SENT_TO_PRINTER:
            case ABORTED:
            case FAILED:
                return false;
        }
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            activity.setTitle(R.string.MPUPrinting);
            mInteractionActivity = (Interaction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement PrintReceiptFragment.Interaction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInteractionActivity = null;
    }

    public void setTransactionIdentifier(String transactionIdentifier) {
        mTransactionIdentifier = transactionIdentifier;
    }

}
