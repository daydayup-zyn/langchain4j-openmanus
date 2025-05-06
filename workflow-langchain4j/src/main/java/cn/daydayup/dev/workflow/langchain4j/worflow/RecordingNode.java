package cn.daydayup.dev.workflow.langchain4j.worflow;

import cn.daydayup.dev.workflow.OverAllState;
import cn.daydayup.dev.workflow.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class RecordingNode implements NodeAction {

	private static final Logger logger = LoggerFactory.getLogger(RecordingNode.class);

	@Override
	public Map<String, Object> apply(OverAllState state) throws Exception {

		String feedback = (String) state.value("classifier_output").get();

		Map<String, Object> updatedState = new HashMap<>();
		if (feedback.contains("positive")) {
			logger.info("Received positive feedback: " + feedback);
			updatedState.put("solution", "Praise, no action taken.");
		}
		else {
			logger.info("Received negative feedback: " + feedback);
			updatedState.put("solution", feedback);
		}

		return updatedState;
	}
}
