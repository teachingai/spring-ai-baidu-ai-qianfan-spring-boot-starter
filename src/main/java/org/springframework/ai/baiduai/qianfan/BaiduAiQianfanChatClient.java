package org.springframework.ai.baiduai.qianfan;

import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.model.chat.ChatRequest;
import com.baidubce.qianfan.model.chat.Function;
import com.baidubce.qianfan.model.chat.FunctionCall;
import com.baidubce.qianfan.model.chat.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.baiduai.qianfan.metadata.BaiduAiQianfanChatResponseMetadata;
import org.springframework.ai.baiduai.qianfan.util.ApiUtils;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BaiduAiQianfanChatClient
        extends AbstractFunctionCallSupport<Message, ChatRequest, ResponseEntity<com.baidubce.qianfan.model.chat.ChatResponse>>
        implements ChatClient, StreamingChatClient {

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

        Flux.create(sink -> {

            chatCompletionsStream.forEachRemaining(chatCompletion -> {
                ChatResponse chatResponse = toChatCompletion(chatCompletion);
                sink.next(chatResponse);
            });

        });
        return Flux.fromIterable(() -> chatCompletionsStream)
                .map(chatChunck -> {

                    @SuppressWarnings("null")
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
                    var generation = new Generation(content).withGenerationMetadata(generateChoiceMetadata(choice));
                    return new ChatResponse(List.of(generation));
                });

        return completionChunks.map(chunk -> toChatCompletion(chunk)).map(chatCompletion -> {

            chatCompletion = handleFunctionCallOrReturn(request, ResponseEntity.of(Optional.of(chatCompletion)))
                    .getBody();

            @SuppressWarnings("null")
            String id = chatCompletion.id();

            List<Generation> generations = chatCompletion.choices().stream().map(choice -> {
                if (choice.message().role() != null) {
                    roleMap.putIfAbsent(id, choice.message().role().name());
                }
                String finish = (choice.finishReason() != null ? choice.finishReason().name() : "");
                var generation = new Generation(choice.message().content(),
                        Map.of("id", id, "role", roleMap.get(id), "finishReason", finish));
                if (choice.finishReason() != null) {
                    generation = generation
                            .withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason().name(), null));
                }
                return generation;
            }).toList();
            return new ChatResponse(generations);
        });
    }

    private ChatResponse toChatCompletion(com.baidubce.qianfan.model.chat.ChatResponse response) {
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
                .map(m -> new Message().setContent(m.getContent()).setRole(),
                        Message.Role.valueOf(m.getMessageType().name())))
                .toList();

        qianfan.chatCompletion()

        var request = new ChatRequest();
        request.setStream(stream);
        request.setMessages(chatCompletionMessages);

        if (this.defaultOptions != null) {
            Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(this.defaultOptions,
                    !IS_RUNTIME_CALL);

            functionsForThisRequest.addAll(defaultEnabledFunctions);

            request = ModelOptionsUtils.merge(request, this.defaultOptions, ChatRequest.class);
        }

        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ChatOptions runtimeOptions) {
                var updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(runtimeOptions, ChatOptions.class,
                        BaiduAiQianfanChatOptions.class);

                Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(updatedRuntimeOptions,
                        IS_RUNTIME_CALL);
                functionsForThisRequest.addAll(promptEnabledFunctions);

                request = ModelOptionsUtils.merge(updatedRuntimeOptions, request,
                        ChatRequest.class);
            }
            else {
                throw new IllegalArgumentException("Prompt options are not of type ChatOptions: "
                        + prompt.getOptions().getClass().getSimpleName());
            }
        }

        // Add the enabled functions definitions to the request's tools parameter.
        if (!CollectionUtils.isEmpty(functionsForThisRequest)) {

            request = ModelOptionsUtils.merge(
                    BaiduAiQianfanChatOptions.builder().withTools(this.getFunctionTools(functionsForThisRequest)).build(),
                    request, ChatRequest.class);
        }

        return request;
    }

    private List<Function> getFunctionTools(Set<String> functionNames) {
        return this.resolveFunctionCallbacks(functionNames).stream().map(functionCallback -> {
            var function = new FunctionCall(functionCallback.getDescription(),
                    functionCallback.getName(), functionCallback.getInputTypeSchema());
            return new Function(function);
        }).toList();
    }

    //
    // Function Calling Support
    //
    @Override
    protected ChatRequest doCreateToolResponseRequest(ChatRequest previousRequest,
                                                      Message responseMessage,
                                                      List<Message> conversationHistory) {

        // Every tool-call item requires a separate function call and a response (TOOL)
        // message.
        for (Message.ToolCall toolCall : responseMessage.getFunctionCall()) {

            var functionName = toolCall.function().name();
            String functionArguments = toolCall.function().arguments();

            if (!this.functionCallbackRegister.containsKey(functionName)) {
                throw new IllegalStateException("No function callback found for function name: " + functionName);
            }

            String functionResponse = this.functionCallbackRegister.get(functionName).call(functionArguments);

            // Add the function response to the conversation.
            conversationHistory
                    .add(new Message(functionResponse, Message.Role.TOOL, functionName, null));
        }

        // Recursively call chatCompletionWithTools until the model doesn't call a
        // functions anymore.
        ChatRequest newRequest = new ChatRequest(previousRequest.requestId(), conversationHistory, false);
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
        return chatCompletion.getBody().getBody().choices().iterator().next().message();
    }

    @Override
    protected ResponseEntity<com.baidubce.qianfan.model.chat.ChatResponse> doChatCompletion(ChatRequest request) {
        return ResponseEntity.ok(this.qianfan.chatCompletion(request));
    }

    @Override
    protected boolean isToolFunctionCall(ResponseEntity<com.baidubce.qianfan.model.chat.ChatResponse> chatCompletion) {
        var body = chatCompletion.getBody();
        if (body == null) {
            return false;
        }
        var choices = body.g();
        if (CollectionUtils.isEmpty(choices)) {
            return false;
        }

        return !CollectionUtils.isEmpty(choices.get(0).message().toolCalls());
    }
}
