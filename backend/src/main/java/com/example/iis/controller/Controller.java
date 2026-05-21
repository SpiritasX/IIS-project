package com.example.iis.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class Controller {
    @GetMapping
    public ResponseEntity<?> helloWorld() {
        return ResponseEntity.ok("Hello, World!");
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody String body) {
        return ResponseEntity.ok(body);
    }
}
