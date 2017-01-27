/*
 * Copyright (C) 2016 Payworks GmbH (http://www.payworks.com)
 *
 * The MIT License (MIT)
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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.mpos.paymentdetails.ApplicationInformation;
import io.mpos.ui.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.util.UiHelper;


public class SelectionFragment extends AbstractTransactionFragment {

    public final static String TAG_APPLICATION_SELECTION = "ApplicationSelFragment";
    public final static String TAG_CREDIT_DEBIT_SELECTION = "CreditDebitSelFragment";

    private final static String SAVED_INSTANCE_STATE_SELECTION_TYPE = "io.mpos.ui.paybutton.view.SelectionFragment.SELECTION_TYPE";

    private enum Type {

        APPLICATION_SELECTION,

        CREDIT_DEBIT_SELECTION

    }

    private Type mType;
    private List<ApplicationInformation> mApplicationSelectionList;
    private List<String> mCreditDebitSelectionList;

    public static SelectionFragment newInstanceForApplicationSelection(List<ApplicationInformation> applications) {
        SelectionFragment fragment = new SelectionFragment();
        fragment.setType(Type.APPLICATION_SELECTION);
        fragment.setApplicationSelectionList(applications);
        return fragment;
    }

    public static SelectionFragment newInstanceForCreditDebitSelection() {
        SelectionFragment fragment = new SelectionFragment();
        fragment.setType(Type.CREDIT_DEBIT_SELECTION);
        return fragment;
    }

    private class ApplicationSelectionAdapter extends ArrayAdapter<ApplicationInformation> {

        ApplicationSelectionAdapter(Context context) {
            super(context, 0, 0, mApplicationSelectionList);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ApplicationInformation applicationInformation = mApplicationSelectionList.get(position);
            TextView tv = new TextView(getContext());
            tv.setText(applicationInformation.getApplicationName());
            UiHelper.styleSelectionItemTextView(getContext(), tv);
            return tv;
        }
    }

    private class CreditDebitSelectionAdapter extends ArrayAdapter<String> {

        CreditDebitSelectionAdapter(Context context) {
            super(context, 0, 0, mCreditDebitSelectionList);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            String item = mCreditDebitSelectionList.get(position);
            TextView tv = new TextView(getContext());
            tv.setText(item);
            UiHelper.styleSelectionItemTextView(getContext(), tv);
            return tv;
        }
    }

    public SelectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mCreditDebitSelectionList = new ArrayList<>(2);
        mCreditDebitSelectionList.add(getString(R.string.MPUCredit));
        mCreditDebitSelectionList.add(getString(R.string.MPUDebit));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SAVED_INSTANCE_STATE_SELECTION_TYPE, mType);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mpu_fragment_application_selection, container, false);

        if (savedInstanceState != null) {
            mType = (Type) savedInstanceState.getSerializable(SAVED_INSTANCE_STATE_SELECTION_TYPE);
        }

        int textColor = MposUi.getInitializedInstance().getConfiguration().getAppearance().getTextColorPrimary();
        int backgroundColor = MposUi.getInitializedInstance().getConfiguration().getAppearance().getColorPrimary();

        TextView headerView = (TextView) view.findViewById(R.id.mpu_header_view);
        headerView.setTextColor(textColor);
        headerView.setBackgroundColor(backgroundColor);

        final ListView listView = (ListView) view.findViewById(R.id.mpu_selection_list_view);

        if (mType == Type.APPLICATION_SELECTION) {
            listView.setAdapter(new ApplicationSelectionAdapter(view.getContext()));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ApplicationInformation applicationInformation = (ApplicationInformation) listView.getItemAtPosition(position);
                    getInteractionActivity().onApplicationSelected(applicationInformation);
                }
            });
        } else {
            listView.setAdapter(new CreditDebitSelectionAdapter(view.getContext()));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int positon, long id) {
                    if (positon == 0) {
                        getInteractionActivity().onCreditSelected();
                    } else {
                        getInteractionActivity().onDebitSelected();
                    }
                }
            });
        }
        view.findViewById(R.id.mpu_abort_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInteractionActivity().onAbortTransactionButtonClicked();
            }
        });
        return view;
    }

    public void setApplicationSelectionList(List<ApplicationInformation> applicationSelectionList) {
        mApplicationSelectionList = applicationSelectionList;
    }

    public void setType(Type type) {
        this.mType = type;
    }
}
