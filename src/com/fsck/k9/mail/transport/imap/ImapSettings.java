package com.fsck.k9.mail.transport.imap;

import com.fsck.k9.activity.setup.autoconfiguration.EmailConfigurationData;
import com.fsck.k9.mail.AuthenticationType;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.store.ImapStore;
import com.fsck.k9.mail.store.ImapStore.ImapConnection;

/**
 * Settings source for IMAP. Implemented in order to remove coupling between {@link ImapStore} and {@link ImapConnection}.
 */
public interface ImapSettings {
    String getHost();

    int getPort();

    ConnectionSecurity getConnectionSecurity();

    AuthenticationType getAuthType();

    String getUsername();

    String getPassword();

    boolean useCompression(int type);

    String getPathPrefix();

    void setPathPrefix(String prefix);

    String getPathDelimeter();

    void setPathDelimeter(String delimeter);

    String getCombinedPrefix();

    void setCombinedPrefix(String prefix);
}
