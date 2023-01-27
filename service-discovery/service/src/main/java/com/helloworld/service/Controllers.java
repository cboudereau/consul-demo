package com.helloworld.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.extension.annotations.WithSpan;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Value;

@RestController
public class Controllers {
	private static final Logger logger = LoggerFactory.getLogger(Controllers.class);
	private static final Random random = new Random();
	@Value
    static class User {
		public User(final String name, final String surname) {
			this.name = name;
			this.surname = surname;
		}
		String name;
		String surname;
	}

	@WithSpan
	private static User getSpringGuruUser() {
		return new User("Spring", "Guru");
	}

	@GetMapping(path="/user")
    User getUser() throws InterruptedException {
		java.time.Instant s = java.time.Instant.now();
		User result = getSpringGuruUser();
		
		long latency = random.nextInt(1000);		
		
		Thread.sleep(latency);
		
		long delta = java.time.Duration.between(s, java.time.Instant.now()).toMillis();
		logger.info("[{}] /user has been called!", delta);
		return result;
	}
}