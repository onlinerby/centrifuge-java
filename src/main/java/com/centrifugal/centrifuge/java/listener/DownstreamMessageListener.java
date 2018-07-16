package com.centrifugal.centrifuge.java.listener;

import com.centrifugal.centrifuge.java.message.DownstreamMessage;

/**
 * This file is part of centrifuge-android
 * Created by semyon on 29.04.16.
 * */
public interface DownstreamMessageListener {

    void onDownstreamMessage(final DownstreamMessage message);

}
