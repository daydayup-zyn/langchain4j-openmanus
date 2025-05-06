package cn.daydayup.dev.workflow.langchain4j.agent;

import cn.daydayup.dev.workflow.CompileConfig;
import cn.daydayup.dev.workflow.CompiledGraph;
import cn.daydayup.dev.workflow.OverAllState;
import cn.daydayup.dev.workflow.StateGraph;
import cn.daydayup.dev.workflow.action.AsyncNodeAction;
import cn.daydayup.dev.workflow.action.NodeAction;
import cn.daydayup.dev.workflow.exception.GraphStateException;
import cn.daydayup.dev.workflow.langchain4j.node.LlmNode;
import cn.daydayup.dev.workflow.langchain4j.node.ToolNode;
import cn.daydayup.dev.workflow.state.strategy.AppendStrategy;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import static cn.daydayup.dev.workflow.StateGraph.END;
import static cn.daydayup.dev.workflow.StateGraph.START;
import static cn.daydayup.dev.workflow.action.AsyncEdgeAction.edge_async;
import static cn.daydayup.dev.workflow.action.AsyncNodeAction.node_async;

public class ReactAgent {

	private static final Logger logger = LoggerFactory.getLogger(ReactAgent.class);

	private String name;

	private final LlmNode llmNode;

	private final ToolNode toolNode;

	private final StateGraph graph;

	private CompiledGraph compiledGraph;

	private List<String> tools;

	private Map<Class<?>, List<String>> toolProvider;

	private int max_iterations = 10;

	private int iterations = 0;

	private CompileConfig compileConfig;

	private OverAllState state;

	private Function<OverAllState, Boolean> shouldContinueFunc;

	public ReactAgent(LlmNode llmNode, ToolNode toolNode,Map<Class<?>, List<String>> toolProvider, int maxIterations, OverAllState state,
                      CompileConfig compileConfig, Function<OverAllState, Boolean> shouldContinueFunc)
			throws GraphStateException {
		this.llmNode = llmNode;
		this.toolNode = toolNode;
		this.max_iterations = maxIterations;
		this.state = state;
		this.compileConfig = compileConfig;
		this.shouldContinueFunc = shouldContinueFunc;
		this.graph = initGraph();
	}

	public ReactAgent(String name, ChatLanguageModel chatClient, List<ToolSpecification> tools,Map<Class<?>, List<String>> toolProvider, int maxIterations)
			throws GraphStateException {
		this.name = name;
		this.llmNode = LlmNode.builder().chatLanguageModel(chatClient).toolCallbacks(tools).messagesKey("messages").build();
		this.toolNode = ToolNode.builder().toolProvider(toolProvider).build();
		this.max_iterations = maxIterations;
		this.graph = initGraph();
	}

	public ReactAgent(String name, ChatLanguageModel chatClient, List<ToolSpecification> tools,Map<Class<?>, List<String>> toolProvider, int maxIterations,
                      OverAllState state, CompileConfig compileConfig)
			throws GraphStateException {
		this.name = name;
		this.llmNode = LlmNode.builder().chatLanguageModel(chatClient).toolCallbacks(tools).messagesKey("messages").build();
		this.toolNode = ToolNode.builder().toolProvider(toolProvider).build();
		this.max_iterations = maxIterations;
		this.state = state;
		this.compileConfig = compileConfig;
		this.graph = initGraph();
	}

	public StateGraph getStateGraph() {
		return graph;
	}

	public CompiledGraph getCompiledGraph() throws GraphStateException {
		return compiledGraph;
	}

	public CompiledGraph getAndCompileGraph(CompileConfig compileConfig) throws GraphStateException {
		this.compiledGraph = getStateGraph().compile(compileConfig);
		return this.compiledGraph;
	}

	public CompiledGraph getAndCompileGraph() throws GraphStateException {
		if (this.compileConfig == null) {
			this.compiledGraph = getStateGraph().compile();
		}
		else {
			this.compiledGraph = getStateGraph().compile(this.compileConfig);
		}
		return this.compiledGraph;
	}

	public NodeAction asNodeAction(String inputKeyFromParent, String outputKeyToParent) {
		return new SubGraphNodeAdapter(inputKeyFromParent, outputKeyToParent, this.compiledGraph);
	}

	public AsyncNodeAction asAsyncNodeAction(String inputKeyFromParent, String outputKeyToParent) {
		if (this.compiledGraph == null) {
			throw new IllegalStateException("ReactAgent not compiled yet");
		}
		return node_async(new SubGraphNodeAdapter(inputKeyFromParent, outputKeyToParent, this.compiledGraph));
	}

	private StateGraph initGraph() throws GraphStateException {
		if (state == null) {
			OverAllState defaultState = new OverAllState();
			defaultState.registerKeyAndStrategy("messages", new AppendStrategy());
			this.state = defaultState;
		}

		return new StateGraph(name, state).addNode("agent", node_async(this.llmNode))
			.addNode("tool", node_async(this.toolNode))
			.addEdge(START, "agent")
			.addConditionalEdges("agent", edge_async(this::think), Map.of("continue", "tool", "end", END))
			.addEdge("tool", "agent");
	}

	private String think(OverAllState state) {
		if (iterations > max_iterations) {
			return "end";
		}

		if (shouldContinueFunc != null && !shouldContinueFunc.apply(state)) {
			return "end";
		}

		List<ChatMessage> messages = (List<ChatMessage>) state.value("messages").orElseThrow();
		AiMessage message = (AiMessage) messages.get(messages.size() - 1);
		logger.info("Thinking: {}", message);
		if (!CollectionUtils.isEmpty(message.toolExecutionRequests())) {
			return "continue";
		}

		return "end";
	}

	List<String> getTools() {
		return tools;
	}

	void setTools(List<String> tools) {
		this.tools = tools;
	}

	int getMax_iterations() {
		return max_iterations;
	}

	void setMax_iterations(int max_iterations) {
		this.max_iterations = max_iterations;
	}

	int getIterations() {
		return iterations;
	}

	void setIterations(int iterations) {
		this.iterations = iterations;
	}

	CompileConfig getCompileConfig() {
		return compileConfig;
	}

	void setCompileConfig(CompileConfig compileConfig) {
		this.compileConfig = compileConfig;
	}

	OverAllState getState() {
		return state;
	}

	void setState(OverAllState state) {
		this.state = state;
	}

	Function<OverAllState, Boolean> getShouldContinueFunc() {
		return shouldContinueFunc;
	}

	void setShouldContinueFunc(Function<OverAllState, Boolean> shouldContinueFunc) {
		this.shouldContinueFunc = shouldContinueFunc;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String name;

		private ChatLanguageModel chatClient;

		private List<ToolSpecification> tools;

		private Map<Class<?>, List<String>> toolProvider;

		private int maxIterations = 10;

		private CompileConfig compileConfig;

		private OverAllState state;

		private Function<OverAllState, Boolean> shouldContinueFunc;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder toolProvider(Map<Class<?>, List<String>> toolProvider) {
			this.toolProvider = toolProvider;
			return this;
		}

		public Builder chatClient(ChatLanguageModel chatClient) {
			this.chatClient = chatClient;
			return this;
		}

		public Builder tools(List<ToolSpecification> tools) {
			this.tools = tools;
			return this;
		}

		public Builder maxIterations(int maxIterations) {
			this.maxIterations = maxIterations;
			return this;
		}

		public Builder state(OverAllState state) {
			this.state = state;
			return this;
		}

		public Builder compileConfig(CompileConfig compileConfig) {
			this.compileConfig = compileConfig;
			return this;
		}

		public Builder shouldContinueFunction(Function<OverAllState, Boolean> shouldContinueFunc) {
			this.shouldContinueFunc = shouldContinueFunc;
			return this;
		}

		public ReactAgent build() throws GraphStateException {
			return new ReactAgent(name, chatClient, tools,toolProvider, maxIterations, state, compileConfig);
		}
	}

	public static class SubGraphNodeAdapter implements NodeAction {

		private String inputKeyFromParent;

		private String outputKeyToParent;

		private CompiledGraph childGraph;

		SubGraphNodeAdapter(String inputKeyFromParent, String outputKeyToParent, CompiledGraph childGraph) {
			this.inputKeyFromParent = inputKeyFromParent;
			this.outputKeyToParent = outputKeyToParent;
			this.childGraph = childGraph;
		}

		@Override
		public Map<String, Object> apply(OverAllState parentState) throws Exception {

			// prepare input for child graph
			String input = (String) parentState.value(inputKeyFromParent).orElseThrow();
			ChatMessage message = new UserMessage(input);
			List<ChatMessage> messages = List.of(message);

			// invoke child graph
			OverAllState childState = childGraph.invoke(Map.of("messages", messages)).get();

			// extract output from child graph
			List<ChatMessage> reactMessages = (List<ChatMessage>) childState.value("messages").orElseThrow();
			AiMessage chatMessage = (AiMessage) reactMessages.get(reactMessages.size() - 1);
			String reactResult = chatMessage.text();

			// update parent state
			return Map.of(outputKeyToParent, reactResult);
		}
	}
}
