package com.fsck.k9.mail;

import com.fsck.k9.R;
import com.fsck.k9.activity.setup.LocalePrintable;

/**
 * The currently available connection security types.
 *
 * <p>
 * Right now this enum is only used by {@link ServerSettings} and converted to store- or
 * transport-specific constants in the different {@link Store} and {@link Transport}
 * implementations. In the future we probably want to change this and use
 * {@code ConnectionSecurity} exclusively.
 * </p>
 */
public enum ConnectionSecurity implements LocalePrintable{
    NONE(R.string.account_setup_incoming_security_none_label),
    STARTTLS_OPTIONAL(R.string.account_setup_incoming_security_tls_optional_label),
    STARTTLS_REQUIRED(R.string.account_setup_incoming_security_tls_label),
    SSL_TLS_OPTIONAL(R.string.account_setup_incoming_security_ssl_optional_label),
    SSL_TLS_REQUIRED(R.string.account_setup_incoming_security_ssl_label);

    private int resourceId;

    ConnectionSecurity(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public int getResourceId() {
        return resourceId;
    }
}
