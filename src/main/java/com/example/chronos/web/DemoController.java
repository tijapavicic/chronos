package com.example.chronos.web;

import com.example.chronos.config.DemoProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private final DemoProperties demoProperties;

    public DemoController(DemoProperties demoProperties) {
        this.demoProperties = demoProperties;
    }

    @GetMapping(path = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    public String hello() {
        return "hello";
    }

    @GetMapping(path = "/message", produces = MediaType.APPLICATION_JSON_VALUE)
    public String message() {
        return demoProperties.getMessage();
    }
}
