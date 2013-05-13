package com.fsck.k9.activity.setup.autoconfiguration;

import android.os.AsyncTask;

public class CheckConnectionTask extends AsyncTask<EmailConfigurationData, String, Integer> {

    private final boolean mCheckIncoming;
    private final boolean mCheckOutgoing;

    public CheckConnectionTask(boolean checkIncoming, boolean checkOutgoing) {
        this.mCheckIncoming = checkIncoming;
        this.mCheckOutgoing = checkOutgoing;
    }

    @Override
    protected Integer doInBackground(EmailConfigurationData... params) {
        /*
            Setup account

            this is used to allow easier creation of the needed remote store,
            this account should not be saved to avoid creation of non-functioning accounts

            if the store creation code was changed or extended to work with
         */


        /*
            Check incoming
         */
        if (mCheckIncoming) {

        }

        /*
            Check outgoing
         */
        if (mCheckOutgoing) {

        }

        return null;
    }
}
