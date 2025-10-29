package com.lifelogix;

import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableCaching
@SpringBootApplication
public class LifelogixApplication {

	public static void main(String[] args) {
		SpringApplication.run(LifelogixApplication.class, args);
	}

}
