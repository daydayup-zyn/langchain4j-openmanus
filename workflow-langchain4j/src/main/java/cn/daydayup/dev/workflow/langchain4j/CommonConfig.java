package cn.daydayup.dev.workflow.langchain4j;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName CommonConfig
 * @Description TODO
 * @Author ZhaoYanNing
 * @Date 2025/5/6 18:33
 * @Version 1.0
 */
@Configuration
public class CommonConfig {

    @Bean
    public ChatLanguageModel chatModel() {
        return QwenChatModel
                .builder()
                .apiKey("sk-a47697a6a5454d1xxxxxxxxxxxxx")
                .modelName("qwen-plus")
                .build();
    }
}
