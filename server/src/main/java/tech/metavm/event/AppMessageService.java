package tech.metavm.event;

import tech.metavm.message.Message;

public interface AppMessageService {
    void sendMessage(Message message);
}
