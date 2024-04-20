package org.springframework.ai.baiduai.qianfan.util;

import org.springframework.ai.chat.messages.Message;

public class ApiUtils {


    public static String toRole(Message message) {
        switch (message.getMessageType()) {
            case USER:
                return Role.USER;
            case ASSISTANT:
                return Role.ASSISTANT;
            case SYSTEM:
                return Role.SYSTEM;
            default:
                throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
        }
    }

}
