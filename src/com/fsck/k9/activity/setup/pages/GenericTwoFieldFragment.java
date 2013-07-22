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

package com.fsck.k9.activity.setup.pages;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.fsck.k9.R;
import com.fsck.k9.helper.wizard.ui.PageFragmentCallbacks;

public class GenericTwoFieldFragment extends Fragment {
    private static final String ARG_KEY = "key";
    private static final String ARG_KEY1 = "key1";
    private static final String ARG_KEY2 = "key2";
    private static final String ARG_LABEL1 = "label1";
    private static final String ARG_LABEL2 = "label2";
    private static final String ARG_INPUTTYPE1 = "it1";
    private static final String ARG_INPUTTYPE2 = "it2";

    private PageFragmentCallbacks mCallbacks;
    private String mPageKey;
    private GenericTwoFieldPage mPage;
    private TextView mField1View;
    private TextView mField2View;

    private String mKey1;
    private String mLabel1;
    private int mInputType1;
    private String mKey2;
    private String mLabel2;
    private int mInputType2;

    public static GenericTwoFieldFragment create(String pageKey,
        String KEY1, String label1, int inputType1,
        String KEY2, String label2, int inputType2) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, pageKey);

        args.putString(ARG_KEY1, KEY1);
        args.putString(ARG_KEY2, KEY2);
        args.putString(ARG_LABEL1, label1);
        args.putString(ARG_LABEL2, label2);
        args.putInt(ARG_INPUTTYPE1, inputType1);
        args.putInt(ARG_INPUTTYPE2, inputType2);

        GenericTwoFieldFragment fragment = new GenericTwoFieldFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public GenericTwoFieldFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mPageKey = args.getString(ARG_KEY);
        mPage = (GenericTwoFieldPage) mCallbacks.onGetPage(mPageKey);

        mKey1 = args.getString(ARG_KEY1);
        mKey2 = args.getString(ARG_KEY2);
        mLabel1 = args.getString(ARG_LABEL1);
        mLabel2 = args.getString(ARG_LABEL2);
        mInputType1 = args.getInt(ARG_INPUTTYPE1);
        mInputType2 = args.getInt(ARG_INPUTTYPE2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.wizard_frag_generictwofield, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        mField1View = ((TextView) rootView.findViewById(R.id.wizard_field1));
        mField1View.setText(mPage.getData().getString(mKey1));
        mField1View.setInputType(mInputType1);

        mField2View = ((TextView) rootView.findViewById(R.id.wizard_field2));
        mField2View.setText(mPage.getData().getString(mKey2));
        mField2View.setInputType(mInputType2);

        ((TextView) rootView.findViewById(R.id.wizard_label_field1)).setText(mLabel1);
        ((TextView) rootView.findViewById(R.id.wizard_label_field2)).setText(mLabel2);

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mField1View.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                          int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(mKey1,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });

        mField2View.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                          int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(mKey2,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        // In a future update to the support library, this should override setUserVisibleHint
        // instead of setMenuVisibility.
        if (mField1View != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }
}
