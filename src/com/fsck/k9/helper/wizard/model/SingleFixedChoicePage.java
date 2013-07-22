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

package com.fsck.k9.helper.wizard.model;

import android.content.Context;
import com.fsck.k9.activity.setup.LocalePrintable;
import com.fsck.k9.helper.wizard.ui.SingleChoiceFragment;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A page offering the user a number of mutually exclusive choices.
 */
public class SingleFixedChoicePage extends Page {
    protected ArrayList<LocalePrintable> mChoices = new ArrayList<LocalePrintable>();

    public SingleFixedChoicePage(ModelCallbacks callbacks, String title, String dataKey) {
        super(callbacks, title, dataKey);
    }

    @Override
    public Fragment createFragment() {
        return SingleChoiceFragment.create(getKey());
    }

    public LocalePrintable getOptionAt(int position) {
        return mChoices.get(position);
    }

    public int getOptionCount() {
        return mChoices.size();
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem(getTitle(), mData.getString(mDataKey), getKey()));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(mDataKey));
    }

    public SingleFixedChoicePage setChoices(LocalePrintable... choices) {
        mChoices.addAll(Arrays.asList(choices));
        return this;
    }

    public SingleFixedChoicePage setValue(String value) {
        mData.putString(mDataKey, value);
        return this;
    }
}
