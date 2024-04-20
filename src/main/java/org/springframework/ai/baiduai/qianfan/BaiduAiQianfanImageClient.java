package org.springframework.ai.baiduai.qianfan;

import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.model.image.Text2ImageRequest;
import com.baidubce.qianfan.model.image.Text2ImageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.baiduai.qianfan.metadata.BaiduAiQianfanImageGenerationMetadata;
import org.springframework.ai.baiduai.qianfan.metadata.BaiduAiQianfanImageResponseMetadata;
import org.springframework.ai.image.*;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class BaiduAiQianfanImageClient implements ImageClient {

    private final static Logger logger = LoggerFactory.getLogger(BaiduAiQianfanImageClient.class);
    private final static BaiduAiQianfanImageGenerationMetadata DEFAULT_METADATA =  new BaiduAiQianfanImageGenerationMetadata("");

    private BaiduAiQianfanImageOptions defaultOptions;

    private final Qianfan qianfan;

    public BaiduAiQianfanImageClient(Qianfan qianfan) {
        this(qianfan, BaiduAiQianfanImageOptions.builder()
                .build());
    }

    public BaiduAiQianfanImageClient(Qianfan qianfan, BaiduAiQianfanImageOptions defaultOptions) {
        Assert.notNull(qianfan, "Qianfan must not be null");
        Assert.notNull(defaultOptions, "defaultOptions must not be null");
        this.qianfan = qianfan;
        this.defaultOptions = defaultOptions;
    }

    public BaiduAiQianfanImageOptions getDefaultOptions() {
        return this.defaultOptions;
    }

    @Override
    public ImageResponse call(ImagePrompt imagePrompt) {

            String instructions = imagePrompt.getInstructions().get(0).getText();
            Text2ImageRequest imageRequest = new Text2ImageRequest().setPrompt(instructions);

            if (this.getDefaultOptions() != null) {
                imageRequest = ModelOptionsUtils.merge(this.getDefaultOptions(), imageRequest, Text2ImageRequest.class);
            }
            if (imagePrompt.getOptions() != null) {
                imageRequest = ModelOptionsUtils.merge(toQianfanAiImageOptions(imagePrompt.getOptions()), imageRequest, Text2ImageRequest.class);
            }

            // Make the request
            Text2ImageResponse imageResponseEntity = qianfan.text2Image(imageRequest);

            // Convert to org.springframework.ai.model derived ImageResponse data type
            return convertResponse(imageResponseEntity, imageRequest);
    }
    
    private ImageResponse convertResponse(Text2ImageResponse imageResponse,
                                          Text2ImageRequest imageRequest) {
        if (imageResponse == null || CollectionUtils.isEmpty(imageResponse.getData())) {
            logger.warn("No image response returned for request: {}", imageResponse);
            return new ImageResponse(List.of());
        }

        List<ImageGeneration> imageGenerationList = imageResponse.getData().stream().map(entry -> new ImageGeneration(new Image(null, entry.getB64Image()), DEFAULT_METADATA)).toList();

        ImageResponseMetadata imageResponseMetadata = BaiduAiQianfanImageResponseMetadata.from(imageResponse);
        return new ImageResponse(imageGenerationList, imageResponseMetadata);
    }

    /**
     * Convert the {@link ImageOptions} into {@link BaiduAiQianfanImageOptions}.
     * @param runtimeImageOptions the image options to use.
     * @return the converted {@link BaiduAiQianfanImageOptions}.
     */
    private BaiduAiQianfanImageOptions toQianfanAiImageOptions(ImageOptions runtimeImageOptions) {
        BaiduAiQianfanImageOptions.Builder builder = BaiduAiQianfanImageOptions.builder();
        if (runtimeImageOptions != null) {
            // Handle portable image options
            if (runtimeImageOptions.getN() != null) {
                builder.withN(runtimeImageOptions.getN());
            }
            if (runtimeImageOptions.getModel() != null) {
                builder.withModel(runtimeImageOptions.getModel());
            }
            if (runtimeImageOptions.getResponseFormat() != null) {
                builder.withResponseFormat(runtimeImageOptions.getResponseFormat());
            }
            if (runtimeImageOptions.getWidth() != null) {
                builder.withWidth(runtimeImageOptions.getWidth());
            }
            if (runtimeImageOptions.getHeight() != null) {
                builder.withHeight(runtimeImageOptions.getHeight());
            }
            // Handle QianfanAI specific image options
            if (runtimeImageOptions instanceof BaiduAiQianfanImageOptions) {
                BaiduAiQianfanImageOptions runtimeBaiduAiQianfanImageOptions = (BaiduAiQianfanImageOptions) runtimeImageOptions;

                if (runtimeBaiduAiQianfanImageOptions.getStyle() != null) {
                    builder.withStyle(runtimeBaiduAiQianfanImageOptions.getStyle());
                }
                if (runtimeBaiduAiQianfanImageOptions.getUser() != null) {
                    builder.withUser(runtimeBaiduAiQianfanImageOptions.getUser());
                }
            }
        }
        return builder.build();
    }

}
