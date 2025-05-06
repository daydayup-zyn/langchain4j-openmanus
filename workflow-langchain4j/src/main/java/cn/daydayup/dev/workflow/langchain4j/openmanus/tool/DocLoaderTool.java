package cn.daydayup.dev.workflow.langchain4j.openmanus.tool;///*
// * Copyright 2025 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package cn.daydayup.dev.workflow.example.langchain4j.openmanus.tool;
//
//import com.alibaba.fastjson.JSON;
//import dev.langchain4j.agent.tool.Tool;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//
//import java.io.FileInputStream;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Slf4j
//public class DocLoaderTool {
//
//	@Tool(name = "docLoader", value = "Get the content information of a local file at a specified path. Use this tool when you want to get some related information asked by the user. This tool accepts the file path and gets the related information content.")
//	public String docLoader(String toolInput) {
//		log.info("DocLoaderTool toolInput: {}", toolInput);
//		try {
//			Map<String, Object> toolInputMap = JSON.parseObject(toolInput, Map.class);
//			String fileType = (String) toolInputMap.get("file_type");
//			String filePath = (String) toolInputMap.get("file_path");
//
//			// 使用 Tika 解析文档内容
//			TikaDocumentParser parser = new TikaDocumentParser();
//			List<org.apache.tika.parser.txt.Document> documentList = parser.parse(new FileInputStream(filePath));
//
//			List<String> documentContents = documentList.stream()
//					.map(doc -> doc.getFormattedContent())
//					.collect(Collectors.toList());
//
//			String documentContentStr = String.join("\n", documentContents);
//			if (StringUtils.isEmpty(documentContentStr)) {
//				return "No Related information";
//			} else {
//				return "Related information: " + documentContentStr;
//			}
//		} catch (Exception e) {
//			return "Error getting related information: " + e.getMessage();
//		}
//	}
//}
//
