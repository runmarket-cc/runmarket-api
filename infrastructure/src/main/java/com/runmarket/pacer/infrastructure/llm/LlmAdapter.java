package com.runmarket.pacer.infrastructure.llm;

import com.runmarket.pacer.domain.port.out.airquality.LlmPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LlmAdapter implements LlmPort {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public LlmAdapter(
            @Value("${app.llm.url}") String url,
            @Value("${app.llm.model}") String model,
            ObjectMapper objectMapper
    ) {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        var factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(120)); // todo decrease

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(factory)
                .build();
        this.model = model;
        this.objectMapper = objectMapper;
    }

    @Override
    public String chat(String prompt) {
        Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "stream", false
        );
        String raw = restClient.post()
                .uri("/api/chat")
                .body(request)
                .retrieve()
                .body(String.class);

        if (raw == null) {
            throw new IllegalStateException("Empty response from LLM");
        }
        LlmResponse response = objectMapper.readValue(raw, LlmResponse.class);
        if (response.message() == null) {
            throw new IllegalStateException("Missing message in LLM response");
        }
        return response.message().content();
    }

    record LlmResponse(Message message) {}
    record Message(String role, String content) {}
}
