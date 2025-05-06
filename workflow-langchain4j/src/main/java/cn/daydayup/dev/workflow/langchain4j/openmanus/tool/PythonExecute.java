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

import cn.daydayup.dev.workflow.langchain4j.openmanus.tool.support.CodeExecutionResult;
import cn.daydayup.dev.workflow.langchain4j.openmanus.tool.support.CodeUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.ChatMemory;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class PythonExecute {

	private final ChatMemory chatMemory;
	private final UUID uuid = UUID.randomUUID();
	private Boolean arm64 = true;

	public PythonExecute(ChatMemory chatMemory) {
		this.chatMemory = chatMemory;
	}

	@Tool(name = "pythonExecute", value = "Executes Python code string. Note: Only print outputs are visible, function return values are not captured. Use print statements to see results.")
	public String pythonExecute(String toolInput) {
		log.info("PythonExecute toolInput: {}", toolInput);
		Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
		String code = (String) toolInputMap.get("code");
		// 执行 Python 代码
		CodeExecutionResult result = CodeUtils.executeCode(code, "python", "tmp_" + uuid.toString() + ".py", arm64, new HashMap<>());
		String output = result.getLogs();
		return output;
	}
}

