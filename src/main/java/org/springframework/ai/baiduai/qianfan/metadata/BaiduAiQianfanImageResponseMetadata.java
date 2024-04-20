package org.springframework.ai.baiduai.qianfan.metadata;

import com.baidubce.qianfan.model.image.Text2ImageResponse;
import org.springframework.ai.image.ImageResponseMetadata;
import org.springframework.util.Assert;

import java.util.Objects;

public class BaiduAiQianfanImageResponseMetadata implements ImageResponseMetadata {

    private final Long created;

    public static BaiduAiQianfanImageResponseMetadata from(Text2ImageResponse imageResponse) {
        Assert.notNull(imageResponse, "Text2ImageResponse must not be null");
        return new BaiduAiQianfanImageResponseMetadata(imageResponse.getCreated());
    }

    protected BaiduAiQianfanImageResponseMetadata(Long created) {
        this.created = created;
    }

    @Override
    public Long created() {
        return this.created;
    }

    @Override
    public String toString() {
        return "QianfanImageResponseMetadata{" + "created=" + created + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BaiduAiQianfanImageResponseMetadata that))
            return false;
        return Objects.equals(created, that.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(created);
    }

}
