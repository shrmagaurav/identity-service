package com.learning.ecommerce.identityservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
public class MyConfig {

//    @GrpcClient("customer-core")
//    private CustomerServiceGrpc.CustomerServiceBlockingStub customerServiceBlockingStub;

//    @Bean
//    public UserDetailsService userDetailsService() {
//        return new CustomUserDetailsService(customerServiceBlockingStub);
//    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }
}
