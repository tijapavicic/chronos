package com.example.chronos.web;

import com.example.chronos.config.DemoProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    private final DemoProperties demoProperties;

    public DemoController(DemoProperties demoProperties) {
        this.demoProperties = demoProperties;
    }

    @GetMapping("/demo/message")
    public String message() {
        return demoProperties.getMessage();
    }
}
