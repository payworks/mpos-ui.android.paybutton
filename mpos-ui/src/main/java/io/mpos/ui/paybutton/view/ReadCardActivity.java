package io.mpos.ui.paybutton.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import io.mpos.accessories.parameters.AccessoryParameters;
import io.mpos.errors.ErrorType;
import io.mpos.errors.MposError;
import io.mpos.transactionprovider.CardProcessDetails;
import io.mpos.transactions.CardDetails;
import io.mpos.ui.R;
import io.mpos.ui.acquirer.MposUiAccountManager;
import io.mpos.ui.acquirer.view.LoginFragment;
import io.mpos.ui.paybutton.controller.StatefulReadCardProcessProxy;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.util.ErrorHolder;
import io.mpos.ui.shared.util.UiHelper;
import io.mpos.ui.shared.util.UiState;
import io.mpos.ui.shared.view.AbstractBaseActivity;
import io.mpos.ui.shared.view.ErrorFragment;

public class ReadCardActivity extends AbstractBaseActivity implements ReadCardFragment.Interaction, ErrorFragment.Interaction, LoginFragment.Interaction {


    private static final String TAG = "ReadCardActivity";

    public final static String BUNDLE_EXTRA_ACQUIRER_LOGIN = "io.mpos.ui.paybutton.ReadCardActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN";
    public final static String BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID = "io.mpos.ui.paybutton.ReadCardActivity.BUNDLE_EXTRA_ACQUIRER_LOGIN";

    public final static String SAVED_INSTANCE_STATE_UI_STATE = "io.mpos.ui.paybutton.view.ReadCardActivity.UI_STATE";

    private boolean mIsAcquirerMode;
    private String mApplicationIdentifier;

    private StatefulReadCardProcessProxy mReadCardProcess;
    private MposUiAccountManager mMposUiAccountManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mpu_activity_read_card);
        setTitle(R.string.MPUReadCard);
        if (getCallingActivity() == null) {
            Log.w(TAG, "The transaction activity was started without startActivityForResult() and will not return a result code.");
        }

        UiHelper.setActionbarWithCustomColors(this, (android.support.v7.widget.Toolbar) findViewById(R.id.mpu_toolbar));
        ErrorHolder.getInstance().clear();

        parseExtras();
        mReadCardProcess = StatefulReadCardProcessProxy.getInstance();

        if (savedInstanceState != null) {
            setUiState((UiState) savedInstanceState.getSerializable(SAVED_INSTANCE_STATE_UI_STATE));
        }

        if (!mReadCardProcess.isOngoing() && savedInstanceState == null) {
            if (mIsAcquirerMode) {
                mMposUiAccountManager = MposUiAccountManager.getInitializedInstance();
                if (mMposUiAccountManager.isLoggedIn()) {
                    startReadCard();
                } else {
                    showLoginFragment(mApplicationIdentifier);
                }
            } else {
                startReadCard();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SAVED_INSTANCE_STATE_UI_STATE, getUiState());
        super.onSaveInstanceState(outState);
    }

    private void parseExtras() {
        if (getIntent().hasExtra(BUNDLE_EXTRA_ACQUIRER_LOGIN)) {
            mIsAcquirerMode = getIntent().hasExtra(BUNDLE_EXTRA_ACQUIRER_LOGIN);
            mApplicationIdentifier = getIntent().getStringExtra(BUNDLE_EXTRA_ACQUIRER_APPLICATION_ID);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReadCardProcess.attachCallback(mReadCardCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mReadCardProcess.attachCallback(null);
    }

    @Override
    public void onBackPressed() {
        navigateBack();
    }

    @Override
    public void navigateBack() {
        if (getUiState() == UiState.LOGIN_DISPLAYING) {
            finishWithResult(false);
        } else if (getUiState() == UiState.FORGOT_PASSWORD_DISPLAYING) {
            LoginFragment loginFragment = (LoginFragment) getFragmentManager().findFragmentByTag(LoginFragment.TAG);
            if (loginFragment != null) {
                loginFragment.setLoginMode(true);
            }
        } else if (getUiState() == UiState.READING_CARD_ERROR) {
            finishWithResult(false);
        } else {
            Toast.makeText(this, R.string.MPUBackButtonDisabled, Toast.LENGTH_LONG).show();
        }
    }

    public void startReadCard() {
        AccessoryParameters accessoryParameters = MposUi.getInitializedInstance().getConfiguration().getTerminalParameters();
        mReadCardProcess.readCard(accessoryParameters);
    }

    private StatefulReadCardProcessProxy.Callback mReadCardCallback = new StatefulReadCardProcessProxy.Callback() {
        @Override
        public void onCompleted(CardProcessDetails processDetails, CardDetails cardDetails) {
            completeAndPostResult(processDetails, cardDetails);
        }

        @Override
        public void onStatusChanged(CardProcessDetails processDetails) {
            ReadCardFragment readCardFragment = (ReadCardFragment) getFragmentManager().findFragmentByTag(ReadCardFragment.TAG);
            if (readCardFragment == null) {
                readCardFragment = ReadCardFragment.newInstance();
                getFragmentManager().beginTransaction().replace(R.id.mpu_fragment_container, readCardFragment, ReadCardFragment.TAG).commit();
            }
            showFragment(readCardFragment, ReadCardFragment.TAG, UiState.READING_CARD, false);
            readCardFragment.updateStatus(processDetails, mReadCardProcess.isAbortable());
        }
    };

    private void completeAndPostResult(CardProcessDetails processDetails, CardDetails cardDetails) {
        switch (processDetails.getState()) {
            case COMPLETED:
                postReadCardCompleted(cardDetails);
                break;
            case FAILED:
                postReadCardError(processDetails.getError());
                break;
            case ABORTED:
                postReadCardAborted();
                break;
        }
    }

    private void postReadCardCompleted(CardDetails cardDetails) {
        if (cardDetails != null) {
            finishWithResult(true);
        } else {
            finishWithResult(false);
        }
    }

    private void postReadCardError(MposError error) {
        ErrorHolder.getInstance().setError(error);
        if (mIsAcquirerMode && error.getErrorType() == ErrorType.SERVER_AUTHENTICATION_FAILED) {
            mMposUiAccountManager.logout(false);
        }
        showErrorFragment(error);
    }

    private void postReadCardAborted() {
        finishWithResult(false);
    }

    private void finishWithResult(boolean success) {
        if (success) {
            setResult(MposUi.RESULT_CODE_READ_CARD_SUCCESS);
        } else {
            setResult(MposUi.RESULT_CODE_READ_CARD_FAILED);
        }
        mReadCardProcess.teardown();
        finish();
    }

    @Override
    public void onReadCardAbortButtonClicked() {
        mReadCardProcess.requestAbort();
    }

    @Override
    public void onErrorRetryButtonClicked() {
        startReadCard();
    }

    @Override
    public void onLoginCompleted() {
        startReadCard();
    }

    @Override
    public void onLoginModeChanged(boolean loginMode) {
        if (loginMode) {
            setUiState(UiState.LOGIN_DISPLAYING);
        } else {
            setUiState(UiState.FORGOT_PASSWORD_DISPLAYING);
        }
    }

    private void showLoginFragment(String applicationIdentifier) {
        LoginFragment fragment = LoginFragment.newInstance(applicationIdentifier);
        showFragment(fragment, LoginFragment.TAG, UiState.LOGIN_DISPLAYING, true);
    }

    private void showReadCardFragment() {
        ReadCardFragment fragment = ReadCardFragment.newInstance();
        showFragment(fragment, ReadCardFragment.TAG, UiState.READING_CARD, false);
    }

    private void showErrorFragment(MposError error) {
        ErrorFragment fragment = ErrorFragment.newInstance(true, error, null);
        showFragment(fragment, ErrorFragment.TAG, UiState.READING_CARD_ERROR, true);
    }

}
