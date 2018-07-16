package com.centrifugal.centrifuge.java.listener;

import com.centrifugal.centrifuge.java.message.presence.JoinMessage;
import com.centrifugal.centrifuge.java.message.presence.LeftMessage;

/**
 * This file is part of centrifuge-android
 * Created by semyon on 29.04.16.
 * */
public interface JoinLeaveListener {

    void onJoin(final JoinMessage joinMessage);

    void onLeave(final LeftMessage leftMessage);

}
