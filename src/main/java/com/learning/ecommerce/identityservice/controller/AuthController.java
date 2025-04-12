package com.learning.ecommerce.identityservice.controller;

import com.learning.ecommerce.identityservice.JwtHelper;
import com.learning.ecommerce.identityservice.grpc.CustomUserDetailsService;
import com.learning.ecommerce.identityservice.grpc.TemplateRenderClient;
import com.learning.ecommerce.identityservice.model.JwtRequest;
import com.learning.ecommerce.identityservice.model.JwtResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private TemplateRenderClient templateRenderClient;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private JwtHelper helper;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private final Logger Logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/loginPage")
    public ResponseEntity<String> getLoginPage() {
        Map<String, String> dynamicData = new HashMap<>();
        dynamicData.put("title", "Welcome to Login Page");

        String renderedTemplate = templateRenderClient.fetchRenderedTemplate("login.html", dynamicData);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(renderedTemplate);
    }

    @GetMapping("/home")
    public ResponseEntity<String> getHomePage() {
        Map<String, String> dynamicData = new HashMap<>();
//        dynamicData.put("title", "Welcome to Login Page");

        String renderedTemplate = templateRenderClient.fetchRenderedTemplate("home.html", dynamicData);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(renderedTemplate);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest request, HttpServletResponse response) {
        try {
            // 🔹 Authenticate using gRPC-based UserDetailsService
            UserDetails userDetails = customUserDetailsService.loadUserByUsernameAndPassword(
                    request.getUsername(), request.getPassword()
            );

            String token = this.helper.generateToken(userDetails);

            // 🔹 Store JWT token in an HTTP-only cookie
            ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", token)
                    .httpOnly(true)
                    .secure(false)  // Set true in production (HTTPS)
                    .path("/")
                    .maxAge(3600) // 1 hour
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok(Map.of("message", "Login successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }


    private void doAuthenticate(String email, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            manager.authenticate(authentication);


        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(" Invalid Username or Password  !!");
        }

    }

    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Credentials Invalid !!";
    }

}
