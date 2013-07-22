package com.fsck.k9.activity.setup.pages;

import android.support.v4.app.Fragment;
import com.fsck.k9.activity.setup.AccountSetupModel;
import com.fsck.k9.activity.setup.autoconfiguration.EmailConfigurationData;
import com.fsck.k9.helper.wizard.model.ModelCallbacks;
import com.fsck.k9.helper.wizard.model.Page;
import com.fsck.k9.helper.wizard.model.ReviewItem;

import java.util.ArrayList;

/**
 * A page asking for a name and an email.
 */
public class AutoConfigurationPage extends Page {

    public static final String FINISHED_KEY = "taskFinished";

    public AutoConfigurationPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return AutoConfigurationFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        EmailConfigurationData data = mData.getParcelable(AccountSetupModel.CONFIGURATION_KEY);

        /*
            Incoming Server
         */
        dest.add(new ReviewItem("Incoming Server", data.incomingServer.get(0).host, getKey()));
        dest.add(new ReviewItem("Type",
                data.incomingServer.get(0).connectionSecurity.name() + "(" + data.incomingServer.get(0).port + ")",
                getKey()));
        /*
            Outgoing Server
         */
    }

    @Override
    public boolean isCompleted() {
        return mData.getBoolean(FINISHED_KEY, false);
    }
}
