package cn.daydayup.dev.workflow.langchain4j.worflow;

import cn.daydayup.dev.workflow.CompiledGraph;
import cn.daydayup.dev.workflow.OverAllState;
import cn.daydayup.dev.workflow.exception.GraphStateException;
import cn.daydayup.dev.workflow.StateGraph;
import cn.daydayup.dev.workflow.action.EdgeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/customer")
public class CustomerServiceController {

	private static final Logger logger = LoggerFactory.getLogger(CustomerServiceController.class);

	private CompiledGraph compiledGraph;

	public CustomerServiceController(@Qualifier("workflowGraph") StateGraph stateGraph) throws GraphStateException {
		this.compiledGraph = stateGraph.compile();
	}

	@GetMapping("/chat")
	public String simpleChat(@RequestParam("query") String query) throws GraphStateException {

		return compiledGraph.invoke(Map.of("input", query)).get().value("solution").get().toString();
	}

	public static class FeedbackQuestionDispatcher implements EdgeAction {

		@Override
		public String apply(OverAllState state) throws Exception {

			String classifierOutput = (String) state.value("classifier_output").orElse("");
			logger.info("classifierOutput: " + classifierOutput);

			if (classifierOutput.contains("positive")) {
				return "positive";
			}
			return "negative";
		}

	}

	public static class SpecificQuestionDispatcher implements EdgeAction {

		@Override
		public String apply(OverAllState state) throws Exception {

			String classifierOutput = (String) state.value("classifier_output").orElse("");
			logger.info("classifierOutput: " + classifierOutput);

			Map<String, String> classifierMap = new HashMap<>();
			classifierMap.put("after-sale", "after-sale");
			classifierMap.put("quality", "quality");
			classifierMap.put("transportation", "transportation");

			for (Map.Entry<String, String> entry : classifierMap.entrySet()) {
				if (classifierOutput.contains(entry.getKey())) {
					return entry.getValue();
				}
			}

			return "others";
		}

	}

}
