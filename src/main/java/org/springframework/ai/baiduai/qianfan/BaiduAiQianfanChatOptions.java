package org.springframework.ai.baiduai.qianfan;

import com.baidubce.qianfan.model.chat.Function;
import com.baidubce.qianfan.model.chat.ToolChoice;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallingOptions;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaiduAiQianfanChatOptions implements FunctionCallingOptions, ChatOptions {

    public static final Float DEFAULT_TEMPERATURE = 0.95F;

    /**
     * 所要调用的模型编码
     */
    @JsonProperty("model")
    private String model;

    /**
     * 采样温度，控制输出的随机性，必须为正数
     * （1）较高的数值会使输出更加随机，而较低的数值会使其更加集中和确定
     * （2）默认0.8，范围 (0, 1.0]，不能为0
     */
    @JsonProperty("temperature")
    private Float temperature = DEFAULT_TEMPERATURE;

    /**
     * 用温度取样的另一种方法，称为核取样取值范围是：(0.0, 1.0) 开区间，不能等于 0 或 1，默认值为 0.8
     * （1）影响输出文本的多样性，取值越大，生成文本的多样性越强
     * （2）默认0.8，取值范围 [0, 1.0]
     */
    @JsonProperty("top_p")
    private Float topP;

    /**
     * 通过对已生成的token增加惩罚，减少重复生成的现象。说明：
     * （1）值越大表示惩罚越大
     * （2）默认1.0，取值范围：[1.0, 2.0]
     */
    @JsonProperty(value = "penaltyScore")
    private Float penaltyScore;

    /**
     * 模型人设，主要用于人设设定，例如，你是xxx公司制作的AI助手，说明：
     * （1）长度限制，message中的content总长度和system字段总内容不能超过20000个字符，且不能超过5120 tokens
     */
    @JsonProperty(value = "system")
    private String system;

    /**
     * 终端用户的唯一ID，协助平台对终端用户的违规行为、生成违法及不良信息或其他滥用行为进行干预。ID长度要求：最少6个字符，最多128个字符。
     */
    @JsonProperty(value = "user_id")
    private String user;

    /**
     * 生成停止标识，当模型生成结果以stop中某个元素结尾时，停止文本生成。说明：
     * （1）每个元素长度不超过20字符
     * （2）最多4个元素
     */
    @JsonProperty("stop")
    private List<String> stop;

    /**
     * 是否强制关闭实时搜索功能，默认false，表示不关闭
     */
    @JsonProperty(value = "disable_search")
    private Boolean disableSearch;

    /**
     * 是否开启上角标返回，说明：
     * （1）开启后，有概率触发搜索溯源信息search_info，search_info内容见响应参数介绍
     * （2）默认false，不开启
     */
    @JsonProperty(value = "enable_citation")
    private Boolean enableCitation;

    /**
     * 是否返回搜索溯源信息，说明：
     * （1）如果开启，在触发了搜索增强的场景下，会返回搜索溯源信息search_info，search_info内容见响应参数介绍
     * （2）默认false，表示不开启
     */
    @JsonProperty(value = "enable_trace")
    private Boolean enableTrace;

    /**
     * 指定模型最大输出token数，说明：
     * （1）如果设置此参数，范围[2, 2048]
     * （2）如果不设置此参数，最大输出token数为2048
     */
    @JsonProperty("max_output_tokens")
    private Integer maxTokens;

    /**
     * 指定响应内容的格式，说明：
     * （1）可选值：
     * · json_object：以json格式返回，可能出现不满足效果情况
     * · text：以文本格式返回
     * （2）如果不填写参数response_format值，默认为text
     */
    @JsonProperty(value = "response_format")
    private String responseFormat;

    /**
     * 一个可触发函数的描述列表
     */
    @NestedConfigurationProperty
    private @JsonProperty("tools") List<Function> tools;

    /**
     * 在函数调用场景下，提示大模型选择指定的函数
     */
    @NestedConfigurationProperty
    private @JsonProperty("tool_choice") ToolChoice toolChoice;

    @Override
    public List<FunctionCallback> getFunctionCallbacks() {
        return null;
    }

    @Override
    public void setFunctionCallbacks(List<FunctionCallback> functionCallbacks) {

    }

    @Override
    public Set<String> getFunctions() {
        return null;
    }

    @Override
    public void setFunctions(Set<String> functions) {

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final BaiduAiQianfanChatOptions options = new BaiduAiQianfanChatOptions();

        public Builder withModel(String model) {
            this.options.setModel(model);
            return this;
        }

        public Builder withMaxToken(Integer maxTokens) {
            this.options.setMaxTokens(maxTokens);
            return this;
        }

        public Builder withTemperature(Float temperature) {
            this.options.setTemperature(temperature);
            return this;
        }

        public Builder withTopP(Float topP) {
            this.options.setTopP(topP);
            return this;
        }

        public Builder withTools(List<Function> tools) {
            this.options.tools = tools;
            return this;
        }

        public Builder withToolChoice(ToolChoice toolChoice) {
            this.options.toolChoice = toolChoice;
            return this;
        }

        public BaiduAiQianfanChatOptions build() {
            return this.options;
        }

    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<String> getStop() {
        return stop;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
    }

    @Override
    public Float getTemperature() {
        return this.temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    @Override
    public Float getTopP() {
        return this.topP;
    }

    public void setTopP(Float topP) {
        this.topP = topP;
    }

    @Override
    @JsonIgnore
    public Integer getTopK() {
        throw new UnsupportedOperationException("Unimplemented method 'getTopK'");
    }

    @JsonIgnore
    public void setTopK(Integer topK) {
        throw new UnsupportedOperationException("Unimplemented method 'setTopK'");
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    /**
     * Convert the {@link BaiduAiQianfanChatOptions} object to a {@link Map} of key/value pairs.
     * @return The {@link Map} of key/value pairs.
     */
    public Map<String, Object> toMap() {
        try {
            var json = new ObjectMapper().writeValueAsString(this);
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
            });
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Filter out the non supported fields from the options.
     * @param options The options to filter.
     * @return The filtered options.
     */
    public static Map<String, Object> filterNonSupportedFields(Map<String, Object> options) {
        return options.entrySet().stream()
                .filter(e -> !e.getKey().equals("model"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


}
