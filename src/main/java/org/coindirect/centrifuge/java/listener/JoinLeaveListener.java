package org.coindirect.centrifuge.java.listener;

import org.coindirect.centrifuge.java.message.presence.JoinMessage;
import org.coindirect.centrifuge.java.message.presence.LeftMessage;

/**
 * This file is part of centrifuge-android
 * Created by semyon on 29.04.16.
 * */
public interface JoinLeaveListener {

    void onJoin(final JoinMessage joinMessage);

    void onLeave(final LeftMessage leftMessage);

}
