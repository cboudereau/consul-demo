package com.client.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "users-service", path = "/user")
public interface UsersServiceClient {
    @RequestMapping(method = RequestMethod.GET)
    User getUser(@RequestParam(value = "id") Integer id);
}