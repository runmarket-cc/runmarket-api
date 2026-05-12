package com.runmarket.pacer.web.dto;

import com.runmarket.pacer.domain.model.AirQualityReport;

import java.util.List;

public record AirQualityDistrictResponse(List<District> districts, String runningAdvice) {

    public record District(
            String name,
            Integer pm10,
            Integer pm25,
            Double ozon,
            Integer cai,
            String caiGrade
    ) {}

    public static AirQualityDistrictResponse from(AirQualityReport report) {
        List<District> districts = report.districts().stream()
                .map(d -> new District(d.getName(), d.getPm10(), d.getPm25(), d.getOzon(), d.getCai(), d.getCaiGrade()))
                .toList();
        return new AirQualityDistrictResponse(districts, report.runningAdvice());
    }
}
