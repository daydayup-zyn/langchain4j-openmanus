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

import cn.daydayup.dev.workflow.langchain4j.openmanus.tool.support.llmbash.BashProcess;
import com.alibaba.fastjson.JSON;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class Bash {

	private String workingDirectoryPath;

	@Tool(name = "", value = "Execute a bash command in the terminal.\n" +
			"* Long running commands: For commands that may run indefinitely, it should be run in the background and the output should be redirected to a file, e.g. command = `python3 app.py > server.log 2>&1 &`.\n" +
			"* Interactive: If a bash command returns exit code `-1`, this means the process is not yet finished. The assistant must then send a second call to terminal with an empty `command` (which will retrieve any additional logs), or it can send additional text (set `command` to the text) to STDIN of the running process, or it can send command=`ctrl+c` to interrupt the process.\n" +
			"* Timeout: If a command execution result says \"Command timed out. Sending SIGINT to the process\", the assistant should retry running the command in the background.")
	public String bash(String toolInput) {
		log.info("Bash toolInput: {}", toolInput);
		Map<String, Object> toolInputMap = JSON.parseObject(toolInput, Map.class);
		String command = (String) toolInputMap.get("command");

		List<String> commandList = new ArrayList<>();
		commandList.add(command);

		List<String> result = BashProcess.executeCommand(commandList, workingDirectoryPath);

		String output = JSON.toJSONString(result);

		return output;
	}

	// 工作目录的 setter （可以注入或外部配置）
	public void setWorkingDirectoryPath(String workingDirectoryPath) {
		this.workingDirectoryPath = workingDirectoryPath;
	}
}

