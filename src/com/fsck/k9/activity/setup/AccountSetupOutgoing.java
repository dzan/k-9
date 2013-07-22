
package com.fsck.k9.activity.setup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.fsck.k9.*;
import com.fsck.k9.activity.K9Activity;
import com.fsck.k9.helper.Utility;
import com.fsck.k9.mail.AuthenticationType;
import com.fsck.k9.mail.transport.SmtpTransport;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class AccountSetupOutgoing extends K9Activity implements OnClickListener,
    OnCheckedChangeListener {
    private static final String EXTRA_ACCOUNT = "account";

    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";

    private static final int smtpPorts[] = {
        587, 465, 465, 587, 587
    };

    private static final String smtpSchemes[] = {
        "smtp", "smtp+ssl", "smtp+ssl+", "smtp+tls", "smtp+tls+"
    };
    /*
    private static final int webdavPorts[] =
    {
        80, 443, 443, 443, 443
    };
    private static final String webdavSchemes[] =
    {
        "webdav", "webdav+ssl", "webdav+ssl+", "webdav+tls", "webdav+tls+"
    };
    */
    private static final AuthenticationType authTypes[] = {
        AuthenticationType.NONE,
        AuthenticationType.LOGIN,
        AuthenticationType.PLAIN,
        AuthenticationType.CRAM_MD5
    };

    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mServerView;
    private EditText mPortView;
    private CheckBox mRequireLoginView;
    private ViewGroup mRequireLoginSettingsView;
    private Spinner mSecurityTypeView;
    private Spinner mAuthTypeView;
    private Button mNextButton;
    private Account mAccount;
    private boolean mMakeDefault;

    public static void actionOutgoingSettings(Context context, Account account, boolean makeDefault) {
        Intent i = new Intent(context, AccountSetupOutgoing.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        context.startActivity(i);
    }

    public static void actionEditOutgoingSettings(Context context, Account account) {
        context.startActivity(intentActionEditOutgoingSettings(context, account));
    }

    public static Intent intentActionEditOutgoingSettings(Context context, Account account) {
        Intent i = new Intent(context, AccountSetupOutgoing.class);
        i.setAction(Intent.ACTION_EDIT);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        return i;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onClick(View v) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}