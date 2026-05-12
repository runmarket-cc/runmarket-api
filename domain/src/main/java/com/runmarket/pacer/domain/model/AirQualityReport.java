package com.runmarket.pacer.domain.model;

import java.util.List;

public record AirQualityReport(List<AirQualityDistrict> districts, String runningAdvice) {}
