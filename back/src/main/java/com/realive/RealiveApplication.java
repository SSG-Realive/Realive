package com.realive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RealiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealiveApplication.class, args);
	}

}
