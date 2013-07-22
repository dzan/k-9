package com.fsck.k9.mail;

import android.os.Bundle;
import com.fsck.k9.Account;

/**
 * This is an abstraction to get rid of the store- and transport-specific URIs.
 *
 * <p>
 * Right now it's only used for settings import/export. But the goal is to get rid of
 * store/transport URIs altogether.
 * </p>
 *
 * @see Account#getStoreUri()
 * @see Account#getTransportUri()
 */
public class ServerSettings {

    /**
     * Some known extra options
     */
    // IMAP
    public static final String IMAP_AUTODETECT_NAMESPACE_KEY = "autoDetectNamespace";               // boolean
    public static final String IMAP_PATH_PREFIX_KEY = "pathPrefix";                                 // string

    // POP3
    public static final String POP3_LEAVE_MESSAGES_ON_SERVER = "pop3LeaveMessagesOnServer";         // ispdb, boolean
    public static final String POP3_DOWNLOAD_ON_BIFF = "pop3DownloadOnBiff";                        // ispdb, boolean
    public static final String POP3_DAYS_TO_LEAVE_MESSAGES_ON_SERVER = "pop3DaysToLeaveMessagesOnServer"; // ispdb, int
    public static final String POP3_CHECK_INTERVAL = "pop3CheckInterval";                           // ispdb, int

    // SMTP
    public static final String SMTP_RESTRICTION = "smtpRestriction";                                // ispdb, string
    public static final String SMTP_ADD_THIS_SERVER = "smtpAddThisServer";                          // ispdb, boolean
    public static final String SMTP_USE_GLOBAL_PREFERRED_SERVER = "smtpUseGlobalPreferredServer";   // ispdb, boolean

    // WEBDAV
    public static final String WEBDAV_ALIAS_KEY = "alias";                                          // string
    public static final String WEBDAV_PATH_KEY = "path";                                            // string
    public static final String WEBDAV_AUTH_PATH_KEY = "authPath";                                   // string
    public static final String WEBDAV_MAILBOX_PATH_KEY = "mailboxPath";                             // string

    /**
     * Name of the store or transport type (e.g. "IMAP").
     */
    public ServerType type;

    /**
     * The host name of the server.
     *
     * {@code null} if not applicable for the store or transport.
     */
    public String host;

    /**
     * The port number of the server.
     *
     * {@code -1} if not applicable for the store or transport.
     */
    public int port;

    /**
     * The type of connection security to be used when connecting to the server.
     *
     * {@link ConnectionSecurity#NONE} if not applicable for the store or transport.
     */
    public ConnectionSecurity connectionSecurity;

    /**
     * The authentication method to use when connecting to the server.
     *
     * {@code null} if not applicable for the store or transport.
     */
    public AuthenticationType authenticationType;

    /**
     * The username part of the credentials needed to authenticate to the server.
     *
     * {@code null} if not applicable for the store or transport.
     */
    public String username;

    /**
     * The password part of the credentials needed to authenticate to the server.
     *
     * {@code null} if not applicable for the store or transport.
     */
    public String password;

    /**
     * Store- or transport-specific settings as key/value pair.
     *
     * {@code null} if not applicable for the store or transport.
     */
    public Bundle extra;


    /**
     * Creates a new {@code ServerSettings} object.
     *
     * @param type
     *         see {@link ServerSettings#type}
     * @param host
     *         see {@link ServerSettings#host}
     * @param port
     *         see {@link ServerSettings#port}
     * @param connectionSecurity
     *         see {@link ServerSettings#connectionSecurity}
     * @param authenticationType
     *         see {@link ServerSettings#authenticationType}
     * @param username
     *         see {@link ServerSettings#username}
     * @param password
     *         see {@link ServerSettings#password}
     */
    public ServerSettings(ServerType type, String host, int port,
            ConnectionSecurity connectionSecurity, AuthenticationType authenticationType, String username,
            String password) {
        this.type = type;
        this.host = host;
        this.port = port;
        this.connectionSecurity = connectionSecurity;
        this.authenticationType = authenticationType;
        this.username = username;
        this.password = password;
        this.extra = new Bundle();
    }

    /**
     * Creates a new {@code ServerSettings} object.
     *
     * @param type
     *         see {@link ServerSettings#type}
     * @param host
     *         see {@link ServerSettings#host}
     * @param port
     *         see {@link ServerSettings#port}
     * @param connectionSecurity
     *         see {@link ServerSettings#connectionSecurity}
     * @param authenticationType
     *         see {@link ServerSettings#authenticationType}
     * @param username
     *         see {@link ServerSettings#username}
     * @param password
     *         see {@link ServerSettings#password}
     * @param extra
     *         see {@link ServerSettings#extra}
     */
    public ServerSettings(ServerType type, String host, int port,
            ConnectionSecurity connectionSecurity, AuthenticationType authenticationType, String username,
            String password, Bundle extra) {
        this.type = type;
        this.host = host;
        this.port = port;
        this.connectionSecurity = connectionSecurity;
        this.authenticationType = authenticationType;
        this.username = username;
        this.password = password;
        this.extra = extra;
    }

    /**
     * Creates an "empty" {@code ServerSettings} object.
     *
     * Everything but {@link ServerSettings#type} is unused.
     *
     * @param type
     *         see {@link ServerSettings#type}
     */
    public ServerSettings(ServerType type) {
        this.type = type;
        host = null;
        port = -1;
        connectionSecurity = ConnectionSecurity.NONE;
        authenticationType = null;
        username = null;
        password = null;
        extra = new Bundle();
    }

    /**
     * Returns store- or transport-specific settings as key/value pair.
     *
     * @return additional set of settings as key/value pair.
     */
    public Bundle getExtra() {
        return extra;
    }

    public ServerSettings newPassword(String newPassword) {
        this.password = newPassword;
        return this;
    }

    /**
     * Used in Store / Account to identify different store instances.
     */
    public String getHash() {
        return (host+username+password+port);
    }
}