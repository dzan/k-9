package com.fsck.k9.mail;

public enum AuthenticationType {
    /*
        Coming from Mozilla ispdb
     */
    PLAIN, CRAM_MD5,
    NTLM, GSSAPI, TLS_CLIENT_CERT,  // unsupported
    CLIENT_IP, NONE,                // none

    /*
        WebDav specific
     */
    WEBDAV_FORM_BASED, WEBDAV_BASIC,

    /*
        Other
     */
    LOGIN;
};
