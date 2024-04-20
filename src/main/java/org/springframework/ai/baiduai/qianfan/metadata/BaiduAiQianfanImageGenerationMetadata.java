package org.springframework.ai.baiduai.qianfan.metadata;

import org.springframework.ai.image.ImageGenerationMetadata;

import java.util.Objects;

public class BaiduAiQianfanImageGenerationMetadata implements ImageGenerationMetadata {

    private String revisedPrompt;

    public BaiduAiQianfanImageGenerationMetadata(String revisedPrompt) {
        this.revisedPrompt = revisedPrompt;
    }

    public String getRevisedPrompt() {
        return revisedPrompt;
    }

    @Override
    public String toString() {
        return "QianfanImageGenerationMetadata{" + "revisedPrompt='" + revisedPrompt + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaiduAiQianfanImageGenerationMetadata that)) {
            return false;
        }
        return Objects.equals(revisedPrompt, that.revisedPrompt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(revisedPrompt);
    }

}
