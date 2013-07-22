package com.fsck.k9.activity.setup.pages;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import com.fsck.k9.EmailAddressValidator;
import com.fsck.k9.activity.setup.AccountSetupModel;
import com.fsck.k9.helper.wizard.model.ModelCallbacks;
import com.fsck.k9.helper.wizard.model.Page;
import com.fsck.k9.helper.wizard.model.ReviewItem;

import java.util.ArrayList;

/**
 * A page asking for a name and an email.
 */
public class BasicAccountInfoPage extends Page {

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
        dest.add(new ReviewItem("Email", mData.getString(AccountSetupModel.INCOMING_USERNAME_KEY), getKey(), -1));
        dest.add(new ReviewItem("Password", mData.getString(AccountSetupModel.INCOMING_PASSWORD_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(AccountSetupModel.INCOMING_PASSWORD_KEY)) &&
                mEmailValidator.isValidAddressOnly(mData.getString(AccountSetupModel.INCOMING_USERNAME_KEY));
    }
}
