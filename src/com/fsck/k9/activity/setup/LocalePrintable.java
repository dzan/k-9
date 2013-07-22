package com.fsck.k9.activity.setup;

public interface LocalePrintable {
    // todo make sure this one is never actually used
    public static final int NO_RESOURCE_ID = -1;

    public int getResourceId();
    public String name();
}
