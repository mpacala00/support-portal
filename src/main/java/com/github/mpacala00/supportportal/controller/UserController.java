package com.github.mpacala00.supportportal.controller;

import com.github.mpacala00.supportportal.domain.User;
import com.github.mpacala00.supportportal.exception.domain.ExceptionHandling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
//this extend will provide ExceptionHandlers
public class UserController extends ExceptionHandling {

    //this endpoint is not included in PUBLIC_URLS, so only authenticated user should be able to access this
    @GetMapping("/home")
    public String showUser() {
        return "application works";
    }

    @GetMapping
    public User showEmptyUser() {
        return new User().builder().id(1L).email("test@test.com").build();
    }
}
