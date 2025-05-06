package cn.daydayup.dev.workflow.langchain4j.worflow;

import cn.daydayup.dev.workflow.GraphRepresentation;
import cn.daydayup.dev.workflow.OverAllState;
import cn.daydayup.dev.workflow.exception.GraphStateException;
import cn.daydayup.dev.workflow.StateGraph;
import cn.daydayup.dev.workflow.langchain4j.node.QuestionClassifierNode;
import cn.daydayup.dev.workflow.state.AgentStateFactory;
import cn.daydayup.dev.workflow.state.strategy.ReplaceStrategy;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.Map;
import static cn.daydayup.dev.workflow.StateGraph.END;
import static cn.daydayup.dev.workflow.StateGraph.START;
import static cn.daydayup.dev.workflow.action.AsyncEdgeAction.edge_async;
import static cn.daydayup.dev.workflow.action.AsyncNodeAction.node_async;


@Configuration
public class WorkflowAutoconfiguration {

	@Bean
	public StateGraph workflowGraph(@Qualifier("chatModel") ChatLanguageModel chatModel) throws GraphStateException {
		AgentStateFactory<OverAllState> stateFactory = (inputs) -> {
			OverAllState state = new OverAllState();
			state.registerKeyAndStrategy("input", new ReplaceStrategy());
			state.registerKeyAndStrategy("classifier_output", new ReplaceStrategy());
			state.registerKeyAndStrategy("solution", new ReplaceStrategy());
			state.input(inputs);
			return state;
		};

		QuestionClassifierNode feedbackClassifier = QuestionClassifierNode.builder()
			.chatModel(chatModel)
			.inputTextKey("input")
			.categories(List.of("positive feedback", "negative feedback"))
			.classificationInstructions(
					List.of("Try to understand the user's feeling when he/she is giving the feedback."))
			.build();

		QuestionClassifierNode specificQuestionClassifier = QuestionClassifierNode.builder()
			.chatModel(chatModel)
			.inputTextKey("input")
			.categories(List.of("after-sale service", "transportation", "product quality", "others"))
			.classificationInstructions(List
				.of("What kind of service or help the customer is trying to get from us? Classify the question based on your understanding."))
			.build();

		StateGraph stateGraph = new StateGraph("Consumer Service Workflow Demo", stateFactory)
			.addNode("feedback_classifier", node_async(feedbackClassifier))
			.addNode("specific_question_classifier", node_async(specificQuestionClassifier))
			.addNode("recorder", node_async(new RecordingNode()))

			.addEdge(START, "feedback_classifier")
			.addConditionalEdges("feedback_classifier",
					edge_async(new CustomerServiceController.FeedbackQuestionDispatcher()),
					Map.of("positive", "recorder", "negative", "specific_question_classifier"))
			.addConditionalEdges("specific_question_classifier",
					edge_async(new CustomerServiceController.SpecificQuestionDispatcher()),
					Map.of("after-sale", "recorder", "transportation", "recorder", "quality", "recorder", "others",
							"recorder"))
			.addEdge("recorder", END);

		GraphRepresentation graphRepresentation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML,
				"workflow graph");

		System.out.println("\n\n");
		System.out.println(graphRepresentation.content());
		System.out.println("\n\n");

		return stateGraph;
	}

}
