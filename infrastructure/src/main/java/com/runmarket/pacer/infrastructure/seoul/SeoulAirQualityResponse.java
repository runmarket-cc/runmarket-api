package com.runmarket.pacer.infrastructure.seoul;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record SeoulAirQualityResponse(
        @JsonProperty("ListAirQualityByDistrictService") ServiceData service
) {
    record ServiceData(List<Row> row) {}

    record Row(
            @JsonProperty("MSRSTN_NM") String name,
            @JsonProperty("PM") Integer pm10,
            @JsonProperty("FPM") Integer pm25,
            @JsonProperty("OZON") Double ozon,
            @JsonProperty("CAI") Integer cai,
            @JsonProperty("CAI_GRD") String caiGrade,
            @JsonProperty("MSRMT_YMD") String measuredAt
    ) {}
}
