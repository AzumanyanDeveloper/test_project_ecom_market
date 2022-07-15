package com.ecom_market.test_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IpAddressDto {

    private String ipAddress;
    private int countLimit;
    private LocalTime requestedTime;
}
