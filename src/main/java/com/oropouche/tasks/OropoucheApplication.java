package com.oropouche.tasks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.oropouche.tasks.domain.models.user")
public class OropoucheApplication {

	public static void main(String[] args) {
		SpringApplication.run(OropoucheApplication.class, args);
	}

}
