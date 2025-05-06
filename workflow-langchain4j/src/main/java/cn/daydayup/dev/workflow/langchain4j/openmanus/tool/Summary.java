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

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Summary {

	private final ChatMemory chatMemory;

	public Summary(ChatMemory chatMemory) {
		this.chatMemory = chatMemory;
	}

	@SystemMessage("You are a helpful assistant that summarizes steps in a workflow.")
	@UserMessage("Please summarize the following step: {{input}}")
	public String summarize(@V("input") String input) {
		log.info("Summary toolInput: {}", input);
		return input;
	}
}

