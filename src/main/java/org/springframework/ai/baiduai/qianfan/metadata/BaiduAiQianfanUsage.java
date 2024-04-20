package org.springframework.ai.baiduai.qianfan.metadata;

import com.huaweicloud.pangu.dev.sdk.client.pangu.PanguUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

public class BaiduAiQianfanUsage implements Usage {

    public static BaiduAiQianfanUsage from(PanguUsage usage) {
        return new BaiduAiQianfanUsage(usage);
    }

    private final PanguUsage usage;

    protected BaiduAiQianfanUsage(PanguUsage usage) {
        Assert.notNull(usage, "Huawei AI PanguUsage must not be null");
        this.usage = usage;
    }

    protected PanguUsage getUsage() {
        return this.usage;
    }

    @Override
    public Long getPromptTokens() {
        return getUsage().getPromptTokens();
    }

    @Override
    public Long getGenerationTokens() {
        return getUsage().getCompletionTokens();
    }

    @Override
    public Long getTotalTokens() {
        return getUsage().getTotalTokens();
    }

    @Override
    public String toString() {
        return getUsage().toString();
    }

}
