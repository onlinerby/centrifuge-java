package org.coindirect.centrifuge.java.subscription;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UnsubscribeRequest {
    @Nonnull
    private String channel;

    public UnsubscribeRequest(@Nonnull final String channel) {
        this.channel = channel;
    }

    @Nonnull
    public String getChannel() {
        return channel;
    }

    public void setChannel(@Nonnull String channel) {
        this.channel = channel;
    }
}
