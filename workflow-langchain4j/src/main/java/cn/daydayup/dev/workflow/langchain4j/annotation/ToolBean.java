package cn.daydayup.dev.workflow.langchain4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName ToolDescription
 * @Description 工具类注解
 * @Author ZhaoYanNing
 * @Date 2024/12/11 14:49
 * @Version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ToolBean {
}
