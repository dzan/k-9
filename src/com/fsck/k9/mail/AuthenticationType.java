package com.fsck.k9.mail;

import com.fsck.k9.activity.setup.LocalePrintable;

public enum AuthenticationType implements LocalePrintable {

    /*
        Coming from Mozilla ispdb
     */
    PLAIN(LocalePrintable.NO_RESOURCE_ID),
    CRAM_MD5(LocalePrintable.NO_RESOURCE_ID),

    // unsupported
    NTLM(LocalePrintable.NO_RESOURCE_ID),
    GSSAPI(LocalePrintable.NO_RESOURCE_ID),
    TLS_CLIENT_CERT(LocalePrintable.NO_RESOURCE_ID),

    // fancy words for none
    CLIENT_IP(LocalePrintable.NO_RESOURCE_ID),
    NONE(LocalePrintable.NO_RESOURCE_ID),

    /*
        WebDav specific
     */
    WEBDAV_FORM_BASED(LocalePrintable.NO_RESOURCE_ID),
    WEBDAV_BASIC(LocalePrintable.NO_RESOURCE_ID),

    /*
        Other
     */
    LOGIN(LocalePrintable.NO_RESOURCE_ID);

    private final int resourceId;

    AuthenticationType(int resourceId) {
        this.resourceId = resourceId;
    }


    @Override
    public int getResourceId() {
        // we might want to check for undefined and return somethings that definatly won't be a res id
        return resourceId;
    }
};
