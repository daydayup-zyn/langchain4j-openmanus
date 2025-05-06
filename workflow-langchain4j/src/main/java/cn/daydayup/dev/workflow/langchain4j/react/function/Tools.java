package cn.daydayup.dev.workflow.langchain4j.react.function;

import cn.daydayup.dev.workflow.langchain4j.annotation.ToolBean;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * @ClassName WeatherTools
 * @Description 工具类
 * @Author ZhaoYanNing
 * @Date 2025/5/3 14:56
 * @Version 1.0
 */
@ToolBean
public class Tools {

    @Tool("返回给定城市的天气预报")
    public String getWeather(
            @P("应返回天气预报的城市") String city
    ) {
        return "The weather in " + city + " is sunny with a high of 25 degrees.";
    }

    @Tool("求两个数的和")
    double add(@P(value = "数字",required = true) int a,
               @P(value = "数字",required = true) int b) {
        return a + b;
    }

    @Tool("求平方根")
    double squareRoot(@P(value = "数字",required = true) double x) {
        return Math.sqrt(x);
    }
}
