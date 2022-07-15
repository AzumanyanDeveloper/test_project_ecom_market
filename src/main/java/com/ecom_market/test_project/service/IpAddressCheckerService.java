package com.ecom_market.test_project.service;

import com.ecom_market.test_project.config.CacheConfig;
import com.ecom_market.test_project.dto.IpAddressDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalTime;

@Service
public class IpAddressCheckerService {

    @Value("${ip-addresses.limiter.time}")
    private int timeLimit;

    @Value("${ip-addresses.limiter.count}")
    private int countLimit;

    private final CacheConfig cacheConfig;

    public IpAddressCheckerService(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
    }

    public String getIpAddress(HttpServletRequest request) {
        var remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }

    public ResponseEntity<String> checkIpAddress(HttpServletRequest request) {
        var cacheManager = cacheConfig.cacheManager();
        var ip = getIpAddress(request);
        var cache = cacheManager.getCache("ipAddresses");
        if (cache != null) {
            var ipAddress = cache.get(ip, IpAddressDto.class);
            if (ipAddress == null) {
                var ipAddressDto = buildIpAddressDto(ip, countLimit);
                cache.put(ip, ipAddressDto);
                return ResponseEntity.ok().build();
            } else if (ipAddress.getRequestedTime().plusMinutes(timeLimit).isAfter(LocalTime.now())) {
                var count = ipAddress.getCountLimit();
                if (count > 0) {
                    ipAddress.setCountLimit(count - 1);
                    cache.put(ip, ipAddress);
                    return ResponseEntity.ok().build();
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
                }
            } else {
                cache.evict(ip);
                var ipAddressDto = buildIpAddressDto(ip, countLimit);
                cache.put(ip, ipAddressDto);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

    private IpAddressDto buildIpAddressDto(String ip, int countLimit) {
        return IpAddressDto.builder()
                .ipAddress(ip)
                .countLimit(countLimit - 1)
                .requestedTime(LocalTime.now())
                .build();
    }
}
