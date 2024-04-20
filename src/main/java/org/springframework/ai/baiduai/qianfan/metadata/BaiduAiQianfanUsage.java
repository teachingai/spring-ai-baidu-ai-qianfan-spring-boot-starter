package org.springframework.ai.baiduai.qianfan.metadata;

import com.baidubce.qianfan.model.chat.ChatUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

import java.util.Optional;

public class BaiduAiQianfanUsage implements Usage {

    public static BaiduAiQianfanUsage from(ChatUsage usage) {
        return new BaiduAiQianfanUsage(usage);
    }

    private final ChatUsage usage;

    protected BaiduAiQianfanUsage(ChatUsage usage) {
        Assert.notNull(usage, "Baidu AI ChatUsage must not be null");
        this.usage = usage;
    }

    protected ChatUsage getUsage() {
        return this.usage;
    }

    @Override
    public Long getPromptTokens() {
        return Optional.ofNullable(getUsage().getPromptTokens()).map(Integer::longValue).orElse(-1L);
    }

    @Override
    public Long getGenerationTokens() {
        return Optional.ofNullable(getUsage().getCompletionTokens()).map(Integer::longValue).orElse(-1L);
    }

    @Override
    public Long getTotalTokens() {
        return Optional.ofNullable(getUsage().getTotalTokens()).map(Integer::longValue).orElse(-1L);
    }

    @Override
    public String toString() {
        return getUsage().toString();
    }

}
