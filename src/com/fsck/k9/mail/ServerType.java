package com.fsck.k9.mail;

import com.fsck.k9.R;
import com.fsck.k9.activity.setup.LocalePrintable;

public enum ServerType implements LocalePrintable{
    /*
        (ui_string_res_id, plain_port, ssl_port, tls_port
     */
    IMAP(R.string.account_setup_account_type_imap_action, 143, 993, 143),
    POP3(R.string.account_setup_account_type_pop_action, 110, 995, 110),
    SMTP(R.string.account_setup_outgoing_smtp_server_label, 25, 465, 587),      // TODO action not label
    WEBDAV(R.string.account_setup_account_type_webdav_action, 80, 443, 443),       // webdav not in mozilla xml, ports just guess
    UNSET(-1, -1, -1, -1); // webdav not in mozilla xml

    private final int resourceId;
    private final int plainPort;
    private final int sslPort;
    private final int tlsPort;

    ServerType(int resourceId, int plainPort, int sslPort, int tlsPort) {
        this.resourceId = resourceId;
        this.plainPort = plainPort;
        this.sslPort = sslPort;
        this.tlsPort = tlsPort;
    }

    @Override
    public int getResourceId() {
        return resourceId;
    }

    public int getPort(ConnectionSecurity conSec) {
        switch (conSec) {
            case SSL_TLS_OPTIONAL:
            case SSL_TLS_REQUIRED:
                return sslPort;
            case STARTTLS_OPTIONAL:
            case STARTTLS_REQUIRED:
                return tlsPort;
            default:
                return plainPort;
        }
    }
}
