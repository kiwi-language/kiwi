package org.metavm.event;

import org.metavm.message.Message;

public interface AppMessageService {
    void sendMessage(Message message);
}
