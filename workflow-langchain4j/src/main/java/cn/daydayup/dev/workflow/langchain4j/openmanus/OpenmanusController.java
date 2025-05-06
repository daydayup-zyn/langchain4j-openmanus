//package cn.daydayup.dev.workflow.langchain4j.openmanus;
//
//import cn.daydayup.dev.workflow.CompiledGraph;
//import cn.daydayup.dev.workflow.GraphRepresentation;
//import cn.daydayup.dev.workflow.OverAllState;
//import cn.daydayup.dev.workflow.StateGraph;
//import cn.daydayup.dev.workflow.langchain4j.openmanus.tool.PlanningTool;
//import cn.daydayup.dev.workflow.exception.GraphStateException;
//import cn.daydayup.dev.workflow.state.AgentStateFactory;
//import cn.daydayup.dev.workflow.state.strategy.ReplaceStrategy;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
//import static cn.daydayup.dev.workflow.action.AsyncNodeAction.node_async;
//
//@RestController
//@RequestMapping("/manus")
//public class OpenmanusController {
//
//	private final ChatClient planningClient;
//
//	private final ChatClient stepClient;
//
//	private CompiledGraph compiledGraph;
//
//	// 也可以使用如下的方式注入 ChatClient
//	public OpenmanusController(ChatModel chatModel) throws GraphStateException {
//
//		this.planningClient = ChatClient.builder(chatModel)
//			.defaultSystem(PLANNING_SYSTEM_PROMPT)
//			// .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
//			.defaultAdvisors(new SimpleLoggerAdvisor())
//			.defaultTools(Builder.getToolCallList())// tools registered will only be used
//													// as tool description
//			.defaultOptions(OpenAiChatOptions.builder().internalToolExecutionEnabled(false).build())
//			.build();
//
//		this.stepClient = ChatClient.builder(chatModel)
//			.defaultSystem(STEP_SYSTEM_PROMPT)
//			// .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
//			.defaultTools(Builder.getManusAgentToolCalls())// tools registered will only
//															// be used as tool description
//			.defaultAdvisors(new SimpleLoggerAdvisor())
//			.defaultOptions(OpenAiChatOptions.builder().internalToolExecutionEnabled(false).build())
//			.build();
//
//		initGraph();
//	}
//
//	public void initGraph() throws GraphStateException {
//
//		AgentStateFactory<OverAllState> stateFactory = (inputs) -> {
//			OverAllState state = new OverAllState();
//			state.registerKeyAndStrategy("plan", new ReplaceStrategy());
//			state.registerKeyAndStrategy("step_prompt", new ReplaceStrategy());
//			state.registerKeyAndStrategy("step_output", new ReplaceStrategy());
//			state.registerKeyAndStrategy("final_output", new ReplaceStrategy());
//
//			state.input(inputs);
//			return state;
//		};
//
//		SupervisorAgent supervisorAgent = new SupervisorAgent(PlanningTool.INSTANCE);
//		ReactAgent planningAgent = new ReactAgent("planningAgent", planningClient, Builder.getFunctionCallbackList(),
//				10);
//		planningAgent.getAndCompileGraph();
//		ReactAgent stepAgent = new ReactAgent("stepAgent", stepClient, Builder.getManusAgentFunctionCallbacks(), 10);
//		stepAgent.getAndCompileGraph();
//
//		StateGraph graph = new StateGraph(stateFactory)
//			.addNode("planning_agent", planningAgent.asAsyncNodeAction("input", "plan"))
//			.addNode("supervisor_agent", node_async(supervisorAgent))
//			.addNode("step_executing_agent", stepAgent.asAsyncNodeAction("step_prompt", "step_output"))
//
//			.addEdge(START, "planning_agent")
//			.addEdge("planning_agent", "supervisor_agent")
//			.addConditionalEdges("supervisor_agent", edge_async(supervisorAgent::think),
//					Map.of("continue", "step_executing_agent", "end", END))
//			.addEdge("step_executing_agent", "supervisor_agent");
//
//		this.compiledGraph = graph.compile();
//
//		GraphRepresentation graphRepresentation = compiledGraph.getGraph(GraphRepresentation.Type.PLANTUML);
//		System.out.println("\n\n");
//		System.out.println(graphRepresentation.content());
//		System.out.println("\n\n");
//	}
//
//	/**
//	 * ChatClient 简单调用
//	 */
//	@GetMapping("/chat")
//	public String simpleChat(String query) {
//
//		return compiledGraph.invoke(Map.of("input", query)).get().data().toString();
//	}
//
//}
