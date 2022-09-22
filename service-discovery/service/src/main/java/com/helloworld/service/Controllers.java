package com.helloworld.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.extension.annotations.WithSpan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Value;

@RestController
public class Controllers {
	Logger logger = LoggerFactory.getLogger(Controllers.class);
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
    User getUser() {
		logger.info("/user has been called!");
		return getSpringGuruUser();
	}
}