package com.ecom_market.test_project.controller;

import com.ecom_market.test_project.service.IpAddressCheckerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ip-validator")
public class IpAddressController {

    private final IpAddressCheckerService ipAddressCheckerService;

    public IpAddressController(IpAddressCheckerService ipAddressCheckerService) {
        this.ipAddressCheckerService = ipAddressCheckerService;
    }

    @PostMapping
    public ResponseEntity<String> checkIpAddress(HttpServletRequest request) {
        return ipAddressCheckerService.checkIpAddress(request);
    }


}
