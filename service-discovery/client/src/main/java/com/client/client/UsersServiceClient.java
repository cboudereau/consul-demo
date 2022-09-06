package com.client.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "users-service", path = "/user")
public interface UsersServiceClient {
    @RequestMapping(path = "/", method = RequestMethod.GET)
    User getUser();
}