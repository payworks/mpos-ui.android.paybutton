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


import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import io.mpos.paymentdetails.ApplicationInformation;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.util.UiHelper;


public class ApplicationSelectionFragment extends AbstractTransactionFragment {

    public final static String TAG = "ApplicationSelectionFragment";

    private List<ApplicationInformation> mApplicationInformations;

    public static ApplicationSelectionFragment newInstance(List<ApplicationInformation> applications) {
        ApplicationSelectionFragment fragment = new ApplicationSelectionFragment();
        fragment.setApplicationInformations(applications);
        return fragment;
    }

    public class ApplicationSelectionAdapter extends ArrayAdapter<ApplicationInformation> {

        public ApplicationSelectionAdapter(Context context) {
            super(context, 0, 0, mApplicationInformations);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ApplicationInformation applicationInformation = mApplicationInformations.get(position);
            TextView tv = new TextView(getContext());
            tv.setText(applicationInformation.getApplicationName());
            tv.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
            int padding = (int) getResources().getDimension(R.dimen.mpu_activity_horizontal_margin);
            tv.setPadding(padding, 0, padding, 0);
            tv.setMinHeight(UiHelper.dpToPx(getContext(), 52));
            tv.setGravity(Gravity.CENTER_VERTICAL);
            return tv;
        }
    }

    public ApplicationSelectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.mpu_fragment_application_selection, container, false);

        int textColor = MposUi.getInitializedInstance().getConfiguration().getAppearance().getTextColorPrimary();
        int backgroundColor = MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary();

        TextView headerView = (TextView) view.findViewById(R.id.mpu_header_view);
        headerView.setTextColor(textColor);
        headerView.setBackgroundColor(backgroundColor);

        final ListView listView = (ListView) view.findViewById(R.id.mpu_application_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApplicationInformation applicationInformation = (ApplicationInformation) listView.getItemAtPosition(position);
                getInteractionActivity().onApplicationSelected(applicationInformation);
            }
        });
        listView.setAdapter(new ApplicationSelectionAdapter(view.getContext()));
        view.findViewById(R.id.mpu_abort_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInteractionActivity().onAbortTransactionButtonClicked();
            }
        });
        return view;
    }


    public void setApplicationInformations(List<ApplicationInformation> applicationInformations) {
        mApplicationInformations = applicationInformations;
    }
}
