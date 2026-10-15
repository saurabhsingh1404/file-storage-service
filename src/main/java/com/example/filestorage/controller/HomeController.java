package com.example.filestorage.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping({"/", "/healthz"})
    public Map<String, String> home() {
        return Map.of(
                "application", "User Document Storage Service",
                "status", "UP",
                "swagger", "/swagger-ui/index.html"
        );
    }
}

