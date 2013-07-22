
package com.fsck.k9.mail.transport;

import android.util.Log;

import com.fsck.k9.Account;
import com.fsck.k9.K9;
import com.fsck.k9.mail.*;
import com.fsck.k9.mail.store.WebDavStore;

public class WebDavTransport extends Transport {
    public static final ServerType TRANSPORT_TYPE = ServerType.WEBDAV;

    /**
     * Decodes a WebDavTransport URI.
     *
     * <p>
     * <b>Note:</b> Everything related to sending messages via WebDAV is handled by
     * {@link WebDavStore}. So the transport URI is the same as the store URI.
     * </p>
     */
    public static ServerSettings decodeUri(String uri) {
        return WebDavStore.decodeUri(uri);
    }

    /**
     * Creates a WebDavTransport URI.
     *
     * <p>
     * <b>Note:</b> Everything related to sending messages via WebDAV is handled by
     * {@link WebDavStore}. So the transport URI is the same as the store URI.
     * </p>
     */
    public static String createUri(ServerSettings server) {
        return WebDavStore.createUri(server);
    }


    private WebDavStore store;

    public WebDavTransport(ServerSettings settings) throws MessagingException {
        if (Store.getRemoteInstance(settings) instanceof WebDavStore) {
            store = (WebDavStore) Store.getRemoteInstance(settings);
        } else {
            store = new WebDavStore(settings);
        }

        if (K9.DEBUG)
            Log.d(K9.LOG_TAG, ">>> New WebDavTransport creation complete");
    }

    public WebDavTransport(Account account) throws MessagingException {
        if (account.getRemoteStore() instanceof WebDavStore) {
            store = (WebDavStore) account.getRemoteStore();
        } else {
            store = new WebDavStore(account);
        }

        if (K9.DEBUG)
            Log.d(K9.LOG_TAG, ">>> New WebDavTransport creation complete");
    }

    @Override
    public void open() throws MessagingException {
        if (K9.DEBUG)
            Log.d(K9.LOG_TAG, ">>> open called on WebDavTransport ");

        store.getHttpClient();
    }

    @Override
    public void close() {
    }

    @Override
    public void sendMessage(Message message) throws MessagingException {
        store.sendMessages(new Message[] { message });
    }
}
