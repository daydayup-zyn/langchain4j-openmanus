package cn.daydayup.dev.workflow.langchain4j.react;

import cn.daydayup.dev.workflow.CompiledGraph;
import cn.daydayup.dev.workflow.OverAllState;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/react")
public class ReactController {

	private final CompiledGraph compiledGraph;

	ReactController(@Qualifier("reactAgentGraph") CompiledGraph compiledGraph) {
		this.compiledGraph = compiledGraph;
	}

	@GetMapping("/chat")
	public String simpleChat(@RequestParam("query") String query) {

		Optional<OverAllState> result = compiledGraph.invoke(Map.of("messages", new UserMessage(query)));
		List<ChatMessage> messages = (List<ChatMessage>) result.get().value("messages").get();
		AiMessage assistantMessage = (AiMessage) messages.get(messages.size() - 1);

		return assistantMessage.text();
	}

}
