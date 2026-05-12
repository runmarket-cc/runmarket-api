package com.runmarket.pacer.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AirQualityDistrict {
    private String name;
    private Integer pm10;
    private Integer pm25;
    private Double ozon;
    private Integer cai;
    private String caiGrade;
    private String measuredAt;
}
