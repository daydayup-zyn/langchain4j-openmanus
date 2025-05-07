/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.daydayup.dev.workflow.langchain4j.openmanus.tool;

import cn.daydayup.dev.workflow.langchain4j.openmanus.tool.support.ToolExecuteResult;
import cn.daydayup.dev.workflow.langchain4j.openmanus.tool.support.serpapi.SerpApiProperties;
import cn.daydayup.dev.workflow.langchain4j.openmanus.tool.support.serpapi.SerpApiService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import dev.langchain4j.agent.tool.Tool;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GoogleSearch {

	private static final String SERP_API_KEY = "48d1bd8f7419387ef582b7f04149acxxxxxxxxxxxxxxxxxx";
	private final SerpApiService service;

	public GoogleSearch() {
		this.service = new SerpApiService(new SerpApiProperties(SERP_API_KEY, "google"));
	}

	@Tool(name = "google_search", value = "Perform a Google search and return a list of relevant links.")
	public ToolExecuteResult run(String toolInput) {
		Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
		String query = (String) toolInputMap.get("query");

		Integer numResults = 2;
		if (toolInputMap.containsKey("num_results")) {
			numResults = (Integer) toolInputMap.get("num_results");
		}

		SerpApiService.Request request = new SerpApiService.Request(query);
		Map<String, Object> response = service.apply(request);

		String toret = extractResponse(response); // 提取响应内容
		return new ToolExecuteResult(toret);
	}

	private String extractResponse(Map<String, Object> response) {
		if (response.containsKey("answer_box")) {
			Map<String, Object> answerBox = (Map<String, Object>) response.get("answer_box");
			if (answerBox.containsKey("answer")) {
				return answerBox.get("answer").toString();
			} else if (answerBox.containsKey("snippet")) {
				return answerBox.get("snippet").toString();
			} else if (answerBox.containsKey("snippet_highlighted_words")) {
				List<String> highlights = (List<String>) answerBox.get("snippet_highlighted_words");
				return highlights.isEmpty() ? "" : highlights.get(0);
			}
		}

		if (response.containsKey("sports_results")) {
			Map<String, Object> sports = (Map<String, Object>) response.get("sports_results");
			return sports.getOrDefault("game_spotlight", "").toString();
		}

		if (response.containsKey("shopping_results")) {
			List<Map<String, Object>> shopping = (List<Map<String, Object>>) response.get("shopping_results");
			return shopping.stream().limit(3).map(map -> map.get("title")).filter(Objects::nonNull)
					.map(Object::toString).toList().toString();
		}

		if (response.containsKey("knowledge_graph")) {
			Map<String, Object> kg = (Map<String, Object>) response.get("knowledge_graph");
			return kg.getOrDefault("description", "").toString();
		}

		List<Map<String, Object>> organic = (List<Map<String, Object>>) response.get("organic_results");
		if (!organic.isEmpty()) {
			Map<String, Object> first = organic.get(0);
			if (first.containsKey("snippet")) {
				return first.get("snippet").toString();
			} else if (first.containsKey("link")) {
				return first.get("link").toString();
			}
		}

		if (response.containsKey("images_results")) {
			List<Map<String, Object>> images = (List<Map<String, Object>>) response.get("images_results");
			List<String> thumbnails = images.stream().limit(10).map(item -> item.get("thumbnail").toString())
					.toList();
			return thumbnails.toString();
		}

		return "No good search result found";
	}
}

