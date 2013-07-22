
package com.fsck.k9.activity.setup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.fsck.k9.*;
import com.fsck.k9.activity.K9Activity;
import com.fsck.k9.helper.Utility;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.ServerSettings;
import com.fsck.k9.mail.Store;
import com.fsck.k9.mail.store.ImapStore;
import com.fsck.k9.mail.store.Pop3Store;
import com.fsck.k9.mail.store.WebDavStore;
import com.fsck.k9.mail.store.ImapStore.ImapStoreSettings;
import com.fsck.k9.mail.store.WebDavStore.WebDavStoreSettings;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class AccountSetupIncoming extends K9Activity implements OnClickListener {
    private static final String EXTRA_ACCOUNT = "account";
    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";

    private static final int[] POP3_PORTS = {
        110, 995, 995, 110, 110
    };

    private static final int[] IMAP_PORTS = {
        143, 993, 993, 143, 143
    };

    private static final int[] WEBDAV_PORTS = {
        80, 443, 443, 443, 443
    };

    private static final ConnectionSecurity[] CONNECTION_SECURITY_TYPES = {
        ConnectionSecurity.NONE,
        ConnectionSecurity.SSL_TLS_OPTIONAL,
        ConnectionSecurity.SSL_TLS_REQUIRED,
        ConnectionSecurity.STARTTLS_OPTIONAL,
        ConnectionSecurity.STARTTLS_REQUIRED
    };

    private static final String[] AUTH_TYPES = {
        "PLAIN", "CRAM_MD5"
    };


    private int[] mAccountPorts;
    private String mStoreType;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mServerView;
    private EditText mPortView;
    private Spinner mSecurityTypeView;
    private Spinner mAuthTypeView;
    private CheckBox mImapAutoDetectNamespaceView;
    private EditText mImapPathPrefixView;
    private EditText mWebdavPathPrefixView;
    private EditText mWebdavAuthPathView;
    private EditText mWebdavMailboxPathView;
    private Button mNextButton;
    private Account mAccount;
    private boolean mMakeDefault;
    private CheckBox mCompressionMobile;
    private CheckBox mCompressionWifi;
    private CheckBox mCompressionOther;
    private CheckBox mSubscribedFoldersOnly;

    public static void actionIncomingSettings(Activity context, Account account, boolean makeDefault) {
        Intent i = new Intent(context, AccountSetupIncoming.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        context.startActivity(i);
    }

    public static void actionEditIncomingSettings(Activity context, Account account) {
        context.startActivity(intentActionEditIncomingSettings(context, account));
    }

    public static Intent intentActionEditIncomingSettings(Context context, Account account) {
        Intent i = new Intent(context, AccountSetupIncoming.class);
        i.setAction(Intent.ACTION_EDIT);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        return i;
    }

    @Override
    public void onClick(View v) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
