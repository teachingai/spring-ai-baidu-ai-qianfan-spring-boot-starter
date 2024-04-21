package org.springframework.ai.baiduai.qianfan.util;

import org.springframework.ai.chat.messages.Message;

import java.util.HashMap;
import java.util.Map;

public class ApiUtils {


    public static String toRole(Message message) {
        switch (message.getMessageType()) {
            case USER:
            case ASSISTANT:
                return message.getMessageType().getValue();
            default:
                throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
        }
    }

    public static Map<String, Object> toMap(String id, com.baidubce.qianfan.model.chat.ChatResponse response) {
        Map<String, Object> map = new HashMap<>();
        if (response.getFinishReason() != null) {
            map.put("finishReason", response.getFinishReason());
        }
        map.put("id", id);
        return map;
    }

}
