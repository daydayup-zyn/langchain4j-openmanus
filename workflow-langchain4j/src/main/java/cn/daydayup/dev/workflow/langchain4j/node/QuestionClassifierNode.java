package cn.daydayup.dev.workflow.langchain4j.node;

import cn.daydayup.dev.workflow.OverAllState;
import cn.daydayup.dev.workflow.action.NodeAction;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.output.Response;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionClassifierNode implements NodeAction {

	private static final String CLASSIFIER_PROMPT_TEMPLATE = """
				### Job Description',
				You are a text classification engine that analyzes text data and assigns categories based on user input or automatically determined categories.
				### Task
				Your task is to assign one category ONLY to the input text and only one category can be  returned in the output. Additionally, you need to extract the key words from the text that are related to the classification.
				### Format
				The input text is: {inputText}. Categories are specified as a category list: {categories}. Classification instructions may be included to improve the classification accuracy: {classificationInstructions}.
				### Constraint
				DO NOT include anything other than the JSON array in your response.
			""";

	private static final String QUESTION_CLASSIFIER_USER_PROMPT_1 = """
				{ "input_text": ["I recently had a great experience with your company. The service was prompt and the staff was very friendly."],
				"categories": ["Customer Service", "Satisfaction", "Sales", "Product"],
				"classification_instructions": ["classify the text based on the feedback provided by customer"]}
			""";

	private static final String QUESTION_CLASSIFIER_ASSISTANT_PROMPT_1 = """
				```json
					{"keywords": ["recently", "great experience", "company", "service", "prompt", "staff", "friendly"]
					"category_name": "Customer Service"}
				```
			""";

	private static final String QUESTION_CLASSIFIER_USER_PROMPT_2 = """
				{"input_text": ["bad service, slow to bring the food"],
				"categories": ["Food Quality", "Experience", "Price"],
				"classification_instructions": []}
			""";

	private static final String QUESTION_CLASSIFIER_ASSISTANT_PROMPT_2 = """
				```json
					{"keywords": ["bad service", "slow", "food", "tip", "terrible", "waitresses"],
					"category_name": "Experience"}
				```
			""";

	private PromptTemplate systemPromptTemplate;

	private ChatLanguageModel chatModel;

	private String inputText;

	private List<String> categories;

	private List<String> classificationInstructions;

	private String inputTextKey;

	public QuestionClassifierNode(ChatLanguageModel chatModel, String inputTextKey, List<String> categories,
								  List<String> classificationInstructions) {
		this.chatModel = chatModel;
		this.inputTextKey = inputTextKey;
		this.categories = categories;
		this.classificationInstructions = classificationInstructions;
		this.systemPromptTemplate = PromptTemplate.from(CLASSIFIER_PROMPT_TEMPLATE);
	}

	@Override
	public Map<String, Object> apply(OverAllState state) throws Exception {
		if (StringUtils.hasLength(inputTextKey)) {
			this.inputText = (String) state.value(inputTextKey).orElse(this.inputText);
		}

		Prompt prompt = systemPromptTemplate.apply(Map.of("inputText", inputText, "categories", categories,
				"classificationInstructions", classificationInstructions));

		List<ChatMessage> messages = new ArrayList<>();
		messages.add(SystemMessage.from(prompt.text()));
		messages.add(UserMessage.from(QUESTION_CLASSIFIER_USER_PROMPT_1));
		messages.add(AiMessage.from(QUESTION_CLASSIFIER_ASSISTANT_PROMPT_1));
		messages.add(UserMessage.from(QUESTION_CLASSIFIER_USER_PROMPT_2));
		messages.add(AiMessage.from(QUESTION_CLASSIFIER_ASSISTANT_PROMPT_2));
		messages.add(UserMessage.from(inputText));


		ChatResponse generateResult = chatModel.chat(messages);

		Map<String, Object> updatedState = new HashMap<>();
		updatedState.put("classifier_output", generateResult.aiMessage().text());
		if (state.value("messages").isPresent()) {
			updatedState.put("messages", generateResult.aiMessage().text());
		}

		return updatedState;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String inputTextKey;

		private ChatLanguageModel chatModel;

		private List<String> categories;

		private List<String> classificationInstructions;

		public Builder inputTextKey(String input) {
			this.inputTextKey = input;
			return this;
		}

		public Builder chatModel(ChatLanguageModel chatModel) {
			this.chatModel = chatModel;
			return this;
		}

		public Builder categories(List<String> categories) {
			this.categories = categories;
			return this;
		}

		public Builder classificationInstructions(List<String> classificationInstructions) {
			this.classificationInstructions = classificationInstructions;
			return this;
		}

		public QuestionClassifierNode build() {
			return new QuestionClassifierNode(chatModel, inputTextKey, categories, classificationInstructions);
		}
	}
}