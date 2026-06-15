package com.runmarket.pacer.web.dto;

import jakarta.validation.constraints.NotNull;

/** 업로드 궤적의 한 점. t 는 epoch milliseconds, acc 는 GPS 정확도(m, nullable). */
public record RunRoutePointRequest(
        @NotNull Double lat,
        @NotNull Double lng,
        @NotNull Long t,
        Double acc
) {}
