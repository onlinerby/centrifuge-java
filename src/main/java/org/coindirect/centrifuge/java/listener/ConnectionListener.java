package org.coindirect.centrifuge.java.listener;

import javax.annotation.Nullable;

/**
 * This file is part of centrifuge-android
 * Created by semyon on 29.04.16.
 */
public interface ConnectionListener {

    void onWebSocketOpen();

    void onConnected(@Nullable String clientId);

    void onDisconnected(final int code, final String reason, final boolean remote, @Nullable Throwable exception);

}
