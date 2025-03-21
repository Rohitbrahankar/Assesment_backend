package com.moonrider.identity.controller;

import com.moonrider.identity.dto.IdentifyRequest;
import com.moonrider.identity.dto.IdentifyResponse;
import com.moonrider.identity.service.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/identify")
public class IdentityController {

    @Autowired
    private IdentityService identityService;

    @PostMapping
    public ResponseEntity<IdentifyResponse> identify(@RequestBody IdentifyRequest request) {
        IdentifyResponse response = identityService.processRequest(request);
        return ResponseEntity.ok(response);
    }
}