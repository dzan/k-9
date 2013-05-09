package com.fsck.k9.activity.setup.pages;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import com.fsck.k9.EmailAddressValidator;
import com.fsck.k9.helper.wizard.model.ModelCallbacks;
import com.fsck.k9.helper.wizard.model.Page;
import com.fsck.k9.helper.wizard.model.ReviewItem;

import java.util.ArrayList;

/**
 * A page asking for a name and an email.
 */
public class BasicAccountInfoPage extends Page {
    public static final String EMAIL_DATA_KEY = "email";
    public static final String PASSWORD_DATA_KEY = "password";

    private EmailAddressValidator mEmailValidator = new EmailAddressValidator();

    public BasicAccountInfoPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return BasicAccountInfoFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Your email", mData.getString(EMAIL_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Your password", mData.getString(PASSWORD_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(PASSWORD_DATA_KEY)) &&
                mEmailValidator.isValidAddressOnly(mData.getString(EMAIL_DATA_KEY));
    }
}
