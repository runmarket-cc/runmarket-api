package com.runmarket.pacer.web.controller;

import com.runmarket.pacer.domain.port.in.airquality.GetAirQualityDistrictUseCase;
import com.runmarket.pacer.web.dto.AirQualityDistrictResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/air-quality")
@RequiredArgsConstructor
public class AirQualityController {

    private final GetAirQualityDistrictUseCase getAirQualityDistrictUseCase;

    @GetMapping("/districts")
    public ResponseEntity<AirQualityDistrictResponse> getDistricts() {
        return ResponseEntity.ok(AirQualityDistrictResponse.from(getAirQualityDistrictUseCase.get()));
    }
}
