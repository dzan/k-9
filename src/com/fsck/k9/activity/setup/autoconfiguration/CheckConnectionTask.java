package com.fsck.k9.activity.setup.autoconfiguration;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import com.fsck.k9.K9;
import com.fsck.k9.R;
import com.fsck.k9.mail.*;
import com.fsck.k9.mail.store.WebDavStore;

public class CheckConnectionTask extends AsyncTask<EmailConfigurationData, String,
        Pair<CheckConnectionTask.Status, CheckConnectionTask.Status>> {
    
    public static enum Status {
        SUCCESS,
        AUTHENTICATION_FAILED, CERTIFICATE_FAILED, OTHER_ERROR
    }

    private final boolean mCheckIncoming;
    private final boolean mCheckOutgoing;
    private final Context mContext;

    public CheckConnectionTask(Context context, boolean checkIncoming, boolean checkOutgoing) {
        this.mCheckIncoming = checkIncoming;
        this.mCheckOutgoing = checkOutgoing;
        this.mContext = context;
    }

    @Override
    protected Pair<Status, Status> doInBackground(EmailConfigurationData... params) {
        Store store = null;
        Status incomingStatus = Status.SUCCESS;
        Status outgoingStatus = Status.SUCCESS;

        /*
            Check incoming
         */
        try {
            if (mCheckIncoming) {
                store = Store.getRemoteInstance(params[0].getActiveIncoming()) ;

                if (store instanceof WebDavStore) {
                    publishProgress(mContext.getString(R.string.account_setup_check_settings_authenticate));
                } else {
                    publishProgress(mContext.getString(R.string.account_setup_check_settings_check_incoming_msg));
                }

                // check
                store.checkSettings();

                if (store instanceof WebDavStore) {
                    publishProgress(mContext.getString(R.string.account_setup_check_settings_fetch));
                }
            }
        } catch (final AuthenticationFailedException afe) {
            incomingStatus = Status.AUTHENTICATION_FAILED;
            Log.e(K9.LOG_TAG, "Error while testing settings", afe);
            publishProgress(mContext.getString(R.string.account_setup_failed_dlg_auth_message_fmt,
                    afe.getMessage() == null ? "" : afe.getMessage()));
        } catch (final CertificateValidationException cve) {
            incomingStatus = Status.CERTIFICATE_FAILED;
            Log.e(K9.LOG_TAG, "Error while testing settings", cve);
            publishProgress(mContext.getString(R.string.account_setup_failed_dlg_certificate_message_fmt));
        } catch (Throwable t) {
            incomingStatus = Status.OTHER_ERROR;
            Log.e(K9.LOG_TAG, "Error while testing settings", t);
            publishProgress(mContext.getString(R.string.account_setup_failed_dlg_server_message_fmt) +
                    (t.getMessage() == null ? "" : t.getMessage()));
        }

        /*
            Check outgoing
         */
        try {
            if (mCheckOutgoing) {
                if (!(store instanceof WebDavStore)) {
                    publishProgress(mContext.getString(R.string.account_setup_check_settings_check_outgoing_msg));
                }
                Transport transport = Transport.getInstance(params[0].getActiveOutgoing());
                transport.close();
                transport.open();
                transport.close();
            }
        } catch (final AuthenticationFailedException afe) {
            outgoingStatus = Status.AUTHENTICATION_FAILED;
            Log.e(K9.LOG_TAG, "Error while testing settings", afe);
            publishProgress(mContext.getString(R.string.account_setup_failed_dlg_auth_message_fmt,
                    afe.getMessage() == null ? "" : afe.getMessage()));
        } catch (final CertificateValidationException cve) {
            outgoingStatus = Status.CERTIFICATE_FAILED;
            Log.e(K9.LOG_TAG, "Error while testing settings", cve);
            publishProgress(mContext.getString(R.string.account_setup_failed_dlg_certificate_message_fmt));
        } catch (Throwable t) {
            outgoingStatus = Status.OTHER_ERROR;
            Log.e(K9.LOG_TAG, "Error while testing settings", t);
            publishProgress(mContext.getString(R.string.account_setup_failed_dlg_server_message_fmt) +
                    (t.getMessage() == null ? "" : t.getMessage()));
        }

        return new Pair<Status, Status>(incomingStatus, outgoingStatus);
    }
}
