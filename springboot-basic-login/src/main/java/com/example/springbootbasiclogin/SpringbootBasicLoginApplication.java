package com.example.springbootbasiclogin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class SpringbootBasicLoginApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringbootBasicLoginApplication.class, args);
	}
}
