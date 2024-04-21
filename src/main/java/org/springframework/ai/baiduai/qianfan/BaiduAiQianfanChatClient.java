package org.springframework.ai.baiduai.qianfan;

import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.model.chat.ChatRequest;
import com.baidubce.qianfan.model.chat.Function;
import com.baidubce.qianfan.model.chat.FunctionCall;
import com.baidubce.qianfan.model.chat.Message;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.baiduai.qianfan.metadata.BaiduAiQianfanChatResponseMetadata;
import org.springframework.ai.baiduai.qianfan.util.ApiUtils;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BaiduAiQianfanChatClient
        extends AbstractFunctionCallSupport<Message, ChatRequest, ResponseEntity<com.baidubce.qianfan.model.chat.ChatResponse>>
        implements ChatClient, StreamingChatClient {

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * Default options to be used for all chat requests.
     */
    private BaiduAiQianfanChatOptions defaultOptions;
    /**
     * Low-level 智普 API library.
     */
    private final Qianfan qianfan;

    public BaiduAiQianfanChatClient(Qianfan qianfan) {
        this(qianfan, BaiduAiQianfanChatOptions.builder()
                        .withTemperature(0.95f)
                        .withTopP(0.7f)
                        //.withModel(QianfanAiApi.ChatModel.GLM_3_TURBO.getValue())
                        .build());
    }

    public BaiduAiQianfanChatClient(Qianfan qianfan, BaiduAiQianfanChatOptions options) {
        this(qianfan, options, null);
    }

    public BaiduAiQianfanChatClient(Qianfan qianfan, BaiduAiQianfanChatOptions options, FunctionCallbackContext functionCallbackContext) {
        super(functionCallbackContext);
        Assert.notNull(qianfan, "Qianfan must not be null");
        Assert.notNull(options, "Options must not be null");
        this.qianfan = qianfan;
        this.defaultOptions = options;
    }

    @Override
    public ChatResponse call(Prompt prompt) {

        Assert.notEmpty(prompt.getInstructions(), "At least one text is required!");

        var request = createRequest(prompt, false);

        ResponseEntity<com.baidubce.qianfan.model.chat.ChatResponse> completionEntity = this.callWithFunctionSupport(request);

        var chatCompletion = completionEntity.getBody();
        if (chatCompletion == null) {
            log.warn("No chat completion returned for prompt: {}", prompt);
            return new ChatResponse(List.of());
        }

        List<Generation> generations = Arrays.asList(new Generation(chatCompletion.getResult(),
                        ApiUtils.toMap(chatCompletion.getId(), chatCompletion))
                        .withGenerationMetadata(ChatGenerationMetadata.from(chatCompletion.getFinishReason(), null)));
        return new ChatResponse(generations, BaiduAiQianfanChatResponseMetadata.from(chatCompletion));
    }


    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {

        Assert.notEmpty(prompt.getInstructions(), "At least one text is required!");

        var request = createRequest(prompt, true);

        var completionChunks = this.qianfan.chatCompletionStream(request);

        // For chunked responses, only the first chunk contains the choice role.
        // The rest of the chunks with same ID share the same role.
        ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();

        Iterator<com.baidubce.qianfan.model.chat.ChatResponse> chatCompletionsStream = this.qianfan.chatCompletionStream(request);

        return Flux.fromIterable(() -> chatCompletionsStream)
                .map(chatChunck -> {
                    if (chatChunck == null) {
                        log.warn("No chat completion returned for prompt: {}", prompt);
                        return new ChatResponse(List.of());
                    }
                    return toChatCompletion(chatChunck);
                });
    }

    private ChatResponse toChatCompletion(com.baidubce.qianfan.model.chat.ChatResponse response) {


        /*@SuppressWarnings("null")
        String id = chatChunck.getId();

        List<Generation> generations = chatChunck.getFinishReason().stream().map(choice -> {
            String finish = (choice.get() != null ? choice.finishReason().name() : "");
            var generation = new Generation(choice.message().content(),
                    Map.of("id", id, "finishReason", finish));
            if (choice.finishReason() != null) {
                generation = generation
                        .withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason().name(), null));
            }
            return generation;
        }).toList();
        return new ChatResponse(generations);

        var content = (choice.getDelta() != null) ? choice.getDelta().getContent() : null;
        var generation = new Generation(content).withGenerationMetadata(generateChoiceMetadata(choice));*/

        List<Generation> generations = Arrays.asList(new Generation(response.getResult(), ApiUtils.toMap(response.getId(), response))
                        .withGenerationMetadata(ChatGenerationMetadata.from(response.getFinishReason(), null)));

        return new ChatResponse(generations, BaiduAiQianfanChatResponseMetadata.from(response));
    }

    /**
     * Accessible for testing.
     */
    ChatRequest createRequest(Prompt prompt, boolean stream) {

        Set<String> functionsForThisRequest = new HashSet<>();

        var chatCompletionMessages = prompt.getInstructions()
                .stream()
                .map(m -> new Message().setContent(m.getContent()).setRole(ApiUtils.toRole(m)))
                .toList();

        var request = new ChatRequest()
                .setStream(stream)
                .setMessages(chatCompletionMessages);

        if (this.defaultOptions != null) {

            Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(this.defaultOptions, !IS_RUNTIME_CALL);

            functionsForThisRequest.addAll(defaultEnabledFunctions);

            request = ModelOptionsUtils.merge(request, this.defaultOptions, ChatRequest.class);
        }

        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ChatOptions runtimeOptions) {

                var updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(runtimeOptions, ChatOptions.class,
                        BaiduAiQianfanChatOptions.class);

                Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(updatedRuntimeOptions, IS_RUNTIME_CALL);
                functionsForThisRequest.addAll(promptEnabledFunctions);

                request = ModelOptionsUtils.merge(updatedRuntimeOptions, request, ChatRequest.class);
            }
            else {
                throw new IllegalArgumentException("Prompt options are not of type ChatOptions: " + prompt.getOptions().getClass().getSimpleName());
            }
        }

        // Add the enabled functions definitions to the request's tools parameter.
        if (!CollectionUtils.isEmpty(functionsForThisRequest)) {

            request = ModelOptionsUtils.merge( BaiduAiQianfanChatOptions.builder().withFunctions(this.getFunctionTools(functionsForThisRequest)).build(),
                    request, ChatRequest.class);
        }

        return request;
    }

    private List<Function> getFunctionTools(Set<String> functionNames) {
        /**
         * {
         *     "name": "get_current_weather",
         *     "description": "Get the current weather in a given location",
         *     "parameters": {
         *         "type": "object",
         *         "properties": {
         *             "location": {
         *                 "type": "string",
         *                 "description": "The city and state, e.g. San Francisco, CA",
         *             },
         *             "unit": {"type": "string", "enum": ["celsius", "fahrenheit"]},
         *         },
         *         "required": ["location"],
         *     }
         * }
         */
        return this.resolveFunctionCallbacks(functionNames).stream().map(functionCallback -> {
            var function = new Function().setName(functionCallback.getName())
                    .setDescription(functionCallback.getDescription());
                    //.setParameters(functionCallback.getParameters())
                    //.setExamples(functionCallback.getExamples())
            var inputTypeSchema =  functionCallback.getInputTypeSchema();
            if(StringUtils.hasText(inputTypeSchema)){
                JSONObject inputTypeSchemaJson = new JSONObject(inputTypeSchema);
                function.setParameters(inputTypeSchemaJson);
            }
            return function;
        }).toList();
    }

    //
    // Function Calling Support
    //

    @Override
    protected ChatRequest doCreateToolResponseRequest(ChatRequest previousRequest,
                                                      Message responseMessage,
                                                      List<Message> conversationHistory) {

        // Every tool-call item requires a separate function call and a response (TOOL) message.
        FunctionCall toolCall = responseMessage.getFunctionCall();
        if (Objects.nonNull(toolCall)) {

            var functionName = toolCall.getName();
            String functionArguments = toolCall.getArguments();

            if (!this.functionCallbackRegister.containsKey(functionName)) {
                throw new IllegalStateException("No function callback found for function name: " + functionName);
            }

            String functionResponse = this.functionCallbackRegister.get(functionName).call(functionArguments);

            // Add the function response to the conversation.
            conversationHistory.add(new Message().setName(functionName).setRole(MessageType.FUNCTION.getValue()).setContent(functionResponse));

        }

        // Recursively call chatCompletionWithTools until the model doesn't call a
        // functions anymore.
        ChatRequest newRequest = new ChatRequest().setMessages(conversationHistory).setStream(false);
        newRequest = ModelOptionsUtils.merge(newRequest, previousRequest, ChatRequest.class);

        return newRequest;
    }

    @Override
    protected List<Message> doGetUserMessages(ChatRequest request) {
        return request.getMessages();
    }

    @SuppressWarnings("null")
    @Override
    protected Message doGetToolResponseMessage(ResponseEntity<com.baidubce.qianfan.model.chat.ChatResponse> chatCompletion) {
        var toolResponse = chatCompletion.getBody();
        return new Message().setName(toolResponse.getId())
                .setRole(MessageType.FUNCTION.getValue())
                .setContent(toolResponse.getResult())
                .setFunctionCall(toolResponse.getFunctionCall());
    }

    @Override
    protected ResponseEntity<com.baidubce.qianfan.model.chat.ChatResponse> doChatCompletion(ChatRequest request) {
        var chatResponse =  this.qianfan.chatCompletion(request);
        if(Objects.nonNull(chatResponse.getNeedClearHistory()) && chatResponse.getNeedClearHistory()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(chatResponse);
        }
        if(Objects.isNull(chatResponse.getFlag()) || 0 != chatResponse.getFlag()){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(chatResponse);
        }
        return ResponseEntity.ok(chatResponse);
    }

    @Override
    protected boolean isToolFunctionCall(ResponseEntity<com.baidubce.qianfan.model.chat.ChatResponse> chatCompletion) {
        var toolResponse = chatCompletion.getBody();
        if (Objects.isNull(toolResponse)) {
            return false;
        }
        return Objects.nonNull(toolResponse.getFunctionCall());
    }
}
