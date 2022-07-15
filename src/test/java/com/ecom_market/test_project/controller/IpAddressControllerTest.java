package com.ecom_market.test_project.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IpAddressControllerTest {

    private static final String CONTROLLER_URL = "/ip-validator";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void test_request_success() throws Exception {
        mockMvc.perform(post(CONTROLLER_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void test_request_failure() throws Exception {
        // Iteration in 3 times for same IP is ok
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post(CONTROLLER_URL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        // Forth request for same IP should throw 502 Bad Gateway
        mockMvc.perform(post(CONTROLLER_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());

    }
}
