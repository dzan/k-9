/*
 * Copyright 2012 Roman Nurik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fsck.k9.helper.wizard.ui;

import com.fsck.k9.R;
import com.fsck.k9.activity.setup.LocalePrintable;
import com.fsck.k9.helper.wizard.model.Page;
import com.fsck.k9.helper.wizard.model.SingleFixedChoicePage;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SingleChoiceFragment extends ListFragment {
    private static final String ARG_KEY = "key";

    private PageFragmentCallbacks mCallbacks;
    private List<String> mChoicesStrings;
    private List<LocalePrintable> mChoices;
    private String mKey;
    private Page mPage;

    public static SingleChoiceFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        SingleChoiceFragment fragment = new SingleChoiceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SingleChoiceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mKey = args.getString(ARG_KEY);
        mPage = mCallbacks.onGetPage(mKey);

        SingleFixedChoicePage fixedChoicePage = (SingleFixedChoicePage) mPage;
        mChoicesStrings = new ArrayList<String>();
        mChoices = new ArrayList<LocalePrintable>();
        int resId;
        for (int i = 0; i < fixedChoicePage.getOptionCount(); i++) {
            mChoices.add(fixedChoicePage.getOptionAt(i));
            resId = mChoices.get(i).getResourceId();
            if (resId != LocalePrintable.NO_RESOURCE_ID) {
                mChoicesStrings.add(getString(resId));
            } else {
                mChoicesStrings.add(mChoices.get(i).name());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        final ListView listView = (ListView) rootView.findViewById(android.R.id.list);
        setListAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.wizard_choice_list_item,
                android.R.id.text1,
                mChoicesStrings));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Pre-select currently selected item.
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                String selection = mPage.getData().getString(mPage.getDataKey());
                for (int i = 0; i < mChoices.size(); i++) {
                    if (mChoices.get(i).name().equals(selection)) {
                        listView.setItemChecked(i, true);
                        break;
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof PageFragmentCallbacks)) {
            throw new ClassCastException("Activity must implement PageFragmentCallbacks");
        }

        mCallbacks = (PageFragmentCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mPage.getData().putString(mPage.getDataKey(),
                mChoices.get(position).name());
        mPage.notifyDataChanged();
    }
}
