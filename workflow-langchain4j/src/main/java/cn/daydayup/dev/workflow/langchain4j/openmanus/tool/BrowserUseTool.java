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
import dev.langchain4j.memory.ChatMemory;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class BrowserUseTool {

	private static final Logger log = LoggerFactory.getLogger(BrowserUseTool.class);

	private final ChatMemory chatMemory;
	private WebDriver driver;

	// 最大返回文本长度
	private static final int MAX_LENGTH = 3000;

	public BrowserUseTool(ChatMemory chatMemory) {
		this.chatMemory = chatMemory;
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--remote-allow-origins=*");
		this.driver = new ChromeDriver(options);
	}

	@Tool(name = "browserUse", value = "Interact with a web browser to perform various actions such as navigation, element interaction, content extraction, and tab management. Supported actions include:\n" +
			"- 'navigate': Go to a specific URL\n" +
			"- 'click': Click an element by index\n" +
			"- 'input_text': Input text into an element\n" +
			"- 'key_enter': Hit the Enter key\n" +
			"- 'screenshot': Capture a screenshot\n" +
			"- 'get_html': Get page HTML content\n" +
			"- 'get_text': Get text content of the page\n" +
			"- 'execute_js': Execute JavaScript code\n" +
			"- 'scroll': Scroll the page\n" +
			"- 'switch_tab': Switch to a specific tab\n" +
			"- 'new_tab': Open a new tab\n" +
			"- 'close_tab': Close the current tab\n" +
			"- 'refresh': Refresh the current page")
	public String browserUse(String toolInput) {
		log.info("BrowserUseTool toolInput: {}", toolInput);
		Map<String, Object> toolInputMap = JSON.parseObject(toolInput, Map.class);

		String action = (String) toolInputMap.get("action");
		String url = (String) toolInputMap.get("url");
		Integer index = (Integer) toolInputMap.get("index");
		String text = (String) toolInputMap.get("text");
		String script = (String) toolInputMap.get("script");
		Integer scrollAmount = (Integer) toolInputMap.get("scroll_amount");
		Integer tabId = (Integer) toolInputMap.get("tab_id");

		try {
			switch (action) {
				case "navigate":
					if (url == null) return "URL is required for 'navigate' action";
					driver.get(url);
					return "Navigated to " + url;

				case "click":
					if (index == null) return "Index is required for 'click' action";
					var elements = driver.findElements(By.cssSelector("*"));
					if (index < 0 || index >= elements.size()) return "Element not found at index " + index;
					elements.get(index).click();
					return "Clicked element at index " + index;

				case "input_text":
					if (index == null || text == null) return "Index and text are required for 'input_text' action";
					WebElement inputEl = driver.findElements(By.cssSelector("input, textarea")).get(index);
					inputEl.sendKeys(text);
					return "Input text '" + text + "' at index " + index;

				case "key_enter":
					if (index == null) return "Index is required for 'key_enter' action";
					WebElement enterEl = driver.findElements(By.cssSelector("input, textarea")).get(index);
					enterEl.sendKeys(Keys.RETURN);
					return "Hit Enter at index " + index;

				case "screenshot":
					TakesScreenshot ts = (TakesScreenshot) driver;
					String base64 = ts.getScreenshotAs(OutputType.BASE64);
					return "Screenshot captured (base64 length: " + base64.length() + ")";

				case "get_html":
					String html = driver.getPageSource();
					return html.length() > MAX_LENGTH ? html.substring(0, MAX_LENGTH) + "..." : html;

				case "get_text":
					String body = driver.findElement(By.tagName("body")).getText();
					int counter = 0;
					while (counter++ < 5 && body.contains("我们的系统检测到您的计算机网络中存在异常流量")) {
						Thread.sleep(10000);
						body = driver.findElement(By.tagName("body")).getText();
					}
					return body;

				case "execute_js":
					if (script == null) return "Script is required for 'execute_js' action";
					((JavascriptExecutor) driver).executeScript(script);
					return "Executed JS: " + script;

				case "scroll":
					if (scrollAmount == null) return "Scroll amount is required for 'scroll' action";
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0," + scrollAmount + ");");
					return "Scrolled by " + scrollAmount + "px";

				case "new_tab":
					if (url == null) return "URL is required for 'new_tab' action";
					((JavascriptExecutor) driver).executeScript("window.open('" + url + "', '_blank');");
					return "Opened new tab with URL: " + url;

				case "close_tab":
					driver.close();
					return "Closed current tab";

				case "switch_tab":
					if (tabId == null) return "Tab ID is required for 'switch_tab' action";
					var handles = driver.getWindowHandles().toArray();
					driver.switchTo().window(handles[tabId].toString());
					return "Switched to tab ID: " + tabId;

				case "refresh":
					driver.navigate().refresh();
					return "Page refreshed";

				default:
					return "Unknown action: " + action;
			}
		} catch (Exception e) {
			if (e instanceof ElementNotInteractableException) {
				return String.format("""
                                Browser action '%s' failed. You may have used the wrong index.
                                Try using 'get_html' first to analyze the page.

                                Tips:
                                1. Ignore hidden input/textarea elements.
                                2. For Baidu, consider using JS scripts.

                                Exception: %s
                                """,
						action, e.getMessage());
			}
			return "Action failed: " + e.getMessage();
		}
	}

	// 可选：在 Bean 销毁时关闭浏览器
	public void close() {
		if (driver != null) {
			driver.quit();
			driver = null;
		}
		System.out.println("Browser resources cleaned up.");
	}
}

