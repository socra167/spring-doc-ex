package com.springdocex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringDocExApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringDocExApplication.class, args);
	}

}
