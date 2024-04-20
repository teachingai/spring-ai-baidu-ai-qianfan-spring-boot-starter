package org.springframework.ai.baiduai.qianfan.metadata;

import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatResp;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

public class BaiduAiQianfanChatResponseMetadata implements ChatResponseMetadata {

    public static BaiduAiQianfanChatResponseMetadata from(PanguChatResp response) {
        Assert.notNull(response, "Huawei PanguChatResp must not be null");
        BaiduAiQianfanUsage usage = BaiduAiQianfanUsage.from(response.getUsage());
        BaiduAiQianfanChatResponseMetadata chatResponseMetadata = new BaiduAiQianfanChatResponseMetadata(response.getId(), usage);
        return chatResponseMetadata;
    }

    private final String id;
    private final Usage usage;

    public BaiduAiQianfanChatResponseMetadata(String id, BaiduAiQianfanUsage usage) {
        this.id = id;
        this.usage = usage;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public Usage getUsage() {
        Usage usage = this.usage;
        return usage != null ? usage : new EmptyUsage();
    }

}
