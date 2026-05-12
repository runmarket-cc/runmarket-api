package com.runmarket.pacer.infrastructure.seoul;

import com.runmarket.pacer.domain.model.AirQualityDistrict;
import com.runmarket.pacer.domain.port.out.airquality.AirQualityPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class SeoulAirQualityAdapter implements AirQualityPort {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String url;
    private final String fallbackUrl;

    public SeoulAirQualityAdapter(
            @Value("${app.seoul-air-district.url}") String url,
            @Value("${app.seoul-air-district.fallback-url}") String fallbackUrl,
            ObjectMapper objectMapper
    ) {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        var factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
        this.url = url;
        this.fallbackUrl = fallbackUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<AirQualityDistrict> fetchDistricts() {
        try {
            return fetch(url);
        } catch (Exception e) {
            log.warn("Seoul air quality API unavailable, using fallback: {}", e.getMessage());
            return fetch(fallbackUrl);
        }
    }

    private List<AirQualityDistrict> fetch(String targetUrl) {
        String raw = restClient.get()
                .uri(targetUrl)
                .retrieve()
                .body(String.class);

        if (raw == null) {
            throw new IllegalStateException("Empty response from air quality API: " + targetUrl);
        }

        SeoulAirQualityResponse response;
        response = objectMapper.readValue(raw, SeoulAirQualityResponse.class);

        if (response.service() == null || response.service().row() == null) {
            throw new IllegalStateException("Missing data in air quality response from: " + targetUrl);
        }

        return response.service().row().stream()
                .map(row -> AirQualityDistrict.builder()
                        .name(row.name())
                        .pm10(row.pm10())
                        .pm25(row.pm25())
                        .ozon(row.ozon())
                        .cai(row.cai())
                        .caiGrade(row.caiGrade())
                        .measuredAt(row.measuredAt())
                        .build())
                .toList();
    }
}
