package cn.daydayup.dev.workflow.langchain4j.react;

import cn.daydayup.dev.workflow.CompiledGraph;
import cn.daydayup.dev.workflow.GraphRepresentation;
import cn.daydayup.dev.workflow.exception.GraphStateException;
import cn.daydayup.dev.workflow.langchain4j.agent.ReactAgent;
import cn.daydayup.dev.workflow.langchain4j.annotation.ToolBean;
import cn.daydayup.dev.workflow.langchain4j.consts.ToolConst;
import cn.daydayup.dev.workflow.langchain4j.react.function.Tools;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
public class ReactAutoconfiguration {

	@Bean(value = "toolClassProvider")
	public Map<Class<?>, List<String>> toolClassProvider(){
		Map<Class<?>, List<String>> objectListHashMap = new HashMap<>();
		// 扫描包中的类
		Reflections reflections = new Reflections(ToolConst.TOOL_PATH);
		// 获取所有的类
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(ToolBean.class);
		for (Class<?> clazz : classes) {
			List<String> methodNames = new ArrayList<>();
			for (Method method : clazz.getDeclaredMethods()) {
				if (method.isAnnotationPresent(Tool.class)) {
					String methodName = method.getName();
					methodNames.add(methodName);
				}
			}
			objectListHashMap.put(clazz,methodNames);
		}
		return objectListHashMap;
	}

	@Bean
	public ReactAgent normalReactAgent(ChatLanguageModel chatModel,@Qualifier("toolClassProvider") Map<Class<?>, List<String>> toolClassScannerConfig) throws GraphStateException {
		List<ToolSpecification> toolSpecifications = ToolSpecifications.toolSpecificationsFrom(Tools.class);

		return ReactAgent.builder()
			.name("React Agent Demo")
			.chatClient(chatModel)
			.tools(toolSpecifications)
			.toolProvider(toolClassScannerConfig)
			.maxIterations(10)
			.build();
	}

	@Bean
	public CompiledGraph reactAgentGraph(@Qualifier("normalReactAgent") ReactAgent reactAgent)
			throws GraphStateException {

		GraphRepresentation graphRepresentation = reactAgent.getStateGraph()
			.getGraph(GraphRepresentation.Type.PLANTUML);

		System.out.println("\n\n");
		System.out.println(graphRepresentation.content());
		System.out.println("\n\n");

		return reactAgent.getAndCompileGraph();
	}

	@Bean
	public RestClient.Builder createRestClient() {

		// 2. 创建 RequestConfig 并设置超时
		RequestConfig requestConfig = RequestConfig.custom()
			.setConnectTimeout(Timeout.of(10, TimeUnit.MINUTES)) // 设置连接超时
			.setResponseTimeout(Timeout.of(10, TimeUnit.MINUTES))
			.setConnectionRequestTimeout(Timeout.of(10, TimeUnit.MINUTES))
			.build();

		// 3. 创建 CloseableHttpClient 并应用配置
		HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

		// 4. 使用 HttpComponentsClientHttpRequestFactory 包装 HttpClient
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

		// 5. 创建 RestClient 并设置请求工厂
		return RestClient.builder().requestFactory(requestFactory);
	}

}
