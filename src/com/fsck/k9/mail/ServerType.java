package com.fsck.k9.mail;

public enum ServerType {
    IMAP(0, "imap"), POP3(1, "pop3"), SMTP(2, "smtp"), WEBDAV(3, "webdav"), UNSET(4,""); // webdav not in mozilla xml
    //UNSET(4, ""), NO_VALUE(5, ""), WRONG_TAG(6, "");

    private int type;
    private String schemeName;

    ServerType(int type, String schemeName) {
        this.type = type;
        this.schemeName = schemeName;
    }
}
