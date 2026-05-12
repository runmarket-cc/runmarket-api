package com.runmarket.pacer.domain.port.out.airquality;

import com.runmarket.pacer.domain.model.AirQualityDistrict;

import java.util.List;

public interface AirQualityPort {
    List<AirQualityDistrict> fetchDistricts();
}
