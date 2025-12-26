package com.smarttours.atlasguidebackend.exposition;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthcheckController {

    @RequestMapping(method = RequestMethod.GET)
    public String healthcheck() {
        return "OK";
    }
}