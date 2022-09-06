package com.client.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.extension.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequiredArgsConstructor
public class Controllers {
	private static final Logger Logger = LoggerFactory.getLogger(Controllers.class);
	@Autowired
	UsersServiceClient client;	

	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@WithSpan
	private String getHello() {
		Logger.info("calling getUser");
		User user = client.getUser();
		long ts = java.time.Instant.now().toEpochMilli();
		return "Hello " + user.getName() + " " + user.getSurname() + " " + ts;
	}

	@GetMapping("/hello")
	public String hello() {
		Logger.info("/hello has been called");
		return getHello();
	}
}