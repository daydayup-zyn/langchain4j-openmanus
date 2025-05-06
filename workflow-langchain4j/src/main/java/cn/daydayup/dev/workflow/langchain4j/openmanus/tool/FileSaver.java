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

import com.alibaba.fastjson.JSON;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class FileSaver {


	@Tool(name = "fileSaver", value = "Save content to a local file at a specified path. Use this tool when you need to save text, code, or generated content to a file on the local filesystem. The tool accepts content and a file path, and saves the content to that location.")
	public String fileSaver(String toolInput) {
		log.info("FileSaver toolInput: {}", toolInput);
		try {
			Map<String, Object> toolInputMap = JSON.parseObject(toolInput, Map.class);
			String content = (String) toolInputMap.get("content");
			String filePath = (String) toolInputMap.get("file_path");

			File file = new File(filePath);
			File directory = file.getParentFile();
			if (directory != null && !directory.exists()) {
				directory.mkdirs();
			}

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				writer.write(content);
			}

			String resultMessage = "Content successfully saved to " + filePath;

			return resultMessage;
		} catch (IOException e) {
			String errorMessage = "Error saving file: " + e.getMessage();
			log.error(errorMessage, e);
			return errorMessage;
		} catch (Exception e) {
			String errorMessage = "Unexpected error: " + e.getMessage();
			log.error(errorMessage, e);
			return errorMessage;
		}
	}
}

