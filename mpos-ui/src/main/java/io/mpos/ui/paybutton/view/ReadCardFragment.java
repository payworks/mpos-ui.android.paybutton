package io.mpos.ui.paybutton.view;


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

import io.mpos.transactionprovider.CardProcessDetails;
import io.mpos.transactionprovider.CardProcessDetailsState;
import io.mpos.transactionprovider.CardProcessDetailsStateDetails;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.util.UiHelper;

public class ReadCardFragment extends Fragment {

    public static final String TAG = "ReadCardFragment";

    public interface Interaction {

        void onReadCardAbortButtonClicked();

    }

    private Interaction mInteractionActivity;

    private ImageView mProgressView;
    private TextView mStatusView;
    private TextView mIconView;
    private Button mAbortButton;

    private CardProcessDetails mProcessDetails;
    private boolean mAbortable;

    public static ReadCardFragment newInstance() {
        return new ReadCardFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.mpu_fragment_read_card, container, false);
        mStatusView = (TextView) rootView.findViewById(R.id.mpu_status_view);
        mProgressView = (ImageView) rootView.findViewById(R.id.mpu_progress_view);

        int color = MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary();
        mIconView = (TextView) rootView.findViewById(R.id.mpu_status_icon_view);
        mIconView.setTypeface(UiHelper.createAwesomeFontTypeface(rootView.getContext()));
        mIconView.setTextColor(color);

        mAbortButton = (Button) rootView.findViewById(R.id.mpu_abort_button);
        mAbortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionActivity.onReadCardAbortButtonClicked();
                mAbortButton.setVisibility(View.GONE);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateViews();
    }

    public void updateStatus(CardProcessDetails details, boolean abortable) {
        mProcessDetails = details;
        mAbortable = abortable;
        updateViews();
    }

    private void updateViews() {
        if (mStatusView == null || mProcessDetails == null) {
            return;
        }

        int visibility = mAbortable ? View.VISIBLE : View.INVISIBLE;
        mAbortButton.setVisibility(visibility);
        String msg = UiHelper.joinAndTrimStatusInformation(mProcessDetails.getInformation());
        mStatusView.setText(msg);
        mIconView.setText(statusIcon(mProcessDetails.getState(), mProcessDetails.getStateDetails()));
        if (showProgressView(mProcessDetails.getState())) {
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

    private boolean showProgressView(CardProcessDetailsState state) {
        switch (state) {
            case CREATED:
            case CONNECTING_TO_ACCESSORY:
                return true;
            case WAITING_FOR_CARD_PRESENTATION:
            case COMPLETED:
            case FAILED:
            case ABORTED:
                return false;
        }
        return false;
    }

    private String statusIcon(CardProcessDetailsState state, CardProcessDetailsStateDetails stateDetails) {
        switch (stateDetails) {
            case CONNECTING_TO_ACCESSORY:
            case CONNECTING_TO_ACCESSORY_CHECKING_FOR_UPDATE:
            case CONNECTING_TO_ACCESSORY_UPDATING:
                return getString(R.string.mpu_fa_lock);
            case CONNECTING_TO_ACCESSORY_WAITING_FOR_READER:
                return getString(R.string.mpu_fa_search);
        }

        switch (state) {
            case CREATED:
                return getString(R.string.mpu_fa_lock);
            case WAITING_FOR_CARD_PRESENTATION:
                return getString(R.string.mpu_fa_credit_card);
            case FAILED:
                return getString(R.string.mpu_fa_times_circle);
        }

        return "";
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mInteractionActivity = (Interaction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ReadCardFragment.Interaction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInteractionActivity = null;
    }

}
