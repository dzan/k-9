package com.fsck.k9.mail;

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
public enum ConnectionSecurity {
    NONE(""),
    STARTTLS_OPTIONAL("tls"),
    STARTTLS_REQUIRED("tls"),
    SSL_TLS_OPTIONAL("ssl"),
    SSL_TLS_REQUIRED("ssl");

    private String schemeName;

    ConnectionSecurity(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getSchemeName() {
        return schemeName;
    }
}
