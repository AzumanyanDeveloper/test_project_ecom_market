package com.ecom_market.test_project.service;

import com.ecom_market.test_project.config.CacheConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EnableConfigurationProperties
@TestPropertySource(locations = "classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IpAddressCheckerServiceTest {

    private static final String TEST_IP_ADDRESS = "220.221.168.0.1";
    private static final String CACHE_NAME = "ipAddresses";

    @Autowired
    private IpAddressCheckerService ipAddressCheckerService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private CacheConfig cacheConfig;

    @Test
    void testRequest_Success() {
        when(cacheConfig.cacheManager()).thenReturn(new ConcurrentMapCacheManager(CACHE_NAME));
        when(request.getHeader("X-FORWARDED-FOR")).thenReturn(TEST_IP_ADDRESS);
        ResponseEntity<String> stringResponseEntity = ipAddressCheckerService.checkIpAddress(request);
        assertThat(stringResponseEntity.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void testRequest_Fail() {
        when(cacheConfig.cacheManager()).thenReturn(new ConcurrentMapCacheManager(CACHE_NAME));
        ResponseEntity<String> stringResponseEntity = ResponseEntity.noContent().build();
        for (int i = 0; i < 5; i++) {
            when(request.getHeader("X-FORWARDED-FOR")).thenReturn(TEST_IP_ADDRESS);
            stringResponseEntity = ipAddressCheckerService.checkIpAddress(request);
        }
        assertThat(stringResponseEntity.getStatusCode().value()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
    }

    @Test
    void testRequest_Parallel() throws InterruptedException {
        when(cacheConfig.cacheManager()).thenReturn(new ConcurrentMapCacheManager(CACHE_NAME));

        AtomicReference<ResponseEntity<String>> stringResponseEntityFirst = new AtomicReference<>(ResponseEntity.noContent().build());
        AtomicReference<ResponseEntity<String>> stringResponseEntitySecond = new AtomicReference<>(ResponseEntity.noContent().build());

        Thread firstExecution = new Thread(() -> {
            HttpServletRequest mock = mock(HttpServletRequest.class);
            when(mock.getHeader("X-FORWARDED-FOR")).thenReturn(TEST_IP_ADDRESS + 1);
            stringResponseEntityFirst.set(ipAddressCheckerService.checkIpAddress(mock));
        });
        Thread secondExecution = new Thread(() -> {
            HttpServletRequest mock2 = mock(HttpServletRequest.class);
            when(mock2.getHeader("X-FORWARDED-FOR")).thenReturn(TEST_IP_ADDRESS + 2);
            stringResponseEntitySecond.set(ipAddressCheckerService.checkIpAddress(mock2));
        });

        firstExecution.start();
        secondExecution.start();

        firstExecution.join();
        secondExecution.join();

        assertThat(stringResponseEntityFirst.get().getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThat(stringResponseEntitySecond.get().getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }
}
