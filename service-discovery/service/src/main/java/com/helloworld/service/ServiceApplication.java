package com.helloworld.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ImportAutoConfiguration(OtlpMetricsExportAutoConfiguration.class)
public class ServiceApplication {

	public static void main(final String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

}
