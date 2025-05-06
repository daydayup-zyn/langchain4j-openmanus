package cn.daydayup.dev.workflow.langchain4j.node;

import cn.daydayup.dev.workflow.OverAllState;
import cn.daydayup.dev.workflow.action.NodeAction;
import cn.hutool.core.lang.UUID;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.service.tool.*;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ToolNode implements NodeAction {

    private String llmResponseKey;
    private String outputKey;
    private AiMessage aiMessage;
    private Map<Class<?>, List<String>> toolProvider;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        if (!StringUtils.isNotEmpty(llmResponseKey)) {
            this.llmResponseKey = LlmNode.LLM_RESPONSE_KEY;
        }

        this.aiMessage = (AiMessage) state.value(this.llmResponseKey).orElseGet(() -> {
            // if key not set, use 'messages' as default
            List<ChatMessage> messages = (List<ChatMessage>) state.value("messages").orElseThrow();
            return messages.get(messages.size() - 1);
        });

        List<ToolExecutionResultMessage> toolResponseMessage = executeFunction(aiMessage);

        Map<String, Object> updatedState = new HashMap<>();
        updatedState.put("messages", toolResponseMessage);
        if (StringUtils.isNotEmpty(this.outputKey)) {
            updatedState.put(this.outputKey, toolResponseMessage);
        }
        return updatedState;
    }

    @SneakyThrows
    private List<ToolExecutionResultMessage> executeFunction(AiMessage aiMessage) {
        List<ToolExecutionResultMessage> toolResponse = new ArrayList<>();
        for (ToolExecutionRequest toolExecutionRequest : aiMessage.toolExecutionRequests()) {
            Class<?> classBean = resolve(toolExecutionRequest.name());
            DefaultToolExecutor defaultToolExecutor = new DefaultToolExecutor(classBean.getDeclaredConstructor().newInstance(),toolExecutionRequest);
            String executeResult = defaultToolExecutor.execute(toolExecutionRequest, UUID.fastUUID());
            toolResponse.add(ToolExecutionResultMessage.from(toolExecutionRequest, executeResult));
        }
        return toolResponse;
    }

    private Class<?> resolve(String toolName) {
        Optional<Class<?>> first = toolProvider.keySet().stream().filter(key -> toolProvider.get(key).contains(toolName)).findFirst();
        return first.get();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String llmResponseKey;
        private String outputKey;
        private Map<Class<?>, List<String>> toolProvider;

        public Builder llmResponseKey(String llmResponseKey) {
            this.llmResponseKey = llmResponseKey;
            return this;
        }

        public Builder outputKey(String outputKey) {
            this.outputKey = outputKey;
            return this;
        }

        public Builder toolProvider(Map<Class<?>, List<String>> toolProvider) {
            this.toolProvider = toolProvider;
            return this;
        }

        public ToolNode build() {
            ToolNode toolNode = new ToolNode();
            toolNode.toolProvider = this.toolProvider;
            toolNode.llmResponseKey = this.llmResponseKey;
            toolNode.outputKey = this.outputKey;
            return toolNode;
        }
    }
}