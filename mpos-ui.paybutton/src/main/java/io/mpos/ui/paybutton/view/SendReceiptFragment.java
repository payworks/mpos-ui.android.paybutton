package io.mpos.ui.paybutton.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.mpos.errors.MposError;
import io.mpos.shared.errors.DefaultMposError;
import io.mpos.transactions.Transaction;
import io.mpos.ui.paybutton.R;
import io.mpos.ui.paybutton.controller.PaymentController;
import io.mpos.ui.paybutton.controller.StatefulTransactionProviderProxy;
import io.mpos.ui.paybutton.util.UIHelper;

/**
 * Created by jakub on 09/04/15.
 */
public class SendReceiptFragment extends AbstractPaymentFragment implements StatefulTransactionProviderProxy.SendReceiptCallback {

    public static final String TAG = "SendReceiptFragment";

    private ImageView mProgressView;
    private TextView mIconView;
    private Button mSendButton;
    private EditText mEmailView;

    private String mActivityDefaultTitle;
    private Transaction mTransaction;


    public static SendReceiptFragment newInstance(Transaction transaction, String activityDefaultTitle) {
        SendReceiptFragment fragment = new SendReceiptFragment();
        fragment.setTransaction(transaction);
        fragment.setActivityDefaultTitle(activityDefaultTitle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_receipt, container, false);
        mProgressView = (ImageView)view.findViewById(R.id.progress_view);

        int color = PaymentController.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary();
        int secondaryColor = PaymentController.getInitializedInstance().getConfiguration().getAppearance().getColorPrimaryDark();

        mIconView = (TextView) view.findViewById(R.id.status_icon_view);
        mIconView.setTypeface(UIHelper.createAwesomeFontTypeface(view.getContext()));
        mIconView.setTextColor(color);
        mIconView.setText(getString(R.string.fa_email));

        mEmailView = (EditText) view.findViewById(R.id.email_address_view);

        mSendButton = (Button) view.findViewById(R.id.send_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailView.getText().toString();
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    hideSoftKeyboard();
                    mSendButton.setEnabled(false);

                    StatefulTransactionProviderProxy.getInstance().sendReceipt(email);
                } else {
                    showErrorDialog(getString(R.string.email_invalid));
                }
            }
        });

        mProgressView.setAnimation(null);
        mProgressView.setVisibility(View.INVISIBLE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.send_receipt);
        StatefulTransactionProviderProxy.getInstance().attachSendReceiptCallback(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().setTitle(mActivityDefaultTitle);
        StatefulTransactionProviderProxy.getInstance().attachSendReceiptCallback(null);
    }

    private void setTransaction(Transaction transaction) {
        mTransaction = transaction;
    }

    private void setActivityDefaultTitle(String activityDefaultTitle) {
        mActivityDefaultTitle = activityDefaultTitle;
    }

    @Override
    public void onSendingStarted() {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotation);
        mProgressView.setAnimation(animation);
        mProgressView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCompleted(MposError error) {
        mSendButton.setEnabled(true);
        mProgressView.setAnimation(null);
        mProgressView.setVisibility(View.INVISIBLE);

        if (error == null) {
            Toast.makeText(getActivity(), R.string.receipt_sent, Toast.LENGTH_LONG).show();
            getPaymentInteractionListener().onReceiptSent();
        } else {
            showErrorDialog(error);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(R.string.error)
                .setMessage(message)
                .setPositiveButton(R.string.close_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}
