package com.learning.ecommerce.identityservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Hello")
public class HelloController {

    @GetMapping("/sayHello/{name}")
    public String sayHello(@PathVariable String name) {
        return "Hello " + name;
    }
}
