package org.coindirect.centrifuge.java.listener;

import org.coindirect.centrifuge.java.message.presence.PresenceMessage;

/**
 * This file is part of centrifuge-android
 * Created by semyon on 29.04.16.
 * */
public interface PresenceListener {

    void onPresence(final PresenceMessage presenceMessage);

}
