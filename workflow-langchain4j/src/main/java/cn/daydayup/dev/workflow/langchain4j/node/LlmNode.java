package cn.daydayup.dev.workflow.langchain4j.node;

import cn.daydayup.dev.workflow.OverAllState;
import cn.daydayup.dev.workflow.action.NodeAction;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.output.Response;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LlmNode implements NodeAction {

    public static final String LLM_RESPONSE_KEY = "llm_response";

    private String systemPrompt;

    private String userPrompt;

    private Map<String, Object> params = new HashMap<>();

    private List<ChatMessage> messages = new ArrayList<>();

    private List<ToolSpecification> toolProviders = new ArrayList<>();

    private ChatLanguageModel chatLanguageModel;

    private String systemPromptKey;

    private String userPromptKey;

    private String paramsKey;

    private String messagesKey;

    private String outputKey;

    public LlmNode() {
    }

    public LlmNode(String systemPrompt, String userPrompt, Map<String, Object> params,List<ToolSpecification> toolProviders, List<ChatMessage> messages,
                   ChatLanguageModel chatLanguageModel) {
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.params = params;
        this.messages = messages;
        this.toolProviders = toolProviders;
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        initNodeWithState(state);

        // Call the language model
        ChatResponse response = call();

        Map<String, Object> updatedState = new HashMap<>();
        updatedState.put("messages", response.aiMessage());
        if (StringUtils.isNotEmpty(this.outputKey)) {
            updatedState.put(this.outputKey, response.aiMessage());
        }
        return updatedState;
    }

    private void initNodeWithState(OverAllState state) {
        if (StringUtils.isNotEmpty(userPromptKey)) {
            this.userPrompt = (String) state.value(userPromptKey).orElse(this.userPrompt);
        }
        if (StringUtils.isNotEmpty(systemPromptKey)) {
            this.systemPrompt = (String) state.value(systemPromptKey).orElse(this.systemPrompt);
        }
        if (StringUtils.isNotEmpty(paramsKey)) {
            this.params = (Map<String, Object>) state.value(paramsKey).orElse(this.params);
        }
        if (StringUtils.isNotEmpty(messagesKey)) {
            this.messages = (List<ChatMessage>) state.value(messagesKey).orElse(this.messages);
        }
        if (StringUtils.isNotEmpty(userPrompt) && !params.isEmpty()) {
            this.userPrompt = renderPromptTemplate(userPrompt, params);
        }
    }

    private String renderPromptTemplate(String prompt, Map<String, Object> params) {
        PromptTemplate promptTemplate = new PromptTemplate(prompt);
        return promptTemplate.apply(params).text();
    }

    public ChatResponse call() {
        if (StringUtils.isNotEmpty(systemPrompt) && StringUtils.isNotEmpty(userPrompt)) {
            ChatRequest request = ChatRequest.builder()
                    .messages(SystemMessage.from(systemPrompt),UserMessage.from(userPrompt))
                    .toolSpecifications(toolProviders)
                    .build();
            return chatLanguageModel.chat(request);
        } else {
            if (StringUtils.isNotEmpty(systemPrompt)) {
                ChatRequest request = ChatRequest.builder()
                        .messages(SystemMessage.from(systemPrompt))
                        .toolSpecifications(toolProviders)
                        .build();
                return chatLanguageModel.chat(request);
            } else if (StringUtils.isNotEmpty(userPrompt)) {
                ChatRequest request = ChatRequest.builder()
                        .messages(UserMessage.from(userPrompt))
                        .toolSpecifications(toolProviders)
                        .build();
                return chatLanguageModel.chat(request);
            } else {
                ChatRequest request = ChatRequest.builder()
                        .messages(messages)
                        .toolSpecifications(toolProviders)
                        .build();
                return chatLanguageModel.chat(request);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String systemPrompt;

        private String userPrompt;

        private Map<String, Object> params;

        private List<ChatMessage> messages;

        private List<ToolSpecification> toolProviders;

        private String systemPromptKey;

        private String userPromptKey;

        private String paramsKey;

        private String messagesKey;

        private String outputKey;

        private ChatLanguageModel chatLanguageModel;

        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder userPrompt(String userPrompt) {
            this.userPrompt = userPrompt;
            return this;
        }

        public Builder params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public Builder messages(List<ChatMessage> messages) {
            this.messages = messages;
            return this;
        }

        public Builder toolCallbacks(List<ToolSpecification> toolProviders) {
            this.toolProviders = toolProviders;
            return this;
        }

        public Builder systemPromptKey(String systemPromptKey) {
            this.systemPromptKey = systemPromptKey;
            return this;
        }

        public Builder userPromptKey(String userPromptKey) {
            this.userPromptKey = userPromptKey;
            return this;
        }

        public Builder paramsKey(String paramsKey) {
            this.paramsKey = paramsKey;
            return this;
        }

        public Builder messagesKey(String messagesKey) {
            this.messagesKey = messagesKey;
            return this;
        }

        public Builder outputKey(String outputKey) {
            this.outputKey = outputKey;
            return this;
        }

        public Builder chatLanguageModel(ChatLanguageModel chatLanguageModel) {
            this.chatLanguageModel = chatLanguageModel;
            return this;
        }

        public LlmNode build() {
            LlmNode llmNode = new LlmNode();
            llmNode.systemPrompt = this.systemPrompt;
            llmNode.userPrompt = this.userPrompt;
            llmNode.params = this.params;
            llmNode.messages = this.messages;
            llmNode.systemPromptKey = this.systemPromptKey;
            llmNode.toolProviders = this.toolProviders;
            llmNode.userPromptKey = this.userPromptKey;
            llmNode.paramsKey = this.paramsKey;
            llmNode.messagesKey = this.messagesKey;
            llmNode.outputKey = this.outputKey;
            llmNode.chatLanguageModel = this.chatLanguageModel;
            return llmNode;
        }
    }
}