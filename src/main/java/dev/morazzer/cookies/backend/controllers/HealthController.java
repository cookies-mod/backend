package dev.morazzer.cookies.backend.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HealthController {

    public record Status(boolean ok) {}

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Status health() {
        return new Status(true);
    }

}
