/*
 * Copyright 2024-2025 the original author or authors.
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
package cn.daydayup.dev.workflow.langchain4j.openmanus.tool.support.serpapi;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.Gson;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SerpApiService {

	private static final Logger logger = LoggerFactory.getLogger(SerpApiService.class);

	private final OkHttpClient okHttpClient;

	private final String apikey;

	private final String engine;

	public SerpApiService(SerpApiProperties properties) {
		this.apikey = properties.getApikey();
		this.engine = properties.getEngine();
		this.okHttpClient = new OkHttpClient.Builder()
				.followRedirects(true)
				.build();
	}

	/**
	 * 使用 serpapi API 搜索数据
	 * @param request the function argument
	 * @return response map
	 */
	public Map<String, Object> apply(Request request) {
		if (request == null || !StringUtils.isBlank(request.query())) {
			return null;
		}

		try {
			// 构建 URL 参数
			HttpUrl url = new HttpUrl.Builder()
					.scheme("https")
					.host(SerpApiProperties.SERP_API_URL.replaceFirst("https?://", ""))
					.addQueryParameter("api_key", apikey)
					.addQueryParameter("engine", engine)
					.addQueryParameter("q", request.query())
					.build();

			okhttp3.Request okHttpRequest = new okhttp3.Request.Builder()
					.url(url)
					.header("User-Agent", SerpApiProperties.USER_AGENT_VALUE)
					.get()
					.build();

			Response response = okHttpClient.newCall(okHttpRequest).execute();

			if (!response.isSuccessful()) {
				logger.error("Unexpected code " + response);
				return null;
			}

			String responseBody = response.body().string();
			logger.info("serpapi search: {}, result: {}", request.query(), responseBody);

			return parseJson(responseBody);
		}
		catch (Exception e) {
			logger.error("Failed to invoke serpapi search: {}", e.getMessage());
			return null;
		}
	}

	private Map<String, Object> parseJson(String jsonResponse) {
		Gson gson = new Gson();
		return gson.fromJson(jsonResponse, Map.class);
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonClassDescription("serpapi search request")
	public record Request(@JsonProperty(required = true, value = "query")
						  @JsonPropertyDescription("The query keyword e.g. Alibaba") String query) {}
}

