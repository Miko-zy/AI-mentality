package com.aimental.serviceImpl;

import cn.hutool.core.util.StrUtil;
import com.aimental.config.AiLlmProperties;
import com.aimental.dto.LlmMessageDTO;
import com.aimental.service.LlmChatClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * OpenAI 兼容协议的大模型客户端实现
 * 只使用 JDK 自带的 HttpClient，不依赖任何厂商 SDK
 * 未配置 Key 或关闭真实调用时，自动使用本地模拟回复，保证完整链路可运行
 */
@Service
public class OpenAiCompatibleLlmClient implements LlmChatClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleLlmClient.class);

    /**
     * 模拟回复模板（无 Key 时使用）
     */
    private static final String[] MOCK_REPLIES = {
            "我在这里，愿意慢慢听你说。你刚刚提到的这些感受，对你来说一定不容易。可以和我讲讲，最近哪一刻让你觉得最难受吗？",
            "听起来你最近承受了不少压力。先不用急着找到答案，我们可以一起把此刻的感受说清楚一些。你现在最希望被理解的是哪一部分？",
            "谢谢你愿意把这些告诉我。面对这样的情绪，感到疲惫是很正常的。要不要先试着深呼吸几次，然后告诉我，今天有没有哪件小事让你稍微轻松一点？",
            "我能感觉到这件事对你很重要。我们不急着解决它，先看看它给你带来了哪些具体的感受和想法，好吗？"
    };

    private final AiLlmProperties properties;

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient;

    public OpenAiCompatibleLlmClient(AiLlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(1, properties.getConnectTimeoutSeconds())))
                .build();
    }

    @Override
    public boolean isRealCallEnabled() {
        return properties.isEnabled() && StrUtil.isNotBlank(properties.getApiKey());
    }

    @Override
    public String chat(List<LlmMessageDTO> messages) {
        if (!isRealCallEnabled()) {
            return mockReply(messages);
        }
        try {
            HttpRequest request = buildRequest(buildRequestBody(messages, false));
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IllegalStateException("大模型接口返回 HTTP " + response.statusCode() + "：" + abbreviate(response.body()));
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || StrUtil.isBlank(contentNode.asText())) {
                throw new IllegalStateException("大模型返回内容为空");
            }
            return contentNode.asText().trim();
        } catch (Exception e) {
            log.error("调用大模型失败，使用兜底回复：{}", e.getMessage());
            return "抱歉，我这边暂时有点卡顿，但我还在这里陪你。愿意再和我说说现在的感受吗？";
        }
    }

    @Override
    public void streamChat(List<LlmMessageDTO> messages, Consumer<String> onDelta) throws Exception {
        if (!isRealCallEnabled()) {
            mockStream(messages, onDelta);
            return;
        }
        HttpRequest request = buildRequest(buildRequestBody(messages, true));
        HttpResponse<java.util.stream.Stream<String>> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
        if (response.statusCode() != 200) {
            String errorBody = response.body().limit(50).collect(Collectors.joining("\n"));
            throw new IllegalStateException("大模型流式接口返回 HTTP " + response.statusCode() + "：" + abbreviate(errorBody));
        }
        final boolean[] received = {false};
        response.body().forEach(line -> {
            String trimmed = line == null ? "" : line.trim();
            if (!trimmed.startsWith("data:")) {
                return;
            }
            String data = trimmed.substring(5).trim();
            if ("[DONE]".equals(data) || StrUtil.isBlank(data)) {
                return;
            }
            try {
                JsonNode root = objectMapper.readTree(data);
                JsonNode deltaNode = root.path("choices").path(0).path("delta").path("content");
                if (!deltaNode.isMissingNode() && !deltaNode.asText().isEmpty()) {
                    received[0] = true;
                    onDelta.accept(deltaNode.asText());
                }
            } catch (Exception e) {
                log.warn("解析大模型流式数据失败：{}", e.getMessage());
            }
        });
        if (!received[0]) {
            throw new IllegalStateException("大模型流式返回内容为空");
        }
    }

    /**
     * 构建 OpenAI 兼容请求体
     */
    private String buildRequestBody(List<LlmMessageDTO> messages, boolean stream) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getModel());
        List<Map<String, String>> messageList = new ArrayList<>();
        for (LlmMessageDTO message : messages) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("role", message.getRole());
            item.put("content", message.getContent());
            messageList.add(item);
        }
        body.put("messages", messageList);
        body.put("temperature", properties.getTemperature());
        body.put("stream", stream);
        return objectMapper.writeValueAsString(body);
    }

    /**
     * 构建 HTTP 请求
     */
    private HttpRequest buildRequest(String body) {
        String baseUrl = StrUtil.removeSuffix(properties.getBaseUrl().trim(), "/");
        return HttpRequest.newBuilder(URI.create(baseUrl + "/chat/completions"))
                .timeout(Duration.ofSeconds(Math.max(10, properties.getRequestTimeoutSeconds())))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "text/event-stream, application/json")
                .header("Authorization", "Bearer " + properties.getApiKey().trim())
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
    }

    /**
     * 本地模拟非流式回复
     */
    private String mockReply(List<LlmMessageDTO> messages) {
        String userText = findLastUserContent(messages);
        if (StrUtil.isBlank(userText)) {
            return MOCK_REPLIES[0];
        }
        if (userText.contains("你好") || userText.contains("在吗") || userText.contains("hello")) {
            return "你好，我是你的AI谈心伙伴。今天想聊点什么？无论开心还是难过的感受，都可以慢慢告诉我。";
        }
        if (userText.contains("失眠") || userText.contains("睡不着")) {
            return "睡不着的夜晚确实很难熬。可以试着把手机放远一点，做几次缓慢的深呼吸，或者把脑子里反复出现的念头写下来。你愿意说说，是什么让你一直无法入睡吗？";
        }
        if (userText.contains("考试") || userText.contains("学习") || userText.contains("工作")) {
            return "学习或工作上的压力，常常会让人觉得喘不过气。我们可以先不急着解决全部问题，只挑一件此刻最让你焦虑的事说说，好吗？";
        }
        int index = Math.abs(userText.hashCode()) % MOCK_REPLIES.length;
        return MOCK_REPLIES[index];
    }

    /**
     * 本地模拟流式回复：按小块推送，便于前端验证打字机效果
     */
    private void mockStream(List<LlmMessageDTO> messages, Consumer<String> onDelta) {
        String reply = mockReply(messages);
        int step = 6;
        for (int i = 0; i < reply.length(); i += step) {
            String chunk = reply.substring(i, Math.min(i + step, reply.length()));
            onDelta.accept(chunk);
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * 取最后一条用户消息
     */
    private String findLastUserContent(List<LlmMessageDTO> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equals(messages.get(i).getRole())) {
                return messages.get(i).getContent();
            }
        }
        return "";
    }

    /**
     * 截断错误响应，避免日志过长
     */
    private String abbreviate(String text) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        String singleLine = text.replaceAll("\\s+", " ").trim();
        return singleLine.length() > 300 ? singleLine.substring(0, 300) + "..." : singleLine;
    }
}
