package org.coindirect.centrifuge.java.message.event;

public final class SendEvent {

    private final String chatId;
    private final String event;
    private final String action;

    public SendEvent(final String chatId,
                     final String event,
                     final String action) {
        this.chatId = chatId;
        this.event = event;
        this.action = action;
    }

    public String getChatId() {
        return chatId;
    }

    public String getEvent() {
        return event;
    }

    public String getAction() {
        return action;
    }
}
