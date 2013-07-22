package com.fsck.k9.activity.setup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import com.fsck.k9.EmailAddressValidator;
import com.fsck.k9.K9;
import com.fsck.k9.R;
import com.fsck.k9.activity.setup.autoconfiguration.EmailConfigurationData;
import com.fsck.k9.activity.setup.pages.AutoConfigurationPage;
import com.fsck.k9.activity.setup.pages.BasicAccountInfoPage;
import com.fsck.k9.activity.setup.pages.GenericTwoFieldPage;
import com.fsck.k9.helper.wizard.model.*;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.ServerType;

import java.util.HashSet;

public class AccountSetupModel extends AbstractWizardModel {
    /*
        All keys used to store data in the wizard pages
     */
    public static final String EMAIL_ADDRESS_KEY = "EMAIL_ADDRESS_KEY";

    public static final String INCOMING_USERNAME_KEY = "INCOMING_USERNAME";
    public static final String INCOMING_PASSWORD_KEY = "INCOMING_PASSWORD";

    public static final String INCOMING_CONNECTION_SECURITY = "INCOMING_CONNECTION_SECURITY";
    public static final String INCOMING_SERVER_TYPE = "INCOMING_SERVER_TYPE";

    public static final String CONFIGURATION_KEY = "CONFIGURATION_DATA";

    public static final String FULL_NAME_KEY = "FULL_NAME";
    public static final String ACCOUNT_NAME_KEY = "ACCOUNT_NAME";

    /*
        IMPORTANT !!!
        Put every key also in this enum, the name must be EXACTLY the same as the string value.
        This is used to speed up detection in onPageDataChanged by using an enum switch instead of
        a bunch of if-elseif-else statements. We use Strings because of the WizardPager architecture.
     */
    public static enum WizardDataKeys {
        INCOMING_USERNAME, INCOMING_PASSWORD, INCOMING_CONNECTION_SECURITY, INCOMING_SERVER_TYPE,
        CONFIGURATION_DATA, FULL_NAME, ACCOUNT_NAME, EMAIL_ADDRESS,
        NONE
    }

    private String mUserPassword;
    private String mUserLogin;

    private enum NewAccountOptions implements LocalePrintable{
        OTHER(R.string.wizard_which_account_other),
        MANUAL(R.string.wizard_which_account_manual);

        private final int resourceId;

        NewAccountOptions(int resourceId) {
            this.resourceId = resourceId;
        }


        @Override
        public int getResourceId() {
            return resourceId;
        }
    }

    private EmailConfigurationData mConfigurationData;

    /*
        We implement a singleton pattern here.
     */
    private static AccountSetupModel mInstance = null;

    public static AccountSetupModel getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AccountSetupModel(context);
        }

        return mInstance;
    }

    private AccountSetupModel(Context context) {
        super(context);
        this.mConfigurationData = new EmailConfigurationData();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Called when the user enters or changed data on one of the setup pages. We then use the key of the data field
     * to identify the type and update our configuration data object.
     *
     * @param page Page that notified the data update.
     */
    @Override
    public void onPageDataChanged(Page page) {
        super.onPageDataChanged(page);
        Bundle pageData = page.getData();

        // update our configuration data
        // TODO we might need to make a copy here for concurrency, not sure
        WizardDataKeys key = WizardDataKeys.NONE;
        for (String keyString : pageData.keySet()) {
            try{
                key = WizardDataKeys.valueOf(keyString);
            } catch (IllegalArgumentException ex) {};

            /*
               We save the changed data in our configuration data model
             */
            switch (key) {
                case EMAIL_ADDRESS:
                    /*
                        This is the key for the very first wizard page. Possible information gained is an user e-mail
                        address / login => fall through
                     */
                case INCOMING_USERNAME:
                    mUserLogin = pageData.getString(INCOMING_USERNAME_KEY);
                    EmailAddressValidator addressValidator = new EmailAddressValidator();

                    if (mUserLogin == null) {
                        String tmpAddress = pageData.getString(EMAIL_ADDRESS_KEY);
                        if (addressValidator.isValidAddressOnly(tmpAddress)) {
                            mUserLogin = tmpAddress;
                        } else {
                            return;
                        }
                    }

                    mConfigurationData.email = mUserLogin;

                    /*
                        using the ispdb the username gets set, if it wasn't set we are in a manual setup so we only
                        set the active server option
                        we set the outgoing to be identical, if not it will be overwritten later
                     */
                    mConfigurationData.getActiveIncoming().username = mUserLogin;
                    mConfigurationData.getActiveOutgoing().username = mUserLogin;
                    break;
                case INCOMING_PASSWORD:
                    mUserPassword = pageData.getString(INCOMING_PASSWORD_KEY);

                    mConfigurationData.getActiveIncoming(); // generate on if no server was in yet
                    for (int i=0; i < mConfigurationData.incomingServer.size(); ++i) {
                        mConfigurationData.incomingServer.get(i).password = mUserPassword;
                    }

                    /*
                        we set the outgoing to be identical, if not it will be overwritten later, if no login is needed
                        it won't be used
                     */
                    mConfigurationData.getActiveOutgoing();
                    for (int i=0; i < mConfigurationData.outgoingServer.size(); ++i) {
                        mConfigurationData.outgoingServer.get(i).password = mUserPassword;
                    }
                    break;
                case INCOMING_SERVER_TYPE:
                    mConfigurationData.getActiveIncoming().type =
                            ServerType.valueOf(pageData.getString(INCOMING_SERVER_TYPE));
                    break;
                case INCOMING_CONNECTION_SECURITY:
                    mConfigurationData.incomingServer.get(0).connectionSecurity =
                            ConnectionSecurity.valueOf(pageData.getString(INCOMING_CONNECTION_SECURITY));
                    break;
                case CONFIGURATION_DATA:
                    // set data, we now lost username & password
                    mConfigurationData = pageData.getParcelable(CONFIGURATION_KEY);
                    mConfigurationData.email = mUserLogin;

                    // set password
                    for (int i=0; i < mConfigurationData.incomingServer.size(); ++i) {
                        mConfigurationData.incomingServer.get(i).password = mUserPassword;
                    }
                    for (int i=0; i < mConfigurationData.outgoingServer.size(); ++i) {
                        mConfigurationData.outgoingServer.get(i).password = mUserPassword;
                    }

                    // set username
                    String username;
                    for (int i=0; i < mConfigurationData.incomingServer.size(); ++i) {
                        username = mConfigurationData.incomingServer.get(i).username;

                        if ( username == null || username.isEmpty() ) {
                            mConfigurationData.incomingServer.get(i).username = mUserLogin;
                        }
                    }
                    for (int i=0; i < mConfigurationData.outgoingServer.size(); ++i) {
                        username = mConfigurationData.outgoingServer.get(i).username;

                        if ( username == null || username.isEmpty() ) {
                            mConfigurationData.outgoingServer.get(i).username = mUserLogin;
                        }
                    }
                    break;
                case FULL_NAME:
                    break;
                case ACCOUNT_NAME:
                    break;
                case NONE:
                default:
                    // should not happen
                    Log.e(K9.LOG_TAG, "Unknown key in wizard pages data: " + keyString);
            }
        }
    }

    // TODO maybe return a copy or something to prevent changes
    public EmailConfigurationData getConfigurationData() {
        return mConfigurationData;
    }

    @Override
    protected PageList onNewRootPageList() {
        /*
            Account Type
                - add all accounts detected on the device
                - add all exported accounts detected
                - add "other" option to manually enter email + password
                - add "import" option to ask for a file
         */
        BranchPage accountsPage =  new BranchPage(this, mContext.getString(R.string.wizard_which_account_title), EMAIL_ADDRESS_KEY);
        accountsPage.setRequired(true);

        /****************************************************************************************
            Create some setup pages used in different steps later on
         ***************************************************************************************/
        /*
            create incoming login pages
         */
        String usernameLabel = mContext.getString(R.string.account_setup_incoming_username_label);
        String passwordLabel = mContext.getString(R.string.account_setup_incoming_password_label);
        GenericTwoFieldPage incomingLoginPage = new GenericTwoFieldPage(this, mContext.getString(R.string.account_setup_incoming_title),
                INCOMING_USERNAME_KEY, usernameLabel, usernameLabel, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, true,
                INCOMING_PASSWORD_KEY, passwordLabel, passwordLabel, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, true);
        incomingLoginPage.setRequired(true);


        /****************************************************************************************
            Add accounts to be imported
         ***************************************************************************************/
        // TODO scan and add all import files
        // TODO add import option


        /****************************************************************************************
            add device accounts

            In API lvl 14+ this could be done with the content provider but
                - We support lower API's
                - It's more heavy weight ( requires 2 permissions & more io )
         ***************************************************************************************/
        Page[] deviceAccountPages = new Page[1];
        deviceAccountPages[0] = new AutoConfigurationPage(this, "Configuring account").setRequired(true);
        // todo add the pages to the array

        // find device accounts and add them as branches
        Account[] accounts = AccountManager.get(mContext).getAccounts();
        HashSet<String> uniqueAddresses = new HashSet<String>();
        EmailAddressValidator addressValidator = new EmailAddressValidator();

        for (final Account account : accounts) {
            if (addressValidator.isValidAddressOnly(account.name)) {
                if (uniqueAddresses.add(account.name)) {
                    accountsPage.addBranch(new LocalePrintable() {
                        @Override
                        public int getResourceId() {
                            return LocalePrintable.NO_RESOURCE_ID;
                        }

                        @Override
                        public String name() {
                            return account.name;
                        }
                    }, deviceAccountPages);
                }
            }
        }

        /******************************************************************************
            add "other" option, will do an auto configuration attempt
         ******************************************************************************/
        accountsPage.addBranch(NewAccountOptions.OTHER,
                new BasicAccountInfoPage(this, "Your info").setRequired(true),
                new AutoConfigurationPage(this, "Configuring account").setRequired(true));


        /*******************************************************************************
            add "manual" option
         *******************************************************************************/
        // plain, SSL, TLS,... page
        SingleFixedChoicePage securityTypePage = new SingleFixedChoicePage(this,
                mContext.getString(R.string.account_setup_incoming_security_label), INCOMING_CONNECTION_SECURITY);
        securityTypePage.setChoices(ConnectionSecurity.NONE, ConnectionSecurity.SSL_TLS_REQUIRED, ConnectionSecurity.STARTTLS_REQUIRED,
                ConnectionSecurity.SSL_TLS_OPTIONAL, ConnectionSecurity.STARTTLS_OPTIONAL);
        securityTypePage.setRequired(true);

        accountsPage.addBranch(NewAccountOptions.MANUAL,
                new BranchPage(this, mContext.getString(R.string.account_setup_account_type_title), INCOMING_SERVER_TYPE)
                        .addBranch(ServerType.IMAP, incomingLoginPage, securityTypePage)
                        .addBranch(ServerType.POP3)
                        .addBranch(ServerType.WEBDAV)
        );

        return new PageList(
            accountsPage,
            new GenericTwoFieldPage(this, "Names",
                    ACCOUNT_NAME_KEY, mContext.getString(R.string.account_setup_names_account_name_label),
                        "Account name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL, false,
                    FULL_NAME_KEY, mContext.getString(R.string.account_setup_names_user_name_label),
                        "Your name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME, true).setRequired(true)
        );
    }
}
