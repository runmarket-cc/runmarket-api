package com.runmarket.pacer.application.service;

import com.runmarket.pacer.domain.model.AirQualityDistrict;
import com.runmarket.pacer.domain.model.AirQualityReport;
import com.runmarket.pacer.domain.port.in.airquality.GetAirQualityDistrictUseCase;
import com.runmarket.pacer.domain.port.out.airquality.AirQualityPort;
import com.runmarket.pacer.domain.port.out.airquality.LlmPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AirQualityDistrictService implements GetAirQualityDistrictUseCase {

    private final AirQualityPort airQualityPort;
    private final LlmPort llmPort;

    @Override
    @Cacheable(value = "air-quality-district", key = "'all'", unless = "#result == null")
    public AirQualityReport get() {
        return fetch();
    }

    @Scheduled(fixedRate = 3_600_000, initialDelay = 0)
    @CachePut(value = "air-quality-district", key = "'all'", unless = "#result == null")
    public AirQualityReport refresh() {
        return fetch();
    }

    private AirQualityReport fetch() {
        List<AirQualityDistrict> districts;
        try {
            districts = airQualityPort.fetchDistricts();
        } catch (Exception e) {
            log.warn("Seoul air quality API unavailable: {}", e.getMessage());
            return null; // todo null 반환은 x
        }

        String advice = null;
        try {
            advice = llmPort.chat(buildPrompt(districts));
        } catch (Exception e) {
            log.warn("LLM unavailable: {}", e.getMessage(), e);
        }

        return new AirQualityReport(districts, advice);
    }

    private String buildPrompt(List<AirQualityDistrict> districts) {
        var sb = new StringBuilder("서울 주요 지역 대기질 현황:\n");
        districts.forEach(d -> sb.append(String.format(
                "- %s: PM10=%s, PM2.5=%s, 오존=%.3f, CAI=%s(%s)%n",
                d.getName(), d.getPm10(), d.getPm25(), d.getOzon(), d.getCai(), d.getCaiGrade())));
        sb.append("\n위 대기질 데이터를 바탕으로 오늘 러닝할 때 참고할 맞춤 조언을 4줄 이내로 작성해주세요. 간결하고 실용적으로 작성해주세요.");
        return sb.toString();
    }
}
