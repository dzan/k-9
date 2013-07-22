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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.fsck.k9.R;
import com.fsck.k9.activity.setup.AccountSetupModel;
import com.fsck.k9.activity.setup.autoconfiguration.EmailConfigurationData;
import com.fsck.k9.activity.setup.autoconfiguration.IspDbTask;
import com.fsck.k9.helper.wizard.ui.PageFragmentCallbacks;

public class AutoConfigurationFragment extends Fragment {
    private static final String ARG_KEY = "key";

    private PageFragmentCallbacks mCallbacks;
    private String mKey;
    private AutoConfigurationPage mPage;

    private TextView mProgressStatusView;

    private IspDbTask mIspDbTask = null;
    private AccountSetupModel mModel = null;

    public static AutoConfigurationFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        AutoConfigurationFragment fragment = new AutoConfigurationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public AutoConfigurationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get our arguments out
        Bundle args = getArguments();
        mKey = args.getString(ARG_KEY);
        mPage = (AutoConfigurationPage) mCallbacks.onGetPage(mKey);
        mModel = mCallbacks.onGetModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.wizard_frag_autoconfiguration, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        mProgressStatusView = ((TextView) rootView.findViewById(R.id.progress_status));
        return rootView;
    }

    /*
        Currently there is no proper "page got paged" callback so this
        is the best solution.

        TODO: check if this works for older api's otherwise switch to setMenuVisibility()
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            // create task
            mIspDbTask = new IspDbTask() {
                @Override
                protected void onProgressUpdate(String... progress) {
                    mProgressStatusView.setText(progress[0]);
                }

                @Override
                protected void onPostExecute(EmailConfigurationData emailConfigurationData) {
                    mPage.getData().putParcelable(AccountSetupModel.CONFIGURATION_KEY, emailConfigurationData);
                    mPage.getData().putBoolean(AutoConfigurationPage.FINISHED_KEY, true);
                    mPage.notifyDataChanged();
                }
            };

            // execute task
            mIspDbTask.execute(mModel.getConfigurationData().incomingServer.get(0).username);
        }
        else {
            if (mIspDbTask != null) {
                mIspDbTask.cancel(true);
            }
        }

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
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        // In a future update to the support library, this should override setUserVisibleHint
        // instead of setMenuVisibility.
        if (mProgressStatusView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }
}
